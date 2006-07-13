package com.nexopia.adblaster;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeTable {
	private Vector<String> invalidRanges;
	private Vector<String> validRanges;
	
	public TimeTable(String times) {
		invalidRanges = new Vector<String>(); 
		validRanges = new Vector<String>(); 
		
		Pattern p = Pattern.compile("[^MmTtWwRrFfYySs\\-\\d,]+");
		Matcher m = p.matcher(times);
		times = m.replaceAll("");
		p = Pattern.compile(",+");
		m = p.matcher(times);
		times = m.replaceAll(",");
		System.out.println(times);
		String[] ranges = times.split(",");
		for (int i=0; i<ranges.length; i++) {
			if (!this.validateRange(ranges[i])) {
				invalidRanges.add(ranges[i]);
				System.out.println(ranges[i] + " is invalid.");
			} else {
				this.validRanges.add(ranges[i]);
			}
		}
		this.parseAllowedTimes();
	}
	
	private void parseAllowedTimes() {
		// TODO Auto-generated method stub
		
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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pattern p = Pattern.compile("[^MmTtWwRrFfYySs\\-\\d,]+");
		System.out.println(p);
		TimeTable t = new TimeTable("M-22, M-F,14Jl)-2  3,,,M,J,T");
	}

}
