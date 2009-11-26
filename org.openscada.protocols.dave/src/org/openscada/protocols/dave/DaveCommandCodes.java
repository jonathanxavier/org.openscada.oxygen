package org.openscada.protocols.dave;

public enum DaveCommandCodes
{
    DAVE_READ ( (byte)0x04 ),
    DAVE_WRITE ( (byte)0x05 );

    private byte commandCode;

    private DaveCommandCodes ( final byte commandCode )
    {
        this.commandCode = commandCode;
    }

    public byte getCommandCode ()
    {
        return this.commandCode;
    }
}
