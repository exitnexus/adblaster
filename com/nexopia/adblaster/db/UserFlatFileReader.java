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
	
	public UserFlatFileReader(File directory) throws FileNotFoundException{
		this.directory = directory;
		this.init();
	}

	public UserFlatFileReader(String directoryName) throws FileNotFoundException{
		directory = new File(directoryName);
		this.init();
	}
	
	public void init() throws FileNotFoundException{
		files = new Vector<FileReader>();
		if (!directory.isDirectory()) {
			throw new SecurityException(directory.getName() + " is not a directory.");
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
	
	//Requires the user be in the current loaded set
	public User getUser(int uid) {
		return users.get(uid);
	}
}
