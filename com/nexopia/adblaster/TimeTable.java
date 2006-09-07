package com.nexopia.adblaster;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeTable {
	private Vector<String> invalidRanges;
	private Vector<String> validRanges;
	boolean allowed[][] = new boolean[7][24];
	Calendar c = Calendar.getInstance();
	
	public TimeTable(String times) {
		invalidRanges = new Vector<String>(); 
		validRanges = new Vector<String>(); 
		if (times.equals("")){
			validRanges.add("M-F0-23");
		}
		
		Pattern p = Pattern.compile("[^MmTtWwRrFfYySs\\-\\d,]+");
		Matcher m = p.matcher(times);
		times = m.replaceAll("");
		p = Pattern.compile(",+");
		m = p.matcher(times);
		times = m.replaceAll(",");
		String[] ranges = times.split(",");
		for (int i=0; i<ranges.length; i++) {
			if (!this.validateRange(ranges[i])) {
				invalidRanges.add(ranges[i]);
				System.out.println(ranges[i] + " is invalid.");
				throw new UnsupportedOperationException("Invalid time range.");
			} else {
				this.validRanges.add(ranges[i]);
			}
		}
		this.parseAllowedTimes();
	}
	
	public Object clone(){
		TimeTable t = new TimeTable("");
		t.allowed = this.allowed.clone();
		return t;
	}
	
	private void parseAllowedTimes() {
		for (int j = 0; j < validRanges.size(); j++){
			String val = validRanges.get(j);

			Pattern weekday = Pattern.compile("[MmTtWwRrFfYySs]");
			Pattern digit = Pattern.compile("\\d+");
			Pattern dash = Pattern.compile("-");
			Matcher weekdayMatcher = weekday.matcher(val);
			Matcher digitMatcher = digit.matcher(val);
			Matcher dashMatcher = dash.matcher(val);
			
			int digit1=-1;
			int digit2=-1;
			if (digitMatcher.find()){
				digit1 = Integer.parseInt(val.substring(digitMatcher.start(), digitMatcher.end()));
				if (digitMatcher.find()){
					digit2 = Integer.parseInt(val.substring(digitMatcher.start(), digitMatcher.end()));
				}
			}
			if (digit1 == -1 && digit2 == -1){
				digit1 = 0; 
				digit2 = 23;
			} else if (digit2 == -1){
				digit2 = digit1;
			}
			
			
			boolean range = false;
			while (weekdayMatcher.find()){
				String s1= "";
				String s2= "";
				s1 = val.substring(weekdayMatcher.start(), weekdayMatcher.end());
				if (val.substring(weekdayMatcher.end(),weekdayMatcher.end()+1).equals("-")){
					range = true;
					if (weekdayMatcher.find()){
						s2 = val.substring(weekdayMatcher.start(), weekdayMatcher.end());
					}
				}
				if (s1.equals("") && s2.equals("")){
					s1 = "M";
					s2 = "S";
				} else if (s2 == ""){
					s2 = s1;
				}
				int day1 = getDayNumber(s1.charAt(0));
				int day2 = getDayNumber(s2.charAt(0));
				int dayrange = day2 - day1;
				int hourrange = digit2 - digit1;
				for (int day = 0; day <= dayrange; day++){
					for (int hour = 0; hour <= hourrange; hour++){
						allowed[day1+day % 7][digit1+hour % 24] = true;
					}
				}
			}
			
			/*for (int i=0; i<val.length(); i++) {
				weekdayMatcher.region(i,i+1);
				digitMatcher.region(i,i+1);
				if (weekdayMatcher.matches()) {
				
				}
			}*/
		}
		
	}

	private int getDayNumber(char c) {
		switch (c){
			case 'M':
			case 'm':
				return 0;
			case 'T':
			case 't':
				return 1;
			case 'W':
			case 'w':
				return 2;
			case 'R':
			case 'r':
				return 3;
			case 'F':
			case 'f':
				return 4;
			case 'Y':
			case 'y':
				return 5;
			case 'S':
			case 's':
				return 6;
			default:
				return -1;
		}
	}

	private boolean validateRange(String val) {
		Pattern weekday = Pattern.compile("[MmTtWwRrFfYySs]");
		Pattern digit = Pattern.compile("\\d");
		boolean invalid = false;
		boolean inRange = false;
		int lastHour = -1;
		char lastDay = '\0';
		int currentHour = -1;
		Matcher weekdayMatcher = weekday.matcher(val);
		Matcher digitMatcher = digit.matcher(val);
		for (int i=0; i<val.length(); i++) {
			weekdayMatcher.region(i,i+1);
			digitMatcher.region(i,i+1);
			if (weekdayMatcher.matches()) {
				if (lastHour >= 0) { //days should always come before hours
					invalid = true;
					break;
				}
				if (lastDay < 0 || !inRange) {
					lastDay = val.charAt(i);
				} else { //$lastDay && $inRange && !$lastHour eg. M-?
					lastDay = val.charAt(i);
					inRange = false;
				}
			} else if (val.substring(i,i+1).equals("-")) {
				if (!(lastHour >= 0 || lastDay != '\0') || inRange) { //We need to have something preceding the hyphen and it can't be a hyphen
					invalid = true;
					break;
				} else {
					inRange = true;
				}
			} else if (digitMatcher.matches()) {
				int start = i;
				try {
					digitMatcher.region(i+1,i+2);
					while (i < val.length()-1 && digitMatcher.matches()) {
						i++;
						digitMatcher.region(i+1,i+2);
					}
				} catch (IndexOutOfBoundsException e) {
					//do nothing here, just move on
				}
				currentHour = Integer.parseInt(val.substring(start,i+1));
				if (currentHour > 23) { //non-existant hour
					invalid = true;
					break;
				}
				if (lastDay == '\0' && lastHour < 0 && !inRange) { //start of input, hours given are for all days
					lastHour = currentHour;
				} else if (lastDay != '\0' && lastHour < 0 && !inRange) {
					lastHour = currentHour;
				} else if (lastHour >= 0  && inRange) {
					lastHour = currentHour;
					inRange = false;
				} else if (lastHour < 0 && inRange) { //M-23 is not a valid range
					invalid = true;
					break;
				} else {//($lastHour && !$inRange) this should never happen but is here for completeness
					invalid = true;
					break;
				}
			} else { //this should never happen but is here for completeness
				invalid = true;
				break;
			}
		}
		return !invalid;
	}

	public boolean getValid(long time){
		Date date = new Date(time);
		c.setTime(date);
		int h = c.get(Calendar.HOUR_OF_DAY);
		int d = (c.get(Calendar.DAY_OF_WEEK) - 2) % 7;
		return this.allowed[d][h];
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pattern p = Pattern.compile("[^MmTtWwRrFfYySs\\-\\d,]+");
		System.out.println(p);
		//TimeTable t = new TimeTable("M-22, F,14Jl)-2  3,,,M,J,T");
		TimeTable t = new TimeTable("MF0-1,MWF3-4,W-Y6-18");
		for(int i = 0; i < 7; i++){
			for (int j = 0; j < 24; j++){
				System.out.print(t.allowed[i][j]?1:0);
			}
			System.out.println();
		}
		t.getValid(System.currentTimeMillis());
	}

}
