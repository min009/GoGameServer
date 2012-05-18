package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
//import java.util.LinkedList;

public class ClientMessage extends Message {
	public GameInfo game;
	public BoardInfo board;
	public ClientInfo blackPlayer;
	public ClientInfo whitePlayer;
	public double blackScore;
	public double whiteScore;
	public ClientInfo winner_or_player;
	public byte reason_or_moveType_or_errorNum;
	public Location loc;
	//list of Location
	public ListInfo captured_stones;
	public ClientInfo player_for_gameover;
	public StringInfo errorMsg;
	
	public void writeTo(OutputStream out) throws IOException
	{
		DataOutputStream data_out = new DataOutputStream(out);
		data_out.writeByte( opCode );

		if( opCode == MessageProtocol.OP_TYPE_GAMESTART )
		{
			game.writeTo(out);
			board.writeTo(out);
			blackPlayer.writeTo(out);
			whitePlayer.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_GAMEOVER )
		{
			game.writeTo(out);
			data_out.writeDouble(blackScore);
			data_out.writeDouble(whiteScore);
			winner_or_player.writeTo(out);
			data_out.writeByte(reason_or_moveType_or_errorNum);
			
			if(reason_or_moveType_or_errorNum != MessageProtocol.GAME_OK){
			//if(winner_or_player == null){
			
			player_for_gameover.writeTo(out);	
			errorMsg.writeTo(out);
			}
			//}
		}
		else if( opCode == MessageProtocol.OP_TYPE_MAKEMOVE )
		{
			game.writeTo(out);
			winner_or_player.writeTo(out);
			data_out.writeByte(reason_or_moveType_or_errorNum);
			
			if(loc == null){
				loc = new Location(0,0);
			}
			loc.writeTo(out);
			
			if(captured_stones == null){
				captured_stones = new ListInfo();
			}
			//write list of captured stones
			captured_stones.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_GETMOVE )
		{
			
		}
		else{ 
			System.out.println("got corrupted message in writeTo of ClientMessage");
			throw new IOException(); 
		}
	}//end writeTo

	public void readFrom(InputStream in) throws IOException
	{
		DataInputStream data_in = new DataInputStream(in);
		opCode = data_in.readByte();
		//System.out.println("aaa");
		if( opCode == MessageProtocol.OP_TYPE_GAMESTART )
		{
			//System.out.println("bbb");
			game = new GameInfo();
			game.readFrom(in);
			//System.out.println("ccc");
			board = new BoardInfo();
			board.readFrom(in);
			blackPlayer = new ClientInfo();
			//System.out.println("ddd");
			blackPlayer.readFrom(in);
			//System.out.println("eee");
			whitePlayer = new ClientInfo();
			whitePlayer.readFrom(in);
			//System.out.println("fff");
		}
		else if( opCode == MessageProtocol.OP_TYPE_GAMEOVER )
		{
			game = new GameInfo();
			game.readFrom(in);
			blackScore = data_in.readDouble();
			whiteScore = data_in.readDouble();
			
			if(blackScore < 0 || whiteScore < 0){
				System.out.println("got corrupted message");
				throw new IOException();
			}
			
			winner_or_player = new ClientInfo();
			winner_or_player.readFrom(in);
			reason_or_moveType_or_errorNum = data_in.readByte();
			
			player_for_gameover = new ClientInfo();
			errorMsg = new StringInfo();
			if(reason_or_moveType_or_errorNum != MessageProtocol.GAME_OK){
				
				player_for_gameover.readFrom(in);
				
				errorMsg.readFrom(in);
			}
		}
		else if( opCode == MessageProtocol.OP_TYPE_MAKEMOVE )
		{
			game = new GameInfo();
			game.readFrom(in);
			winner_or_player = new ClientInfo();
			winner_or_player.readFrom(in);
			reason_or_moveType_or_errorNum = data_in.readByte();
			loc = new Location();
			loc.readFrom(in);
			captured_stones = new ListInfo();
			captured_stones.original_object = new Location();
			captured_stones.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_GETMOVE )
		{
			
		}
		else{
			System.out.println("got corrupted message in readFrom of ClientMessage");
			throw new IOException();
		}
	}//end readFrom
	
	public Writable instantiate(){
    	return new ClientMessage();
    }
}//end ClientMessage class
