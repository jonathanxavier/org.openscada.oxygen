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

package org.openscada.protocols.arduino.testing;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArduinoHandler implements SingleSessionIoHandler
{
    private final static Logger logger = LoggerFactory.getLogger ( ArduinoHandler.class );

    private final IoSession session;

    public ArduinoHandler ( final IoSession session )
    {
        this.session = session;
    }

    @Override
    public void exceptionCaught ( final Throwable error ) throws Exception
    {
        System.out.println ( "Exception: " );
        error.printStackTrace ();
    }

    @Override
    public void messageReceived ( final Object message ) throws Exception
    {
        System.out.println ( System.currentTimeMillis () + " Received: " + this.session + " -> " + message );
    }

    @Override
    public void messageSent ( final Object message ) throws Exception
    {
        System.out.println ( "Sent: " + this.session + " -> " + message );
    }

    @Override
    public void sessionClosed () throws Exception
    {
        System.out.println ( "Closed: " + this.session );
    }

    @Override
    public void sessionCreated () throws Exception
    {
        System.out.println ( "Created: " + this.session );
    }

    @Override
    public void sessionIdle ( final IdleStatus status ) throws Exception
    {
        System.out.println ( "Idle: " + this.session + " -> " + status );
    }

    @Override
    public void sessionOpened () throws Exception
    {
        logger.debug ( "Session opened" );
    }

}
