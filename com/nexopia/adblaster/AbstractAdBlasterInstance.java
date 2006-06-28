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
		boolean age = doesAgeMatch(bv, b);
		if (age){
			boolean sex = b.sexes.isEmpty() || b.sexes.contains(new Integer(bv.getUser().sex));
			if (sex){
				boolean interests = bv.getUser().interests.hasAnyIn(b.interests);
				if (interests){
					boolean timerange = true;
					//boolean timerange = this.nearestWithinTimeRange(b, bv);
						return interests && timerange && age && sex;
				}
			}
		}
		return false;
	}


	private boolean doesAgeMatch(BannerView bv, Banner b) {
		if (b.ages.contains(new Integer(0))){
			return !(b.ages.contains(new Integer(bv.getUser().age)));
		} else {
			return b.ages.isEmpty() || b.ages.contains(new Integer(bv.getUser().age));
		}
	}

	private boolean nearestWithinTimeRange(Banner b, BannerView bv) {
		//return true;
		//XXX: should tell us if we've overrun the interval
		// Doesn't work right now because we need more bannerview data
		
		
		if (((Integer)bannerCountMap.get(b)).intValue()+1 >= b.getViewsperuser()){
			Vector range = scan(b.getViewsperuser(), b.getViewsperuser(), b, bv);
			
			for (int i = 0; (i + b.getViewsperuser()) < range.size(); i++){
				//int index1 = ((Integer)range.get(i)).intValue();
				//int index2 = ((Integer)range.get(i + b.getViewsperuser())).intValue();
				BannerView first = (BannerView) range.get(i);
				BannerView last = (BannerView) range.get(i+b.getViewsperuser());
				if (last.getTime() - first.getTime() < b.getLimitbyperiod()){
					return false;
				}
			}
		}
		return true;
		
	}


	private Vector getAllMatching(Banner b, User u, int time, int range){
		Vector vec = new Vector();
		for (int i = 0; i < this.getViewCount(); i++){
			BannerView bv = getView(i);
			if (bv.getUser() == u && bv.getTime() > time - range && bv.getTime() < time + range){
				vec.add(bv);
			}
		}
		return vec;
		
	}
	
	private Vector scan(int before, int after, Banner b, BannerView bv) {
		/*Vector matches = new Vector();
		
		
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
		*/	
		Vector vec = getAllMatching(b, bv.getUser(), bv.getTime(), b.getLimitbyperiod() );
		vec.add(bv);
		//return orderBannersByTime(vec);
		return vec;
	}

	private Vector orderBannersByTime(Vector input) {
		Vector vec = new Vector();
		int bestMatch = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < input.size(); j++){
			BannerView bv = (BannerView) input.get(j);
			int score = bv.getTime();
			int i = 0;
			int score2 = Integer.MIN_VALUE;
			while (i < vec.size() && score2 < score){
				BannerView b2 = (BannerView) vec.get(i);
				score2 = b2.getTime();
				i++;
			}
			vec.insertElementAt(bv, i);
			
		}
		return vec;
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
		/**Loaded bannerview
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
				System.err.println("Error here: null banners in the list?");
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
