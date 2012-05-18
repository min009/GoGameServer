package edu.berkeley.cs.cs162.Hash;
import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class HashTest {
	public static void main(String [ ] args)
	{	try{
		String password = "";
		MessageDigest md;
		byte [] pwdBytes;
		byte [] pwdBytes2;
		byte [] hashedPwd;
		ClientHash CH = new ClientHash();
		HashNSalt HS = new HashNSalt();
		md = MessageDigest.getInstance("SHA-256");

		while(!password.equals("byebye")){
			System.out.println("Please enter a password to test or byebye to end testing.");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			password = in.readLine();
			pwdBytes = password.getBytes("UTF-16");
			md.reset();
			md.update(pwdBytes);
			hashedPwd = md.digest();
			String ClientHash = CH.getHash(password);
//			byte [] CHResult = CH.getHash(password);
//			StringBuffer sb1 = new StringBuffer();
//			for (int i = 0; i < CHResult.length; i++) {
//				sb1.append(Integer.toString((CHResult[i] & 0xff) + 0x100, 16).substring(1));
//			}
			StringBuffer sb2 = new StringBuffer();
			for (int i = 0; i < hashedPwd.length; i++) {
				sb2.append(Integer.toString((hashedPwd[i] & 0xff) + 0x100, 16).substring(1));
			}
//			String ClientHash = new String(CHResult, "ASCII");
//			String ClientHash = sb1.toString();
//			String LocalHash = new String(hashedPwd, "ASCII");
			String LocalHash = sb2.toString();
			System.out.println("ClientHash result");
			System.out.println("============================================================");
			System.out.println("The hash by ClientHash's getHash function is " + ClientHash);
			System.out.println("The hash by the local hash(solution) is " + LocalHash);
			if(ClientHash.equals(LocalHash))
				System.out.println("The hash are the same and should be correct\n\n");
			else
				System.out.println("The hash are different! Error!\n\n");
			System.out.println("HashNSalt result");
			System.out.println("============================================================");
			String Result = HS.getHashNSalt(ClientHash);
			System.out.println("The result from the Hash and salt is " + Result);
			System.out.println("============================================================");

		}
	}catch(Exception e){System.out.println("something failed in HastTest");}
	System.out.println("Test ending, good bye");
	}


}

