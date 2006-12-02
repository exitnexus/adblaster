package com.nexopia.adblaster;

import java.io.IOException;
import java.net.UnknownHostException;

public class FileScanTest {

	public static void main(String args[]) throws UnknownHostException, IOException{
		
		FlatFileParser ffp = new FlatFileParser(args[0]);
		String request;
		int i=0;
	    while ((request = ffp.readLine()) != null){
	    	/*if (i % 1000 == 0){
				//ProgressIndicator.show(i, instanc.getViewCount());
				System.out.println("Result " + i + " : " + s);
			}*/
			i++;
		}
	    System.out.println("Total lines = " + i);
		
	}
}
