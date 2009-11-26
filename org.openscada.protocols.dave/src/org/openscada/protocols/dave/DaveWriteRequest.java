package org.openscada.protocols.dave;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.mina.core.buffer.IoBuffer;

public class DaveWriteRequest extends DaveMessage
{
    public static abstract class Request extends DaveReadRequest.Request
    {
        public Request ( final AddressType type, final byte area, final short block, final short start, final short count )
        {
            super ( type, area, block, start, count );
        }

        public abstract IoBuffer getData ();
    }

    public static class ByteRequest extends Request
    {
        private final IoBuffer data;

        public ByteRequest ( final byte area, final short block, final short start, final IoBuffer data )
        {
            super ( AddressType.BYTE, area, block, start, (short)data.remaining () );
            this.data = data;
        }

        public ByteRequest ( final byte area, final short block, final short start, final byte data )
        {
            super ( AddressType.BYTE, area, block, start, (short)1 );
            this.data = IoBuffer.allocate ( 1 );
            this.data.put ( data );
            this.data.flip ();
        }

        @Override
        public IoBuffer getData ()
        {
            return this.data;
        }
    }

    public static class BitRequest extends Request
    {
        private final IoBuffer data;

        public BitRequest ( final byte area, final short block, final short start, final boolean[] bits )
        {
            super ( AddressType.BIT, area, block, start, (short)bits.length );
            this.data = IoBuffer.allocate ( bits.length );

            for ( int i = 0; i < bits.length; i++ )
            {
                this.data.put ( bits[i] ? (byte)0x01 : (byte)0x00 );
            }

            this.data.flip ();
        }

        public BitRequest ( final byte area, final short block, final short start, final boolean bit )
        {
            super ( AddressType.BIT, area, block, start, (short)1 );
            this.data = IoBuffer.allocate ( 1 );
            this.data.put ( bit ? (byte)0x01 : (byte)0x00 );
            this.data.flip ();
        }

        @Override
        public IoBuffer getData ()
        {
            return this.data;
        }
    }

    private final Collection<Request> requests = new LinkedList<Request> ();

    public DaveWriteRequest ()
    {
    }

    public void addRequest ( final Request request )
    {
        this.requests.add ( request );
    }

    public Collection<Request> getRequests ()
    {
        return Collections.unmodifiableCollection ( this.requests );
    }
}
