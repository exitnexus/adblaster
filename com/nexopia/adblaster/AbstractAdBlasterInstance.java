package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public abstract class AbstractAdBlasterInstance {

	private Vector views;
	AbstractAdBlasterUniverse campaign;
	HashMap bannerCountMap = null;
	
	public AbstractAdBlasterInstance(AbstractAdBlasterUniverse ac){
		bannerCountMap = new HashMap();
		for (int i = 0; i < ac.getBannerCount(); i++){
			bannerCountMap.put(ac.getBanner(i), new Integer(0));
		}
		campaign = ac;
		views = new Vector();
	}
	
	public abstract void fillInstance(AdBlasterPolicy pol);
	
	public boolean isValidBannerForUser(User u, Banner b) {
		return u.interests.hasAllIn(b.interests);
	}
	
	public BannerView randomView(AbstractAdBlasterUniverse ac) {
		// TODO Auto-generated method stub
		User randomPick = campaign.getUser((int) (Math.random()*campaign.getUserCount()));
		int time = (int) (Math.random()*60*60*24);
		return new BannerView(randomPick, null, time);
	}

	public void addView(BannerView bv) {
		this.views.add(bv);
		if (bv.b != null){
			this.bannerCountMap.put(bv.b, new Integer(((Integer)bannerCountMap.get(bv.b)).intValue()+1));
		}
	}

	public void setBannerView(int j, Banner b) {
		BannerView bv = (BannerView)views.get(j);
		if (bv.b != null){
			this.bannerCountMap.put(bv.b, new Integer(((Integer)bannerCountMap.get(bv.b)).intValue()-1));
		}
		if (b != null){
			this.bannerCountMap.put(b, new Integer(((Integer)bannerCountMap.get(b)).intValue()-1));
		}
		
		bv.b = b;
	}

	public Banner getBannerForView(int i){
		return ((BannerView)this.views.get(i)).b;
	}
	public User getUserForView(int i){
		return ((BannerView)this.views.get(i)).u;
	}
	public int getTimeForView(int i){
		return ((BannerView)this.views.get(i)).time;
	}
	
	public static AdBlasterInstance randomInstance(int num, AbstractAdBlasterUniverse ac) {
		AdBlasterInstance instance = new AdBlasterInstance(ac);
		for (int i = 0; i < num; i++){
			BannerView bv = instance.randomView(ac);
			instance.addView(bv);
		}
		return instance;
	}

	public Vector getUnserved() {
		/**Loaded bannervie
		 * For a particular instance, get a list of all of the banners that were not served
		 * that could have made a profit.
		 * @return A vector of banners.
		 */
		Vector unserved = new Vector();
		for (int i = 0; i < this.campaign.getBannerCount(); i++){
			Banner b = (Banner)this.campaign.getBanner(i);
			int count = count(b);
			if (count < b.getMaxHits()){
				unserved.add(new Tuple(b, new Integer(b.getMaxHits() - count)));
			}
		}
		return unserved;

	}

	int count(Banner banner) {
		return ((Integer)bannerCountMap.get(banner)).intValue();
		
	}

	public float totalProfit() {
		float count = 0;
		for (int i = 0; i < views.size(); i++){
			if (((BannerView)views.get(i)).b != null){
				count += ((BannerView)views.get(i)).b.getPayrate();
			}
		}
		return count;
	}

	abstract public AbstractAdBlasterInstance copy();

	public int getViewCount() {
		return this.views.size();
	}

	static Vector getAllBannerViewsThatCanSwapWith(Banner b, AbstractAdBlasterInstance instance) {
		Vector v = new Vector();
		for (Iterator it = instance.views.iterator(); it.hasNext() ;){
			BannerView bv = (BannerView)it.next();
			if (instance.isValidBannerForUser(bv.u, b)){
				v.add(bv);
			}
		}
		return v;
	}

	public Vector depthLimitedDFS(BannerView src, Banner b, AbstractAdBlasterInstance instance, int depth) {
		if (instance.isValidBannerForUser(src.u,b)){
			Vector path = new Vector();
			path.add(src);
			return path;
		}
		if (depth < 0){
			return null;
		}
		Vector v2 = AbstractAdBlasterInstance.getAllBannerViewsThatCanSwapWith(src.b, instance);
		for (Iterator it = v2.iterator(); it.hasNext() ;){
			BannerView next_vert = (BannerView)it.next();
			Vector result = depthLimitedDFS(next_vert, b, instance, depth-1);
			if (result != null && !result.contains(src)){
				result.add(src);
				return result;
			}
		}
		return null;
	}

	public Vector depthLimitedDFS(int j, Banner b, int l) {
		return this.depthLimitedDFS((BannerView)views.get(j), b, this, l);
	}
	
	public void makeMeADatabase(Environment dbEnv){
		try {			

			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			
			BannerViewDatabase db = new BannerViewDatabase();

			Random r = new Random(1);
			System.out.println("Should be inserting " + this.getViewCount() + " BannerViews.");
			for (int i=0; i<this.getViewCount(); i++) {
				db.insert((BannerView)this.views.get(i));
			}
			
			db.close();
		} catch (DatabaseException e) {
			System.err.println("DatabaseException: " + e);
			e.printStackTrace();
		}
	}

	void doSwap(Vector swaps, Banner endBanner) {
		//System.out.println("Swapping " + swaps);
		Iterator it = swaps.iterator();
		BannerView second = (BannerView)it.next();
		this.bannerCountMap.put(second.b, new Integer(((Integer)bannerCountMap.get(second.b)).intValue()-1));
		for (; it.hasNext(); ){
			BannerView first = second;
			second = (BannerView)it.next();
			
			if (isValidBannerForUser(first.u, second.b)){
				first.b = second.b;
			} else {
				System.err.println("Error:  Bad switch.");
			}
			
		}
		second.b = endBanner;
		this.bannerCountMap.put(endBanner, new Integer(((Integer)bannerCountMap.get(endBanner)).intValue()+1));
		
	}


}
