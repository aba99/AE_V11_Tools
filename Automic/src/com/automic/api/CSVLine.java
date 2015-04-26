package com.automic.api;

public class CSVLine {

	private String user;
	private String pass;
	private String host;
	private String port;
	private String protocol;
	private String os_type;
	private String usrgroup;
	
	public CSVLine(String h,String os,String u,String p,String ugroup,String pro,String po)
	{
		host=h;
		os_type=os;
		user=u;
		pass=p;
		usrgroup=ugroup;
		protocol=pro;
		port=po;
	
		
	}
	public String getUser()
	{
		return user;
	}
	public String getPassword()
	{
		return pass;
	}
	public String getHostname()
	{
		return host;
	}
	public String getPort()
	{
		return port;
	}
	public String getProtocol()
	{
		return protocol;
	}
	public String getOS()
	{
		return os_type;
	}
	public String getUsrGrp()
	{
		return usrgroup;
	}
}
