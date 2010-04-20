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

package org.openscada.protocols.dave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.mina.core.buffer.IoBuffer;

public class DaveReadResult extends DaveMessage
{
    private final Collection<Result> result;

    public DaveReadResult ( final Collection<Result> result )
    {
        this.result = new ArrayList<Result> ( result );
    }

    public Collection<Result> getResult ()
    {
        return Collections.unmodifiableCollection ( this.result );
    }

    public static class Result
    {
        private final Short error;

        private final IoBuffer data;

        public Result ( final short error )
        {
            this.error = error;
            this.data = null;
        }

        public Result ( final IoBuffer data )
        {
            this.error = null;
            this.data = data;
        }

        public IoBuffer getData ()
        {
            return this.data;
        }

        public boolean isError ()
        {
            return this.error != null;
        }

        public Short getError ()
        {
            return this.error;
        }

        @Override
        public String toString ()
        {
            return String.format ( isError () ? "E:" + this.error : "D:" + this.data );
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder sb = new StringBuilder ();

        sb.append ( "Result: " );

        int i = 0;
        for ( final Result result : this.result )
        {
            sb.append ( "[" );

            sb.append ( i + ": " );
            sb.append ( result );
            sb.append ( " " );

            sb.append ( "]" );

            i++;
        }

        return sb.toString ();
    }
}
