package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.IntObjectHashMap;

public class UserFlatFileReader {

	public interface Scanner {
		
	}
	
	private Vector<FileReader> files;
	private File directory;
	private BufferedReader reader;
	private IntObjectHashMap<User> users;
	
	public UserFlatFileReader(String directoryName) throws FileNotFoundException{
		this.init(directoryName);
	}
	
	public void init(String directoryName) throws FileNotFoundException{
		files = new Vector<FileReader>();
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			throw new SecurityException(directoryName + " is not a directory.");
		}
		users = new IntObjectHashMap<User>();
	}
	
	public void load(int fileNumber) throws IOException {
		files.get(fileNumber);
		File f = new File(directory, "user."+fileNumber+".db");
		reader = new BufferedReader(new FileReader(f));
		String userString;
		while ((userString = reader.readLine()) != null) {
			User u = new User(userString);
			users.put(u.getID(), u);
		}
	}
	
	//TODO: Make this method work.
	public User getUser(int uid) {
		return null;
	}
}
