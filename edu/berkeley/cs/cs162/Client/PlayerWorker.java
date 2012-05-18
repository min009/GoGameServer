package edu.berkeley.cs.cs162.Client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import edu.berkeley.cs.cs162.Synchronization.ReaderWriterLock;
import edu.berkeley.cs.cs162.Synchronization.Semaphore;
import edu.berkeley.cs.cs162.Synchronization.ThreadSafeQueue;
//import edu.berkeley.cs.cs162.Synchronization.ThreadSafeQueue;
//import edu.berkeley.cs.cs162.common.OworkerMessage;
import edu.berkeley.cs.cs162.Writable.ClientMessage;
import edu.berkeley.cs.cs162.Writable.ClientReply;
import edu.berkeley.cs.cs162.Writable.MessageProtocol;
import edu.berkeley.cs.cs162.Writable.ServerMessage;
import edu.berkeley.cs.cs162.Writable.ServerReply;
import edu.berkeley.cs.cs162.Writable.Location;
import edu.berkeley.cs.cs162.Writable.StringInfo;
import edu.berkeley.cs.cs162.common.StoneColor;

public class PlayerWorker implements Runnable{
	//only for to server thread
	protected ThreadSafeQueue< PlayerWorkerMessage > q;
	//public LinkedList<PlayerWorkerMessage> q;
	protected ReaderWriterLock qLock;
	protected int myid;
	
	private Socket s;
	public int numReceived;
	public int initialRanNum;
	public boolean sToc = false;
	
	//starts at 0, call se.V() when completed 3way handshake
	protected Semaphore twh_semaphore;
	//starts at 0, V() called twice when done assigning roles
	protected Semaphore magical_semaphore;
	boolean exit;
	
	protected PlayerWorker partner;
	protected Player player_obj; 
	
	public int debug = 1;
	
	public PlayerWorker(InetAddress ipaddress, int port, int randNum, Semaphore s_para, Semaphore ms, Player po, int id){
		numReceived = -99999;
		initialRanNum = randNum;
		try{
			s = new Socket(ipaddress, port);
			s.setSoTimeout(3000);
		}catch (IOException e){}
		catch (Exception e){}
		exit = false;
		twh_semaphore = s_para;
		magical_semaphore = ms;
		player_obj = po;
		this.myid = id;
		q = new ThreadSafeQueue<PlayerWorkerMessage>(100);
	}
	
	public void addToQueue(PlayerWorkerMessage m){
		//qLock.writeLock();
		q.add(m);
		//qLock.writeUnlock();
	}
	
	public void sendRand(int num){
		//serialize the random number and send it
		try{
			OutputStream out = s.getOutputStream();
			DataOutputStream data_out = new DataOutputStream(out);
	    	data_out.writeInt( num );
			
		}catch(IOException e){
			System.out.println("sendRand timeout error!");
			System.exit(-1);
		}
	}
	
	public void setPartner(PlayerWorker w){
		partner = w;
	}
	//needed for printingobserver to access its received random number
	public int getReceivedRan(){
		return numReceived;
	}
	
	public void readyExit(){
		exit = true;
	}
	
	public void setReceived(int i){
		numReceived = i;
	}
	
	//3 way handshake last step: send back the random number plus one
	public void sendReceivedPlusOne(){
		try{
			OutputStream out = s.getOutputStream();
			DataOutputStream data_out = new DataOutputStream(out);
	    	data_out.writeInt( numReceived+1 );
			
		}
		catch(IOException e){
			System.out.println("sendRand timeout error!");
			System.exit(-1);
		}
	}
	
	private void handlePlayerInput(OutputStream out){
		System.out.println("Please enter M to make a move, enter F to forfeit the game, OR anything else to make a pass:");
		//System.out.println("Board for " + player_obj.name);
		//System.out.println("Board size: " + player_obj.the_board.getSize());
	    System.out.println("--Current Board:");
		if(player_obj.the_board == null){
			System.out.println("The board is null");
		}
	    player_obj.the_board.printBoard();
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try{
			while(!in.ready()){
				Thread.sleep(100);
			}
			String inputDecision=in.readLine();
			if(inputDecision.toUpperCase().equals("M") ){
				try{
					while(true){
						System.out.print("x:");
					    while(!in.ready()){
					    	Thread.sleep(100);
					    }
						int x_value = 0;
						inputDecision=in.readLine();
						try{
							x_value=Integer.parseInt(inputDecision);
						}catch(NumberFormatException e)
						{	
							System.out.println("wrong input");
							continue;									
						}
						System.out.print("y:");
						int y_value = 0;
						inputDecision=in.readLine();
						try{
							y_value=Integer.parseInt(inputDecision);
						}catch(NumberFormatException e){
							System.out.println("wrong input");
							continue;
						}
						
						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
						r.replyOpCode = MessageProtocol.OP_STATUS_OK;
						r.moveType = MessageProtocol.MOVE_STONE;
						r.loc = new Location(x_value, y_value);
						r.writeTo(out);		
						break;
					}
				}catch (Exception e){System.out.println("Connection down!");}
			}
			else if(inputDecision.toUpperCase().equals("F")){
				ServerReply r = new ServerReply();
				r.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
				r.replyOpCode = MessageProtocol.OP_STATUS_OK;
				r.moveType = MessageProtocol.MOVE_FORFEIT;
				r.loc = new Location(0,0);
				r.writeTo(out);
			}
			else{ //pass
				ServerReply r = new ServerReply();
				r.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
				r.replyOpCode = MessageProtocol.OP_STATUS_OK;
				r.moveType = MessageProtocol.MOVE_PASS;
				r.loc = new Location(1,1);
				r.writeTo(out);
			}
		}catch(Exception e){System.out.println("time out interrupted! in handle player input 2");}
	}
	
	private void run_server_to_client() {
		try {
			s.setSoTimeout(0);
			ClientMessage incoming_message = new ClientMessage();
			InputStream in = s.getInputStream();
			OutputStream out = s.getOutputStream();
			
			while (!exit) {
				// wait for gameStart message from server and set board and color
				try{
					incoming_message.readFrom(in);
				}
				catch(IOException e){
					System.out.println("Connection down! (server to client)");
					player_obj.interruptPlayer(myid);
					System.out.println(e.getMessage());
					handleOptionServerFailure();
					partner.addToQueue(new PlayerWorkerMessage((byte)1111));
					exit = true;
				}
				if(exit!=true){
	                if(incoming_message.getMsgType() == MessageProtocol.OP_TYPE_GAMESTART){
						player_obj.interruptPlayer(myid);
						System.out.println("game start...");
	                	System.out.println("Game " + incoming_message.game.name.s + " is started.");
						System.out.println("BlackStone player is " + incoming_message.blackPlayer.name.s + " " +
								          ",and WhiteStone player is " + incoming_message.whitePlayer.name.s);
						player_obj.receive_gameStart(incoming_message);
						
						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_GAMESTART);
						r.replyOpCode = MessageProtocol.OP_STATUS_OK;
						r.writeTo(out);
	                }
	                else if (incoming_message.getMsgType() == MessageProtocol.OP_TYPE_MAKEMOVE) {
						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_MAKEMOVE);
						r.replyOpCode = MessageProtocol.OP_STATUS_OK;
						r.writeTo(out);
						
						player_obj.update_board(incoming_message);
						System.out.println("The board received from MAKEMOVE");
						player_obj.the_board.printBoard();
					} else if (incoming_message.getMsgType() == MessageProtocol.OP_TYPE_GETMOVE) {
						// prompt for user input and reply
							handlePlayerInput(out);
					}
					else if(incoming_message.getMsgType() == MessageProtocol.OP_TYPE_GAMEOVER){
						ServerReply r = new ServerReply();
						r.setMsgType(MessageProtocol.OP_TYPE_GAMEOVER);
						r.replyOpCode = MessageProtocol.OP_STATUS_OK;
						r.writeTo(out);
						player_obj.interruptPlayer(myid);
						if(incoming_message.player_for_gameover.name == null){
							incoming_message.player_for_gameover.name = new StringInfo();
						}
						player_obj.display_game_over(incoming_message.blackScore, incoming_message.whiteScore, incoming_message.winner_or_player.name.s, incoming_message.player_for_gameover.name.s, incoming_message.errorMsg.s);					
						partner.addToQueue(new PlayerWorkerMessage((byte)1000));
					}
				}//end if(exit!=true)
			} // end while(!exit)
		} 
		catch (Exception e) {
			System.out.println("Exception caught in Server_client worker.");
			System.exit(-1);
		}
	}//end run_server_to_client()
	
	private void run_client_to_server() {
		try {
			//addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_CONNECT));
			
			handleOption(); // give the user menu
            
			OutputStream out = s.getOutputStream();
			InputStream  in  = s.getInputStream();
			
			while (!exit) {
				PlayerWorkerMessage c = new PlayerWorkerMessage((byte)9999);
				boolean isOut = false;
				while (!isOut){
					try{
						//System.out.println("getting q");
						c = q.getWithTimeout(1000);
						isOut = true;
						//System.out.println("after q");
					}catch(Exception e){
						//System.out.println("no q");
						q.get.release();
					}
					if(s.isClosed()){ // handle timeout for the other socket
						  //player_obj.interruptPlayer(myid);  // interrupt the other worker thread
					      System.out.println("Server down!");
						  isOut = true;
						  exit = true;
						  c = new PlayerWorkerMessage((byte)9999);
					}
					
				}
				if (c.b == MessageProtocol.OP_TYPE_CONNECT) {
					// send connect message to server
					System.out.println("Please your password:");
					BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));					
					String passWd=decision.readLine();
					ServerMessage connect_message = player_obj.make_connect_message(passWd);
					connect_message.writeTo(out);
									
					ClientReply cr = new ClientReply();		
					cr.setMsgType(MessageProtocol.OP_TYPE_CONNECT);
					cr.readFrom(in);
					
					if(debug == 1){
						System.out.println("Message received after sending message 'connect': "+cr.replyOpCode);
					}
					if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
						System.out.println("Congratulation connection established!");
						addToQueue(new PlayerWorkerMessage((byte)1000));
					}
					else if (cr.replyOpCode == MessageProtocol.OP_ERROR_REJECTED){
						System.out.println("Already Connected");
						addToQueue(new PlayerWorkerMessage((byte)1000));
					}
					else if (cr.replyOpCode == MessageProtocol.OP_ERROR_BAD_AUTH){
						System.out.println("Invalid Password");
						addToQueue(new PlayerWorkerMessage((byte)1000));
					}
				} 
				else if (c.b == MessageProtocol.OP_TYPE_REGISTER){
					System.out.println("Please enter a password:");
					BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));
					String passWd=decision.readLine();
					System.out.println(" password: " + passWd);
					ServerMessage register_message = player_obj.make_register_message(passWd);
					register_message.writeTo(out);
					
					ClientReply cr = new ClientReply();
					cr.setMsgType(MessageProtocol.OP_TYPE_REGISTER);
					cr.readFrom(in);
					if(debug == 1){
						System.out.println("Message received after sending message 'register': "+cr.replyOpCode);
					}
					if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
						System.out.println("Register successfully.");
						addToQueue(new PlayerWorkerMessage((byte)1000));
					}
					else if(cr.replyOpCode == MessageProtocol.OP_ERROR_REJECTED){
					    System.out.println("The same name is already registered, or if register is sent at any point after connect, such as during a game.");
					    addToQueue(new PlayerWorkerMessage((byte)1000));
					}
				}
				else if (c.b == MessageProtocol.OP_TYPE_CHANGEPW){
					System.out.println("Please enter a new password:");
					BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));
					try{
						String passWd=decision.readLine();
						ServerMessage changePW_message = player_obj.make_changePW_message(passWd);
						changePW_message.writeTo(out);
						
						ClientReply cr = new ClientReply();
						cr.setMsgType(MessageProtocol.OP_TYPE_CHANGEPW);
						cr.readFrom(in);
						
						if(debug == 1){
							System.out.println("Message received after sending message 'changePW': "+cr.replyOpCode);
						}
						if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
							System.out.println("Password changed successfully.");
							addToQueue(new PlayerWorkerMessage((byte)1000));
						}
						else if(cr.replyOpCode == MessageProtocol.OP_ERROR_UNCONNECTED){
						    System.out.println("Can not change password before connecting to the server.");
						    addToQueue(new PlayerWorkerMessage((byte)1000));
						}
						else if(cr.replyOpCode == MessageProtocol.OP_ERROR_REJECTED){
							System.out.println("Can not change password of someone else.");
							addToQueue(new PlayerWorkerMessage((byte)1000));
						}
						
					}catch(Exception e){System.out.println("Invalid Input.");
					                    addToQueue(new PlayerWorkerMessage((byte)1000));}
				}
				else if (c.b == MessageProtocol.OP_TYPE_WAITFORGAME) {
					// send waitForGame to server
					ServerMessage waitForGame_message = player_obj.make_waitForGame_message();
					waitForGame_message.writeTo(out);
					
					ClientReply cr = new ClientReply();
					cr.setMsgType(MessageProtocol.OP_TYPE_WAITFORGAME);
					cr.readFrom(in);
					
					if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
						System.out.println("Waiting for a game...");
						System.out.println("You can also have the following options: ");
						addToQueue(new PlayerWorkerMessage((byte)1000));				
					}
					else if (cr.replyOpCode == MessageProtocol.OP_STATUS_RESUME){
						System.out.println("Game is resuming...");
						System.out.println("printing in resume: ");
						cr.board.printBoard();
						player_obj.the_board = cr.board;
						if(cr.blackPlayer.name.s.equals(player_obj.name)){
							player_obj.s_color = StoneColor.BLACK;
						}
						else{
							player_obj.s_color = StoneColor.WHITE;
						}
					}
					else if (cr.replyOpCode == MessageProtocol.OP_ERROR_REJECTED){
						System.out.println("You are already waiting / in a Game.");
						System.out.println("You can also have the following options: ");
						addToQueue(new PlayerWorkerMessage((byte)1000));
						
					}
					else if (cr.replyOpCode == MessageProtocol.OP_ERROR_UNCONNECTED){
						System.out.println("Not connected yet!");
						System.out.println("Do you want to connect? (y/n)");
						BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));
						try{
							while(!decision.ready()){
								Thread.sleep(100);
							}
							String inputDecision=decision.readLine();
							if(inputDecision.toUpperCase().equals("Y") ){
								addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_CONNECT));
							}
							else{
								System.out.println("Without connection, can do nothing!");
							}
						}catch(Exception e){System.out.println("time out interrupted! erro erjected");}
					}
				} 
				else if (c.b == MessageProtocol.OP_TYPE_DISCONNECT) {
					ServerMessage d = new ServerMessage();
					d.setMsgType(MessageProtocol.OP_TYPE_DISCONNECT);
					d.writeTo(out);
					System.out.println("Disconnected");
					System.exit(-1);
					// Sever will disconnect the socket?
				}
				else if(c.b == (byte)4444){
					System.out.println("Please enter the name of the account you want to hack: ");
					BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));
					try{
						String name = decision.readLine();
						System.out.println("Please enter a new password:");
						String passWd=decision.readLine();		
						ServerMessage changePW_message = player_obj.make_changePW_message(passWd);
						changePW_message.player.name.s =  name;				
						
						changePW_message.writeTo(out);
						
						ClientReply cr = new ClientReply();
						cr.setMsgType(MessageProtocol.OP_TYPE_CHANGEPW);
						cr.readFrom(in);
						
						if(debug == 1){
							System.out.println("Message received after sending message 'changePW': "+cr.replyOpCode);
						}
						if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
							System.out.println("Password changed successfully.");
							addToQueue(new PlayerWorkerMessage((byte)1000));
						}
						else if(cr.replyOpCode == MessageProtocol.OP_ERROR_UNCONNECTED){
						    System.out.println("Can not change password before connecting to the server.");
						    addToQueue(new PlayerWorkerMessage((byte)1000));
						}
						else if(cr.replyOpCode == MessageProtocol.OP_ERROR_REJECTED){
							System.out.println("Can not change password of someone else.");
							addToQueue(new PlayerWorkerMessage((byte)1000));
						}
						
					}catch(Exception e){System.out.println("Invalid Input.");
					                    addToQueue(new PlayerWorkerMessage((byte)1000));}
				}
				else if(c.b == (byte)1000){
					handleOption();
				}
				else if(c.b == (byte)1111){ // reconnection
					exit = true;
					System.out.println("1111");
				}
			}
		}catch (Exception e) {
            System.out.println("IO exception");
            addToQueue(new PlayerWorkerMessage((byte)1000));
		}
	}//run_client_to_server()
	
	public void handleOption(){
		System.out.println("Please enter a number for your decision");
		System.out.println(" enter 1 for (Register)");
		System.out.println(" enter 2 for (Connect)");
		System.out.println(" enter 3 for (WaitForGame)");
		System.out.println(" enter 4 for (Disconnect)");
		System.out.println(" enter 5 for (Change Password)");
		System.out.println(" enter 6 for (hacker)");
		BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));
		try{
			try {
				while(!decision.ready()){
					if(!Thread.interrupted()){
					    Thread.sleep(1000);
					}
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.out.println("I/O exception. (Decision)");
			}
			try{
				String inputDecision=decision.readLine();
				int input = Integer.parseInt(inputDecision);
				if(input == 1){
					addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_REGISTER));
				}
				else if(input == 2){
					addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_CONNECT));
				}
				else if(input == 3){
					addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_WAITFORGAME));
				}
				else if(input == 4){
					addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_DISCONNECT));
				}
				else if(input == 5){
					addToQueue(new PlayerWorkerMessage(MessageProtocol.OP_TYPE_CHANGEPW));
				}
				else if(input == 6){
					addToQueue(new PlayerWorkerMessage((byte)4444));
				}
			}catch(Exception e){System.out.println(" I/O exception");addToQueue(new PlayerWorkerMessage((byte)1000));}
		}catch (InterruptedException e){System.out.println("Can not can have options anymore.");}
	}
	
	public void handleOptionServerFailure(){
		System.out.println("Please enter a number for your decision");
		System.out.println(" enter 1 for (Reconnect)");
		System.out.println(" enter 2 for (disconnect)");
		BufferedReader decision = new BufferedReader(new InputStreamReader(System.in));
		try{
			String inputDecision=decision.readLine();
			int input = Integer.parseInt(inputDecision);
			if(input == 1  ){
				System.out.println("Reconnecting...");
			}
			else if(input == 2){
				System.out.println("exit!");
				System.exit(-1);
			}
		}catch(Exception e){System.out.println(" Wrong Input!"); }
	}
	
	public void run(){
		//3 way handshake set up
		sendRand(this.initialRanNum);
		//wait to receive the new random number
		try{
			InputStream in = s.getInputStream();
			DataInputStream data_in = new DataInputStream(in);
	    	int received_int = data_in.readInt();
	    	setReceived(received_int);
	    	System.out.format("This should be the sent number + 1: %d\n",data_in.readInt());
		}catch(IOException e){
			System.out.println("Timeout!");
			System.exit(-1);
		}
		
		//send back the random number + 1
		sendReceivedPlusOne();
		//release the semaphore in printingObserver
		twh_semaphore.v();  //done with 3 way handshake
		
		//block until done assigning role
		magical_semaphore.p();
		
		if(sToc){
			run_server_to_client();
			if(debug == 1){
				System.out.println("server_to_client shut down");
			}
		}
		else{
			run_client_to_server();
			if(debug == 1){
				System.out.println("client_to_server shut down");
			}
		}
		
	}//end run
}
