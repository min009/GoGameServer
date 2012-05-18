package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import edu.berkeley.cs.cs162.common.StoneColor;

public class StoneColorInfo implements Writable {
	public static StoneColor get_opposite(StoneColor c){
		if(c == StoneColor.BLACK){
			return StoneColor.WHITE;
		}
		else if(c == StoneColor.WHITE){
			return StoneColor.BLACK;
		}
		else{
			return StoneColor.NONE;
		}
	}
	
	public StoneColor s;
	
    public StoneColorInfo() {
        // TODO Auto-generated constructor stub
    	s = StoneColor.NONE;
    }

    @Override
    public void readFrom(InputStream in) throws IOException {
        // TODO Auto-generated method stub
    	DataInputStream data_in = new DataInputStream(in);
    	byte b = data_in.readByte();
    	
    	if( b == MessageProtocol.STONE_BLACK )
    	   s = StoneColor.BLACK;
    	else if( b == MessageProtocol.STONE_WHITE )
    	   s = StoneColor.WHITE;
    	else if( b == MessageProtocol.STONE_NONE )
    	   s = StoneColor.NONE;
    	else{
    		System.out.println("got corrupted message");
			throw new IOException();
    	}

    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    	DataOutputStream data_out = new DataOutputStream(out);
    	if( s == StoneColor.BLACK ){
    		data_out.writeByte( MessageProtocol.STONE_BLACK );
    	}
    	else if( s == StoneColor.WHITE ){
    		data_out.writeByte( MessageProtocol.STONE_WHITE );
    	}
    	else{
    		data_out.writeByte( MessageProtocol.STONE_NONE );
    	}

    }

    public Writable instantiate(){
    	return new StoneColorInfo();
    }
}//end class StoneColorInfo
