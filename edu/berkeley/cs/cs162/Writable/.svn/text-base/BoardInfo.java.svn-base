package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import edu.berkeley.cs.cs162.common.StoneColor;
import edu.berkeley.cs.cs162.common.Board;

//used in gameStart and join's reply
public class BoardInfo implements Writable {
	private Board b;
	
	//default constructor
    public BoardInfo() {
        // TODO Auto-generated constructor stub
    	b = new Board(3);
    }
    
    public BoardInfo(int size) {
    	b = new Board(size);
    }
    
    public BoardInfo(Board obj){
    	b = obj;
    }
    
    public int getSize() {
    	return b.getSize();
    }
    
    public boolean addStone(Location loc, StoneColor color) {
    	return b.addStone(loc, color);
    }
    
    public boolean removeStone(Location loc) {
    	return b.removeStone(loc);
    }
    
    public StoneColor getAtLocation(Location loc) {
    	return b.getAtLocation(loc);
    }
    
    public void printBoard(){
    	b.printBoard();
    }
    
    public Board getBoard(){
    	return b;
    }
    
    public boolean equals(BoardInfo obj){
    	return b.equals(obj.b);
    }
    
    public void readFrom(InputStream in) throws IOException {
    	DataInputStream data_in = new DataInputStream(in);
    	int s = data_in.readInt();
    	
    	if( s < 0 ){
    		System.out.println("got corrupted message");
			throw new IOException();
    	}
    	
    	b = new Board( s );
    	
    	for(int i = 0; i < s; i++){
    		int useless = data_in.readInt();
    		
    		if(useless < 0){
    			System.out.println("got corrupted message");
    			throw new IOException();
    		}
    		
    		for(int x = 0; x < s; x++){
    			StoneColorInfo t = new StoneColorInfo();
    			t.readFrom(in);
    		
    			if(t.s != StoneColor.NONE){
    				b.addStone(new Location(x, i), t.s);
    			}
    		}
    	}
    }
    
    public void writeTo(OutputStream out) throws IOException {
    	int s = b.getSize();
    	DataOutputStream data_out = new DataOutputStream(out);
    	data_out.writeInt( s );
    	
    	for(int i = 0; i < s; i++){
    		data_out.writeInt( s );
    		for(int x = 0; x < s; x++){
    			StoneColor sc = b.getAtLocation(new Location(x, i));
    			StoneColorInfo t = new StoneColorInfo();
    			t.s = sc;
    			t.writeTo(out);
    		}
    	}
    }
    
    /*
    @Override
    public void readFrom(InputStream in) throws IOException {
        // TODO Auto-generated method stub
    	DataInputStream data_in = new DataInputStream(in);
    	b = new Board( data_in.readInt() );
    	    	
    	for(int iy = 0; iy < b.getSize(); iy++)
    	{
    		for(int ix = 0; ix < b.getSize(); ix++)
    	    {
    			Location current_location = new Location(ix, iy);
    			StoneColorInfo current_color = new StoneColorInfo();
    	        current_color.readFrom(in);
    	        
    	        //if stone color read is not none
    	        if( current_color.s != StoneColor.NONE ){
    	        	b.addStone(current_location, current_color.s);
    	        }
    	       
    	    }
    	 }
    }//end readFrom

    @Override
    public void writeTo(OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    	DataOutputStream data_out = new DataOutputStream(out);
    	data_out.writeInt( b.getSize() );
    	Location current_location = new Location();
    	
    	for( int iy = 0; iy < b.getSize(); iy++ )
    	{
    	   for( int ix = 0; ix < b.getSize(); ix++ )
    	   {
    	      current_location.x =  ix;
    	      current_location.y = iy;
    	      
    	      StoneColor c = b.getAtLocation(current_location);
    	      StoneColorInfo temporary = new StoneColorInfo();
    	      temporary.s = c;
    	      temporary.writeTo( out );
    	      
    	   }
    	}

    }//end writeTo
	*/
    public Writable instantiate(){
    	return new BoardInfo();
    }
}//end BoardInfo
