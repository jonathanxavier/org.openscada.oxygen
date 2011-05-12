package org.openscada.protocols.arduino.messages;

import org.openscada.protocols.arduino.CommandCode;

public class DataMessage extends CommonMessage
{
    private final Object[] data;

    public DataMessage ( final int sequence, final CommandCode commandCode, final Object[] data )
    {
        super ( sequence, commandCode );
        this.data = data.clone ();
    }

    public Object[] getData ()
    {
        return this.data;
    }

}
