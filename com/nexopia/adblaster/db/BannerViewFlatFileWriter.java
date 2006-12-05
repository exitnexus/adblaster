package com.nexopia.adblaster.db;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.Vector;

import com.nexopia.adblaster.struct.User;

public class BannerViewFlatFileWriter {
	private static final int FILE_COUNT = 100; //How many files should the bannerviews be split across 
	Vector<FileWriter> files;
	File directory;
	
	
	public BannerViewFlatFileWriter(String directoryName, boolean append) throws IOException {
		this.init(directoryName, append);
	}
	
	private void init(String directoryName, boolean append) throws IOException {
		files = new Vector<FileWriter>();
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			directory.mkdir();
			if (!directory.isDirectory()) {
				throw new SecurityException(directoryName + " is not a directory and cannot be created as one.");
			}
		}
		for (int i=0; i<FILE_COUNT;i++) {
			File f = new File(directory, "bannerview."+i+".db");
			files.add(new FileWriter(f, append));
		}
	}
	
	public void write(int userid, int bannerid, int time, int size, int pageIndex) throws IOException {
		int fileID = Math.abs(userid)%FILE_COUNT;
		files.get(fileID).write(userid + " " + bannerid + " " + time + " " + size + " " + pageIndex + "\n");
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