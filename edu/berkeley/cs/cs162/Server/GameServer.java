//package release.edu.berkeley.cs.cs162.Server;
package edu.berkeley.cs.cs162.Server;

import java.net.*;
import java.io.IOException;
import java.sql.DriverManager;
import java.util.*;

import edu.berkeley.cs.cs162.common.*;
import edu.berkeley.cs.cs162.Synchronization.*;
import edu.berkeley.cs.cs162.Writable.*;
import java.sql.*;


//import java.lang.*;
/*
 * idea: (needed server workers, pair check worker, 
 * 1. instantiate serversocket and then listen to the port, allocate new socket and new 
 * worker object, pass the sokcet to the worker and start the worker thread.
 * 2. we need a new subworker thread to check each worker object's random number they
 * received, if they are equal, pair them with the player name and choose one for server
 * to client, the other one for client to server.
 * 3. do 3 way handshaking(expand)
 * 4. 
 * 
 * ======================p3=====================
 * need to delete all the debug statement in gameserver constructor
 * 
 * 
 */

public class GameServer implements Runnable {
	
	public ServerSocket ss;
	public int connections;//(readerwriterlock needed for this)
	protected HashMap<String, Game> listOfGame; //(readerwriterlock needed for this)
	//explicitly make serverworker[0] as s to c, [1] as c to s
	protected HashMap<String, ServerWorker[] > clients; //(readerwriterlock needed for this)
	protected HashMap<Integer, ServerWorker> workerMaps;
	protected Queue<String> waitForGamePlayers;
	protected ReaderWriterLock RWLForConnections;
	protected ReaderWriterLock workerMapsLock;
	protected ReaderWriterLock clientsLock;
	protected ReaderWriterLock waitForGameLock;
	protected ReaderWriterLock gameLock;
	public Socket s=null;
	//added unfinished games for game reconstruction
	//here are variables for unfinished games and players
	protected HashMap<String, Game> pendingGame;  //used to quick access the unfinished games (key: gName, val: game obj)
	protected ReaderWriterLock pendingGameLock;
	protected HashMap<String, String> pendingPlayers;  //for player who has unfinished game to check if its opponent is already waiting for game (key: gameName, val:pName)
	protected ReaderWriterLock pendingPlayerLock;
	protected HashMap<String, String> forcingPlayers;  //used to quick lookup players who has unfinished game to play(key: player name, value: game name)
	protected ReaderWriterLock forcingLock;  
	protected HashMap<String,String> lastMoveMadeBy;
	protected Connection conn;
	protected Statement stat;
	
	
	public GameServer() throws Exception{
		//initialize all the necessary variables
		connections=0;
		listOfGame=new HashMap<String, Game>();
		clients=new HashMap<String, ServerWorker[]>();
		workerMaps=new HashMap<Integer, ServerWorker>();
		RWLForConnections=new ReaderWriterLock();
		workerMapsLock=new ReaderWriterLock();
		clientsLock=new ReaderWriterLock();
		waitForGamePlayers=new LinkedList<String>();
		waitForGameLock=new ReaderWriterLock();
		gameLock=new ReaderWriterLock();
		
		//added for p3
		pendingGame = new HashMap<String, Game>();
		pendingGameLock=new ReaderWriterLock();
		pendingPlayers=new HashMap<String,String>();
		pendingPlayerLock=new ReaderWriterLock();
		forcingPlayers=new HashMap<String, String>();
		forcingLock=new ReaderWriterLock();
		lastMoveMadeBy=new HashMap<String,String>();
		//add checking for db, if exist, check for unfinished games
		// if not exist, create one
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:cs162-project3.db");
		stat = conn.createStatement();
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS clients (clientId INTEGER PRIMARY KEY AUTOINCREMENT,name text NOT NULL UNIQUE,type int NOT NULL, passwordHash text NOT NULL)");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS games (gameId INTEGER PRIMARY KEY AUTOINCREMENT, blackPlayer int NOT NULL, whitePlayer int NOT NULL, boardSize int NOT NULL, blackScore real, whiteScore real, winner int, moveNum int NOT NULL, reason int, FOREIGN KEY(blackPlayer) REFERENCES clients(clientId), FOREIGN KEY(whitePlayer) REFERENCES clients(clientId), FOREIGN KEY(winner) REFERENCES clients(clientId))");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS moves (moveId INTEGER PRIMARY KEY AUTOINCREMENT, clientId int NOT NULL, gameId int NOT NULL, moveType int NOT NULL, x int, y int, moveNum int NOT NULL, FOREIGN KEY(clientId) REFERENCES clients(clientId), FOREIGN KEY(gameId) REFERENCES games(gameId))");
		stat.executeUpdate("CREATE TABLE IF NOT EXISTS captured_stones (stoneId INTEGER PRIMARY KEY AUTOINCREMENT, moveId int, x int, y int, FOREIGN KEY(moveId) REFERENCES moves(moveId))");
		
		//finish construct table, begin construct unfinished games
		ResultSet rs = stat.executeQuery("SELECT * FROM games;");
		while(rs.next()){
			int win = rs.getInt("winner");
			if (win==0){
				int bId=rs.getInt("blackPlayer");
				int wId=rs.getInt("whitePlayer");
				int bSize=rs.getInt("boardSize");
				ClientInfo bPlayer=new ClientInfo();
				ClientInfo wPlayer=new ClientInfo();
				//get black player name
				ResultSet forClient = stat.executeQuery("SELECT name FROM clients WHERE clientId=" + bId);
				while(forClient.next()){
					bPlayer.name.s=forClient.getString("clients");
					bPlayer.playerType=(byte)forClient.getInt("type");
				}
				//get white player name
				forClient = stat.executeQuery("SELECT name FROM clients WHERE clientId=" + wId);
				while(forClient.next()){
					wPlayer.name.s=forClient.getString("clients");
					wPlayer.playerType=(byte)forClient.getInt("type");
				}
				Board newBoard = new Board(bSize);
				int gameid = rs.getInt("gameId");
				String gameName = wPlayer.name.s+bPlayer.name.s;
				forcingPlayers.put(wPlayer.name.s, gameName);
				forcingPlayers.put(bPlayer.name.s, gameName);
				//construct a game object that is an unfinished game
				Game g = new Game(gameName, bPlayer, wPlayer, newBoard);
				g.setGameId(gameid);
				Location loc;
				lastMoveMadeBy.put(gameName, null);
				//get all the moves for this game
				forClient = stat.executeQuery("SELECT * FROM moves WHERE gameId=" + gameid + "ORDER BY moveNum");
				while(forClient.next()){
					//check move type in database
					if(forClient.getInt("moveType") == 0){  //move stone, need confirm by group
						int locX=forClient.getInt("x");
						int locY=forClient.getInt("y");
						loc=new Location(locX, locY);
						if(forClient.getInt("clientId")==bId){ // it is black player move
							g.makeMove(bPlayer.name.s, loc);
							lastMoveMadeBy.put(gameName, bPlayer.name.s);
						}
						else if(forClient.getInt("clientId")==wId){
							g.makeMove(wPlayer.name.s, loc);
							lastMoveMadeBy.put(gameName, wPlayer.name.s);
						}
						else{  //purely for debugging purpose
							System.out.println("something wrong with the clientId in moves make move, check it");
							System.exit(-1);
						}
					}
					else if(forClient.getInt("moveTpe")==1){
						if(forClient.getInt("clientId")==bId){ // it is black player move
							g.makePass(bPlayer.name.s);
							lastMoveMadeBy.put(gameName, bPlayer.name.s);
						}
						else if(forClient.getInt("clientId")==wId){
							g.makePass(wPlayer.name.s);
							lastMoveMadeBy.put(gameName, wPlayer.name.s);
						}
						else{  //purely for debugging purpose
							System.out.println("something wrong with the clientId in moves make pass, check it");
							System.exit(-1);
						}
					}
					else if(forClient.getInt("clientId")==2){//debugging purpose
						System.out.println("something wrong with our atomic commit, it didn't commit correctly when player forfeit check it");
						System.exit(-1);
					}
				}
				
				pendingGame.put(gameName, g);
			}
		} //finish reconstructing unfinished games
	}
	
	/*
	 * methods: 1. add game to game list
	 * 2. associate players observers(workers) for one game(used by pair check worker)
	 * 3. 
	 */
	//check if there are serverworker which has same randNum. also if there is
	//set their numToSend, set their sending type(c to s, or s to c)
	public void addWorkerForPairup(ServerWorker s){
		workerMapsLock.writeLock();
		if(workerMaps.containsKey(s.numFromClient)){
			ServerWorker part=workerMaps.get(s.numFromClient);
			part.partner=s;
			s.partner=part;
			//explicitly set s to be server to client, part to be client to server
			s.isSToC=true;
			part.isSToC=false;
			workerMaps.remove(s.numFromClient);
			workerMapsLock.writeUnlock();
			Random rand=new Random();
			int randNum=Math.abs(rand.nextInt());
			//set randNum to send for both workers
			part.setNumToSend(randNum);
			s.setNumToSend(randNum+3);
			s.waitForPartner.v();
			part.waitForPartner.v();

		}
		else{
			workerMaps.put(s.numFromClient, s);
			workerMapsLock.writeUnlock();
		}

	}
	
	
	//response for connect message
	public void responseToConnect(String player, ServerWorker sw){
		ServerWorker[] swList=new ServerWorker[2];
		swList[1]=sw; //since sw is c to s worker base on its call to this function
		swList[0]=sw.partner;
		//for debugging purpose
		if(sw.partner==null){
			System.out.println("partner is null");
		}
		clientsLock.writeLock();
		clients.put(player, swList);
		clientsLock.writeUnlock();
	}
	
	//response for disconnect message
	public void responseToDisconnect(String player){
		clientsLock.writeLock();
		RWLForConnections.writeLock();
		if(clients.containsKey(player)){
			clients.remove(player);
			connections=connections-2;
			System.out.println("call RTD");
		}
		clientsLock.writeUnlock();
		RWLForConnections.writeUnlock();
	}
	
	public void removeFromPendingPlayer(String s){
		pendingPlayerLock.writeLock();
		pendingPlayers.remove(s);
		pendingPlayerLock.writeUnlock();
	}
	
	public boolean containClient(String s){
		return clients.containsKey(s);
	}
	
	//response for disconnect before connect message
	public void disconnectBeforeConnect(){
		RWLForConnections.writeLock();
		connections=connections-2;
		System.out.println("call DBC");
		RWLForConnections.writeUnlock();
	}
	
	public void removeFromWait(String s){
		waitForGameLock.writeLock();
		waitForGamePlayers.remove(s);
		waitForGameLock.writeUnlock();
	}
	
	//response for waitforgame, return true if there is waiting player already, else false
	public boolean responseToWaitForGame(String player, ServerWorker sw){
		forcingLock.writeLock();
		if(forcingPlayers.containsKey(player)){
			String gn=forcingPlayers.get(player);
			forcingLock.writeUnlock();
			pendingPlayerLock.writeLock();
			//check if pendingPlayers contain the game name, which means its opponent is already waiting for game
			if(pendingPlayers.containsKey(gn)){
				pendingGameLock.writeLock();
				Game gg=pendingGame.get(gn);
				pendingGame.remove(gn); //remove the game from pending game list
				pendingGameLock.writeUnlock();
				String opponentName = pendingPlayers.get(gn);
				pendingPlayers.remove(gn);
				pendingPlayerLock.writeUnlock();
				//remove from forcingPlayers
				forcingLock.writeLock();
				forcingPlayers.remove(player);
				forcingPlayers.remove(opponentName);
				forcingLock.writeUnlock();
				
				sw.isInGame=true;
				sw.gameName=gn;
				sw.partner.isInGame=true;
				sw.partner.gameName=gn;
				String lastMove = lastMoveMadeBy.get(gn);
				ServerWorkerMessage getM = ServerWorkerMessage.make_getMove(gg);
				ServerWorker[] opSW=clients.get(opponentName);
				opSW[0].isInGame=true;
				opSW[1].isInGame=true;
				opSW[0].gameName=gn;
				opSW[1].gameName=gn;
				opSW[1].releasePendingSemaphore();
				if(lastMove==null){
					if(player.equals(gg.getBlackPlayerName())){
						sw.partner.q.add(getM);
					}
					else{
						opSW[0].q.add(getM);
					}
				}
				else if(lastMove.equals(player)){
					opSW[0].q.add(getM);
				}
				else{
					sw.partner.q.add(getM);
				}
				
			}
			else{ //remove player from pendingPlayer when player disconnect, remove the game from game list
				pendingPlayers.put(gn, player);
				pendingPlayerLock.writeUnlock();
				
				pendingGameLock.writeLock();
				Game gg=pendingGame.get(gn);
				pendingGameLock.writeUnlock();
				responseToGameStart(gn,gg); //add game to the game list
				sw.setPendingSemaphore();
			}
			return false;
			
		}
		else{
			forcingLock.writeUnlock();
			waitForGameLock.writeLock();
			if(waitForGamePlayers.isEmpty()){
				waitForGamePlayers.add(player);
				waitForGameLock.writeUnlock();
				sw.isWaitForGame=true;
				sw.partner.isWaitForGame=true;
				return false;
			}
			else{
				sw.oponentName=waitForGamePlayers.poll();
				waitForGameLock.writeUnlock();
				//set iswaitForGame to be false, inGame to be true for server workers
				sw.isWaitForGame=false;
				sw.isInGame=true;
				sw.partner.isWaitForGame=false;
				sw.isInGame=true;
				clientsLock.readLock();
				ServerWorker oponent=clients.get(sw.oponentName)[1];
				clientsLock.readUnlock();
				oponent.isWaitForGame=false;
				oponent.isInGame=true;
				oponent.partner.isWaitForGame=false;
				oponent.partner.isInGame=true;
				Random rand = new Random();
				Board initialBoard = new Board(10);
				String gn=sw.clientName+oponent.clientName;
				Game newGame = new Game(gn, new ClientInfo(oponent.clientName, oponent.playerType), new ClientInfo(sw.clientName, sw.playerType), initialBoard );
				//listOfGame.put(gn, newGame);
				int blackId=0, whiteId=0;
				try{
					ResultSet rs=stat.executeQuery("SELECT * FROM clients WHERE name=\'" + oponent.clientName +"\'");
					while(rs.next()){
						blackId=rs.getInt("clientId");
					}
					rs = stat.executeQuery("SELECT * FROM clients WHERE name=\'" + player +"\'");
					while(rs.next()){
						whiteId=rs.getInt("clientId");
					}
					stat.executeUpdate("INSERT INTO games (blackPlayer, whitePlayer, boardSize, blackScore, whiteScore, winner, moveNum, reason) VALUES (" + blackId + ", " + whiteId + ", " + 10 + ", NULL, NULL, NULL, 0, NULL)");
				}catch(SQLException e){
					System.out.println("sql exception caught in waitfor game while try to update games table");
				}
				sw.gameName=gn;
				sw.partner.gameName=gn;
				oponent.gameName=gn;
				oponent.partner.gameName=gn;
				sw.oponentName=oponent.clientName;
				sw.partner.oponentName=oponent.clientName;
				oponent.oponentName=sw.clientName;
				oponent.partner.oponentName=sw.clientName;
				//for debugging purpose
				if(sw.oponentName==null){
					System.out.println(sw.clientName+" name is null");
				}
				if(sw.partner.oponentName==null){
					System.out.println(sw.clientName+" name is null");
				}
				if(oponent.oponentName==null){
					System.out.println(oponent.clientName+" op name is null");
				}
				if(oponent.partner.oponentName==null){
					System.out.println(oponent.clientName+" op name is null");
				}
				responseToGameStart(gn,newGame);
				return true;
			}
		}
	}
	
	public Game getGame(String gn){
		return listOfGame.get(gn);
	}
	
	public ServerWorker[] getServerWorker(String n){
		return clients.get(n);
	}
	
	//response to gameStart
	public void responseToGameStart(String gameName, Game gameObj){
		gameLock.writeLock();
		listOfGame.put(gameName, gameObj);
		gameLock.writeUnlock();
	}
	//response to gameOver
	public void responseToGameOver(String gameName){
		gameLock.writeLock();
		listOfGame.remove(gameName);
		gameLock.writeUnlock();
	}
	
	//response to listGame
	public List<Writable> responseToListGame(){
		gameLock.readLock();
		Set<String> s=listOfGame.keySet();
		gameLock.readUnlock();
		Iterator<String> iter=s.iterator();
		List<Writable> toReturn= new LinkedList<Writable>();
		while(iter.hasNext()){
			GameInfo gi=new GameInfo();
			gi.name.s=iter.next();
			toReturn.add(gi);
		}
		return toReturn;
	}
	
	//response to join
	public Game responseToJoin(GameInfo gi){
		gameLock.readLock();
		Game s=listOfGame.get(gi.name.s);
		gameLock.readUnlock();
		return s;
	}
	
	public void addConnection(){
		RWLForConnections.writeLock();
		connections+=1;
		RWLForConnections.writeUnlock();
	}
	
	
	
	
	public static byte[] translate(String ip4address){
		byte[] blah = new byte[4];
		if(ip4address.equals("localhost")){
			blah=new byte[] {(byte)127,(byte)0,(byte)0,(byte)1};
		}
		int blah_index = 0;
		int start_index = 0;
		for(int i = 0; i < ip4address.length(); i++){
			if(ip4address.charAt(i) == '.'){
				blah[blah_index] = (byte) Integer.parseInt(ip4address.substring(start_index, i));
				blah_index++;
				start_index = i+1;
			}
			else if(i == ip4address.length() - 1){
				blah[blah_index] = (byte) Integer.parseInt(ip4address.substring(start_index));
			}
		}
		System.out.format("%d,%d,%d,%d", (int)blah[0],(int)blah[1],(int)blah[2],(int)blah[3]);
		return blah;
	}
	
	public static void main(String[] args){
		
		//byte[] ip_address = translate(args[0]);
    	int port = Integer.parseInt(args[1]);
    	GameServer gs= null;
    	try{
    		gs= new GameServer();
    	}catch (Exception e){
    		System.out.println("Exception caught when create game server, exit...");
    		System.exit(-1);
    	}
    	
    	try{
    		gs.ss=new ServerSocket(port);
    		//gs.ss=new ServerSocket(port, port, InetAddress.getByAddress(ip_address));
    	}catch(UnknownHostException e){System.out.println("UnkownHostException caught");
    	System.exit(-1);}
    	catch(IOException e){System.out.println("IOException caught");
    	System.exit(-1);}
    	System.out.println("\nsucceed create server socket");
    	gs.run();
    	//allocate new pair worker object and thread
    	
    	/*while(true){
    		try{
    			System.out.println("GameServer waiting for connection");
    			gs.s=gs.ss.accept();
    			System.out.println("a connection has been made");
    			//check if connection exceed 200
    			gs.RWLForConnections.readLock();
    			if (gs.connections>=200){
    				gs.RWLForConnections.readUnlock();
    				System.out.println("connection exceed 200+");
    				gs.s.close();
    			}
    			else{
        			//create worker object and thread for this socket
        			//run the thread
    				System.out.println("create worker object and thread");
    				gs.RWLForConnections.readUnlock();
    				ServerWorker sw=new ServerWorker(gs.s, gs);
    				gs.RWLForConnections.writeLock();
    				gs.connections++;
    				gs.RWLForConnections.writeUnlock();
    				Thread t=new Thread(sw);
    				t.start();
    				System.out.println("a thread started "+ gs.connections );

    			}
    		}catch(IOException e){
    			System.out.println("should not be here in game server.");
    		}
    		
    	}*/
		
	}
	
	public void run(){
		while(true){
    		try{
    			System.out.println("GameServer waiting for connection");
    			s=ss.accept();
    			System.out.println("a connection has been made");
    			//check if connection exceed 200
    			RWLForConnections.readLock();
    			if (connections>=200){
    				RWLForConnections.readUnlock();
    				System.out.println("connection exceed 200+");
    				s.close();
    			}
    			else{
        			//create worker object and thread for this socket
        			//run the thread
    				System.out.println("create worker object and thread");
    				RWLForConnections.readUnlock();
    				ServerWorker sw=null;
    				try{
    					sw=new ServerWorker(s, this);
    				}catch(Exception e){
    					System.out.println("exception caught when create server worker, exit...");
    					System.exit(-1);
    				}
    				
    				//RWLForConnections.writeLock();
    				//connections++;
    				//RWLForConnections.writeUnlock();
    				Thread t=new Thread(sw);
    				t.start();

    			}
    		}catch(IOException e){
    			System.out.println("should not be here in game server.");
    		}
    		
    	}
	}

}
