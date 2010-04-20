/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2006-2010 inavare GmbH (http://inavare.com)
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

package org.openscada.protocols.dave.testing;

import java.net.InetSocketAddress;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerDelegate;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.openscada.protocols.dave.DaveFilter;
import org.openscada.protocols.iso8073.COTPFilter;
import org.openscada.protocols.tkpt.TPKTFilter;

public class Application1 implements IApplication
{
    private volatile boolean running = true;

    public Object start ( final IApplicationContext context ) throws Exception
    {
        final NioSocketConnector connector = new NioSocketConnector ();

        connector.setHandler ( new SingleSessionIoHandlerDelegate ( new SingleSessionIoHandlerFactory () {

            public SingleSessionIoHandler getHandler ( final IoSession session ) throws Exception
            {
                return new DaveHandler ( session );
            }
        } ) );

        connector.getFilterChain ().addLast ( "logger", new LoggingFilter ( this.getClass ().getName () ) );
        connector.getFilterChain ().addLast ( "tpkt", new TPKTFilter ( 3 ) );
        connector.getFilterChain ().addLast ( "cotp", new COTPFilter ( 0, (byte)3 ) );
        connector.getFilterChain ().addLast ( "dave", new DaveFilter () );

        connector.connect ( new InetSocketAddress ( "192.168.1.83", 102 ) );

        while ( this.running )
        {
            Thread.sleep ( 1000 );
        }

        return null;
    }

    public void stop ()
    {
        this.running = false;
    }

}
