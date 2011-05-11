package org.openscada.protocols.arduino.testing;

import java.net.InetSocketAddress;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openscada.core.Variant;
import org.openscada.protocols.arduino.ArduinoDevice;
import org.openscada.protocols.arduino.ArduinoDeviceListener;
import org.openscada.protocols.arduino.DeviceState;

public class Application implements IApplication
{

    private volatile boolean running = true;

    @Override
    public Object start ( final IApplicationContext context ) throws Exception
    {

        final ArduinoDevice device = new ArduinoDevice ( new InetSocketAddress ( "172.20.11.105", 8888 ), new ArduinoDeviceListener () {

            @Override
            public void deviceConnected ( final Map<Integer, Object>[] parameters )
            {
                System.out.println ( "Device connected" );
                for ( int i = 0; i < parameters.length; i++ )
                {
                    for ( final Map.Entry<Integer, Object> entry : parameters[i].entrySet () )
                    {
                        System.out.println ( String.format ( "%02d - %02d - %s", i, entry.getKey (), entry.getValue () ) );
                    }
                }
            }

            @Override
            public void deviceDisconnected ()
            {
                System.out.println ( "Device disconnected" );
            }

            @Override
            public void stateChange ( final DeviceState deviceState )
            {
                System.out.println ( "State change: " + deviceState );
            }

            @Override
            public void dataChange ( final Variant[] data )
            {
                System.out.println ( "Data change" );
                for ( int i = 0; i < data.length; i++ )
                {
                    System.out.println ( String.format ( "  #%02d - %s", i, data[i] ) );
                }
            };
        }, true );

        device.start ();

        while ( this.running )
        {
            Thread.sleep ( 1000 );
        }

        return null;
    }

    @Override
    public void stop ()
    {
        this.running = false;
    }

}
