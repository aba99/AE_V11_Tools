package com.automic.api;


public class AeStringUtils {
	
	private static String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789$@_.#";
	private static String defaultChar = "_";
	
	public static String makeAeValid(String str)
	{
		str=str.toUpperCase();

		char[]charArray = str.toCharArray();
 
		for(char temp:charArray){

			if(!allowed.contains(Character.toString(temp)))
			{
				str=str.replace(Character.toString(temp), defaultChar);
			}
		}
		return str;
	}

}
