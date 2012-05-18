package edu.berkeley.cs.cs162.Writable;

public abstract class Message implements Writable {
    
    /**
     * Specifies the opCode of the Message
     */
    byte opCode;
    
    public Message() {
        // TODO Auto-generated constructor stub    
    	opCode = (byte) 30;
    }
    
    public byte getMsgType() {
        return opCode;
    }
    
    public void setMsgType( byte o )
    {
		if (o == MessageProtocol.OP_TYPE_CONNECT
		 || o == MessageProtocol.OP_TYPE_DISCONNECT
		 || o == MessageProtocol.OP_TYPE_LISTGAMES
		 || o == MessageProtocol.OP_TYPE_JOIN
		 || o == MessageProtocol.OP_TYPE_LEAVE
		 || o == MessageProtocol.OP_TYPE_WAITFORGAME
		 || o == MessageProtocol.OP_TYPE_REGISTER
		 || o == MessageProtocol.OP_TYPE_CHANGEPW
		 || o == MessageProtocol.OP_TYPE_GAMESTART
		 || o == MessageProtocol.OP_TYPE_GAMEOVER
		 || o == MessageProtocol.OP_TYPE_MAKEMOVE
		 || o == MessageProtocol.OP_TYPE_GETMOVE) 
		{
			opCode = o;
		} 
		else {
			System.out.println("Error in setMsgType!!!");
		}
    }

}//end Message class
