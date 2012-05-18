package edu.berkeley.cs.cs162.common;
import java.security.MessageDigest;
public class HashNSalt {

	String password;
	String salt;
	byte [] pwdBytes;
	byte [] hashedPwd;
	MessageDigest md;
	public HashNSalt(){
		salt = "cs162project3istasty";
		try{
			md = MessageDigest.getInstance("SHA-256");}
		catch(Exception E){System.out.println("failed to get SHA-256 Instance");}
	}

	public byte[] getHashNSalt (String input) throws Exception{
		password = input;
		password.concat(salt);
		pwdBytes = password.getBytes("ASCII");
		md.reset();
		md.update(pwdBytes);
		hashedPwd = md.digest();
		return hashedPwd;
	}

}
