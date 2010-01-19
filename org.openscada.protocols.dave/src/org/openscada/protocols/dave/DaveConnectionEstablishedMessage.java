package org.openscada.protocols.dave;

public class DaveConnectionEstablishedMessage extends DaveMessage
{

    private final int maxPdu;

    public DaveConnectionEstablishedMessage ( final int maxPDU )
    {
        this.maxPdu = maxPDU;
    }

    public int getMaxPdu ()
    {
        return this.maxPdu;
    }

}
