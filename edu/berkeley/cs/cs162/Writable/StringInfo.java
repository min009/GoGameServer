package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class StringInfo implements Writable
{
	public String s;

	public StringInfo(){
		s = "";
	}
	
	public void readFrom(InputStream in) throws IOException
	{
		//System.out.println("za");
		DataInputStream data_in = new DataInputStream(in);
		int size = data_in.readInt();
		
		if(size < 0){
			System.out.println("got corrupted message in readFrom of StringInfo");
			throw new IOException();
		}
		
		//System.out.println("stringinfo read, a size" + Integer.toString(size));
		char[] temp = new char[size];
		for( int i = 0; i < size; i++ )
		{
			temp[i] = data_in.readChar();
			//System.out.println("zc");
		}
		//System.out.println("stringinfo read, b");
		s = String.copyValueOf( temp );
	}//end readFrom

	public void writeTo(OutputStream out) throws IOException
	{
		DataOutputStream data_out = new DataOutputStream(out);
		data_out.writeInt( s.length() );

		for( int i = 0; i < s.length(); i++ )
		{
			data_out.writeChar( s.charAt(i) );
		}
   }//end writeTo
	
	public Writable instantiate(){
    	return new StringInfo();
    }
}