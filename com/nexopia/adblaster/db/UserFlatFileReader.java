package com.nexopia.adblaster.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Vector;

import com.nexopia.adblaster.struct.User;

public class UserFlatFileReader {

	public interface Scanner {
		
	}
	
	private Vector<FileReader> files;
	private File directory;

	public UserFlatFileReader(String directoryName) throws FileNotFoundException{
		directory = new File(directoryName);
		this.init();
	}
	
	public UserFlatFileReader(File directory) throws FileNotFoundException{
		this.directory = directory;
		this.init();
	}

	public void init() throws FileNotFoundException{
		if (!directory.isDirectory()) {
			throw new SecurityException(directory.getName() + " is not a directory.");
		}
		files = new Vector<FileReader>();
		for (int i=0; i<UserFlatFileWriter.FILE_COUNT;i++) {
			File f = new File(directory, "user."+i+".db");
			files.add(new FileReader(f));
		}
	}

	public User getUser(int i) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
