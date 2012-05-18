package edu.berkeley.cs.cs162.Client;

/*observer worker - main message
 * types:
 * 1. connect
 * 2. disconnect
 * 3. listgame(observer only)
 * 4. join(observer only)
 * 5. leave (observer only)
 *
 */

public class OworkerMessage {
	
	int type;
	String gameName;
	public OworkerMessage(int i){
		type=i;
	}
	
	public int getType(){
		return type;
	}
	
	public void setJoinGame(String gn){
		gameName=gn;
	}
	
	public String getJoinGame(){
		return gameName;
	}
}
