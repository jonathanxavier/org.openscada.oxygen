package org.openscada.protocols.arduino.messages;

import java.util.Map;

import org.openscada.protocols.arduino.CommandCode;

public class ConfigurationMessage extends CommonMessage
{
    private final Map<Integer, Object>[] parameters;

    public ConfigurationMessage ( final int sequence, final CommandCode commandCode, final Map<Integer, Object>[] parameters )
    {
        super ( sequence, commandCode );
        this.parameters = parameters;
    }

    public Map<Integer, Object>[] getParameters ()
    {
        return this.parameters;
    }
}
