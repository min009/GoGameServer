package edu.berkeley.cs.cs162.Server;

import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.*;
import java.net.SocketTimeoutException;
import java.sql.*;

//import edu.berkeley.cs.cs162.common.*;
import edu.berkeley.cs.cs162.Synchronization.*;
//import edu.berkeley.cs.cs162.Server.*;
import edu.berkeley.cs.cs162.Writable.*;
import edu.berkeley.cs.cs162.Hash.*;


//remember to set scope of each variable approriately
//do necessary clean up when exceptions are caught
//!!!!!!!note, ready exit also need to close the socket
public class ServerWorker implements Runnable {
	
	protected Socket S_W_socket;
	public ThreadSafeQueue<ServerWorkerMessage> q;
	public int numFromClient;
	private boolean exit;
	protected int numToSend;
	GameServer gs;
	public ServerWorker partner;
	public boolean isSToC;
	public boolean connected;
	public byte playerType;
	public String clientName;
	public String oponentName;
	public String gameName;
	public boolean isWaitForGame;
	public boolean isInGame;
	public Semaphore waitForPartner;
	public ArrayList<String> gamesWatch;
	private Connection conn;
	private Semaphore forPendingGame;
	private Statement stat;
	private HashNSalt hashPass;
	
	
	
	public ServerWorker(Socket s, GameServer g) throws Exception{
		S_W_socket=s;
		exit=false;
		gs=g;
		connected=false;
		waitForPartner=new Semaphore(0);
		isWaitForGame=false;
		isInGame=false;
		gamesWatch = new ArrayList<String>();
		forPendingGame = new Semaphore (1);
		q = new ThreadSafeQueue<ServerWorkerMessage>(100);
		hashPass=new HashNSalt();
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:cs162-project3.db");
		stat = conn.createStatement();
		try{
			S_W_socket.setSoTimeout(3000);
		}catch(SocketException e){System.out.println("setsotimeout error, exit...");
		try{
			S_W_socket.close();
		}catch(IOException ex){System.out.println("error when close the socket");}
		}
	}
	
	public void setPendingSemaphore(){
		try{  //wait for one minute
		forPendingGame.p(60000);
		}catch (Exception e){
			//===================need to write to the database=======================
			Game g = gs.getGame(gameName);
			ClientInfo opo=g.getOponentInfo(clientName);
			ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, new ClientInfo(clientName, playerType), opo);
			LinkedList<String> obs=g.getListOfObservers();
			for(int i=0; i<obs.size(); i++){
				ServerWorker[] op=gs.getServerWorker(obs.get(i));
				if(op!=null){
					op[0].q.add(swm);
				}
			}
			gs.responseToGameOver(gameName);
		}
	}
	
	public void releasePendingSemaphore(){
		forPendingGame.v();
	}
	
	public void setNum(int i){
		numFromClient=i;
	}
	
	public void readyExit(){
		exit=true;
	}
	
	public void setPlayerType(byte type){
		playerType=type;
	}
	
	public void setNumToSend(int i){
		numToSend=i;
	}
	
	public void sendRanInt(){
		try{
			OutputStream out = S_W_socket.getOutputStream();
			DataOutputStream data_out = new DataOutputStream(out);
			data_out.writeInt( numToSend );
			data_out.writeInt( numFromClient+1);

		}catch(IOException e){System.out.println("error when try to sendRanInt");
		cleanUpFunc();
		}
		
	}
	
	//used for clean up
	public void cleanUpFunc(){
		if(isSToC){
			partner.readyExit();
			try{
				partner.S_W_socket.close();
			}catch(IOException ex){System.out.println("error when close the partner socket");}
			try{
				S_W_socket.close();
			}catch(IOException ex){System.out.println("error when close the socket");}
			}
		else{
			ServerWorkerMessage swm = ServerWorkerMessage.make_exit();
			partner.q.add(swm);
		}
	}
	
	
	public void exceptionInSToCPlayer(Game g){
		//same as forfeit
		gs.responseToDisconnect(clientName);
		//send game over
		ClientInfo opInfo=g.getOponentInfo(clientName);
		ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
		ServerWorker[] op=gs.getServerWorker(oponentName);
		if(op!=null){
			op[0].isInGame=false;
			op[1].isInGame=false;
			op[0].q.add(swm);
		}
		LinkedList<String> obs=g.getListOfObservers();
		for(int i=0; i<obs.size(); i++){
			op=gs.getServerWorker(obs.get(i));
			if(op!=null){
				op[0].q.add(swm);
			}
		}
		gs.responseToGameOver(gameName);
	}
	
	public void exceptionInSToCObserver(){
		for(String gms:gamesWatch){
			Game gm=gs.getGame(gms);
			if (gm!=null){
				gm.removeObserver(clientName);
			}
		}
	}
	
	
	

	
	public void run(){
		try{
			InputStream in = S_W_socket.getInputStream();
			DataInputStream data_in = new DataInputStream(in);
	    	int received_int = data_in.readInt();
	    	setNum(received_int);
			
		}catch(SocketTimeoutException e){
			System.out.println("Timeout when try to get first rand from client!");
			try{
				S_W_socket.close();
			}catch(IOException ex){System.out.println("error when close the socket");}
		}
		catch(IOException e){
			System.out.println("IOException");
			try{
				S_W_socket.close();
			}catch(IOException ex){System.out.println("error when close the socket");}
		}
		//add worker to workerMaps
		gs.addWorkerForPairup(this);
		//here, we use this for this thread to wait for its partner thread, if the partner died before this step, this thread will also close socket and exit
		try{
			waitForPartner.p(60000);
			System.out.println("partner is ready");
		}catch(TimeoutException e){System.out.println("Timeout while waiting for partner. 60s.");
		try{
			S_W_socket.close();
		}catch(IOException ex){System.out.println("error when close the socket");}
		}
		
		//send the random number to the client
		sendRanInt();
		
		//check received number
		try{
			InputStream in = S_W_socket.getInputStream();
			DataInputStream data_in = new DataInputStream(in);
	    	int received_int = data_in.readInt();
	    	if (received_int!=(numToSend+1)){
	    		System.out.println("wrong ack received during last session of 3way handshaking, terminating...");
	    		//do necessary clean up and kill its peer worker thread
	    		cleanUpFunc();
	    	}
		}catch(SocketTimeoutException e){
			System.out.println("Timeout when get rand+1 from client!");
			cleanUpFunc();
		}
		catch(IOException e){
			System.out.println("IOException in three way handshake");
			cleanUpFunc();
		}
		//3 way handshake done
		gs.addConnection();
		
		//set the client to server socket to have a read() timeout of infinity
		if (!isSToC){
			try{
				
				S_W_socket.setSoTimeout(0); //set timeout to be infinite, waiting for connect message
			}catch(SocketException e){System.out.println("setsotimeout error before while.");
			cleanUpFunc();
			}
		}
		//===================================================================================
		//============================No Change before here==================================
		//===================================================================================
		
		if(isSToC){  //server to client worker
			try{
				OutputStream out = S_W_socket.getOutputStream();
				InputStream in = S_W_socket.getInputStream();
				while(!exit){
					ServerWorkerMessage m = q.get();
					
					//game start message
					if( m.is_gameStart() ){
						Game g = m.get_game();
						//*make message and send to client*
						ClientMessage msg = new ClientMessage();
						msg.setMsgType(MessageProtocol.OP_TYPE_GAMESTART);
						msg.game = new GameInfo(g.getName());
						msg.board = new BoardInfo(g.getBoard());

						msg.blackPlayer = new ClientInfo(g.getBlackPlayerName(), gs.getServerWorker(g.getBlackPlayerName())[0].playerType);
						msg.whitePlayer = new ClientInfo(g.getWhitePlayerName(), gs.getServerWorker(g.getWhitePlayerName())[0].playerType);
						
						msg.writeTo(out);

						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_GAMESTART);
						try{
							r.readFrom(in);
						}catch(SocketTimeoutException e){
							if(playerType!=MessageProtocol.TYPE_OBSERVER){
								exceptionInSToCPlayer(g);
							}
							else{
								exceptionInSToCObserver();
							}
							cleanUpFunc();
						}
						
						//is this part needed? client has no choice but to reply with status_ok!
						if(r.replyOpCode != MessageProtocol.OP_STATUS_OK ){
							if(playerType!=MessageProtocol.TYPE_OBSERVER){
								exceptionInSToCPlayer(g);
							}
							else{
								exceptionInSToCObserver();
							}
							cleanUpFunc();
						}
					}
					// game over message
					else if( m.is_gameOver() ){
						Game g = m.get_game();
						if(playerType != MessageProtocol.TYPE_OBSERVER){
							isInGame = false;
							partner.isInGame=false;
						}
						else{
							gamesWatch.remove(g.getName());
						}
						
						ClientMessage msg = new ClientMessage();
						msg.setMsgType(MessageProtocol.OP_TYPE_GAMEOVER);
						msg.game = new GameInfo(g.getName());
						msg.blackScore = g.getScore(g.getBlackPlayerName());
						msg.whiteScore = g.getScore(g.getWhitePlayerName());
						String winner = g.theWinner();
						
						//msg.winner_or_player = new ClientInfo(winner, g.getWinerType());
						msg.reason_or_moveType_or_errorNum = m.getError();
						//set extra clientInfo and human readable string before writeTo
						if(m.getError()==MessageProtocol.PLAYER_FORFEIT){
							msg.winner_or_player=m.getWinInfo();
							msg.player_for_gameover=m.getLosInfo();
							msg.errorMsg=new StringInfo();
							msg.errorMsg.s="Move Forfeit error";
						}
						else if (m.getError()==MessageProtocol.PLAYER_INVALID_MOVE){
							msg.winner_or_player = new ClientInfo(winner, g.getWinerType());
							msg.player_for_gameover=new ClientInfo(g.getLoser(), g.getLoserType());
							msg.errorMsg=new StringInfo();
							msg.errorMsg.s="Invalid move error";
						}
						else if (m.getError()==MessageProtocol.PLAYER_KO_RULE){
							msg.player_for_gameover=new ClientInfo(g.getLoser(), g.getLoserType());
							msg.errorMsg=new StringInfo();
							msg.errorMsg.s="KO Rule violated";
						}
						else{
							msg.winner_or_player = new ClientInfo(winner, g.getWinerType());
							msg.player_for_gameover=new ClientInfo(g.getLoser(), g.getLoserType());
						}
						msg.writeTo(out);

						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_GAMEOVER);
						try{
							r.readFrom(in);
						}catch(SocketTimeoutException e){
							//do clean up because of time out
							gs.responseToDisconnect(clientName);
							cleanUpFunc();
						}
						
						if(r.replyOpCode != MessageProtocol.OP_STATUS_OK ){
							//do clean up because of unknown corrupted message
							gs.responseToDisconnect(clientName);
							cleanUpFunc();
						}

					}
					//make move message
					else if( m.is_makeMove() ){
						Game g = m.get_game();
						
						ClientMessage msg = new ClientMessage();
						msg.setMsgType(MessageProtocol.OP_TYPE_MAKEMOVE);
						msg.game = new GameInfo(g.getName());
						msg.winner_or_player = new ClientInfo(m.getName(), m.getPlayerType());
						msg.reason_or_moveType_or_errorNum = m.getMoveType();
						msg.loc = m.getLoc();
						Vector<Location> v = m.getCaptured();
						List<Writable> lis = new LinkedList<Writable>();
						if(v!=null){
						for(int i = 0; i < v.size(); i++){
							lis.add(v.get(i));
						}
						}
						ListInfo li = new ListInfo();
						li.l = lis;
						msg.captured_stones = li;
						msg.writeTo(out);
						
						//try to get reply
						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_MAKEMOVE);
						//boolean beenInTry=false;
						try{
							r.readFrom(in);
						}catch(SocketTimeoutException e){
							if(playerType!=MessageProtocol.TYPE_OBSERVER){
								exceptionInSToCPlayer(g);
							}
							else{
								exceptionInSToCObserver();
							}
							cleanUpFunc();
						}
						
						//is this needed? client must reply with status_ok
						if(r.replyOpCode != MessageProtocol.OP_STATUS_OK ){
							if(playerType!=MessageProtocol.TYPE_OBSERVER){
								exceptionInSToCPlayer(g);
							}
							else{
								exceptionInSToCObserver();
							}
							cleanUpFunc();
						}
					}//end if( m.is_makeMove() )
					else if( m.is_getMove() ){
						ClientMessage msg = new ClientMessage();
						msg.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
						msg.writeTo(out);

						ServerReply sr = new ServerReply();
						sr.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
						if(playerType == MessageProtocol.TYPE_HUMAN){  //if it is human set read()timeout to be 30s
							S_W_socket.setSoTimeout(30000);
						}
						else{   //if it is machine set read() timeout to be 2s
							S_W_socket.setSoTimeout(2000);
						}

						try{
							sr.readFrom(in);
							if(sr.moveType == MessageProtocol.MOVE_STONE){
								//need to update move table, capture stone table if any stone been captured, and games table if the game ends in one sigle transaction
								
								if(gs.getGame(gameName)!=null){
								Game g = gs.getGame(gameName);
								
								byte reason = g.makeMove(this.clientName, sr.loc);
								if(reason == MessageProtocol.GAME_OK){
									Vector<Location> v=g.capturedStones();
									ServerWorkerMessage swm = ServerWorkerMessage.make_makeMove(g, clientName, playerType, sr.moveType, sr.loc, v);
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										ServerWorker[] op=gs.getServerWorker(obs.get(i));
										if(op!=null){
											op[0].q.add(swm);
										}
									}
									ServerWorkerMessage getM = ServerWorkerMessage.make_getMove(g);
									System.out.println(oponentName);
									gs.getServerWorker(oponentName)[0].q.add(getM);

								}
								else if(reason == MessageProtocol.PLAYER_KO_RULE){
									ClientInfo opInfo=g.getOponentInfo(clientName);
									ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_KO_RULE, opInfo, new ClientInfo(clientName, playerType));
									isInGame=false;
									partner.isInGame=false;
									ServerWorker[] op=gs.getServerWorker(oponentName);
									if(op!=null){
										op[0].isInGame=false;
										op[1].isInGame=false;
									}
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										ServerWorker[] opp=gs.getServerWorker(obs.get(i));
										if(opp!=null){
											opp[0].q.add(swm);
										}
									}
									gs.responseToGameOver(gameName);

								}
								else if(reason == MessageProtocol.PLAYER_INVALID_MOVE){
									ClientInfo opInfo=g.getOponentInfo(clientName);
									ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_INVALID_MOVE, opInfo, new ClientInfo(clientName, playerType));
									isInGame=false;
									partner.isInGame=false;
									ServerWorker[] op=gs.getServerWorker(oponentName);
									if(op!=null){
										op[0].isInGame=false;
										op[1].isInGame=false;
									}
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										ServerWorker[] opp=gs.getServerWorker(obs.get(i));
										if(opp!=null){
											opp[0].q.add(swm);
										}
									}
									gs.responseToGameOver(gameName);
								}
								else{ //corrupt message type, clean state, close socket
									gs.responseToDisconnect(clientName);
									ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, new ClientInfo(partner.clientName, partner.playerType), new ClientInfo(clientName, playerType));
									isInGame=false;
									partner.isInGame=false;
									ServerWorker[] op=gs.getServerWorker(oponentName);
									if(op!=null){
										op[0].isInGame=false;
										op[1].isInGame=false;
									}
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										ServerWorker[] opp=gs.getServerWorker(obs.get(i));
										if(opp!=null){
											opp[0].q.add(swm);
										}
									}
									gs.responseToGameOver(gameName);
									cleanUpFunc();
								}

							}}
							else if(sr.moveType == MessageProtocol.MOVE_PASS){
								//here need to update the move table with move type as pass, need to bundle with game table update if there are two passes
								if(gs.getGame(gameName)!=null){
								Game g = gs.getGame(gameName);
								boolean result = g.makePass(this.clientName);
								if(result){
									ServerWorkerMessage swm = ServerWorkerMessage.make_makeMove(g, clientName, playerType, MessageProtocol.MOVE_PASS, null, null);
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										ServerWorker[] opp=gs.getServerWorker(obs.get(i));
										if(opp!=null){
											opp[0].q.add(swm);
										}
									}
									ServerWorkerMessage getM = ServerWorkerMessage.make_getMove(g);
									gs.getServerWorker(oponentName)[0].q.add(getM);
								}
								else{
									ServerWorkerMessage swm = ServerWorkerMessage.make_makeMove(g, clientName, playerType, MessageProtocol.MOVE_PASS, null, null);
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										ServerWorker[] opp=gs.getServerWorker(obs.get(i));
										if(opp!=null){
											opp[0].q.add(swm);
										}
									}
									//after sending make move, send game over
									ClientInfo opInfo=g.getOponentInfo(clientName);
									ServerWorkerMessage swmGO = ServerWorkerMessage.make_gameOver(g, MessageProtocol.GAME_OK, opInfo, new ClientInfo(clientName, playerType));
									isInGame=false;
									partner.isInGame=false;
									ServerWorker[] op=gs.getServerWorker(oponentName);
									if(op!=null){
										op[0].isInGame=false;
										op[1].isInGame=false;
									}
									gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swmGO);
									gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swmGO);
									LinkedList<String> obs2=g.getListOfObservers();
									for(int i=0; i<obs2.size(); i++){
										ServerWorker[] opp=gs.getServerWorker(obs.get(i));
										if(opp!=null){
											opp[0].q.add(swmGO);
										}
									}
									gs.responseToGameOver(gameName);
								}
							}}
							else{ //if forfeit
								//need to update move table and game table in one single transaction
								if(gs.getGame(gameName)!=null){
								Game g = gs.getGame(gameName);
								ClientInfo opInfo=g.getOponentInfo(clientName);
								ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
								isInGame=false;
								partner.isInGame=false;
								ServerWorker[] op=gs.getServerWorker(oponentName);
								if(op!=null){
									op[0].isInGame=false;
									op[1].isInGame=false;
								}
								gs.getServerWorker(g.getBlackPlayerName())[0].q.add(swm);
								gs.getServerWorker(g.getWhitePlayerName())[0].q.add(swm);
								LinkedList<String> obs = g.getListOfObservers();
								for(int i=0; i<obs.size(); i++){
									ServerWorker[] opp=gs.getServerWorker(obs.get(i));
									if(opp!=null){
										opp[0].q.add(swm);
									}
								}
								gs.responseToGameOver(gameName);
							}}
						//}

							S_W_socket.setSoTimeout(3000);   //set back socket timeout to 3s
						}catch(SocketTimeoutException e){
							//Timeout: clean up state and close socket, then make game over message, then exit
							//same as forfeit,
							gs.responseToDisconnect(clientName);
							if(gs.getGame(gameName)!=null){
							Game g = gs.getGame(gameName);
							ClientInfo opInfo=g.getOponentInfo(clientName);
							ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
							ServerWorker[] op=gs.getServerWorker(oponentName);
							if(op!=null){
								op[0].isInGame=false;
								op[1].isInGame=false;
								op[0].q.add(swm);
							}
							if(g!=null){
								LinkedList<String> obs=g.getListOfObservers();
								for(int i=0; i<obs.size(); i++){
									op=gs.getServerWorker(obs.get(i));
									if(op!=null){
										op[0].q.add(swm);
									}
								}
							}
							gs.responseToGameOver(gameName);
							cleanUpFunc();
						}}
					}


					else //if exit
					{
						exit = true;
					}
				}//end while




			}catch(IOException e){
				//same as forfeit
				gs.responseToDisconnect(clientName);
				if(isInGame && playerType!=MessageProtocol.TYPE_OBSERVER ){
					//send game over
					if(gs.getGame(gameName)!=null){
					Game g = gs.getGame(gameName);
					ClientInfo opInfo=g.getOponentInfo(clientName);
					ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
					ServerWorker[] op=gs.getServerWorker(oponentName);
					if(op!=null){  //double check if its oponent also timeout or sends an invalid message
						op[0].isInGame=false;
						op[1].isInGame=false;
						op[0].q.add(swm);
					}
					if(g!=null){
						LinkedList<String> obs=g.getListOfObservers();
						for(int i=0; i<obs.size(); i++){
							op=gs.getServerWorker(obs.get(i));
							if(op!=null){
								op[0].q.add(swm);
							}
						}
					}
					gs.responseToGameOver(gameName);
				}}
				else if (isWaitForGame && playerType!=MessageProtocol.TYPE_OBSERVER){
					gs.removeFromWait(clientName);
				}
				else{
					for(String gms:gamesWatch){
						gs.getGame(gms).removeObserver(clientName);
					}
				}
				cleanUpFunc();
			}
		}
		//================checked on 11/29========================
		
		else{  //client to server worker
				while(!exit){
					try{
						InputStream in =S_W_socket.getInputStream();
						ServerMessage smsg=new ServerMessage();
						smsg.readFrom(in);
						byte mType=smsg.getMsgType();
						if(mType == MessageProtocol.OP_TYPE_CONNECT){
							OutputStream out=S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_CONNECT);
							
							if(!connected){
								ClientInfo pInfo=smsg.player;
								StringInfo  password=smsg.passwordHash;
								//need to check whether the client name already exist in server's clients variable
								if(gs.containClient(pInfo.name.s)){
									cr.replyOpCode=MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);
								}
								else{
									//check password
									try{
										ResultSet rs = stat.executeQuery("SELECT * FROM clients WHERE name=\'" + pInfo.name.s + "\'");
										String pw=null;
										while(rs.next()){
											pw=rs.getString("passwordHash");
										}
										if(pw==null){
											cr.replyOpCode=MessageProtocol.OP_ERROR_BAD_AUTH;
											cr.writeTo(out);
										}
										else if(pw.equals(hashPass.getHashNSalt(password.s))){
											cr.replyOpCode=MessageProtocol.OP_STATUS_OK;
											cr.writeTo(out);
											
											connected = true;
											partner.connected = true;
											playerType = smsg.player.playerType;
											partner.playerType = smsg.player.playerType;
											System.out.println("p type "+playerType);
											clientName = smsg.player.name.s;
											partner.clientName = smsg.player.name.s;
											gs.responseToConnect(clientName, this);
										}
										else{
											cr.replyOpCode=MessageProtocol.OP_ERROR_BAD_AUTH;
											cr.writeTo(out);
										}
									}catch(SQLException e){
										System.out.println("sql exception caught in connect clause");
									}catch(Exception ex){
										System.out.println("or maybe hashsalt exception in connect clause");
									}
								}
								
								/*
								cr.replyOpCode = MessageProtocol.OP_STATUS_OK;
								cr.writeTo(out);

								connected = true;
								partner.connected = true;
								playerType = smsg.player.playerType;
								partner.playerType = smsg.player.playerType;
								System.out.println("p type "+playerType);
								clientName = smsg.player.name.s;
								partner.clientName = smsg.player.name.s;
								gs.responseToConnect(clientName, this);
								*/
							}
							else{
								cr.replyOpCode = MessageProtocol.OP_ERROR_REJECTED;
								cr.writeTo(out);
								
							}
						}
						//register message
						else if(mType == MessageProtocol.OP_TYPE_REGISTER){
							//check if the client is already registered
							System.out.println("register been called by " + clientName);
							ClientInfo pInfo=smsg.player;
							StringInfo password=smsg.passwordHash;
							OutputStream out=S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_REGISTER);
							try{
								ResultSet rs=stat.executeQuery("SELECT name FROM clients WHERE name=\'" + pInfo.name.s + "\'");
								System.out.println("no problem here");
								if(rs.next()){  //the client is already registered
									cr.replyOpCode=MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);
								}
								else{
									stat.executeUpdate("INSERT INTO clients (name, type, passwordHash) VALUES (\'" + pInfo.name.s + "\', " + pInfo.playerType + ", \'" + hashPass.getHashNSalt(password.s) + "\')");
									cr.replyOpCode=MessageProtocol.OP_STATUS_OK;
									cr.writeTo(out);
								}
							}catch(SQLException e){
								System.out.println("sql exception caught in register clause");
							}catch(Exception ex){
								System.out.println("or maybe hashsalt exception in register clause");
							}
								
						}
						
						else if (mType==MessageProtocol.OP_TYPE_CHANGEPW){
							OutputStream out=S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_CHANGEPW);
							if(!connected){
								cr.replyOpCode = MessageProtocol.OP_ERROR_UNCONNECTED;
								cr.writeTo(out);
								gs.disconnectBeforeConnect();
								exit=true;
								//cleanup
								cleanUpFunc();
							}
							else{  //connected
								//check if the clientInfo we receive equal to the clientInfo we have
								ClientInfo pInfo=smsg.player;
								StringInfo password=smsg.passwordHash;
								if(pInfo.name.s.equals(clientName)){
									System.out.println("change password been called and it is will update pass word");
									try{
										stat.executeUpdate("UPDATE clients SET passwordHash=\'" + hashPass.getHashNSalt(password.s) + "\' WHERE name=\'" + pInfo.name.s + "\'");
									}catch(SQLException e){
										System.out.println("sql exception caught in change password clause");
									}catch(Exception ex){
										System.out.println("or maybe hashsalt exception in change password clause");
									}
									cr.replyOpCode=MessageProtocol.OP_STATUS_OK;
									cr.writeTo(out);
									
								}
								else{ //attempt to change other people's password
									cr.replyOpCode=MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);
								}
								
							}
						}
						
						else if(mType == MessageProtocol.OP_TYPE_DISCONNECT){ //special handle, don't need to check connected
							//like clean up, also call gs's response to disconnect, write a remove method in game
							exit=true;
							if(!connected){
								gs.disconnectBeforeConnect();
								exit=true;
								//cleanup
								cleanUpFunc();
							}
							else{
								//if is a player and is in game, send game over with error code forfeit
								if(isInGame && playerType!=MessageProtocol.TYPE_OBSERVER){
									//same as forfeit
									gs.responseToDisconnect(clientName);
									Game g = gs.getGame(gameName);
									ClientInfo opInfo=g.getOponentInfo(clientName);
									ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
									ServerWorker[] op=gs.getServerWorker(oponentName);
									if(op!=null){
										op[0].isInGame=false;
										op[1].isInGame=false;
										op[0].q.add(swm);
									}
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										op=gs.getServerWorker(obs.get(i));
										if(op!=null){
											op[0].q.add(swm);
										}
									}
									gs.responseToGameOver(gameName);
								}
								else if(isWaitForGame && playerType!=MessageProtocol.TYPE_OBSERVER){
									gs.removeFromWait(clientName);
									gs.responseToDisconnect(clientName);
								}
								else if(playerType!=MessageProtocol.TYPE_OBSERVER){
									gs.responseToDisconnect(clientName);
									//added for p3
									gs.removeFromPendingPlayer(clientName);
								}
								else{
									gs.responseToDisconnect(clientName);
									for(String gms:gamesWatch){
										Game gm=gs.getGame(gms);
										if (gm!=null){
											gm.removeObserver(clientName);
										}
									}
								}
								cleanUpFunc();	
							}

						}
						
						else if(mType == MessageProtocol.OP_TYPE_WAITFORGAME){
							OutputStream out = S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_WAITFORGAME);
							
							if(!connected){
								cr.replyOpCode = MessageProtocol.OP_ERROR_UNCONNECTED;
								cr.writeTo(out);
								gs.disconnectBeforeConnect();
								exit=true;
								//cleanup
								cleanUpFunc();
							}
							else{
								if(playerType == MessageProtocol.TYPE_OBSERVER){  //observer
									cr.replyOpCode = MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);
								}
								else{ //player
									if(isWaitForGame || isInGame){
										cr.replyOpCode = MessageProtocol.OP_ERROR_REJECTED;
										cr.writeTo(out);
									}
									else{
										cr.replyOpCode = MessageProtocol.OP_STATUS_OK;
										cr.writeTo(out);
										boolean started=gs.responseToWaitForGame(clientName, this);
										if(started){  //if there is another player waitforgame, put message game starts on s to c queue
											Game game_obj = gs.getGame(gameName);
											ServerWorkerMessage swm = ServerWorkerMessage.make_gameStart(game_obj);
											gs.getServerWorker(game_obj.getBlackPlayerName())[0].q.add(swm);
											gs.getServerWorker(game_obj.getWhitePlayerName())[0].q.add(swm);
											LinkedList<String> obs=game_obj.getListOfObservers();
											for(int i=0; i<obs.size(); i++){
												ServerWorker[] opp=gs.getServerWorker(obs.get(i));
												if(opp!=null){
													opp[0].q.add(swm);
												}
											}
											ServerWorkerMessage getM = ServerWorkerMessage.make_getMove(game_obj);
											gs.getServerWorker(game_obj.getBlackPlayerName())[0].q.add(getM);
										}
									}
								}
							}
						}
						
						else if(mType == MessageProtocol.OP_TYPE_LISTGAMES){
							OutputStream out=S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_LISTGAMES);
							
							if(!connected){
								cr.replyOpCode = MessageProtocol.OP_ERROR_UNCONNECTED;
								cr.writeTo(out);
								gs.disconnectBeforeConnect();
								exit=true;
								//cleanup
								cleanUpFunc();
							}
							else{ 
								if(playerType == MessageProtocol.TYPE_OBSERVER){
									cr.replyOpCode = MessageProtocol.OP_STATUS_OK;
									ListInfo li = new ListInfo();
									li.l=gs.responseToListGame();
									cr.game_list=li;
									cr.writeTo(out);
								}
								else{
									cr.replyOpCode = MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);
								}
							}
						}
						
						else if(mType == MessageProtocol.OP_TYPE_JOIN){
							OutputStream out = S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_JOIN);
							
							if(!connected){
								cr.replyOpCode = MessageProtocol.OP_ERROR_UNCONNECTED;
								cr.writeTo(out);
								gs.disconnectBeforeConnect();
								exit=true;
								//cleanup
								cleanUpFunc();
							}
							else{
								if(playerType == MessageProtocol.TYPE_OBSERVER){
									GameInfo gi=smsg.game;
									Game returnedGame=gs.responseToJoin(gi);
									if(returnedGame!=null){  
										cr.replyOpCode = MessageProtocol.OP_STATUS_OK;
										cr.board = new BoardInfo(returnedGame.getBoard());
										cr.whitePlayer = new ClientInfo();
										cr.whitePlayer.name.s = returnedGame.getWhitePlayerName();
										cr.blackPlayer = new ClientInfo();
										cr.blackPlayer.name.s = returnedGame.getBlackPlayerName();
										cr.writeTo(out);
										if(!returnedGame.containOb(clientName)){
											returnedGame.addObserver(clientName);
											gamesWatch.add(gi.name.s); //add to game watch list
										}
									}
									else{
										cr.replyOpCode = MessageProtocol.OP_ERROR_INVALID_GAME;
										cr.writeTo(out);
									}
								}
								else{
									cr.replyOpCode = MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);
								}

							}
						}
						
						else if(mType == MessageProtocol.OP_TYPE_LEAVE){
							GameInfo gi=smsg.game;
							Game returnedGame=gs.responseToJoin(gi);
							OutputStream out = S_W_socket.getOutputStream();
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_LEAVE);
							
							if(!connected){
								cr.replyOpCode = MessageProtocol.OP_ERROR_UNCONNECTED;
								cr.writeTo(out);
								gs.disconnectBeforeConnect();
								exit=true;
								//cleanup
								cleanUpFunc();
							}
							else{
								if(playerType == MessageProtocol.TYPE_OBSERVER && returnedGame!=null){
									if(gamesWatch.contains(smsg.game.name.s)){
										cr.replyOpCode = MessageProtocol.OP_STATUS_OK;
										cr.writeTo(out);
										gamesWatch.remove(smsg.game.name.s); //remove from game watch
										if(returnedGame!=null){
											returnedGame.removeObserver(clientName);
										}
									}
									else{
										cr.replyOpCode = MessageProtocol.OP_ERROR_INVALID_GAME;
										cr.writeTo(out);

									}
								}
								else{
									cr.replyOpCode = MessageProtocol.OP_ERROR_REJECTED;
									cr.writeTo(out);

								}
							}

						}
						else{
							//do clean up because receive invailid message opcode
							exit=true;
							if(!connected){
								//System.out.println("call DBC in catch exception");

								gs.disconnectBeforeConnect();
							}
							else{
								gs.responseToDisconnect(clientName);
								if(playerType!=MessageProtocol.TYPE_OBSERVER){
									gs.removeFromPendingPlayer(clientName);
									//send game over
									if(isInGame){
										//same as forfeit
										Game g = gs.getGame(gameName);
										if(g!=null){
											ClientInfo opInfo=g.getOponentInfo(clientName);
											ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
											ServerWorker[] op=gs.getServerWorker(oponentName);
											if(op!=null){
												op[0].isInGame=false;
												op[1].isInGame=false;
												op[0].q.add(swm);
											}
											if(g!=null){
												LinkedList<String> obs=g.getListOfObservers();
												for(int i=0; i<obs.size(); i++){
													op=gs.getServerWorker(obs.get(i));
													if(op!=null){
														op[0].q.add(swm);
													}
												}
											}
										}
										gs.responseToGameOver(gameName);
									}
									else{
										if(isWaitForGame){
											gs.removeFromWait(clientName);
										}
									}
								}
								else{
									for(String gms:gamesWatch){
										Game gm=gs.getGame(gms);
										if (gm!=null){
											gm.removeObserver(clientName);
										}
									}
								}
								cleanUpFunc();
							}
						}
					}catch(IOException e){
						exit=true;
						if(!connected){
							//System.out.println("call DBC in catch exception");
							
							gs.disconnectBeforeConnect();
						}
						else{
							gs.responseToDisconnect(clientName);
							if(playerType!=MessageProtocol.TYPE_OBSERVER){
								gs.removeFromPendingPlayer(clientName);
								//send game over
								if(isInGame){
								//same as forfeit
								Game g = gs.getGame(gameName);
								if(g!=null){
									ClientInfo opInfo=g.getOponentInfo(clientName);
									ServerWorkerMessage swm = ServerWorkerMessage.make_gameOver(g, MessageProtocol.PLAYER_FORFEIT, opInfo, new ClientInfo(clientName, playerType));
									ServerWorker[] op=gs.getServerWorker(oponentName);
								if(op!=null){
									op[0].isInGame=false;
									op[1].isInGame=false;
									op[0].q.add(swm);
								}
								if(g!=null){
									LinkedList<String> obs=g.getListOfObservers();
									for(int i=0; i<obs.size(); i++){
										op=gs.getServerWorker(obs.get(i));
										if(op!=null){
											op[0].q.add(swm);
										}
									}
								}
								}
								gs.responseToGameOver(gameName);
								}
								else{
									if(isWaitForGame){
										gs.removeFromWait(clientName);
									}
								}
							}
							else{
								for(String gms:gamesWatch){
									Game gm=gs.getGame(gms);
									if (gm!=null){
										gm.removeObserver(clientName);
									}
								}
							}
						}
						cleanUpFunc();
					}

				}
			
		
		}
		
		//last clean up here
		try{
			S_W_socket.close();
		}catch(IOException e){
			System.out.println("last exception been caught");
		}
		
		if(isSToC){
			System.out.println("server to client die");
		}
		else{
			System.out.println("client to server die");
		}
			
		
	}

}
