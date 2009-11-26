package org.openscada.protocols.dave;

import org.apache.mina.core.buffer.IoBuffer;

public class DaveGenericMessage extends DaveMessage
{
    private final IoBuffer parameters;

    private final IoBuffer data;

    public DaveGenericMessage ( final IoBuffer parameters, final IoBuffer data )
    {
        this.parameters = parameters;
        this.data = data;
    }

    public IoBuffer getData ()
    {
        return this.data;
    }

    public IoBuffer getParameters ()
    {
        return this.parameters;
    }

    @Override
    public String toString ()
    {
        return String.format ( "Generic Message : Parameters: %s, Data: %s", this.parameters, this.data );
    }

}
