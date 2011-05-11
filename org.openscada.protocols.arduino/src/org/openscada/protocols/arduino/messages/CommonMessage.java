package org.openscada.protocols.arduino.messages;

import org.openscada.protocols.arduino.CommandCode;

public class CommonMessage
{
    private final int sequence;

    private final CommandCode commandCode;

    public CommonMessage ( final int sequence, final CommandCode commandCode )
    {
        this.sequence = sequence;
        this.commandCode = commandCode;
    }

    public int getSequence ()
    {
        return this.sequence;
    }

    public CommandCode getCommandCode ()
    {
        return this.commandCode;
    }
}
