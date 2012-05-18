package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

//import edu.berkeley.cs.cs162.Client.Player;

public class ClientInfo implements Writable {
	public StringInfo name;
	public byte playerType;

    public ClientInfo() {
        // TODO Auto-generated constructor stub 
    	name = new StringInfo();
    	name.s = "";
    	playerType = MessageProtocol.TYPE_HUMAN;
    }
    
    public ClientInfo(String sp, byte t){
    	name = new StringInfo();
    	name.s = sp;
    	playerType = t;
    }
    
    @Override
    public void readFrom(InputStream in) throws IOException {
        // TODO Auto-generated method stub 
    	DataInputStream data_in = new DataInputStream(in);
    	name = new StringInfo();
    	//System.out.println("clientinfo read1");
    	name.readFrom(in);
    	//System.out.println("clientinfo read2");
    	playerType = data_in.readByte();
    	//System.out.println("clientinfo read3");
    	if(playerType != MessageProtocol.TYPE_HUMAN && 
    	   playerType != MessageProtocol.TYPE_MACHINE && 
    	   playerType != MessageProtocol.TYPE_OBSERVER){
    		System.out.println("got corrupted message");
			throw new IOException();
    	}
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    	DataOutputStream data_out = new DataOutputStream(out);
    	name.writeTo(out);
    	data_out.writeByte(playerType);    
    }
    
    public Writable instantiate(){
    	return new ClientInfo();
    }
}//end ClientInfo class
