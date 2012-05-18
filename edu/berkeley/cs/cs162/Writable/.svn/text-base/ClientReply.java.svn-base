package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

//a reply to the client
public class ClientReply extends Reply
{
	//list of GameInfo
	public ListInfo game_list;
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
		//data_out.writeByte(opCode);
		data_out.writeByte( replyOpCode );

		// *write parameters, if there's any*
		if( opCode == MessageProtocol.OP_TYPE_LISTGAMES && replyOpCode == MessageProtocol.OP_STATUS_OK )
		{
			if(game_list == null){
				game_list = new ListInfo();
			}
			game_list.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_JOIN && replyOpCode == MessageProtocol.OP_STATUS_OK )
		{
			board.writeTo(out);
			blackPlayer.writeTo(out);
			whitePlayer.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_WAITFORGAME && replyOpCode == MessageProtocol.OP_STATUS_RESUME)
		{
			game.writeTo(out);
			board.writeTo(out);
			blackPlayer.writeTo(out);
			whitePlayer.writeTo(out);
		}
	}//end writeTo

	//remember to set opCode before calling this method
	public void readFrom(InputStream in) throws IOException
	{
		if( opCode == (byte) 30 ){
			throw new IOException();
		}
		
		DataInputStream data_in = new DataInputStream(in);
		//opCode=data_in.readByte();
		replyOpCode = data_in.readByte();

		if( !is_valid_replyOpCode(replyOpCode) ){
			System.out.println("got corrupted message in readFrom of ClientReply");
			throw new IOException();
		}
		
		if( opCode == MessageProtocol.OP_TYPE_LISTGAMES && replyOpCode == MessageProtocol.OP_STATUS_OK )
		{
			game_list = new ListInfo();
			game_list.original_object = new GameInfo();
			game_list.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_JOIN && replyOpCode == MessageProtocol.OP_STATUS_OK )
		{
			board = new BoardInfo();
			board.readFrom(in);
			blackPlayer = new ClientInfo();
			blackPlayer.readFrom(in);
			whitePlayer = new ClientInfo();
			whitePlayer.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_WAITFORGAME && replyOpCode == MessageProtocol.OP_STATUS_RESUME)
		{
			game = new GameInfo();
			game.readFrom(in);
			board = new BoardInfo();
			board.readFrom(in);
			blackPlayer = new ClientInfo();
			blackPlayer.readFrom(in);
			whitePlayer = new ClientInfo();
			whitePlayer.readFrom(in);
		}
	}//end readFrom
	
	public Writable instantiate(){
    	return new ClientReply();
    }
}//end class ClientReply
