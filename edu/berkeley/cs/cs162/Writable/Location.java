package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Location implements Writable {

	public int x, y;
	
    public Location() {
        // TODO Auto-generated constructor stub  
    	x = 0;
    	y = 0;
    }
    
    public Location(int xp, int yp) {
    	this.x = xp;
    	this.y = yp;
    }
    
    //copy constructor
    public Location(Location obj){
    	this.x = obj.x;
    	this.y = obj.y;
    }
    
    public int getX() { return x; }
    public int getY() { return y; }
    
    public boolean equals(Object other) {
    	if (other instanceof Location) {
    		Location otherLocation = (Location)other;
    		return this.x == otherLocation.x && this.y == otherLocation.y;
    	} else {
    		return false;
    	}
    }
    
    public String toString() {
    	return "(" + getX() + "," + getY() + ")";
    }
    
    public int hashCode(){
    	return this.x + 934 * this.y;
    }
    
    @Override
    public void readFrom(InputStream in) throws IOException {
        // TODO Auto-generated method stub
    	DataInputStream data_in = new DataInputStream(in);
    	x = data_in.readInt();
    	y = data_in.readInt();
    	
    	/*
    	if( x < 0 || y < 0 ){
    		System.out.println("got corrupted message in readFrom of Location");
			throw new IOException();
    	}*/
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    	DataOutputStream data_out = new DataOutputStream(out);
    	data_out.writeInt(x);
    	data_out.writeInt(y);
    }

    public Writable instantiate(){
    	return new Location();
    }
}//end Location class
