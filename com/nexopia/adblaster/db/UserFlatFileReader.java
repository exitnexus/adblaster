package com.nexopia.adblaster.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Vector;

public class UserFlatFileReader {

	public interface Scanner {
		
	}
	
	private Vector<FileReader> files;
	private File directory;

	public UserFlatFileReader(String directoryName) throws FileNotFoundException{
		this.init(directoryName);
	}
	
	public void init(String directoryName) throws FileNotFoundException{
		files = new Vector<FileReader>();
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			throw new SecurityException(directoryName + " is not a directory.");
		}
		for (int i=0; i<UserFlatFileWriter.FILE_COUNT;i++) {
			File f = new File(directory, "user."+i+".db");
			files.add(new FileReader(f));
		}
	}
	
	
}
