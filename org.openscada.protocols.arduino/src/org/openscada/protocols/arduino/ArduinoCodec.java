package org.openscada.protocols.arduino;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.openscada.core.Variant;
import org.openscada.protocols.arduino.messages.CommonMessage;
import org.openscada.protocols.arduino.messages.ConfigurationMessage;
import org.openscada.protocols.arduino.messages.DataMessage;

public class ArduinoCodec implements ProtocolEncoder, ProtocolDecoder
{

    private final Charset charset;

    public ArduinoCodec ()
    {
        this.charset = Charset.forName ( "ISO-8859-1" );
    }

    @Override
    public void decode ( final IoSession session, final IoBuffer data, final ProtocolDecoderOutput output ) throws Exception
    {
        final short magic = data.getShort ();
        final byte version = data.get ();
        final int sequence = data.getInt ();
        final byte commandCode = data.get ();

        if ( magic != 1202 )
        {
            throw new ProtocolCodecException ( String.format ( "Magic code should be 1202 but is %s", magic ) );
        }
        if ( version != 1 )
        {
            throw new ProtocolCodecException ( String.format ( "Version should be %s but is %s", 1, version ) );
        }

        decodeMessage ( sequence, commandCode, data, output );
    }

    private void decodeMessage ( final int sequence, final byte commandCode, final IoBuffer data, final ProtocolDecoderOutput output ) throws ProtocolCodecException
    {
        switch ( commandCode )
        {
        case 3:
            decodeResponseConfiguration ( data, output, sequence );
            break;
        case 5:
            decodeResponseData ( data, output, sequence );
            break;
        }

    }

    private void decodeResponseData ( final IoBuffer data, final ProtocolDecoderOutput output, final int sequence ) throws ProtocolCodecException
    {
        final byte nin = data.get ();

        final Variant[] vars = new Variant[nin];

        for ( int i = 0; i < nin; i++ )
        {
            vars[i] = decodeData ( data );
        }

        final DataMessage msg = new DataMessage ( sequence, CommandCode.RESPOND_DATA, vars );
        output.write ( msg );
    }

    private Variant decodeData ( final IoBuffer data ) throws ProtocolCodecException
    {
        final byte dataType = data.get ();
        switch ( dataType )
        {
        case 1:
            return Variant.valueOf ( data.get () != 0x00 );
        case 2:
            return Variant.valueOf ( data.getInt () );
        case 3:
            return Variant.valueOf ( data.getLong () );
        case 4:
            return Variant.valueOf ( data.getDouble () );
        default:
            throw new ProtocolCodecException ( String.format ( "Data type %02x is unknown", dataType ) );
        }
    }

    private void decodeResponseConfiguration ( final IoBuffer data, final ProtocolDecoderOutput output, final int sequence )
    {
        final byte nin = data.get ();
        final byte nout = data.get ();

        @SuppressWarnings ( "unchecked" )
        final Map<Integer, Object> parameters[] = new Map[nin + nout];

        while ( data.hasRemaining () )
        {
            final byte type = data.get ();
            final byte signalNumber = data.get ();
            final byte len = data.get ();

            final byte[] dataBuffer = new byte[len];
            data.get ( dataBuffer );

            if ( parameters[signalNumber] == null )
            {
                parameters[signalNumber] = new HashMap<Integer, Object> ();
            }
            parameters[signalNumber].put ( (int)type, convertData ( type, dataBuffer ) );
        }

        final ConfigurationMessage msg = new ConfigurationMessage ( sequence, CommandCode.RESPOND_CONFIGURATION, parameters );
        output.write ( msg );
    }

    private Object convertData ( final byte type, final byte[] dataBuffer )
    {
        switch ( type )
        {
        case 0x01:
            return dataBuffer[0];
        case 0x02:
            return String.valueOf ( this.charset.decode ( ByteBuffer.wrap ( dataBuffer ) ) );
        default:
            return dataBuffer;
        }
    }

    @Override
    public void finishDecode ( final IoSession session, final ProtocolDecoderOutput output ) throws Exception
    {
    }

    @Override
    public void dispose ( final IoSession session ) throws Exception
    {
    }

    @Override
    public void encode ( final IoSession session, final Object message, final ProtocolEncoderOutput output ) throws Exception
    {
        final IoBuffer data = IoBuffer.allocate ( 0 );
        data.setAutoExpand ( true );

        if ( message instanceof CommonMessage )
        {
            encodeHeader ( data, (CommonMessage)message );
        }
        data.flip ();
        output.write ( data );
    }

    private void encodeHeader ( final IoBuffer data, final CommonMessage message )
    {
        data.putShort ( (short)1202 );
        data.put ( (byte)0x01 );
        data.putInt ( message.getSequence () );
        data.put ( message.getCommandCode ().getCommandCode () );
    }

}
