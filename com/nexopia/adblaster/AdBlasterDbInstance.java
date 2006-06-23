package com.nexopia.adblaster;

import java.io.File;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance	{


	public AdBlasterDbInstance(AbstractAdBlasterUniverse c){
		super(c);
	}

	public void fillInstance(AdBlasterPolicy pol) {
		try {
			BannerViewDatabase db;
			db = new BannerViewDatabase();
			BannerViewCursor cursor = db.getCursor(0,0);
			int i = 0;
			BannerView bv = null;
			bv = cursor.getCurrent();
			while(bv  != null){
				i++;
				if (i%100 == 0){
					System.out.println("Loaded bannerview " + i + ": " + bv);
				}
				bv.b = pol.getBestBanner(this, bv);
				this.addView(bv);
				bv = cursor.getNext();
			}
			cursor.close();
			db.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AbstractAdBlasterInstance copy() {
		AdBlasterDbInstance instance = new AdBlasterDbInstance(this.campaign);
		//xxx:clear out original instance.views
		for (int i = 0; i < this.getViewCount(); i++){
			instance.addView(new BannerView(getUserForView(i), getBannerForView(i), getTimeForView(i)));
		}
		return instance;

	}
	
	public static void main(String args[]){
		EnvironmentConfig envConf = new EnvironmentConfig();
		envConf.setAllowCreate(true);

		Environment dbEnv = null;
		try {
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
		} catch (DatabaseException e1) {
			e1.printStackTrace();
		}

		AdBlasterDbUniverse abu = new AdBlasterDbUniverse();
		AdBlasterDbInstance abdbi = new AdBlasterDbInstance(abu);
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(abu);
		abdbi.fillInstance(pol);
		
	}


}
