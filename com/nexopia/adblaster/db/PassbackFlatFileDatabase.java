package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PassbackFlatFileDatabase {
	File file;
	File directory;
	FileWriter writer;
	BufferedReader reader;
	int[] passbackCounts; //This is a sparse array that directly maps a bannerid to its count of passbacks
	private static final int INITIAL_ARRAY_SIZE = 500;
	
	public PassbackFlatFileDatabase(String directoryName, boolean append) throws IOException {
		this.init(directoryName, append);
	}
	
	private void init(String directoryName, boolean append) throws IOException {
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			directory.mkdir();
			if (!directory.isDirectory()) {
				System.exit(-1);
				throw new SecurityException(directory.getName() + " is not a directory and cannot be created as one.");
			}
		}
		passbackCounts = new int[INITIAL_ARRAY_SIZE];
		file = new File(directory, "passback.db");
		writer = new FileWriter(file, append);
		reader = new BufferedReader(new FileReader(file));
		if (append) {
			this.load();
		}
	}
	
	public void load() throws IOException {
		String passbackString;
		while ((passbackString = reader.readLine()) != null) {
			increment(Integer.parseInt(passbackString));
		}
	}

	public void increment(int bannerid) {
		if (passbackCounts.length <= bannerid) {
			int newLength = Math.max(passbackCounts.length*2,bannerid+1);
			int newArray[] = new int[newLength];
			for (int i=0; i<passbackCounts.length; i++) {
				newArray[i] = passbackCounts[i];
			}
		}
		passbackCounts[bannerid]++;
	}
	
	public void write(int passback) throws IOException {
		increment(passback);
		writer.write(passback+"\n");
	}
	
	public void sync() throws IOException {
		writer.flush();
	}

	public void close() throws IOException {
		this.sync();
		writer.close();
		reader.close();
	}
	
	public int getPassbackCount(int bannerid) {
		if (bannerid < passbackCounts.length) {
			return passbackCounts[bannerid];
		} else {
			return 0;
		}
	}

}
