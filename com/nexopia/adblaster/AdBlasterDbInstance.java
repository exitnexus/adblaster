package com.nexopia.adblaster;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbInstance extends AbstractAdBlasterInstance	{
	HashMap swappedViews; //always look for a view here before checking the database
	BannerViewDatabase db;
	
	public AdBlasterDbInstance(AbstractAdBlasterUniverse c){
		super(c);
		try {
			db = new BannerViewDatabase();
		} catch (DatabaseException e) {
			System.err.println("Failed to make a BannerViewDatabase object: " + e);
			e.printStackTrace();
		}
	}

	//TODO Don't know how this should behave, it needs to be dealt with
	public void fillInstance(AdBlasterPolicy pol) {
		System.err.println("Someone write a fillInstance method.");
		System.exit(0);
	}

	/*public AbstractAdBlasterInstance copy() {
		AdBlasterDbInstance instance = new AdBlasterDbInstance(this.universe);
		//xxx:clear out original instance.views
		for (int i = 0; i < this.getViewCount(); i++){
			instance.addView(new BannerView(getUserForView(i), getBannerForView(i), getTimeForView(i)));
		}
		return instance;

	}*/
	
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

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterInstance#setBannerView(int, com.nexopia.adblaster.Banner)
	 */
	public void setBannerView(int j, Banner b) {
		
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterInstance#getBannerForView(int)
	 */
	public Banner getBannerForView(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterInstance#getUserForView(int)
	 */
	public User getUserForView(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterInstance#getTimeForView(int)
	 */
	public int getTimeForView(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterInstance#totalProfit()
	 */
	public float totalProfit() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterInstance#getViewCount()
	 */
	public int getViewCount() {
		// TODO Auto-generated method stub
		return 0;
	}


}
