package edu.berkeley.cs.cs162.Hash;
import java.security.MessageDigest;
public class HashNSalt {

	String password;
	byte [] tempPass;
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

	public String getHashNSalt (String input) throws Exception{
		String password = input;
		password = password + salt;
		System.out.println("Password after concat is " + password);
		pwdBytes = password.getBytes("UTF-16");
		md.reset();
		md.update(pwdBytes);
		hashedPwd = md.digest();
		StringBuffer sb2 = new StringBuffer();
		for (int i = 0; i < hashedPwd.length; i++) {
			sb2.append(Integer.toString((hashedPwd[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb2.toString();
	}



}
