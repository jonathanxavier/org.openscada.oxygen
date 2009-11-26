package org.openscada.protocols.dave;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class DaveReadRequest extends DaveMessage
{
    public static class Request
    {
        private final byte area;

        private final short block;

        private final short count;

        private final short start;

        private final AddressType type;

        public static enum AddressType
        {
            BIT ( (byte)0x01 ),
            BYTE ( (byte)0x02 ),
            WORD ( (byte)0x04 );

            private byte type;

            private AddressType ( final byte type )
            {
                this.type = type;
            }

            public byte getType ()
            {
                return this.type;
            }
        }

        public Request ( final byte area, final short block, final short start, final short count )
        {
            this ( AddressType.BYTE, area, block, start, count );
        }

        public Request ( final AddressType type, final byte area, final short block, final short start, final short count )
        {
            this.type = type;
            this.area = area;
            this.block = block;
            this.start = start;
            this.count = count;
        }

        public AddressType getType ()
        {
            return this.type;
        }

        public short getStart ()
        {
            return this.start;
        }

        public byte getArea ()
        {
            return this.area;
        }

        public short getBlock ()
        {
            return this.block;
        }

        public short getCount ()
        {
            return this.count;
        }
    }

    private final Collection<Request> requests = new LinkedList<Request> ();

    public DaveReadRequest ()
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
