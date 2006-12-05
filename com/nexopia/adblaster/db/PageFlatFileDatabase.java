package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import com.nexopia.adblaster.util.IntObjectHashMap;

public class PageFlatFileDatabase {
	File file;
	File directory;
	FileWriter writer;
	BufferedReader reader;
	
	private HashMap<String, Integer> pages_reverse; //track the pages in the database
	private IntObjectHashMap<String> pages;
	private int count;
	
	public PageFlatFileDatabase(File directory, boolean append) throws IOException {
		this.directory = directory;
		this.init(append);
	}

	public PageFlatFileDatabase(String directoryName, boolean append) throws IOException {
		directory = new File(directoryName);
		this.init(append);
	}
	
	private void init(boolean append) throws IOException {
		if (!directory.isDirectory()) {
			directory.mkdir();
			if (!directory.isDirectory()) {
				throw new SecurityException(directory.getName() + " is not a directory and cannot be created as one.");
			}
		}
		file = new File(directory, "page.db");
		writer = new FileWriter(file, append);
		reader = new BufferedReader(new FileReader(file));
		pages_reverse = new HashMap<String, Integer>();
		pages = new IntObjectHashMap<String>();
		if (append) {
			this.load();
		}
	}
	
	public void load() throws IOException {
		String line;
		pages_reverse = new HashMap<String, Integer>();
		pages = new IntObjectHashMap<String>();
		while ((line = reader.readLine()) != null) {
			String words[] = line.split(" ");
			pages_reverse.put(words[1], Integer.valueOf(words[0]));
			pages.put(Integer.valueOf(words[0]), words[1]);
		}
	}
	
	//returns the id assigned to the page
	public int write(String page) throws IOException {
		if (!pages_reverse.containsKey(page)) {
			pages_reverse.put(page, Integer.valueOf(count));
			pages.put(count, page);
			count++;
		}
		return pages_reverse.get(page).intValue();
	}
	
	public void sync() throws IOException {
		writer.flush();
	}

	public void close() throws IOException {
		this.sync();
		writer.close();
	}

	public String getPage(int page) {
		return pages.get(page);
	}

	public int getPage(String p) {
		return pages_reverse.get(p);
	}
	
}