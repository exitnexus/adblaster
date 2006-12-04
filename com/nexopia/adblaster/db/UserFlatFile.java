package com.nexopia.adblaster.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import com.nexopia.adblaster.struct.User;

public class UserFlatFile {
	private static final int FILE_COUNT = 100; //How many files should the users be split across 
	Vector<FileWriter> files;
	File directory;
	public UserFlatFile(String directoryName, boolean append) throws IOException {
		files = new Vector<FileWriter>();
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			directory.mkdir();
			if (!directory.isDirectory()) {
				throw new SecurityException(directoryName + " is not a directory and cannot be created as one.");
			}
		}
		for (int i=0; i<FILE_COUNT;i++) {
			File f = new File(directory, i+".user.db");
			files.add(new FileWriter(f, append));
		}
	}
	
	public void insert(User u) throws IOException {
		int fileID = u.getID()%FILE_COUNT;
		files.get(fileID).write(u.databaseString());
	}
	
	public void sync() throws IOException {
		for (FileWriter file: files) {
			file.flush();
		}
	}	
}