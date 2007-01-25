/**
 * 
 */
package com.nexopia.adblaster.struct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import com.nexopia.adblaster.util.IntIntHashMap;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.Interests;
import com.thoughtworks.xstream.XStream;

public class TypeStat {
	private static XStream xstream = new XStream();
	private static final boolean COMPRESS = true; 
	private static final String GZIP_ENCODING = "ISO8859_1";
	
	public byte[] toXML() {
		xstream.alias("typestat", TypeStat.class);
		xstream.alias("integer", Integer.class);
		if (!COMPRESS) {
			try {
				return xstream.toXML(this).getBytes(GZIP_ENCODING);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			GZIPOutputStream gz = null;
			ByteArrayOutputStream bytes = null;
			try {
				bytes = new ByteArrayOutputStream(30000); 
				gz = new GZIPOutputStream(bytes);
				gz.write(xstream.toXML(this).getBytes());
				gz.finish();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//System.out.println(bytes.toString());
			return bytes.toByteArray();
		}
		
	}
	
	public static int INITIAL_ARRAY_SIZE = 100;
	public int starttime;
	public int total;
	private int[][] agesex;
	private int[] loc;
	private IntIntHashMap interests;
	private int[][] hittimes;
	private HashMap<String, Integer> pages;
	
	public TypeStat() {
		total = 0;
		starttime = (int)(System.currentTimeMillis()/1000);
		loc = new int[INITIAL_ARRAY_SIZE];
		interests = new IntIntHashMap();
		agesex = new int[INITIAL_ARRAY_SIZE][3];
		hittimes = new int[7][24];
		pages = new HashMap<String, Integer>();
	}
	
	public void hit(int age, int sex, int loc, Interests interests, String page, int time) {
		total++;
		Calendar c = Calendar.getInstance();
		c.setTime(new Timestamp((long)time*1000));
		hittimes[c.get(Calendar.DAY_OF_WEEK)-1][c.get(Calendar.HOUR_OF_DAY)]++;
		if (age < 100 && age >= 0) {
			agesex[age][sex]++;
		}
		this.loc = expandArray(this.loc, loc);
		this.loc[loc]++;
		for(int i=interests.getChecked().nextSetBit(0); i>=0; i=interests.getChecked().nextSetBit(i+1)) { 
			this.interests.put(i, this.interests.get(i)+1);
		}
		Integer pageviews = pages.get(page);
		if (pageviews == null) {
			pageviews = Integer.valueOf(1);
		} else {
			int i = pageviews.intValue();
			i++;
			pageviews.free();
			pageviews = Integer.valueOf(i);
		}
		pages.put(page, pageviews);
			
	}

	private int[] expandArray(int[] array, int new_val) {
		if (array.length > new_val) {
			return array;
		} else {
			int new_size = Math.max(array.length*2, new_val+1);
			int[] new_array = new int[new_size];
			for (int i=0; i<array.length; i++) {
				new_array[i] = array[i];
			}
			return new_array;
		}
	}

	public int memory_usage() {
		int bytes = 26;
		for (int[] sex: agesex) {
			bytes += sex.length*4;
		}
		bytes += interests.size()*5;//guestimate that we are about 80% efficient with our hash
		for (int[] times: hittimes) {
			bytes += times.length*4;
		}
		bytes += pages.size()*5;
		return bytes;
	}
}