package org.openscada.protocols.dave.testing;

import java.net.InetSocketAddress;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerDelegate;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openscada.protocols.dave.DaveFilter;
import org.openscada.protocols.iso8073.COTPFilter;
import org.openscada.protocols.tkpt.TPKTFilter;

public class Application1 implements IApplication
{
    private volatile boolean running = true;

    public Object start ( final IApplicationContext context ) throws Exception
    {
        final NioSocketConnector connector = new NioSocketConnector ();

        connector.setHandler ( new SingleSessionIoHandlerDelegate ( new SingleSessionIoHandlerFactory () {

            public SingleSessionIoHandler getHandler ( final IoSession session ) throws Exception
            {
                return new DaveHandler ( session );
            }
        } ) );

        connector.getFilterChain ().addLast ( "logger", new LoggingFilter ( this.getClass ().getName () ) );
        connector.getFilterChain ().addLast ( "tpkt", new TPKTFilter ( 3 ) );
        connector.getFilterChain ().addLast ( "cotp", new COTPFilter ( 0, (byte)2 ) );
        connector.getFilterChain ().addLast ( "dave", new DaveFilter () );

        connector.connect ( new InetSocketAddress ( "192.168.1.82", 102 ) );

        while ( this.running )
        {
            Thread.sleep ( 1000 );
        }

        return null;
    }

    public void stop ()
    {
        this.running = false;
    }

}
