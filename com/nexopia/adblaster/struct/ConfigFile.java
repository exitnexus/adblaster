package com.nexopia.adblaster.struct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ConfigFile {
	private HashMap<String,String> options;
	
	public ConfigFile(File n){
		options = new HashMap<String,String>();
		try {
			FileReader fr = new FileReader(n);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while ((s = br.readLine()) != null){
				String kv[] = s.split("=");
				options.put(kv[0].trim().toLowerCase(), kv[1].trim().toLowerCase()); 
			}
		} catch (FileNotFoundException e1) {
			System.out.println("'banner.config' file must be created in " + System.getProperty("user.dir"));
			System.out.println("The format of db.config is: ");
			System.out.println("db_url=jdbc:mysql://server:port/tablename");
			System.out.println("db_user=...");
			System.out.println("db_pass=...");
			System.out.println("port=...");
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getInt(String key){
		return Integer.valueOf(options.get(key.toLowerCase())).intValue();
	}
	
	public String getString(String key){
		return options.get(key.toLowerCase());
	}
	
	public boolean getBool(String key){
		return (options.get(key.toLowerCase()).equals("y") ||
				options.get(key.toLowerCase()).equals("yes") ||
				options.get(key.toLowerCase()).equals("t") ||
				options.get(key.toLowerCase()).equals("true") ||
				options.get(key.toLowerCase()).equals("1"));
	}
}
