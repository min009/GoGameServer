package edu.berkeley.cs.cs162.Server;

import edu.berkeley.cs.cs162.Synchronization.ReaderWriterLock;
import edu.berkeley.cs.cs162.Writable.Location;
import edu.berkeley.cs.cs162.Writable.Rules;
import edu.berkeley.cs.cs162.Writable.MessageProtocol;
import edu.berkeley.cs.cs162.Writable.*;
import edu.berkeley.cs.cs162.common.*;


import java.util.LinkedList;
import java.util.Vector;

/**
   A Game represents a single game of Go. There may be multiple games running at
   once, each with two players and any number of observers. The Game must keep
   track of the board state and check whether or not moves made by players are
   valid. If a player's move is not valid, the game should end immediately and
   the player who made the invalid move loses.

   The Game is responsible for informing all players and observers of all game
   events and deciding when a game is over. All players and observers should be
   kept track of by name. Games may not have direct access to Observer or Player
   objects.
 */
public class Game {
    private Board theBoard;
    //*player's score. Territory score is calculated when game is over.
    private double black_player_score = 0;
    private double white_player_score = 0.5;
    //front of the list is oldest board state, back of list is newest board state
    private LinkedList<Board> pastBoards = new LinkedList<Board>();
    private String gameName;
    private String blackPlayer, whitePlayer;
    private int num_turn; //record how many turns have passed 
    private LinkedList<String> observerList;
    //private Boolean exit = false;
    private int passed = 0; //if 2 players pass consecutively, then game over
	Vector<Location> captured_stone_locations=new Vector<Location>();
	protected ReaderWriterLock observerLock;
	Board copyBoard;
    private byte btype, wtype;
    String expectPlayer;
    //added gameId for p3
    private int gameId;
    
    int debugging = 0;
    
    public Game(String game_name_para, ClientInfo blackPlayer_para, ClientInfo whitePlayer_para, Board initialBoard) {
    	// TODO: implement me
        theBoard = initialBoard;
        this.gameName = game_name_para;
        blackPlayer = blackPlayer_para.name.s;
        whitePlayer = whitePlayer_para.name.s;
        btype=blackPlayer_para.playerType;
        wtype=whitePlayer_para.playerType;
        expectPlayer = blackPlayer;
        observerList=new LinkedList<String>();
        observerLock=new ReaderWriterLock();
    }
    
    public void setGameId(int id){
    	gameId=id;
    }
    
    public int getGameId(){
    	return gameId;
    }

    public String getName() {
    	return gameName;
    }
    
    public String getLoser(){
    	if(black_player_score > white_player_score){
    		return whitePlayer;
    	}
    	else{
    		return blackPlayer;
    	}
    }
    
    public byte getLoserType(){
    	if(black_player_score > white_player_score){
    		return wtype;
    	}
    	else{
    		return btype;
    	}
    }
    
    public ClientInfo getOponentInfo(String s){
    	if(blackPlayer.equals(s)){
    		return new ClientInfo(whitePlayer, wtype);
    	}
    	else{
    		return new ClientInfo(blackPlayer, btype);
    	}
    }
    
    public boolean containOb(String ob){
    	return observerList.contains(ob);
    }
    
    public byte getWinerType(){
    	if(black_player_score > white_player_score){
    		return btype;
    	}
    	else{
    		return wtype;
    	}
    }
    
    public void addObserver(String ob){
    	observerLock.writeLock();
    	observerList.add(ob);
    	observerLock.writeUnlock();
    }
    
    public void removeObserver(String ob){
    	observerLock.writeLock();
    	observerList.remove(ob);
    	observerLock.writeUnlock();
    }
    public LinkedList<String> getListOfObservers(){
    	return observerList;
    }
    
    
    
    public String getBlackPlayerName(){
    	return blackPlayer;
    }
    
    public String getWhitePlayerName(){
    	return whitePlayer;
    }

    /**
       @return The current board state.
     */
    public Board getBoard() {
    	return theBoard;
    }

    public String theWinner(){
    	if(black_player_score > white_player_score){
    		return blackPlayer;
    	}
    	else{
    		return whitePlayer;
    	}
    }

    public double getScore(String name){
    	if(name.equals(blackPlayer)){
    		return black_player_score;
    	}
    	else{
    		return white_player_score;
    	}
    }
    
    public Vector<Location> capturedStones(){
		return captured_stone_locations;
    }
    
    public void calculateTotalScores(){
    	black_player_score += Rules.countOwnedTerritory(theBoard, StoneColor.BLACK);
		white_player_score += Rules.countOwnedTerritory(theBoard, StoneColor.WHITE);
		
    }
    
    public boolean isYourTurn(String name){
    	if(expectPlayer == name){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    public byte makeMove(String pName, Location loc){
		byte returnOpType = MessageProtocol.GAME_OK;
		StoneColor color;
		StoneColor captured_stone_color = StoneColor.NONE;
		
		captured_stone_locations.clear();

		if( pName.equals(this.blackPlayer) ){
			color = StoneColor.BLACK;
		}
		else{
			color = StoneColor.WHITE;
		}
			
		passed = 0; // ever call to makeMove will set 'passed' to 0
		num_turn++;
		
		boolean violate_rule = false;
		
		copyBoard = new Board(theBoard);
		if(theBoard.getAtLocation(loc) != StoneColor.NONE){ //place stone on occupied position
			violate_rule = true;
			returnOpType = MessageProtocol.PLAYER_INVALID_MOVE;
		}
		else if (loc.x<0 || loc.y<0 || loc.x>=theBoard.getSize() || loc.y>=theBoard.getSize()){
			violate_rule = true;
			returnOpType = MessageProtocol.PLAYER_INVALID_MOVE;
		}
		else{			
			//copyBoard = new Board(theBoard);
			
			theBoard.addStone(loc, color);  //place the current stone to the board
			
			//have to use vector here, because getCapturedStones in Rule returns a vector
			captured_stone_locations = Rules.getCapturedStones(theBoard, color, loc);
		    
			for(int index = 0; index < captured_stone_locations.size(); index++){
				theBoard.removeStone( captured_stone_locations.elementAt(index) );
			}
			
		    if(!pastBoards.isEmpty()){
				if(theBoard.equals(pastBoards.getFirst()) && pastBoards.size()==2){    //check whether the player violates KO rule
					violate_rule = true;
					returnOpType = MessageProtocol.PLAYER_KO_RULE;
				}
		    }
			
			for(int index = 0; index < captured_stone_locations.size() && !violate_rule; index++){
				if(index == 0){
					captured_stone_color = copyBoard.getAtLocation( captured_stone_locations.elementAt(index) );
				}
				if( captured_stone_color == StoneColor.BLACK ){
					this.white_player_score += 1;
				}
				else if( captured_stone_color == StoneColor.WHITE ){
					this.black_player_score += 1;
				}
			}
			
		}
			
		/*
		 * Conditions that violate the rule
		 * 1. violate KO rule
		 * 2. illegal move: place on occupied position 
		 * */
		
		//if illegal(repeat board position or place a stone on another stone)
		if( violate_rule ){
			theBoard = copyBoard;
			
			calculateTotalScores();
			if(debugging == 1){
				theBoard.printBoard();
			}
            return returnOpType;
		
		}
		else{ // the Game is ok to move on
			if(debugging == 1){
				theBoard.printBoard();
			}
			if(!pastBoards.isEmpty() && pastBoards.size() == 2){
			   pastBoards.removeFirst();
			}
			pastBoards.addLast(new Board(theBoard));
			return returnOpType;
    	}
		
	}
    
    /*
     * @return false if pass in sequence; true otherwise;
     * */
	public boolean makePass(String pName){
	    captured_stone_locations.clear();
		passed += 1;
		num_turn++;
		if (passed == 2){
			calculateTotalScores();
			if(debugging == 1){
				theBoard.printBoard();
			}
			return false;  //pass in sequence
		}
		else{
			if(debugging == 1){
				theBoard.printBoard();
			}
			return true;   //good to move on
		}
	}
	
	public void debug(){
		debugging = 1;
	}
	
	
	//for debug purpose
	public void makeCopyOfMove(String pName,Location loc, Board copy){
		if (pName.equals(blackPlayer)){
			copy.addStone(loc, StoneColor.BLACK);
		}
		else{
			copy.addStone(loc, StoneColor.WHITE);
		}
	}
	
	//for test purpose
	public double b=0,w=0.5;
	public void captureStone(Board theBB, Vector<Location> stone_locations, Double whiteScore, Double blackScore){
		StoneColor captured_color;
		System.out.println( " size: " + stone_locations.size());
		for(int index = 0; index < stone_locations.size(); index++){
			if(index == 0){
				captured_color = theBB.getAtLocation( stone_locations.elementAt(index) );
			
				if( captured_color == StoneColor.BLACK ){
					w += 1;
				}
				else if( captured_color == StoneColor.WHITE ){
					b += 1;
				}
				theBB.removeStone( captured_stone_locations.elementAt(index) );
			}
		}
		//System.out.println("in capture, b score: " + b + " w score: "+  w);
	}
}

