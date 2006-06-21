package com.nexopia.adblaster;

import java.io.File;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance 
		implements I_AdBlasterInstance {

	BannerViewDatabase bannerview_db;
	Environment dbEnv;
	
	public AdBlasterDbInstance(AbstractAdBlasterUniverse c){
		super(c);
		try {
			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			dbEnv = new Environment(new File("BerkDBTester.db"), envConf);
			db = new BannerViewDatabase(dbEnv);
			bannerview_db = new BannerViewDatabase(this.dbEnv);
		} catch (DatabaseException dbe) {
			System.err.println("DatabaseException: " + dbe);
		}
		
	}

	public void fillInstance(AdBlasterPolicy pol) {
		try {
			BannerViewCursor cursor = bannerview_db.getCursor(0,0);
			while(cursor.getCurrent() != null){
				BannerView bv = cursor.getCurrent();
				bv.b = pol.getBestBanner(this, bv);
				this.views.add(bv);
				cursor.getNext();
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Vector getUnserved() {
		return null;
	}

	public AbstractAdBlasterInstance copy() {
		AbstractAdBlasterInstance instance = new AdBlasterDbInstance(this.campaign);
		instance.views = new Vector();
		for (int i = 0; i < this.views.size(); i++){
			instance.views.add(((BannerView)this.views.get(i)).copy());
		}
		instance.dbEnv = this.dbEnv;
		instance.db = this.db;
		return instance;

	}
	
	public static void main(String args[]){
		new AdBlasterDbInstance(null);
		
	}


}
