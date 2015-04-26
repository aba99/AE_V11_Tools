package com.automic.api;

import java.io.IOException;

import com.uc4.communication.Connection;
import com.uc4.communication.requests.CreateSession;

public class ConnectionManager {

	private Connection conn = null;
	
	public ConnectionManager(){
		
	}
	
	public Connection authenticate(String AEHost, int AEPort, int AEClient, String AELogin, String AEDep, String AEPwd, char AELanguage) throws IOException{
		
		System.out.println("Authentication...");
		conn = Connection.open(AEHost, AEPort);
		CreateSession sess = conn.login(AEClient, AELogin, AEDep, AEPwd, AELanguage);
		
		if(sess.getMessageBox()!=null){
			System.out.println("-- Error: " + sess.getMessageBox());
			System.exit(1);
			return null;
		}
		
		return conn;
		
	}
}
