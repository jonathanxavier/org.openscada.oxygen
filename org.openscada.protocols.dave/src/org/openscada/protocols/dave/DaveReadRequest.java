/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2010 TH4 SYSTEMS GmbH (http://th4-systems.com)
 *
 * OpenSCADA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * OpenSCADA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License version 3 for more details
 * (a copy is included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with OpenSCADA. If not, see
 * <http://opensource.org/licenses/lgpl-3.0.html> for a copy of the LGPLv3 License.
 */

package org.openscada.protocols.dave;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.openscada.utils.lang.Immutable;

public class DaveReadRequest extends DaveMessage
{
    @Immutable
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

        @Override
        public String toString ()
        {
            return String.format ( "Area: %s, block: %s, start: %s, count: %s", this.area, this.block, this.start, this.count );
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
