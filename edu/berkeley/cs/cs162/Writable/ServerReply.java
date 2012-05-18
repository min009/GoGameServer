package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

//a reply to the server
public class ServerReply extends Reply
{
	public byte moveType;
	public Location loc;
	
	public GameInfo game;
	public BoardInfo board;
	public ClientInfo blackPlayer;
	public ClientInfo whitePlayer;
	
	//remember to set opCode before calling this method
	public void writeTo(OutputStream out) throws IOException
	{
		if( opCode == (byte) 30 ){
			throw new IOException();
		}
		
		DataOutputStream data_out = new DataOutputStream(out);
		data_out.writeByte( replyOpCode );
		
		// *write parameters, if there's any*
		if( opCode == MessageProtocol.OP_TYPE_GETMOVE )
		{
			data_out.writeByte( moveType );
			if(loc == null){
				loc = new Location(0, 0);
			}
			loc.writeTo( out );
		}
	}

	//remember to set opCode before calling this method
	public void readFrom(InputStream in) throws IOException
	{
		if( opCode == (byte) 30 ){
			throw new IOException();
		}
		
		DataInputStream data_in = new DataInputStream(in);
		replyOpCode = data_in.readByte();

		if( !is_valid_replyOpCode(replyOpCode) ){
				System.out.println("got corrupted message in readFrom of ServerReply");
				throw new IOException();
		}
		
		if( opCode == MessageProtocol.OP_TYPE_GETMOVE )
		{
			moveType = data_in.readByte();
			
			if(moveType != MessageProtocol.MOVE_STONE && 
			   moveType != MessageProtocol.MOVE_PASS && 
			   moveType != MessageProtocol.MOVE_FORFEIT){
				System.out.println("got corrupted message in readFrom of ServerReply");
				throw new IOException();
			}
			loc = new Location();
			loc.readFrom( in );
		}
	}

	public Writable instantiate(){
    	return new ServerReply();
    }
}
