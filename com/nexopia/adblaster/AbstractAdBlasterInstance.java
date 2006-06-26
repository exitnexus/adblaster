package com.nexopia.adblaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public abstract class AbstractAdBlasterInstance {

	AbstractAdBlasterUniverse universe;
	HashMap bannerCountMap = null;
	
	public AbstractAdBlasterInstance(AbstractAdBlasterUniverse ac){
		bannerCountMap = new HashMap();
		for (int i = 0; i < ac.getBannerCount(); i++){
			bannerCountMap.put(ac.getBannerByIndex(i), new Integer(0));
		}
		universe = ac;
	}
	
	
	public boolean isValidBannerForView(BannerView bv, Banner b) {
		return bv.getBanner().interests.hasAllIn(b.interests) && 
			this.nearestWithinTimeRange(b, bv);
	}

	public boolean isValidBannerForView(Banner b, int i) {
		return getView(i).getUser().interests.hasAllIn(b.interests) && 
			this.nearestWithinTimeRange(b, getView(i));
	}

	private boolean nearestWithinTimeRange(Banner b, BannerView bv) {
		return true;
		
		//XXX: should tell us if we've overrun the interval
		/*
		 Doesn't work right now because we need more bannerview data
		
		
		int cursor = this.indexOfView(bv);
		if (((Integer)bannerCountMap.get(b)).intValue()+1 >= b.getViewsperuser()){
			Vector range = scan(b.getViewsperuser(), b.getViewsperuser(), b, cursor);
			
			for (int i = 0; i < b.getViewsperuser() && i < range.size(); i++){
				int index1 = ((Integer)range.get(i)).intValue();
				int index2 = ((Integer)range.get(i + b.getViewsperuser())).intValue();
				BannerView first = getView(index1);
				BannerView last = getView(index2);
				if (last.getTime() - first.getTime() < b.getLimitbyperiod()){
					return false;
				}
			}
		}
		return true;
		*/
	}


	private Vector scan(int before, int after, Banner b, int startIndex) {
		Vector matches = new Vector();
		
		int count = 0;
		int index = startIndex-1;
		while (count < before && index > 0){
			BannerView bv = (BannerView)getView(index);
			if (bv.getBanner() == b){
				matches.add(new Integer(index));
				count++;
			}
			index -= 1;
		}
		
		count = 0;
		index = startIndex+1;
		while (count < after && index < getViewCount()){
			BannerView bv = (BannerView)getView(index);
			if (bv.getBanner() == b){
				matches.add(new Integer(index));
				count++;
			}
			index += 1;
		}
		return matches;
	}

	/*
	public void addView(BannerView bv) {
		this.views.add(bv);
		if (bv.b != null){
			Integer i = (Integer)bannerCountMap.get(bv.b);
			this.bannerCountMap.put(bv.b, new Integer(i.intValue()+1));
		}
	}*/
	
	
	public Vector getUnserved() {
		/**Loaded bannervie
		 * For a particular instance, get a list of all of the banners that were not served
		 * that could have made a profit.
		 * @return A vector of banners.
		 */
		Vector unserved = new Vector();
		Collection banners = this.universe.getBanners();
		Banner b = null;
		for (Iterator i = banners.iterator(); i.hasNext(); ){
			b = (Banner)i.next();
			if (b == null){
				System.err.println("Another error here: null banners in the list?");
			}  else {
				int count = count(b);
				if (count < b.getMaxHits()){
					unserved.add(new Tuple(b, new Integer(b.getMaxHits() - count)));
				}
			}
		}
		return unserved;
	}

	int count(Banner banner) {
		return ((Integer)bannerCountMap.get(banner)).intValue();
		
	}

	public float totalProfit() {
		float count = 0;
		for (int i = 0; i < getViewCount(); i++){
			if (((BannerView)getView(i)).getBanner() != null){
				count += ((BannerView)getView(i)).getBanner().getPayrate();
			}
		}
		return count;
	}

	/*//These functions need to be redesigned based on new indexes plan*/ 
	Vector getAllBannerViewsThatCanSwapWith(Banner b) {
		Vector v = new Vector();
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if (isValidBannerForView(bv, b)){
				v.add(bv);
			}
		}
		return v;
	}

	public Vector depthLimitedDFS(BannerView src, Banner b, AbstractAdBlasterInstance instance, int depth) {
		if (instance.isValidBannerForView(src,b)){
			Vector path = new Vector();
			path.add(src);
			return path;
		}
		if (depth < 0){
			return null;
		}
		Vector v2 = instance.getAllBannerViewsThatCanSwapWith(src.getBanner());
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
		return this.depthLimitedDFS(getView(j), b, this, l);
	}
	/************************************************************/
	

	void doSwap(Vector swaps, Banner endBanner) {
		//System.out.println("Swapping " + swaps);
		Iterator it = swaps.iterator();
		BannerView second = (BannerView)it.next();
		this.bannerCountMap.put(second.getBanner(), 
				new Integer(((Integer)bannerCountMap.get(second.getBanner())).intValue()-1));
		for (; it.hasNext(); ){
			BannerView first = second;
			second = (BannerView)it.next();
			
			if (isValidBannerForView(first, second.getBanner())){
				first.setBanner(second.getBanner());
			} else {
				System.err.println("Error:  Bad switch.");
			}
			
		}
		second.setBanner(endBanner);
		this.bannerCountMap.put(endBanner, new Integer(((Integer)bannerCountMap.get(endBanner)).intValue()+1));
		
	}

	/**
	 * NotifyChange must be called BEFORE the actual change occurs.
	 * @param bv
	 * @param b
	 */
	protected void notifyChange(BannerView bv, Banner b){
		if (bv.getBanner() != null){
			this.bannerCountMap.put(bv.getBanner(), 
					new Integer(((Integer)bannerCountMap.get(bv.getBanner())).intValue()-1));
		}
		if (b != null){
			this.bannerCountMap.put(b, 
					new Integer(((Integer)bannerCountMap.get(b)).intValue()+1));
		}
	}

	public abstract void notifyChangeUser(BannerView view);
	public abstract void notifyChangeTime(BannerView view);
	public abstract void fillInstance(AdBlasterPolicy pol);
	protected abstract BannerView getView(int i);
	public abstract int getViewCount();

}
