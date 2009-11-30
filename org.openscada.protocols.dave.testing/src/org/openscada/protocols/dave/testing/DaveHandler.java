package org.openscada.protocols.dave.testing;

import java.util.Random;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.openscada.protocols.dave.DaveConnectionEstablishedMessage;
import org.openscada.protocols.dave.DaveReadRequest;
import org.openscada.protocols.dave.DaveWriteRequest;
import org.openscada.protocols.dave.DaveReadRequest.Request;
import org.openscada.protocols.iso8073.DataTPDU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaveHandler implements SingleSessionIoHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( DaveHandler.class );

    private final IoSession session;

    private final Random r = new Random ();

    private int tix = 0;

    public DaveHandler ( final IoSession session )
    {
        this.session = session;
    }

    public void exceptionCaught ( final Throwable error ) throws Exception
    {
        System.out.println ( "Exception: " );
        error.printStackTrace ();
    }

    public void messageReceived ( final Object message ) throws Exception
    {
        System.out.println ( System.currentTimeMillis () + " Received: " + this.session + " -> " + message );

        if ( message instanceof DaveConnectionEstablishedMessage )
        {
            // sendWriteFloatData ();
            sendReadData ();
        }
    }

    public void messageSent ( final Object message ) throws Exception
    {
        // System.out.println ( "Sent: " + this.session + " -> " + message );
    }

    public void sessionClosed () throws Exception
    {
        System.out.println ( "Closed: " + this.session );
    }

    public void sessionCreated () throws Exception
    {
        System.out.println ( "Created: " + this.session );
    }

    public void sessionIdle ( final IdleStatus status ) throws Exception
    {
        System.out.println ( "Idle: " + this.session + " -> " + status );
    }

    public void sessionOpened () throws Exception
    {
        logger.debug ( "Session opened" );

        // sendInit ();
        // sendReadData ();
    }

    private void sendInit ()
    {
        final byte[] data = new byte[] { 0x32, 0x01, 0x00, 0x00, (byte)0xff, (byte)0xff, 0x00, 0x08, 0x00, 0x00, (byte)0xf0, 0x00, 0x00, 0x01, 0x00, 0x01, 0x03, (byte)0xc0 };
        final IoBuffer buffer = IoBuffer.wrap ( data );

        this.session.write ( new DataTPDU ( buffer ) );
    }

    private void sendWriteData ()
    {
        final DaveWriteRequest request = new DaveWriteRequest ();

        this.tix++;

        final boolean[] bits = new boolean[] { this.tix % 8 == 0, this.tix % 8 == 1, this.tix % 8 == 2, this.tix % 8 == 3, this.tix % 8 == 4, this.tix % 8 == 5, this.tix % 8 == 6, this.tix % 8 == 7, };

        request.addRequest ( new DaveWriteRequest.BitRequest ( (byte)0x84, (short)1, (short) ( 14 * 8 ), this.tix % 2 == 0 ) );

        this.session.write ( request );
    }

    private void sendWriteFloatData ()
    {

        final float f = this.r.nextFloat ();

        logger.info ( "Sending float: {}", f );

        final DaveWriteRequest request = new DaveWriteRequest ();

        this.tix++;

        final IoBuffer data = IoBuffer.allocate ( 4 );
        data.putFloat ( f );
        data.flip ();
        request.addRequest ( new DaveWriteRequest.ByteRequest ( (byte)0x84, (short)1, (short)214, data ) );

        this.session.write ( request );
    }

    private void sendReadData ()
    {
        final DaveReadRequest request = new DaveReadRequest ();

        request.addRequest ( new Request ( (byte)0x84, (short)1350, (short)56, (short)40 ) );

        this.session.write ( request );
    }

}
