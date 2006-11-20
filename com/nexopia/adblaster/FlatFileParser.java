package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FlatFileParser {
	public File f;
	BufferedReader bfr;
	
	FlatFileParser(String fname){
		try {
			f = new File(fname);
			FileReader fr;
			fr = new FileReader(f);
			bfr = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String readLine() throws IOException{
		return bfr.readLine();
	}
}
