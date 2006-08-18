package com.nexopia.adblaster;

import java.util.Vector;

import com.sleepycat.je.DatabaseException;


/*
 * A representation of all data we store for one day, along with some functions to
 * fill with learning data.
 */
public class AdBlasterInstance extends AbstractAdBlasterInstance{
	private Vector<BannerView> views;
	
	public AdBlasterInstance(AbstractAdBlasterUniverse ac) {
		super(ac);
		views = new Vector<BannerView>();
		
	}

	public float totalProfit(){
		float count = 0;
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			if (System.currentTimeMillis() - time > 5000){
				System.out.println(""+ (float)i/(float)getViewCount()*100 + "% done calculating profit.");
				time = System.currentTimeMillis();
			}
			BannerView bv =((BannerView)getView(i)); 
			if (bv.getBanner() != null){
				count += bv.getBanner().getRealPayrate();
			}
		}
		return count;
	}
	
	public void fillInstance(I_Policy pol){
		long time = System.currentTimeMillis();
		for (int i = 0; i < getViewCount(); i++){
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete clearing.");
				time = System.currentTimeMillis();
			}
			getView(i).setBanner(null);
		}
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if ((System.currentTimeMillis() - time) > 5000){
				System.out.println("..." + ((float)i/(float)getViewCount())*100 + "% complete filling.");
				time = System.currentTimeMillis();
			}
			//Banner b = pol.getBestBanner(this, bv);
			Banner b = universe.getRandomBannerMatching(bv, this);
			bv.setBanner(b);
		}
	}

	public void addView(BannerView bv) {
		this.views.add(bv);
		if (bv.getBanner() != null){
			updateMap(bv);
		}
	}
	
/*	public AbstractAdBlasterInstance copy() {
		AdBlasterInstance instance = new AdBlasterInstance(this.universe);
		//xxx:clear out original instance.views
		for (int i = 0; i < this.getViewCount(); i++){
			BannerView bv = getView(i);
			instance.addView(new BannerView(instance, bv.getUserID(), bv.getUser(), bv.getBanner(), bv.getTime()));
		}
		return instance;

	}
*/
	static int index = 0;

	public BannerView randomView(AdBlasterDbUniverse ac, 
			AdBlasterInstance instance) {
		int randomPick = (int) (Math.random()*(universe.getUserCount()-1.0));
		int time = (int) (Math.random()*60*60*24);
		byte size = (byte) (Math.random()*8);
		return new BannerView(instance, index++, ac.getUserByIndex(randomPick), null, time, size, 0);
	}

	public static AdBlasterInstance randomInstance(int num, AdBlasterDbUniverse ac) {
		AdBlasterInstance instance = new AdBlasterInstance(ac);
		long time = System.currentTimeMillis();
		for (int i = 0; i < num; i++){
			BannerView bv = instance.randomView(ac, instance);
			if (System.currentTimeMillis() - time > 5000){
				System.out.println("" + ((float)i/(float)num) * 100 + "%");
				time = System.currentTimeMillis();
			}
			instance.addView(bv);
		}
		return instance;
	}

	public int getViewCount() {
		return this.views.size();
	}

	public void makeMeADatabase(){
		try {			
			
			BannerViewDatabase db = new BannerViewDatabase("test");

			System.out.println("Should be inserting " + this.getViewCount() + " BannerViews.");
			long time = System.currentTimeMillis();
			for (int i=0; i<this.getViewCount(); i++) {
				if (System.currentTimeMillis() - time > 5000){
					System.out.println(""+((float)i)/(float)this.getViewCount() * 100+"% done making database");
					time = System.currentTimeMillis();
				}
				db.insert(this.views.get(i));
			}
			
			db.close();
		} catch (DatabaseException e) {
			System.err.println("DatabaseException: " + e);
			e.printStackTrace();
		}
	}
	
	public int indexOfView(BannerView bv) {
		return views.indexOf(bv);
	}

	protected BannerView getView(int i) {
		return this.views.get(i);
	}

	public void notifyChangeUser(BannerView view) {
	}

	public void notifyChangeTime(BannerView view) {
	}

}
