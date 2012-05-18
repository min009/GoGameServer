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
import java.net.SocketTimeoutException;
import java.util.*;
//import edu.berkeley.cs.cs162.Client.*;
import edu.berkeley.cs.cs162.Synchronization.Semaphore;
import edu.berkeley.cs.cs162.Synchronization.ThreadSafeQueue;
import edu.berkeley.cs.cs162.Writable.*;

//note: put appropriate function on the catch exception block

public class Worker implements Runnable{
	
	protected ThreadSafeQueue<OworkerMessage> q;
	//public int flag; //probably no needed
	public Socket s;
	public int numReceived;
	public int initialRanNum;
	protected Semaphore se;
	boolean exit;
	boolean sToc = false;
	protected Worker partner;
	protected Observer observer_obj;
	Semaphore whileSem;
	ClientInfo clientInfo;
	PrintingObserver parentObject;
	List<GameInfo> joinedGames = new ArrayList<GameInfo>();
	
	int userChoice = 0; // hold the user input number (1 for register, 2 for connect)
	String passwordReg = new String(); //hold the user input registration password
	String passwordCon = new String(); //hold the user input connection password
	boolean connectDecesion = false;
	
	public Worker(InetAddress ipaddress,int port, int randNum, Semaphore p,Semaphore sem, ClientInfo info, PrintingObserver obj){
		q = new ThreadSafeQueue<OworkerMessage>(100);
		numReceived = -99999;
		initialRanNum = randNum;
		whileSem = sem;
		clientInfo = info;
		parentObject = obj;
		observer_obj = obj;
		
		try{
			s = new Socket(ipaddress, port);
			s.setSoTimeout(0);
		}catch (IOException e){System.out.println("creating socket error");parentObject.killWorkers();}
		catch (Exception e){parentObject.killWorkers();}
		exit = false;
		se = p;
	}
	
	//MK, I assume this way, the number is sent
	public void sendRand(int num){
		//serialize the random number and send it
		try{
			OutputStream out = s.getOutputStream();
			DataOutputStream data_out = new DataOutputStream(out);
	    	data_out.writeInt( num );
			
		}catch(IOException e){
			System.out.println("sendRand timeout error!");
			parentObject.killWorkers();
			try{s.close();}catch(Exception ex){System.out.println("error closing socket");}
			System.exit(-1);
		}
	}
	
	public void setPartner(Worker w){
		partner = w;
	}
	
	//needed for printingobserver to access its received random number
	public int getReceivedRan(){
		return numReceived;
	}
	
	public void readyExit(){
		exit=true;
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
			
		}catch(IOException e){
			System.out.println("sendRand timeout error!");
			parentObject.killWorkers();
			try{s.close();}catch(Exception ex){System.out.println("error closing socket");}
			System.exit(-1);
		}
	}
	
	public void addToQueue(OworkerMessage m){
		q.add(m);
	}
	
	//
	public void setflag_sToc(){
		this.sToc = true; //set sToc default value to be true
	}
	
	public void print_joinedgames(List<GameInfo> list){
		for(int i = 0; i < list.size(); i++ ){
			System.out.println(list.get(i).name.s);
		}
	}
	
	public void userOption(boolean temp){
		while (temp){
			System.out.println("1. register ");
			System.out.println("2. connect ");
			// user input for register or connect
			try{
				BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
				String selection = input.readLine();
				int inputNum = Integer.parseInt(selection);
				System.out.println("input is " + inputNum);
				if (inputNum > 2 || inputNum < 1)
					System.out.println("You entered a invalid option. Please enter again ...");
				else{
					if (inputNum == 1){
						System.out.println("Please enter your password to complete registration: ");
						try{
							BufferedReader pswReg = new BufferedReader(new InputStreamReader(System.in));	
							String temp1 = pswReg.readLine();
							passwordReg = temp1;
							passwordCon = temp1;
							}
						catch(Exception e){
							System.out.println("User Input Password to Regstration Error!");
						}
					}
					else if(inputNum == 2){
						System.out.println("Please enter your password to connect: ");
						try{
							BufferedReader pswcon = new BufferedReader(new InputStreamReader(System.in));
							String temp2 = pswcon.readLine();
							passwordCon = temp2;
							connectDecesion = true; 
						}
						catch(Exception e){
							System.out.println("User Input Password to Connect Error! ");
						}
					}
					temp = false;
					userChoice = inputNum;
				}
			}
			catch(Exception e){
				System.out.println("User Input Error !");
			}
				
			}
		}
	
	public void connectOption(boolean para){
		while (para){
			System.out.println("Enter 1 for connect, 2 for not connect: ");
			try{
				BufferedReader connectOpt = new BufferedReader(new InputStreamReader(System.in));
				int connectOptNum = Integer.parseInt(connectOpt.readLine());
				if (connectOptNum == 1){
					System.out.println("Your connection will be started soon.");
					connectDecesion = true;
					para = false;
				}
				else if (connectOptNum == 2){
					System.out.println("You decided to not connect, thank you");
					connectDecesion = false;
					para = false;
					//System.exit(-1);
					
				}
				else
					System.out.println("Invalid input, please try again:");
			}
			catch(Exception e){
				System.out.println("Error!");
			}
		}
	}
	
	public void run(){
		//3 way handshake set up
		sendRand(this.initialRanNum);
		System.out.format("Sent number %d\n", this.initialRanNum);
		//wait to receive the new random number
		try{
			InputStream in = s.getInputStream();
			DataInputStream data_in = new DataInputStream(in);
	    	int received_int = data_in.readInt();
	    	setReceived(received_int);
	    	System.out.format("Random number received from server is %d\n", received_int);
	    	System.out.format("This should be the sent number + 1: %d\n",data_in.readInt());
			
		}catch(IOException e){
			System.out.println("Timeout!");
			parentObject.killWorkers();
			try{s.close();}catch(Exception ex){System.out.println("error closing socket");}
			System.exit(-1);
			}
		//send back the random number + 1
		sendReceivedPlusOne();
		//release the semaphore in printingObserver
		se.v();  //done with 3 way handshake
		//while loop checking its queue, act correspond to queue message
		
		whileSem.p();
		if (sToc){
			while(!exit){
				ClientMessage C_msg = new ClientMessage();
				try{
					C_msg.readFrom(s.getInputStream());
					byte msg_opcode = C_msg.getMsgType();
			
					if (msg_opcode == MessageProtocol.OP_TYPE_GAMESTART){
						System.out.println("Game " + C_msg.game.name.s + " is started.");
						System.out.println("BlackStone player is " + C_msg.blackPlayer.name.s + " " +
								          ",and WhiteStone player is " + C_msg.whitePlayer.name.s);
					}
					else if(msg_opcode == MessageProtocol.OP_TYPE_GAMEOVER){
						System.out.println("The game " + C_msg.game.name.s + " is over");
						System.out.println("BlackStone player score is " + C_msg.blackScore + 
								          " ,and WhiteStone player score is " + C_msg.whiteScore);
						System.out.println("The winner for this game is " + C_msg.winner_or_player.name.s);
						System.out.println();
					}
					else if(msg_opcode == MessageProtocol.OP_TYPE_MAKEMOVE){
						System.out.println("In Game " + C_msg.game.name.s + ", " + C_msg.winner_or_player.name.s + " has moved");
						System.out.println(C_msg.winner_or_player.name.s + " placed a stone at location ("
						                   +  C_msg.loc.x + ", " + C_msg.loc.y + ")");
					}
					
					ServerReply sReply = new ServerReply();
					sReply.setMsgType(msg_opcode);
					sReply.replyOpCode = MessageProtocol.OP_STATUS_OK;
					OutputStream msg_out = s.getOutputStream();
					sReply.writeTo(msg_out);  // msg reply to server
					
				}catch(IOException e){
					System.out.println("Input stream reading error12.");
					parentObject.killWorkers();
					try{s.close();
					System.out.println("close");}catch(Exception ex){System.out.println("error closing socket");}
				}
			}
		}
		else{//client to server
			try{
			OutputStream out = s.getOutputStream();
			InputStream  in  = s.getInputStream();
			System.out.println("Welcome!");
			System.out.println("Inorder to get you to the right place, please selecte your option: ");
			boolean continueInput = true;
			userOption(continueInput); // call userOption to allow user to decide either register or connect 
			System.out.println("userChoice is " + userChoice);
			
			if (userChoice == 1){// user wants to make a registration
				System.out.println("passwordReg is " + passwordReg);
				
				ServerMessage register_message = observer_obj.make_register_message(passwordReg);
				register_message.writeTo(out);
				
				ClientReply cr = new ClientReply();
				cr.setMsgType(MessageProtocol.OP_TYPE_REGISTER);
				cr.readFrom(in);
				
				if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
					System.out.println("Registration successfully.");
					System.out.println("Now, do you want to connect? ");
					boolean go = true;
					connectOption(go);//call connectOption to ask user whether want to connect the server or not
				}	
				else if(cr.replyOpCode == MessageProtocol.OP_ERROR_REJECTED){
				    System.out.println("The same name is already registered, or if register is sent at any point after connect, such as during a game.");
				    System.out.println("Since you are already registered, do you want to connect? ");
				    boolean go = true;
				    connectOption(go);
				}
			}
			
			if (connectDecesion){//If connectDecesion is true
			try{
			ServerMessage connect_message = observer_obj.make_connect_message(passwordCon);
			connect_message.player = clientInfo;
			connect_message.writeTo(out);
			
			ClientReply cReply1 = new ClientReply();
			cReply1.setMsgType(MessageProtocol.OP_TYPE_CONNECT);
			cReply1.readFrom(s.getInputStream());
			
			if(cReply1.replyOpCode == MessageProtocol.OP_STATUS_OK)
				System.out.println("connected");
			else{
				System.out.println("Error, trying to connect while connected");
			}
			}catch(Exception writeErr){System.out.println("Error111 ");}
			while(!exit){
				System.out.println("Please enter a number for your decision");
				System.out.println(" enter 1 for (list of games)");
				System.out.println(" enter 2 for (disconnect)");
				System.out.println(" enter 3 for (leave)");
				System.out.println(" enter 4 for (change your password)");
				// user input
				BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
				try{String number = userInput.readLine();
					int input = Integer.parseInt(number);
					if (input > 4 || input < 1){//handle the case that if user enter a number greater than 3
						System.out.println("You can only enter a number between 1 and 3 !");
						System.out.println("Try again");
						System.out.println();
					}
					else if (input == 1){//observer decides to get list of games
					//send a message to server
						ServerMessage sMsg = new ServerMessage();
						sMsg.setMsgType(MessageProtocol.OP_TYPE_LISTGAMES);
						sMsg.writeTo(s.getOutputStream());
			
					//wait for server's response. 
						try{
							ClientReply CReply = new ClientReply();
							CReply.setMsgType(MessageProtocol.OP_TYPE_LISTGAMES);
							CReply.readFrom(s.getInputStream());
							
							if (CReply.game_list.get_as_GameInfo().isEmpty()){
								System.out.println("There is no game available at this moment.");
								System.out.println("Please come back later...");
								System.out.println();
							}
							else{
								System.out.println("Games on the list are: "); 
								CReply.game_list.display_as_GameInfo();
								System.out.println(" is available, do you want to join some games?");
								System.out.println("enter 0 for not join any game, or 1 for the first game on the list, etc");
							
								//user input for join games
								BufferedReader joinGame = new BufferedReader(new InputStreamReader(System.in));
								String gameNum = joinGame.readLine();
								int joinDecision = Integer.parseInt(gameNum);
							
								// user input 0, 1, 2 ... (0 --> user don't want to join game, 1 --> user want to join game 1, etc
								if (joinDecision != 0){
									GameInfo selectedGame;
									byte replyOpCode;
									ClientReply joinReply = new ClientReply();
									//send msg to server; (joinDecision == 1, send join_game1 msg to server 
									List<GameInfo> listOfGames = CReply.game_list.get_as_GameInfo();
									selectedGame = listOfGames.get(joinDecision - 1);
									ServerMessage joinMsg = new ServerMessage();
									joinMsg.setMsgType(MessageProtocol.OP_TYPE_JOIN);
									joinMsg.game = selectedGame;
									joinMsg.writeTo(s.getOutputStream());
								
									joinReply.setMsgType(MessageProtocol.OP_TYPE_JOIN);
									joinReply.readFrom(s.getInputStream());
									replyOpCode = joinReply.replyOpCode;
									if ( replyOpCode == MessageProtocol.OP_STATUS_OK){
										System.out.println("BlackStone player is " + joinReply.blackPlayer.name.s + " " +
															",and WhiteStone player is " + joinReply.whitePlayer.name.s);
										System.out.println("The current board is : \n");
										joinReply.board.printBoard();
										joinReply.board.printBoard();
										joinedGames.add(selectedGame); //add to joinedGames
									}
									else if (replyOpCode == MessageProtocol.OP_ERROR_INVALID_GAME){
										System.out.println("Error, this game is invalid.");
									}
									else if (replyOpCode == MessageProtocol.OP_ERROR_UNCONNECTED){
										System.out.println("Please connect to game server first");
									}
								}
							}
							}
							
						catch(SocketTimeoutException e){
							System.out.println("Time out error");
							parentObject.killWorkers();
							try{s.close();
							}catch(Exception ex){
								System.out.println("error closing socket");
								}
						//do clean up
						}
						catch(IOException e){
							System.out.println("IO Exception"); //do necessary clean up
							parentObject.killWorkers();
							try{s.close();
							}catch(Exception ex){
								System.out.println("error closing socket");
								}
						}
					}
					else if (input == 2){//observer decides to disconnect from server
						//send disconnect msg to server
						ServerMessage sMsg = new ServerMessage();
						sMsg.setMsgType(MessageProtocol.OP_TYPE_DISCONNECT);
						OutputStream msg_out = s.getOutputStream();
						sMsg.writeTo(msg_out); // disconnect msg reply to server
						parentObject.killWorkers();
					}
					else if (input == 3){//observer decides to leave the game
						
						ClientReply CReply2 = new ClientReply();
						ServerMessage sMsg2 = new ServerMessage();// for leaving particular game.
					
						if(joinedGames.isEmpty()){
							System.out.println("You are not in any game; There is no point of leaving game !!!");
							System.out.println();
						}
						
						else{
							System.out.println("games : "); 
							print_joinedgames(joinedGames);
							System.out.println(" which game do you want to leave?");
							System.out.println("1 for the first game on the list, etc");
							
							BufferedReader leaveGame = new BufferedReader(new InputStreamReader(System.in));
							String leaveNum = leaveGame.readLine();
							int leaveDecision = Integer.parseInt(leaveNum);
							
							GameInfo selectedGame = joinedGames.get(leaveDecision-1);
							sMsg2.game = selectedGame;
							sMsg2.setMsgType(MessageProtocol.OP_TYPE_LEAVE);
							sMsg2.writeTo(s.getOutputStream());
							CReply2.setMsgType(MessageProtocol.OP_TYPE_LEAVE);
							CReply2.readFrom(s.getInputStream());
							
							if(CReply2.replyOpCode == MessageProtocol.OP_STATUS_OK){
								System.out.println("The game has been successfully left!");
								joinedGames.remove(leaveDecision - 1);
								}
							else{
								System.out.println("Game not left");
							}
						}
					}
				
					else if (input == 4){
						System.out.println("Please enter your new password:");
						try{
							BufferedReader changePsw = new BufferedReader(new InputStreamReader(System.in));
							String newPsw = changePsw.readLine();
							
							ServerMessage changePW_message = observer_obj.make_changePW_message(newPsw);
							changePW_message.writeTo(out);
							
							ClientReply cr = new ClientReply();
							cr.setMsgType(MessageProtocol.OP_TYPE_CHANGEPW);
							cr.readFrom(in);
							if(cr.replyOpCode == MessageProtocol.OP_STATUS_OK){
								System.out.println("Password changed successfully.");
							}
							else if(cr.replyOpCode == MessageProtocol.OP_ERROR_UNCONNECTED){
							    System.out.println("Can not change password before connecting to the server.");
							}
							
						}
						catch (Exception e){
							System.out.println("Change Password Input Error");
						}
					}
				}
				
				catch(IOException i){parentObject.killWorkers();
					try{s.close();
					}catch(Exception ex){
						System.out.println("error closing socket");
						}
				}
			}
		try{s.close();
		}catch(Exception ex){
			System.out.println("Error in closing socket");
			}
		}
			}
		catch(Exception e){
			System.out.println("Error55");}
		}}
		
}

