package org.openscada.protocols.arduino.messages;

import org.openscada.core.Variant;
import org.openscada.protocols.arduino.CommandCode;

public class DataMessage extends CommonMessage
{
    private final Variant[] data;

    public DataMessage ( final int sequence, final CommandCode commandCode, final Variant[] data )
    {
        super ( sequence, commandCode );
        this.data = data.clone ();
    }

    public Variant[] getData ()
    {
        return this.data;
    }

}
