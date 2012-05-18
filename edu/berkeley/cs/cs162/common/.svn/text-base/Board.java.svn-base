package edu.berkeley.cs.cs162.common;

import edu.berkeley.cs.cs162.Writable.Location;
import java.util.HashMap;
import java.util.Iterator;
//import java.lang.Exception;

//To DO: modify add stone to make sure that it checks if the stone added is within the board.

/**
   The Board contains all information about a board state. A Board is square,
   and can have a width of any integer value in the range [3,19].
 */
public class Board {
	//must be between 3 and 19
	private int board_size;
	//Location that are not on the hashtable are empty
	private HashMap<Location, StoneColor> table;
	
    /*
       @param size The width of one side of the board.
    */
    public Board(int size) {
    	// TODO: implement me
    	if(3 <= size && size <= 19){
    		board_size = size;
    	}
    	else{
    		//lazy to throw exception
    		board_size = 19;
    	}
    	
    	table = new HashMap<Location, StoneColor>();
    		
    }

    //copy constructor
    public Board(Board obj){
    	board_size = obj.board_size;
    	table = new HashMap<Location, StoneColor>(obj.table);
    	
    }
    
    /*
       return The size of the board (width of one side of the board).
    */
    public int getSize() {
    	// TODO: implement me
    	return board_size;
    }

    /*
       Add a stone of the specified color to a location on the board.
    */
    //add a stone of the specified color to a location on the board, returning true when successful
    //precondition: loc must be a valid empty location on the board, and color cannot be NONE
    public boolean addStone(Location loc, StoneColor color) {
    	// TODO: implement me
    	//if loc is an empty location on the board and color is not NONE
        if( getAtLocation(loc) == StoneColor.NONE && color != StoneColor.NONE )
    	{
           table.put(loc, color);
           return true;
        }
        else
        {
           return false;
        }
    }

    /*
       Remove a stone from a location on the board.
    */
    //remove the stone at the location
    //return false if failed
    //precondition: loc must be on the board
    public boolean removeStone(Location loc) {
    	// TODO: implement me
    	//if loc is on the board (and not empty)
    	if( table.containsKey(loc) )
        {
           table.remove(loc);
           return true;
        }
        else
        {
           return false;
        }
    }

    /*
       return The StoneColor of the stone at the specified location.
    */
    //get StoneColor at the location
    //precondition: loc must be on the board
    public StoneColor getAtLocation(Location loc) {
    	// TODO: implement me
    	//if loc is on the board
    	
    	if( 0 <= loc.getX() && loc.getX() < board_size &&
    		0 <= loc.getY() && loc.getY() < board_size	)
        {
           //if loc is in keys of table
    		if( table.containsKey(loc) ){
              return table.get(loc);
    		}
           else
              return StoneColor.NONE;
        }
        else
        {
        	return StoneColor.NONE;
        }

    }
    
    public boolean equals(Board obj){
    	if( this.board_size == obj.board_size ){ //&& this.table.equals(obj.table) )
    	    Iterator<Location> i = this.table.keySet().iterator();
    	    while(i.hasNext()){
    	    	Location l = (Location) i.next();
        		if(obj.table.containsKey( l )){
        			if(obj.table.get(l) != this.table.get(l)){
        				return false;
        			}
        		}
        		else{
        			return false;
        		}
    	    }
    		return true;
    	}
    	else
    		return false;
    }
    
	public void printBoard(){
		System.out.println("B: Black; W: White");
		//System.out.println("the size of the table: " + table.size());
		for(int i=0; i < getSize(); i++){
			for(int j=0; j < getSize(); j++){
				Location loc = new Location(j,i);
				StoneColor col = getAtLocation(loc);
				if(col==StoneColor.BLACK){
					System.out.print("B");
				}
				else if(col==StoneColor.WHITE){
					System.out.print("W");
				}
				else{
				  System.out.print("+");
				}
			}
			System.out.println();
		}
	}
    
}
