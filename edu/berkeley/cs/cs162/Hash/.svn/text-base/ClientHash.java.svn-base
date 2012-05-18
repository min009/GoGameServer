package edu.berkeley.cs.cs162.Hash;
import java.security.MessageDigest;
public class ClientHash {
	String password;
	byte [] pwdBytes;
	byte [] hashedPwd;
	MessageDigest md;

	public ClientHash(){
		try{
		md = MessageDigest.getInstance("SHA-256");
		}catch(Exception e){System.out.println("Failed to get SHA instance");}
	}
	public String getHash(String input)throws Exception{
		password = input;
		pwdBytes = password.getBytes("UTF-16");
		md.reset();
		md.update(pwdBytes);
		hashedPwd = md.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hashedPwd.length; i++) {
			sb.append(Integer.toString((hashedPwd[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

}
