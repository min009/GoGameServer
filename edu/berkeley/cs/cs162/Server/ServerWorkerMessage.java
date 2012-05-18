package edu.berkeley.cs.cs162.Server;

import edu.berkeley.cs.cs162.Writable.*;
import java.util.*;

public class ServerWorkerMessage {
	/*
	 0 = gameStart
	 1 = gameOver
	 2 = makeMove
	 3 = getMove
	 4 = exit
	 */
	private int b;
	private Game game;
	private byte errorType;
	private String playerMadeMove;
	private byte pType;
	private byte moveType;
	private Location loc;
	private Vector<Location> captured;
	private ClientInfo winner;
	private ClientInfo loser;
	
	private ServerWorkerMessage(int a){
		b = a;
	}
	
	public String getWinner(){
		return winner.name.s;
	}
	
	public byte getWinnerType(){
		return winner.playerType;
	}
	
	public String getLoser(){
		return loser.name.s;
	}
	
	public byte getLoserType(){
		return loser.playerType;
	}
	
	public ClientInfo getWinInfo(){
		return winner;
	}
	
	public ClientInfo getLosInfo(){
		return loser;
	}
	
	public Game get_game(){
		return game;
	}
	
	public byte getError(){
		return errorType;
	}
	
	public Location getLoc(){
		return loc;
	}
	
	public byte getMoveType(){
		return moveType;
	}
	
	public Vector<Location> getCaptured(){
		return captured;
	}
	
	public byte getPlayerType(){
		return pType;
	}
	
	public String getName(){
		return playerMadeMove;
	}
	
	public boolean is_gameStart(){
		return b == 0;
	}
	
	public boolean is_gameOver(){
		return b == 1;
	}
	
	public boolean is_makeMove(){
		return b == 2;
	}
	
	public boolean is_getMove(){
		return b == 3;
	}
	
	public boolean is_exit(){
		return b == 4;
	}
	
	public static ServerWorkerMessage make_gameStart(Game g){
		ServerWorkerMessage m = new ServerWorkerMessage(0);
		m.game = g;
		return m;
	}
	
	public static ServerWorkerMessage make_gameOver(Game g, byte error, ClientInfo win, ClientInfo lose){
		ServerWorkerMessage m = new ServerWorkerMessage(1);
		m.game = g;
		m.errorType=error;
		m.winner=win;
		m.loser=lose;
		return m;
	}
	
	public static ServerWorkerMessage make_makeMove(Game g, String p, byte t, byte mt, Location l, Vector<Location> v){
		ServerWorkerMessage m = new ServerWorkerMessage(2);
		m.game=g;
		m.playerMadeMove=p;
		m.pType=t;
		m.moveType=mt;
		m.loc=l;
		m.captured=v;
		return m;
	}
	
	public static ServerWorkerMessage make_getMove(Game g){
		ServerWorkerMessage m = new ServerWorkerMessage(3);
		m.game=g;
		return m;
	}
	
	public static ServerWorkerMessage make_exit(){
		ServerWorkerMessage m = new ServerWorkerMessage(4);
		return m;
	}
}
