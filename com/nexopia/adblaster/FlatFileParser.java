package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.nexopia.adblaster.db.BannerViewFlatFileReader;
import com.nexopia.adblaster.db.UserFlatFileReader;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.db.FlatFileConfig;

public class FlatFileParser {
	public File f;
	int current_file;
	int current_index;
	BannerViewFlatFileReader bvffr;
	UserFlatFileReader uffr;
	
	FlatFileParser(String fname){
		try {
			bvffr = new BannerViewFlatFileReader(fname);
			uffr = new UserFlatFileReader(fname); 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		current_file = -1;
		current_index = -1;
		
	}
	
	void loadNext(){
		current_file++;
		if (current_file > FlatFileConfig.FILE_COUNT)
			return;
		try {
			System.out.println("Loading...");
			bvffr.load(current_file);
			uffr.load(current_file);
			System.out.println("Done.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		current_index = 0;
	}
	
	String readLine() throws IOException{
		if (current_index == -1 || current_index >= bvffr.getCurrentBannerViews().size()){
			loadNext();
		}
		
		if (current_file > FlatFileConfig.FILE_COUNT)
			return null;

		BannerView bv = bvffr.getCurrentBannerViews().get(current_index);
		User u = uffr.getUser(bv.getUserID());
		
		current_index++;
		String s = "get " + bv.getTime() + " " + bv.getSize() + " " + bv.getUserID() + 
			" " + u.getAge() + " " + u.getSex() + " " + u.getLocation() + " " + 
			u.getInterests()+ " " + bv.getPage() + " " + bv.getPassback() + " " + "0";  
//		System.out.println(s);
		return s;
	}
}
