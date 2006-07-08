package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class TextReader {
	
	public static void main(String args[]){
		try {
			File f = new File("newbanner.log");
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader buf = new BufferedReader(isr);
			String s = "";
			int i = 0;
			while ((s = buf.readLine()) != null){
				if (s.indexOf("get") >= 0){
					i++;
				}
			}
			System.out.println(i);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
