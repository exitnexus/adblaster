package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.PageValidator1;
import com.nexopia.adblaster.util.PageValidator2;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.Utilities;

public class SimulationServer {

	public static void main(String args[]) throws UnknownHostException, IOException{
		Object arg[] = {};
		PageValidatorFactory factory = 
			new PageValidatorFactory(PageValidator1.class,arg);
		CampaignDB campaignDB = new Campaign.CampaignDB(factory);
		BannerDatabase bannerDB = new BannerDatabase(campaignDB, factory);
		float profit = 0;
		
		FlatFileParser ffp = new FlatFileParser(args[0]);
		String request;
		Socket socket = new Socket("localhost", 8000);
		BufferedWriter writer = new BufferedWriter(
	            new OutputStreamWriter(
	              socket.getOutputStream()));
		BufferedReader reader = new BufferedReader(
		          new InputStreamReader(
		                  socket.getInputStream()));
		int i=0;
	    while ((request = ffp.readLine()) != null){
	    	writer.write(request + "\n");
	    	writer.flush();
		//System.out.println("Written...");
	    	String s = reader.readLine();
		//System.out.println("Read: " + s);
	    	if (i % 1000 == 0){
				//ProgressIndicator.show(i, instanc.getViewCount());
				System.out.println("Result " + i + " : " + s);
			}
			int b = Integer.parseInt(s);
			Banner banner = bannerDB.getBannerByID(b);
			if (banner != null)
				profit += banner.getRealPayrate();
			i++;
		}
	    writer.write("shutdown \n");
	    writer.flush();
	    System.out.println("Total profit: " + profit);

		
	}
}
