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

package org.openscada.protocols.iso8073;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class COTPFilter extends IoFilterAdapter
{

    private final static Logger logger = LoggerFactory.getLogger ( COTPFilter.class );

    private static final byte COMMAND_CONNECT_CONFIRM = (byte)0x0D;

    private static final byte COMMAND_DATA = (byte)0x0F;

    private final int rack;

    private final byte slot;

    public COTPFilter ( final int rack, final byte slot )
    {
        this.rack = rack;
        this.slot = slot;
    }

    @Override
    public void sessionOpened ( final NextFilter nextFilter, final IoSession session ) throws Exception
    {
        logger.debug ( "Session opened: {}", session );

        final IoBuffer buffer = IoBuffer.allocate ( 1 );
        buffer.setAutoExpand ( true );

        buffer.put ( new byte[] { 0x11, (byte)0xE0, 0x00, 0x00, 0x00, 0x01, 0x00, (byte)0xC1, 2, 1, 0, (byte)0xC2, 2, 0, 1, (byte)0xC0, 1, 9 } );

        buffer.put ( 13, (byte) ( this.rack + 1 ) );
        buffer.put ( 14, this.slot );

        buffer.flip ();

        session.write ( buffer );
    }

    @Override
    public void filterWrite ( final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest ) throws Exception
    {
        if ( writeRequest.getMessage () instanceof DataTPDU )
        {
            logger.debug ( "Filter as DT: {}", writeRequest );
            handleDT ( nextFilter, session, writeRequest );
        }
        else
        {
            nextFilter.filterWrite ( session, writeRequest );
        }
    }

    /**
     * Handle data transmission
     * @param nextFilter
     * @param session
     * @param writeRequest
     */
    private void handleDT ( final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest )
    {
        final DataTPDU userData = (DataTPDU)writeRequest.getMessage ();
        final IoBuffer data = IoBuffer.allocate ( userData.getData ().remaining () + 3 );

        data.put ( (byte)0x02 ); // header length
        data.put ( (byte) ( COMMAND_DATA << 4 ) );

        final byte seq = (byte)0x80; // bit 8 to 1 (last packet)... bit 7..1 to zero (packet #0)

        data.put ( seq );

        data.put ( userData.getData () );

        data.flip ();

        nextFilter.filterWrite ( session, new WriteRequestWrapper ( writeRequest ) {
            public Object getMessage ()
            {
                return data;
            }
        } );
    }

    @Override
    public void messageSent ( final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest ) throws Exception
    {
        logger.debug ( "Message sent: {} -> {}", session, writeRequest );
        nextFilter.messageSent ( session, writeRequest );
    }

    @Override
    public void messageReceived ( final NextFilter nextFilter, final IoSession session, final Object message ) throws Exception
    {
        logger.debug ( "Message received: {} - {}", new Object[] { session, message } );
        if ( ! ( message instanceof IoBuffer ) )
        {
            return;
        }
        final IoBuffer buffer = (IoBuffer)message;

        final short command = (short) ( buffer.getUnsigned ( 1 ) >> 4 );

        logger.debug ( "Command: {}", command );

        if ( command == COMMAND_CONNECT_CONFIRM )
        {
            handleConnectConfirm ( nextFilter, session, buffer );
        }
        else if ( command == COMMAND_DATA )
        {
            handleData ( nextFilter, session, buffer );
        }
        else
        {
            logger.warn ( "Unknown command: {}", command );
        }
    }

    private void handleData ( final NextFilter nextFilter, final IoSession session, final IoBuffer buffer )
    {
        final short len = buffer.getUnsigned ();
        final short command = buffer.getUnsigned ();
        final short nr = buffer.getUnsigned ();

        // FIXME: append data TPDUs if there is more than one 

        nextFilter.messageReceived ( session, new DataTPDU ( buffer ) );
    }

    private void handleConnectConfirm ( final NextFilter nextFilter, final IoSession session, final IoBuffer buffer )
    {
        // CC Connect Confirm
        nextFilter.sessionOpened ( session );
    }
}
