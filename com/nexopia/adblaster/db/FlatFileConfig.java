package com.nexopia.adblaster.db;

import java.io.File;
import java.util.Calendar;

public class FlatFileConfig {
	public static final int FILE_COUNT = 100;
	public static File defaultDirectory = null;
	public static int defaultDay = 0;
	private static Calendar calendar = Calendar.getInstance();

	public static File getDefaultDirectory() {
		if (defaultDirectory != null) {
			return defaultDirectory;
		}
		dayBefore(); 
		File directory = null;
		int counter = 0;
		while ((directory == null || !directory.canRead()) && counter < 1000) {
			String folder = String.format("DB_%1$tF", calendar);
			directory = new File(folder);
			dayBefore();
			counter++;
		}
		
		if (!directory.canRead()) {
			throw new RuntimeException("Unable to find any valid directories for default flat file directory.");
		}
		System.out.println("Chosen default directory is: " + directory);
		defaultDirectory = directory;
		defaultDay = Integer.parseInt(directory.getName().substring(3));
		return directory;			
	}
	
	public static int getDefaultDay() {
		if (defaultDirectory == null) {
			getDefaultDirectory();
		}
		return defaultDay;
	}
	
	private static void dayBefore() {
		calendar.add(Calendar.DAY_OF_YEAR, -1);
	}
}
