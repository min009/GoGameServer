package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameInfo implements Writable {
	public StringInfo name;
	
    public GameInfo() {
        // TODO Auto-generated constructor stub  
    	name = new StringInfo();
    	name.s = "";
    }
    
    public GameInfo(String n){
    	name = new StringInfo();
    	name.s = n;
    }
    
    @Override
    public void readFrom(InputStream in) throws IOException {
        // TODO Auto-generated method stub
    	name = new StringInfo(); 
    	name.readFrom(in);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    	name.writeTo(out);
    }

    public Writable instantiate(){
    	return new GameInfo();
    }
}//end GameInfo class
