package com.nexopia.adblaster;

import java.io.File;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance	{

	Environment dbEnv;

	public AdBlasterDbInstance(AbstractAdBlasterUniverse c, Environment dbEnv){
		super(c);
		this.dbEnv = dbEnv;
		
	}

	public void fillInstance(AdBlasterPolicy pol) {
		try {
			BannerViewDatabase db;
			db = new BannerViewDatabase(dbEnv);
			BannerViewCursor cursor = db.getCursor(0,0);
			int i = 0;
			BannerView bv = null;
			while((bv = cursor.getNext()) != null){
				i++;
				if (i%100 == 0){
					System.out.println("Loaded bannerview " + i + ": " + bv);
				}
				//bv.b = pol.getBestBanner(this, bv);
				this.views.add(bv);
			}
			cursor.close();
			db.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AbstractAdBlasterInstance copy() {
		AdBlasterDbInstance instance = new AdBlasterDbInstance(this.campaign, this.dbEnv);
		instance.views = new Vector();
		for (int i = 0; i < this.views.size(); i++){
			instance.views.add(((BannerView)this.views.get(i)).copy());
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

		AdBlasterDbUniverse abu = new AdBlasterDbUniverse(dbEnv);
		AdBlasterDbInstance abdbi = new AdBlasterDbInstance(abu, dbEnv);
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(abu);
		abdbi.fillInstance(pol);
		
	}


}
