package edu.berkeley.cs.cs162.Writable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.LinkedList;

//used in: 
//1) list of Location in makeMove
//2) list of GameInfo in reply of listGames

public class ListInfo implements Writable
{
	public List<Writable> l;
	//set this to a Writable object before calling readFrom
	public Writable original_object;
	
	//set original_object first!
	public void readFrom(InputStream in) throws IOException
	{
		DataInputStream data_in = new DataInputStream(in);
		
		l = new LinkedList<Writable>();
		
		int size = data_in.readInt();
		
		for(int i = 0; i < size; i++)
		{
			l.add( original_object.instantiate() );
			(l.get(i)).readFrom(in);
		}
	}//end readFrom

	public void writeTo(OutputStream out) throws IOException
	{
		DataOutputStream data_out = new DataOutputStream(out);
		data_out.writeInt( l.size() );

		for(int i = 0; i < l.size(); i++)
		{
			(l.get(i)).writeTo( out );
		}
   }//end writeTo

	public Writable instantiate(){
    	return new ListInfo();
    }

	//assume l is a list of GameInfo and display it
	public void display_as_GameInfo(){
		GameInfo g;
		for(int i = 0; i < l.size(); i++){
			g = (GameInfo) l.get(i);
			System.out.println(g.name.s);
		}
	}

	//assume l is a list of GameInfo and return it
	public List<GameInfo> get_as_GameInfo(){
		LinkedList<GameInfo> gi = new LinkedList<GameInfo>();
		
		for(int i = 0; i < l.size(); i++){
			gi.add( (GameInfo) l.get(i) );
		}
		
		return gi;
	}
}//end ListInfo
