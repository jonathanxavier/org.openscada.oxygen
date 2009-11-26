/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2008-2009 inavare GmbH (http://inavare.com)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openscada.protocols.tkpt;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.core.write.WriteRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TPKTFilter extends IoFilterAdapter
{

    private final static Logger logger = LoggerFactory.getLogger ( TPKTFilter.class );

    private static final String SESSION_BUFFER_ATTR = "tpkt.sessionBuffer";

    private final int version;

    public TPKTFilter ( final int version )
    {
        this.version = version;
    }

    @Override
    public void messageReceived ( final NextFilter nextFilter, final IoSession session, final Object message ) throws Exception
    {
        if ( ! ( message instanceof IoBuffer ) )
        {
            nextFilter.messageReceived ( session, message );
            return;
        }

        final IoBuffer in = (IoBuffer)message;
        final IoBuffer sessionBuffer = getSessionBuffer ( session );

        logger.debug ( "received message - sessionBuffer: {}, in: {}", new Object[] { sessionBuffer.remaining (), in.remaining () } );

        // first add to the session buffer (may be optimized later)
        logger.debug ( "1Session buffer: {}", sessionBuffer );
        sessionBuffer.position ( sessionBuffer.limit () );
        sessionBuffer.put ( in );
        logger.debug ( "2Session buffer: {}", sessionBuffer );
        sessionBuffer.flip ();
        logger.debug ( "3Session buffer: {}", sessionBuffer );

        while ( sessionBuffer.remaining () >= 4 )
        {
            final int len = sessionBuffer.getUnsignedShort ( 2 );
            if ( sessionBuffer.remaining () < len )
            {
                logger.debug ( "Next packet requires {} bytes", new Object[] { len } );
                // not enough data for body
                return;
            }

            logger.debug ( "{} bytes left in session buffer - len: {}", new Object[] { sessionBuffer.remaining (), len } );

            // convert
            final IoBuffer data = IoBuffer.allocate ( len - 4 );
            sessionBuffer.get (); // version
            sessionBuffer.get (); // reserved
            sessionBuffer.getUnsignedShort (); // length

            sessionBuffer.get ( data.array () );

            nextFilter.messageReceived ( session, data );

            logger.debug ( "{} bytes left in session buffer", sessionBuffer.remaining () );
        }

        if ( sessionBuffer.hasRemaining () )
        {
            sessionBuffer.compact ();
        }
        else
        {
            sessionBuffer.clear ().flip ();
        }
        logger.debug ( "XSession buffer: {}", sessionBuffer );
    }

    private IoBuffer getSessionBuffer ( final IoSession session )
    {
        IoBuffer buffer = (IoBuffer)session.getAttribute ( SESSION_BUFFER_ATTR );
        if ( buffer == null )
        {
            buffer = IoBuffer.allocate ( 0 );
            buffer.setAutoExpand ( true );
            // buffer.setAutoShrink ( true );
            session.setAttribute ( SESSION_BUFFER_ATTR, buffer );
        }
        return buffer;
    }

    @Override
    public void sessionClosed ( final NextFilter nextFilter, final IoSession session ) throws Exception
    {
        clearSessionBuffer ( session );
        nextFilter.sessionClosed ( session );
    }

    private void clearSessionBuffer ( final IoSession session )
    {
        session.removeAttribute ( SESSION_BUFFER_ATTR );
    }

    @Override
    public void filterWrite ( final NextFilter nextFilter, final IoSession session, final WriteRequest writeRequest ) throws Exception
    {
        if ( writeRequest.getMessage () instanceof IoBuffer )
        {
            final IoBuffer inData = (IoBuffer)writeRequest.getMessage ();
            final IoBuffer outData = IoBuffer.allocate ( inData.remaining () + 4 );

            outData.put ( (byte)this.version );
            outData.put ( (byte)0 );

            outData.putShort ( (short) ( inData.remaining () + 4 ) );

            outData.put ( inData );

            outData.flip ();

            logger.debug ( "TPKT out: {}", outData );

            nextFilter.filterWrite ( session, new WriteRequestWrapper ( writeRequest ) {
                public Object getMessage ()
                {
                    return outData;
                }
            } );
        }
        else
        {
            nextFilter.filterWrite ( session, writeRequest );
        }
    }
}
