package org.openscada.protocols.arduino;

import java.net.InetSocketAddress;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.openscada.protocols.arduino.messages.CommonMessage;
import org.openscada.protocols.arduino.messages.ConfigurationMessage;
import org.openscada.protocols.arduino.messages.DataMessage;
import org.openscada.utils.concurrent.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArduinoDevice extends IoHandlerAdapter
{

    private final static Logger logger = LoggerFactory.getLogger ( ArduinoDevice.class );

    private static final long TIMEOUT = 2000;

    private static final long POLL_TIME = 100;

    private final InetSocketAddress address;

    private boolean started = false;

    private ScheduledExecutorService executorService;

    private IoSession session;

    private DeviceState state;

    private int sequence;

    private final ArduinoDeviceListener listener;

    private NioDatagramConnector connector;

    private ScheduledFuture<?> connectJob;

    private ScheduledFuture<?> requestJob;

    private Calendar lastData;

    private Calendar lastRequest;

    private final boolean activateLogger;

    public ArduinoDevice ( final InetSocketAddress address, final ArduinoDeviceListener listener, final boolean activateLogger )
    {
        this.address = address;
        this.listener = listener;
        this.activateLogger = activateLogger;
    }

    public synchronized void start ()
    {
        if ( this.started )
        {
            return;
        }

        this.started = true;

        this.executorService = Executors.newSingleThreadScheduledExecutor ( new NamedThreadFactory ( "ArduninoDevice/" + this.address ) );

        this.connector = new NioDatagramConnector ();

        this.connector.setHandler ( this );
        if ( this.activateLogger )
        {
            this.connector.getFilterChain ().addLast ( "logger", new LoggingFilter ( this.getClass ().getName () ) );
        }

        final ArduinoCodec codec = new ArduinoCodec ();
        this.connector.getFilterChain ().addLast ( "codec", new ProtocolCodecFilter ( codec, codec ) );

        this.connector.connect ( this.address );
    }

    public synchronized void stop ()
    {
        if ( !this.started )
        {
            return;
        }

        this.started = false;

        this.connector.dispose ();

        if ( this.session != null )
        {
            this.session.close ( true );
        }

        this.executorService.shutdown ();
        this.executorService = null;

    }

    @Override
    public void sessionCreated ( final IoSession session ) throws Exception
    {
        this.sequence = 0;
        setState ( DeviceState.DISCONNECTED );
        this.session = session;
    }

    private synchronized void setState ( final DeviceState state )
    {
        logger.debug ( "State changed: {}", state );

        this.state = state;
        this.executorService.execute ( new Runnable () {

            @Override
            public void run ()
            {
                ArduinoDevice.this.listener.stateChange ( state );
                if ( state == DeviceState.DISCONNECTED )
                {
                    ArduinoDevice.this.listener.deviceDisconnected ();
                }
            }
        } );
    }

    @Override
    public void sessionOpened ( final IoSession session ) throws Exception
    {
        connect ();
    }

    @Override
    public void sessionClosed ( final IoSession session ) throws Exception
    {
        this.session = null;
        setState ( DeviceState.DISCONNECTED );
    }

    private void connect ()
    {
        setState ( DeviceState.CONNECTING );
        this.session.write ( new CommonMessage ( this.sequence++, CommandCode.REQUEST_CONFIGURATION ) );
        this.connectJob = this.executorService.schedule ( new Runnable () {

            @Override
            public void run ()
            {
                connectTimeout ();
            }
        }, 10, TimeUnit.SECONDS );
    }

    protected synchronized void connectTimeout ()
    {
        if ( !this.started )
        {
            return;
        }

        setState ( DeviceState.DISCONNECTED );
        triggerReconnect ();
    }

    private synchronized void triggerReconnect ()
    {
        this.connectJob = this.executorService.schedule ( new Runnable () {

            @Override
            public void run ()
            {
                connect ();
            }
        }, TIMEOUT, TimeUnit.MILLISECONDS );
    }

    @Override
    public void messageReceived ( final IoSession session, final Object message ) throws Exception
    {
        if ( message instanceof CommonMessage )
        {
            switch ( ( (CommonMessage)message ).getCommandCode () )
            {
            case RESPOND_CONFIGURATION:
                respondConfiguration ( (ConfigurationMessage)message );
                break;
            case RESPOND_DATA:
                respondData ( (DataMessage)message );
                break;
            }
        }
    }

    private synchronized void respondData ( final DataMessage message )
    {
        this.lastData = Calendar.getInstance ();
        this.executorService.execute ( new Runnable () {

            @Override
            public void run ()
            {
                ArduinoDevice.this.listener.dataChange ( message.getData () );
            }
        } );
    }

    private synchronized void respondConfiguration ( final ConfigurationMessage message )
    {
        if ( this.state != DeviceState.CONNECTING )
        {
            return;
        }

        setState ( DeviceState.CONNECTED );
        // stop the connect job
        if ( this.connectJob != null )
        {
            this.connectJob.cancel ( false );
            this.connectJob = null;
        }

        this.executorService.execute ( new Runnable () {

            @Override
            public void run ()
            {
                ArduinoDevice.this.listener.deviceConnected ( message.getParameters () );
            }
        } );

        startRequestJob ();

        triggerRequest ();
    }

    private synchronized void startRequestJob ()
    {
        stopRequestJob ();

        this.lastData = Calendar.getInstance ();

        this.requestJob = this.executorService.scheduleAtFixedRate ( new Runnable () {

            @Override
            public void run ()
            {
                checkTimeout ();
                triggerRequest ();
            }
        }, POLL_TIME, POLL_TIME, TimeUnit.MILLISECONDS );
    }

    protected synchronized void checkTimeout ()
    {
        if ( this.lastData != null && System.currentTimeMillis () - this.lastData.getTimeInMillis () > TIMEOUT )
        {
            handleTimeout ();
        }
    }

    private synchronized void stopRequestJob ()
    {
        if ( this.requestJob != null )
        {
            this.requestJob.cancel ( false );
            this.requestJob = null;
        }
    }

    protected synchronized void triggerRequest ()
    {
        if ( this.state != DeviceState.CONNECTED )
        {
            return;
        }

        if ( this.lastRequest == null || System.currentTimeMillis () - this.lastRequest.getTimeInMillis () > TIMEOUT / 300 )
        {
            this.lastRequest = Calendar.getInstance ();
            this.session.write ( new CommonMessage ( this.sequence++, CommandCode.REQUEST_DATA ) );
        }
    }

    protected synchronized void handleTimeout ()
    {
        stopRequestJob ();
        setState ( DeviceState.DISCONNECTED );
        triggerReconnect ();
    }

}
