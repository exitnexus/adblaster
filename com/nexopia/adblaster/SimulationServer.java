package com.nexopia.adblaster;

import java.io.File;
import java.io.IOException;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.PageValidator1;
import com.nexopia.adblaster.util.PageValidator2;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.ProgressIndicator;
import com.nexopia.adblaster.util.Utilities;

public class SimulationServer {

	private static File user_dir = null;
	private static File page_dir = null;
	private static File bv_dir = null;
	private static AdBlasterDbUniverse ac;
	private static AdBlasterDbInstance instanc;


	public static void main(String args[]){
		File dataFile = null;
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
		
		if (args.length == 4){
			dataFile = new File(args[3]);
		}

		ac = new AdBlasterDbUniverse(user_dir, page_dir);
		instanc = new AdBlasterDbInstance(ac);

		Object args1[] = {ac.pageDb};
		PageValidatorFactory factory = 
			new PageValidatorFactory(PageValidator2.class, args1);
		CampaignDB cdb = new CampaignDB(factory);
		BannerDatabase bdb = new BannerDatabase(cdb, factory);
		BannerServer banners = new BannerServer(bdb, cdb, 1);
		
		try {
			((AdBlasterDbInstance)instanc).loadNoCount(bv_dir, null);
		} catch (Exception ignored)	{}
		System.out.println("Running...");
		ProgressIndicator.setTitle("Running...");
		ProgressIndicator.show(0, 1);
		for (int i = 0; i < instanc.getViewCount(); i++){
			BannerView bv = instanc.getView(i);
			try {
				String s = banners.receive("get " + bv.getTime() + " " + 
						bv.getSize() + " " + 
						bv.getUserID() + " " + 
						(bv.getUser() != null?
							bv.getUser().getAge() + " " + 
							bv.getUser().getSex() + " " + 
							bv.getUser().getLocation() + " " + 
							bv.getUser().getInterests().toString() + " "
						:
							"0 0 0 0 ") +
						ac.getPageDatabase().getPage(bv.getPage()) +
						" 0 0");
				if (i % 1000 == 0){
					ProgressIndicator.show(i, instanc.getViewCount());
				}
				System.out.println(s);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 

	}
}
