package edu.berkeley.cs.cs162.Client;

import java.util.List;

import edu.berkeley.cs.cs162.Writable.ClientMessage;
import edu.berkeley.cs.cs162.Writable.MessageProtocol;
import edu.berkeley.cs.cs162.Writable.ServerMessage;
import edu.berkeley.cs.cs162.Writable.BoardInfo;
import edu.berkeley.cs.cs162.Writable.ServerReply;
import edu.berkeley.cs.cs162.Writable.Location;
import edu.berkeley.cs.cs162.Writable.StoneColorInfo;
import edu.berkeley.cs.cs162.Writable.Writable;
import edu.berkeley.cs.cs162.common.StoneColor;

abstract public class Player extends BaseClient {
		
	String name;
    byte type;
    BoardInfo the_board;
    StoneColor s_color;
    
    public Player() {
        this("");
    }

    public Player(String name) {
        this(name, (byte)-1);
    }
    
    public Player(String name, byte type) {
        this.name = name;
        this.type = type;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setType(byte type) {
        this.type = type;
    }
    
    public byte getType() {
        return this.type;
    }
    
    public ServerMessage make_waitForGame_message(){
		ServerMessage blah = new ServerMessage();
		blah.setMsgType(MessageProtocol.OP_TYPE_WAITFORGAME);
		
		return blah;
	}
    
    public ServerReply make_place_stone_reply(Location l){
    	ServerReply r = new ServerReply();
    	r.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
    	r.replyOpCode = MessageProtocol.OP_STATUS_OK;
    	r.moveType = MessageProtocol.MOVE_STONE;
    	r.loc = l;
    	
    	return r;
    }
    
    public ServerReply make_pass_reply(){
    	ServerReply r = new ServerReply();
    	r.setMsgType(MessageProtocol.OP_TYPE_GETMOVE);
    	r.replyOpCode = MessageProtocol.OP_STATUS_OK;
    	r.moveType = MessageProtocol.MOVE_PASS;
    	
    	return r;
    }
    
    //initialize board and color based on gameStart message
    public void receive_gameStart(ClientMessage incoming_message) throws Exception{
    	if(incoming_message.getMsgType() != MessageProtocol.OP_TYPE_GAMESTART){
    		throw new Exception();
    	}
    	
    	System.out.println("I am in ");
    	the_board = incoming_message.board;
    	
    	//if this player's name is black player's name in incoming_message
    	if( name.equals( incoming_message.blackPlayer.name.s )){
    		s_color = StoneColor.BLACK;
    	}
    	else{
    		s_color = StoneColor.WHITE;
    	}
    }

    //updates the board based on the received makeMove message
    public void update_board(ClientMessage m){
    	
    	if(m.reason_or_moveType_or_errorNum == MessageProtocol.MOVE_STONE){
			StoneColor added_stone_color;
			
			if( name.equals(m.winner_or_player.name.s) ){
				added_stone_color = s_color;
			}
			else{
				added_stone_color = StoneColorInfo.get_opposite(s_color);
			}
			
			the_board.addStone(m.loc, added_stone_color);
			
			List<Writable> cs = m.captured_stones.l;
			for(int i = 0; i < cs.size(); i++){
				the_board.removeStone( (Location) cs.get(i) );
			}
		}
    }
    

    public void display_game_over(double black_score, double white_score, String winner, String player_making_error, String error_message){
    	
		System.out.println("Game over. b score: " + black_score + " w score: " + white_score + ". winner is " + winner);
		if(player_making_error != null){
			if( !player_making_error.equals("") ){
				System.out.println("player making error is " + player_making_error + " and errorMsg is " + error_message);
			}
		}
	}
    
    public abstract void interruptPlayer(int id);
}
