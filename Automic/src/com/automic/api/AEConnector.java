package com.automic.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import com.uc4.communication.Connection;

public class AEConnector {
	
		private AeConnection ae;
	
		private static HashMap<String,String> config_params = new HashMap<String,String>();
		
		
	public AEConnector(String connectFile) throws FileNotFoundException
	{
	
	    	try {
				connect(connectFile);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	private void connect(String connectFile) throws NumberFormatException, IOException
	{
		/// Reading connectFile info
		Scanner sc = null;
		try {
			sc = new Scanner(new File(connectFile));
			while (sc.hasNext()) {
				String line=sc.nextLine().trim();
				if (line.length()>0)
					if (!line.startsWith("#")) 
						if (line.contains("="))
							config_params.put(line.split("=")[0].trim().toUpperCase(), line.substring(line.indexOf("=")+1).trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		} finally {
			sc.close();
		}
		
		
		if(!config_params.containsKey("AEHostnameOrIp".toUpperCase()) || !config_params.containsKey("AECPPort".toUpperCase())
				||!config_params.containsKey("AEClientToConnect".toUpperCase())||!config_params.containsKey("AEUserLogin".toUpperCase())
				|| !config_params.containsKey("AEDepartment".toUpperCase())|| !config_params.containsKey("AEUserPassword".toUpperCase())
				|| !config_params.containsKey("AEMessageLanguage".toUpperCase()))
		{
			System.out.println("Error ! Missing fields in AE Config   :\""+connectFile+"\"");

			System.exit(-1);
		}
		
		
		System.out.println("Connecting to AE : \""+config_params.get("AEHostnameOrIp".toUpperCase())+":"+config_params.get("AECPPort".toUpperCase())+"\"");
		
		
		Connection conn = new ConnectionManager().authenticate(
				config_params.get("AEHostnameOrIp".toUpperCase())
				, Integer.parseInt(config_params.get("AECPPort".toUpperCase()))
				, Integer.parseInt(config_params.get("AEClientToConnect".toUpperCase()))
				, config_params.get("AEUserLogin".toUpperCase())
				, config_params.get("AEDepartment".toUpperCase())
				, config_params.get("AEUserPassword".toUpperCase())
				, config_params.get("AEMessageLanguage".toUpperCase()).charAt(0));
		
	
		
		 ae = new AeConnection(conn);

			 
		System.out.println("--> Connected !");
		
			
		
	}
	
	public AeConnection getAeConnection()
	{
		return ae;
	}
		
		  
		    

}

