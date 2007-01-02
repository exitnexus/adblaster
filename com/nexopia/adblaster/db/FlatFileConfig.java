package com.nexopia.adblaster.db;

import java.io.File;
import java.util.Calendar;

public class FlatFileConfig {
	public static final int FILE_COUNT = 100;
	
	public static File getDefaultDirectory() {
		int day = dayBefore(Calendar.DAY_OF_YEAR); 
		File directory = null;
		
		while ((directory != null || !directory.canRead()) && day != Calendar.DAY_OF_YEAR) {
			directory = new File("DB_" + day);
		}
		
		if (!directory.canRead()) {
			throw new RuntimeException("Unable to find any valid directories for default flat file directory.");
		}
		return directory;			
	}
	
	private static int dayBefore(int day) {
		if (day <= 0) {
			return 366;
		} else {
			return day-1;
		}
	}
}
