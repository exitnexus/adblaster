package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class PageFlatFileDatabase {
	File file;
	File directory;
	FileWriter writer;
	BufferedReader reader;
	
	private HashMap<String, Integer> pages; //track the pages in the database
	private int count;
	
	public PageFlatFileDatabase(String directoryName, boolean append) throws IOException {
		this.init(directoryName, append);
	}
	
	private void init(String directoryName, boolean append) throws IOException {
		directory = new File(directoryName);
		if (!directory.isDirectory()) {
			directory.mkdir();
			if (!directory.isDirectory()) {
				throw new SecurityException(directoryName + " is not a directory and cannot be created as one.");
			}
		}
		file = new File(directory, "page.db");
		writer = new FileWriter(file, append);
		reader = new BufferedReader(new FileReader(file));
		if (append) {
			this.load();
		} else {
			pages = new HashMap<String, Integer>();
		}
	}
	
	public void load() throws IOException {
		pages = new HashMap<String, Integer>();
		String line;
		while ((line = reader.readLine()) != null) {
			String words[] = line.split(" ");
			pages.put(words[1], Integer.valueOf(words[0]));
		}
	}
	
	//returns the id assigned to the page
	public int write(String page) throws IOException {
		if (!pages.containsKey(page)) {
			pages.put(page, Integer.valueOf(count));
			count++;
		}
		return pages.get(page).intValue();
	}
	
	public void sync() throws IOException {
		writer.flush();
	}

	public void close() throws IOException {
		this.sync();
		writer.close();
	}
	
}