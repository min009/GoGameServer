package edu.berkeley.cs.cs162.Writable;

public abstract class Reply extends Message
{
	//can only be reply op code
	public byte replyOpCode = (byte) 30;
	
	protected boolean is_valid_replyOpCode(byte r){
		if( r == MessageProtocol.OP_STATUS_OK ||
			r == MessageProtocol.OP_STATUS_RESUME ||
			r == MessageProtocol.OP_ERROR_REJECTED ||
			r == MessageProtocol.OP_ERROR_INVALID_GAME ||
			r == MessageProtocol.OP_ERROR_BAD_AUTH ||
			r == MessageProtocol.OP_ERROR_UNCONNECTED )
			return true;
		else{
			return false;
		}
	}
}
