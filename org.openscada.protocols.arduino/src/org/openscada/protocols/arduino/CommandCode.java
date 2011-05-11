package org.openscada.protocols.arduino;

/*
 * This file is part of the OpenSCADA project
 * Copyright (C) 2010-2010 TH4 SYSTEMS GmbH (http://th4-systems.com)
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

public enum CommandCode
{
    DEVICE_DISCOVERY ( (byte)0x00 ),
    DISCOVER_ME ( (byte)0x01 ),

    REQUEST_CONFIGURATION ( (byte)0x02 ),
    RESPOND_CONFIGURATION ( (byte)0x03 ),

    REQUEST_DATA ( (byte)0x04 ),
    RESPOND_DATA ( (byte)0x05 ),

    REQUEST_WRITE ( (byte)0x06 ),
    RESPOND_WRITE ( (byte)0x07 );

    private byte commandCode;

    private CommandCode ( final byte commandCode )
    {
        this.commandCode = commandCode;
    }

    public byte getCommandCode ()
    {
        return this.commandCode;
    }
}
