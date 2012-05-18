package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

//represents a message for the server
public class ServerMessage extends Message
{
	public ClientInfo player;
	public StringInfo passwordHash;
	//public byte moveType;
	public Location loc;
	public GameInfo game;

	public ServerMessage(){
		player = new ClientInfo();
		passwordHash = new StringInfo();
		loc = new Location();
		game = new GameInfo();
	}
	
	public void readFrom(InputStream in) throws IOException
	{
		DataInputStream data_in = new DataInputStream(in);
		opCode = data_in.readByte();

		if( opCode == MessageProtocol.OP_TYPE_REGISTER){
			player = new ClientInfo();
			player.readFrom(in);
			passwordHash = new StringInfo();
			passwordHash.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_CHANGEPW){
			player = new ClientInfo();
			player.readFrom(in);
			passwordHash = new StringInfo();
			passwordHash.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_CONNECT ){
			player = new ClientInfo();
			player.readFrom(in);
			passwordHash = new StringInfo();
			passwordHash.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_DISCONNECT ){}
		else if( opCode == MessageProtocol.OP_TYPE_WAITFORGAME ){}
		else if( opCode == MessageProtocol.OP_TYPE_LISTGAMES ){}
		else if( opCode == MessageProtocol.OP_TYPE_JOIN ){
			game = new GameInfo();
			game.readFrom(in);
		}
		else if( opCode == MessageProtocol.OP_TYPE_LEAVE ){
			game = new GameInfo();
			game.readFrom(in);
		}
		else{
			System.out.println("got corrupted message in readFrom of ServerMessage");
			throw new IOException();
		}

	}//end readFrom

	public void writeTo(OutputStream out) throws IOException
	{
		DataOutputStream data_out = new DataOutputStream(out);
		data_out.writeByte( opCode );

		if( opCode == MessageProtocol.OP_TYPE_REGISTER){
			player.writeTo(out);
			passwordHash.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_CHANGEPW){
			player.writeTo(out);
			passwordHash.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_CONNECT )
		{
			player.writeTo(out);
			passwordHash.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_DISCONNECT ){}
		else if( opCode == MessageProtocol.OP_TYPE_WAITFORGAME ){}
		else if( opCode == MessageProtocol.OP_TYPE_LISTGAMES ){}
		else if( opCode == MessageProtocol.OP_TYPE_JOIN )
		{
			game.writeTo(out);
		}
		else if( opCode == MessageProtocol.OP_TYPE_LEAVE ){
			game.writeTo(out);
		}
		else{
			System.out.println("got corrupted message in writeTo of ServerMessage");
			throw new IOException();
		}
	}//end writeTo
	
	public Writable instantiate(){
    	return new ServerMessage();
    }

}//end ServerMessage class
