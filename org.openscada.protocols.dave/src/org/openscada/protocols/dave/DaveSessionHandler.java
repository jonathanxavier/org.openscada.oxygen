package org.openscada.protocols.dave;

public interface DaveSessionHandler
{
    public void sessionOpened ();

    public void sessionClosed ();

    public void messageReceived ( DaveMessage message );
}
