package com.nexopia.adblaster.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Vector;

import com.nexopia.adblaster.struct.User;

public class UserFlatFileWriter {
	private static final int FILE_COUNT = 100; //How many files should the users be split across 
	Vector<FileWriter> files;
	File directory;
	private BitSet users; //track the users that have been written.
	
	
	public UserFlatFileWriter(String directoryName, boolean append) throws IOException {
		this.init(directoryName, append, true);
	}
	
	public UserFlatFileWriter(String directoryName, boolean append, boolean trackUsers) throws IOException {
		this.init(directoryName, append, trackUsers);
	}
	
	private void init(String directoryName, boolean append, boolean trackUsers) throws IOException {
		files = new Vector<FileWriter>();
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			directory.mkdir();
			if (!directory.isDirectory()) {
				throw new SecurityException(directoryName + " is not a directory and cannot be created as one.");
			}
		}
		for (int i=0; i<FILE_COUNT;i++) {
			File f = new File(directory, "user."+i+".db");
			files.add(new FileWriter(f, append));
		}
		if (trackUsers) {
			users = new BitSet();
		} else {
			users = null;
		}
	}
	
	public void write(User u) throws IOException {
		if (u.getID() >= 0 && (users == null || !users.get(u.getID()))) {
			int fileID = u.getID()%FILE_COUNT;
			files.get(fileID).write(u.databaseString());
			users.set(u.getID());
		}
	}
	
	public void sync() throws IOException {
		for (FileWriter file: files) {
			file.flush();
		}
	}

	public void close() throws IOException {
		this.sync();
		for (FileWriter file: files) {
			file.close();
		}
	}
}