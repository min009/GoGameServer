package edu.berkeley.cs.cs162.Client;

import edu.berkeley.cs.cs162.Hash.ClientHash;
import edu.berkeley.cs.cs162.Writable.ClientInfo;
import edu.berkeley.cs.cs162.Writable.ServerMessage;
import edu.berkeley.cs.cs162.Writable.MessageProtocol;

abstract public class BaseClient implements Client {

    String name;
    byte type;
    ClientHash hash = new ClientHash();

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getType() {
        return this.type;
    }

    public BaseClient() {
        this("");
    }

    public BaseClient(String name) {
        this(name, (byte) -1);
    }

    public BaseClient(String name, byte type) {
        this.name = name;
        this.type = type;
    }
    
    public ClientInfo getClientInfo() {
        return null;
    }
    
    public ServerMessage make_connect_message(String passWd){
		ServerMessage blah = new ServerMessage();
		blah.setMsgType(MessageProtocol.OP_TYPE_CONNECT);
		blah.player = getClientInfo();
		try{
			String hashPassWd = hash.getHash(passWd);
			blah.passwordHash.s = hashPassWd;
		}
		catch(Exception e){
			System.out.println("Exception thrown in hashing password");
		}
		return blah;
	}
    public ServerMessage make_changePW_message(String passWd){
		ServerMessage blah = new ServerMessage();
		blah.setMsgType(MessageProtocol.OP_TYPE_CHANGEPW);
		blah.player = getClientInfo();
		try{
			String hashPassWd = hash.getHash(passWd);
			blah.passwordHash.s = hashPassWd;
		}
		catch(Exception e){
			System.out.println("Exception thrown in hashing password");
		}
		return blah;
    }
    
    public ServerMessage make_register_message(String passWd){
    	ServerMessage blah = new ServerMessage();
    	blah.setMsgType(MessageProtocol.OP_TYPE_REGISTER);
    	blah.player = getClientInfo();
		try{
			String hashPassWd = hash.getHash(passWd);
			blah.passwordHash.s = hashPassWd;
		}
		catch(Exception e){
			System.out.println("Exception thrown in hashing password");
		}
    	return blah;
    }
    
    public ServerMessage make_disconnect_message(){
		ServerMessage blah = new ServerMessage();
		blah.setMsgType(MessageProtocol.OP_TYPE_DISCONNECT);
		
		return blah;
	}
}
