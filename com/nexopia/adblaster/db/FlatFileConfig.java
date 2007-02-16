package com.nexopia.adblaster.db;

import java.io.File;
import java.util.Calendar;

public class FlatFileConfig {
	public static final int FILE_COUNT = 100;
	public static File defaultDirectory = null;
	public static int defaultDay = 0;
	
	public static File getDefaultDirectory() {
		if (defaultDirectory != null) {
			return defaultDirectory;
		}
		Calendar c = Calendar.getInstance();
		int day = dayBefore(c.get(Calendar.DAY_OF_YEAR)); 
		File directory = null;
		
		while ((directory == null || !directory.canRead()) && day != c.get(Calendar.DAY_OF_YEAR)) {
			directory = new File("DB_" + day);
			day = dayBefore(day);
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
	
	private static int dayBefore(int day) {
		if (day <= 0) {
			return 366;
		} else {
			return day-1;
		}
	}
}
