package edu.berkeley.cs.cs162.common;
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
	public byte[] getHash(String input)throws Exception{
		password = input;
		pwdBytes = password.getBytes("ASCII");
		md.reset();
		md.update(pwdBytes);
		hashedPwd = md.digest();
		return hashedPwd;
	}

}
