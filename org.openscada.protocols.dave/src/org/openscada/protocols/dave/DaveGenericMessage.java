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

import org.apache.mina.core.buffer.IoBuffer;

public class DaveGenericMessage extends DaveMessage
{
    private final IoBuffer parameters;

    private final IoBuffer data;

    public DaveGenericMessage ( final IoBuffer parameters, final IoBuffer data )
    {
        this.parameters = parameters;
        this.data = data;
    }

    public IoBuffer getData ()
    {
        return this.data;
    }

    public IoBuffer getParameters ()
    {
        return this.parameters;
    }

    @Override
    public String toString ()
    {
        return String.format ( "Generic Message : Parameters: %s, Data: %s", this.parameters, this.data );
    }

}
