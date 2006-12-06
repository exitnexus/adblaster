package com.nexopia.adblaster;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.PageValidator2;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.Utilities;
import com.sleepycat.je.DatabaseException;

public class DatabaseDumper {
	private static File user_dir = null;
	private static File page_dir = null;
	private static File bv_dir = null;
	private static AdBlasterDbUniverse ac;
	private static AdBlasterDbInstance instanc;


	/*
	 * Args: 
	 * 1st: bannerview database path
	 * 2nd: user database path
	 * 3rd: page databse path
	 * 4th: output file
	 */
	public static void main(String args[]) throws IOException{
		//File dataFile = null;
		if (args.length >= 3){
			System.out.println("Running with selected directories.");
			bv_dir = new File(args[0]);
			user_dir = new File(args[1]);
			page_dir = new File(args[2]);
		} else {
			bv_dir = Utilities.getDir("BannerView");
			user_dir = Utilities.getDir("User");
			page_dir = Utilities.getDir("Page");
		}
		
		//if (args.length == 4){
			//dataFile = new File(args[3]);
		//}

		PageFlatFileDatabase pageDb = null;
		pageDb = new PageFlatFileDatabase(page_dir, false);
		Object args2[] = {pageDb};
		PageValidatorFactory factory = 
			new PageValidatorFactory(PageValidator2.class,args2);

		ac = new AdBlasterDbUniverse(factory);
		instanc = new AdBlasterDbInstance(ac);

		try {
			((AdBlasterDbInstance)instanc).loadNoCount(bv_dir, user_dir, null);
		} catch (Exception ignored)	{}
		System.out.println("Running...");
		//ProgressIndicator.setTitle("Running...");
		//ProgressIndicator.show(0, 1);
		FileWriter fw;
		fw = new FileWriter(new File(args[3]));
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < instanc.getViewCount(); i++){
			BannerView bv = instanc.getViews().elementAt(i);
			try {
				String request = "get " + bv.getTime() + " " + 
						bv.getSize() + " " + 
						bv.getUserID() + " " + 
						(instanc.getUser(bv.getUserID()) != null?
								instanc.getUser(bv.getUserID()).getAge() + " " + 
								instanc.getUser(bv.getUserID()).getSex() + " " + 
								instanc.getUser(bv.getUserID()).getLocation() + " " + 
								instanc.getUser(bv.getUserID()).getInterests().toString() + " "
						:
							"0 0 0 0 ") +
						pageDb.getPage(bv.getPage()) +
						" 0 0\n";
				bw.write(request);
				if (i % 1000 == 0){
					//ProgressIndicator.show(i, instanc.getViewCount());
					System.out.println("Result " + i + " : " + request);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		try {
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Done.");

	}

}
