package fudan.wbc.phaseA.macro;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class MyAuthenticator extends Authenticator {
	private String user = "";
	private String password = "";
	public MyAuthenticator(String user, String password){
		this.user = user;
		this.password = password;
	}
	protected PasswordAuthentication getPasswordAuthentication(){
		return new PasswordAuthentication(user,password.toCharArray());
	}
	
}
