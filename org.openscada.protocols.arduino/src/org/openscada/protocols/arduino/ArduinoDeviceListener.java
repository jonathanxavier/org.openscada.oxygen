package org.openscada.protocols.arduino;

import java.util.Map;

import org.openscada.core.Variant;

public interface ArduinoDeviceListener
{
    public void stateChange ( DeviceState deviceState );

    public void deviceConnected ( Map<Integer, Object>[] parameters );

    public void deviceDisconnected ();

    public void dataChange ( Variant[] data );
}
