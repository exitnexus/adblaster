package com.nexopia.adblaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public abstract class AbstractAdBlasterInstance {

	AbstractAdBlasterUniverse universe;
	private HashMap <Banner, Integer>bannerCountMap = null;
	private HashMap <Campaign, Integer>campaignCountMap = null;
	static Integer pool[];
	static {
		pool = new Integer[200];
		for (int i = 0; i < 200; i++){
			pool[i] = new Integer(i);
		}
	}
	
	public AbstractAdBlasterInstance(AbstractAdBlasterUniverse ac){
		bannerCountMap = new HashMap<Banner, Integer>();
		for (int i = 0; i < ac.getBannerCount(); i++){
			bannerCountMap.put(ac.getBannerByIndex(i), new Integer(0));
		}
		campaignCountMap = new HashMap<Campaign, Integer>();
		for (int i = 0; i < ac.getCampaignCount(); i++){
			campaignCountMap.put(ac.getCampaignByIndex(i), new Integer(0));
		}
		universe = ac;
	}
	
	
	public boolean isValidBannerForViewWithComments(BannerView bv, Banner b, StringBuffer buf) {
		if (bv.getUserID() == -521590000 && b.id == 57){
			System.out.println("ooga");
		}
		boolean b1 = (b == null);
		boolean b2 = bv.getSize() == b.getSize();
		boolean b3 = b.validUser(bv.getUser());
		boolean b4 = b.validPage(bv.getPage());
		boolean b5 = this.nearestWithinTimeRange(b, bv);
		buf.append(b1 + ":"); 
		buf.append(b2 + ":"); 
		buf.append(b3 + ":"); 
		buf.append(b4 + ":"); 
		buf.append(b5 + ":"); 
		return b1 || (b2 && b3 && b4 && b5);
	}

	public boolean isValidBannerForView(BannerView bv, Banner b) {
		return (b == null) || (bv.getSize() == b.getSize() &&
				b.validUser(bv.getUser()) &&
				b.validPage(bv.getPage()) &&
				this.nearestWithinTimeRange(b, bv));
	}


	Integer zero = new Integer(0);
	
	/*Poorly named... detects whether a bannerview satisfies time period per user*/
	private boolean nearestWithinTimeRange(Banner b, BannerView bv) {
		Vector<BannerView> range = null;
		if (b.getViewsperuser() != 0){
			range = scan(b, bv);
			for (int i = 0; (i + b.getViewsperuser()) < range.size(); i++) {
				BannerView first = range.get(i);
				BannerView last = range.get(i + b.getViewsperuser());
				if (last.getTime() - first.getTime() <= b.getLimitbyperiod()) {
					return false;
				}
			}
		}
		if (b.campaign.getViewsPerUser() != 0) {
			Campaign c = b.getCampaign();
			if (range == null){
				range = scan(c, bv);
			}
			for (int i = 0; (i + c.getViewsPerUser()) < range.size(); i++) {
				BannerView first = range.get(i);
				BannerView last = range.get(i + c.getViewsPerUser());
				if (last.getTime() - first.getTime() <= c.getLimitByPeriod()) {
					return false;
				}
			}
		}
		return true;
		
	}

	/*
	 * private boolean nearestBeforeTimeRange(Banner b, BannerView bv) { Vector<BannerView>
	 * range = scan(b, bv);
	 * 
	 * for (int i = 0; (i + b.getViewsperuser()) < range.size(); i++){
	 * BannerView first = (BannerView) range.get(i); BannerView last =
	 * (BannerView) range.get(i+b.getViewsperuser()); if (last.getTime() -
	 * first.getTime() <= b.getLimitbyperiod()){ return false; } } return true; }
	 */



	private Vector<BannerView> getAllMatching(Vector<BannerView> vec, Banner b, int time, int period) {
		Vector<BannerView> vec2 = new Vector<BannerView>();
		for (int i = 0; i < vec.size(); i++){
			BannerView bv = vec.get(i);
			if (bv.getTime() > time - period && bv.getTime() < time + period && bv.getBanner() == b){
				vec2.add(bv);
			}
		}
		return vec2;
	}

/*	private HashMap<User, HashMap<Banner, Vector<BannerView>>> getAllMatching(){
		HashMap<User, HashMap<Banner, Vector<BannerView>>> userHash = new HashMap<User, HashMap<Banner, Vector<BannerView>>>();
		for (int i = 0; i < this.getViewCount(); i++){
			BannerView bv = getView(i);
			User u = bv.getUser();
			HashMap<Banner,Vector<BannerView>> bannerHash = userHash.get(u);
			if (bannerHash == null){
				bannerHash = new HashMap<Banner, Vector<BannerView>>();
				userHash.put(u, bannerHash);
			}
			Vector<BannerView> vec = bannerHash.get(bv.getBanner());
			if (vec == null) {
				vec = new Vector<BannerView>();
				bannerHash.put(bv.getBanner(), vec);
			}
			vec.add(bv);
		}
		return userHash;
	
	}
*/
	private HashMap<User, Vector<BannerView>> getAllMatching() {
		HashMap<User, Vector<BannerView>> userHash = new HashMap<User, Vector<BannerView>>();
		for (int i = 0; i < this.getViewCount(); i++) {
			BannerView bv = getView(i);
			User u = bv.getUser();
			Vector<BannerView> vec = userHash.get(u);
			if (vec == null) {
				vec = new Vector<BannerView>();
				userHash.put(u, vec);
			}
			vec.add(bv);
		}
		return userHash;
	}
	
	//returns a time sorted vector of bannerviews that are from the same user and banner that could 
	//potentially have frequency conflicts the vector also contains @param bv.
	private Vector<BannerView> scan(Banner b, BannerView bv) {
		if (allMatching == null){
			System.out.println("Building map: " + this.getClass());
			allMatching = getAllMatching();
		}
		User user = bv.getUser();
		Vector<BannerView>hb = allMatching.get(user);
		
		Vector <BannerView> vec = (Vector<BannerView>) getAllMatching(hb, b, bv.getTime(), b.getLimitbyperiod());
		
		//put bv in the list as well
		vec.add(bv);
		//return orderBannersByTime(vec);
		return vec;
	}

	//returns a time sorted vector of bannerviews that are from the same user and campaign that could 
	//potentially have frequency conflicts the vector also contains @param bv.
	private Vector<BannerView> scan(Campaign c, BannerView bv) {
		if (allMatching == null){
			System.out.println("Building map." + this.getClass());
			allMatching = getAllMatching();
		}
		User user = bv.getUser();
		Vector<BannerView>hb = allMatching.get(user);
		
		Vector <BannerView> vec = getAllMatching(hb, c, bv.getTime(), c.getLimitByPeriod());
		
		//put bv in the list as well
		vec.add(bv);
		//return orderBannersByTime(vec);
		return vec;
	}
	
	private Vector<BannerView> getAllMatching(Vector<BannerView> vec, Campaign c, int time, int period) {
		Vector<BannerView> vec2 = new Vector<BannerView>();
		for (int i = 0; i < vec.size(); i++){
			BannerView bv = vec.get(i);
			if (bv.getTime() > time - period && bv.getTime() < time + period && bv.getBanner() != null && bv.getBanner().getCampaign() == c){
				vec2.add(bv);
			}
		}
		return vec2;
	}


	HashMap <User, Vector<BannerView>> allMatching = null;
	
	private Vector<BannerView> orderBannersByTime(Vector input) {
		Vector<BannerView> vec = new Vector<BannerView>();
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
		if (input.size() != vec.size()){
			
			throw new UnsupportedOperationException();
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
	
	
	public Vector<Banner> getUnserved() {
		/**Loaded bannerview
		 * For a particular instance, get a list of all of the banners that were not served
		 * that could have made a profit.
		 * @return A vector of banners.
		 */
		Vector<Banner> unserved = new Vector<Banner>();
		Collection banners = this.universe.getBanners();
		Banner b = null;
		for (Iterator i = banners.iterator(); i.hasNext(); ){
			b = (Banner)i.next();
			if (b == null){
				System.err.println("Error here: null banners in the list?");
			}  else {
				if (bannerCount(b) < b.getViewsperday() && campaignCount(b) < b.getCampaign().getViewsperuser()){
					unserved.add(b);
				}
			}
		}
		return unserved;
	}

	int bannerCount(Banner banner) {
		return bannerCountMap.get(banner).intValue();
	}
	int campaignCount(Banner banner) {
		return campaignCountMap.get(banner.getCampaign()).intValue();
	}
	
	synchronized protected void updateMap(BannerView bv) {
		Integer count = this.bannerCountMap.get(bv.getBanner());
		this.bannerCountMap.put(bv.getBanner(), Integer.valueOf(count.intValue() + 1));

		count = this.campaignCountMap.get(bv.getBanner().getCampaign());
		this.campaignCountMap.put(bv.getBanner().getCampaign(), Integer.valueOf(count.intValue() + 1));

	}

	public float totalProfit() {
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

	/*//These functions need to be redesigned based on new indexes plan*/ 
	Vector<BannerView> getAllBannerViewsThatCanSwapWith(Banner b) {
		Vector<BannerView> v = new Vector<BannerView>();
		for (int i = 0; i < getViewCount(); i++){
			BannerView bv = getView(i);
			if (isValidBannerForView(bv, b)){
				v.add(bv);
			}
		}
		return v;
	}

	public Vector<BannerView> depthLimitedDFS(BannerView src, Banner b, int depth) {
		if (isValidBannerForView(src,b)){
			Vector<BannerView> path = new Vector<BannerView>();
			path.add(src);
			return path;
		}
		if (depth < 0){
			return null;
		}
		Vector<BannerView> v2 = getAllBannerViewsThatCanSwapWith(src.getBanner());
		for (Iterator it = v2.iterator(); it.hasNext() ;){
			BannerView next_vert = (BannerView)it.next();
			Vector<BannerView> result = depthLimitedDFS(next_vert, b, depth-1);
			if (result != null && !result.contains(src)){
				result.add(src);
				return result;
			}
		}
		return null;
	}

	synchronized void doSwap(Vector swaps, Banner endBanner) {
		//System.out.println("Swapping " + swaps);
		Iterator it = swaps.iterator();
		BannerView second = (BannerView)it.next();
		this.bannerCountMap.put(second.getBanner(), 
				new Integer(((Integer)bannerCountMap.get(second.getBanner())).intValue()-1));
		this.campaignCountMap.put(second.getBanner().getCampaign(), 
				new Integer(((Integer)campaignCountMap.get(second.getBanner().getCampaign())).intValue()-1));
		
		if (bannerCountMap.get(second.getBanner()).intValue() < 0){
			throw new UnsupportedOperationException();
		}
		if (campaignCountMap.get(second.getBanner().getCampaign()).intValue() < 0){
			throw new UnsupportedOperationException();
		}
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
		this.bannerCountMap.put(endBanner, new Integer((bannerCountMap.get(endBanner)).intValue()+1));
		this.campaignCountMap.put(endBanner.getCampaign(), 
				new Integer(campaignCountMap.get(endBanner.getCampaign()).intValue()+1));
		
	}

	/**
	 * NotifyChange must be called BEFORE the actual change occurs.
	 * @param bv
	 * @param b
	 */
	synchronized protected void notifyChange(BannerView bv, Banner b){
		if (bv.getBanner() != null){
			this.bannerCountMap.put(bv.getBanner(), 
					new Integer((bannerCountMap.get(bv.getBanner())).intValue()-1));

			this.campaignCountMap.put(bv.getBanner().getCampaign(), 
					new Integer((campaignCountMap.get(bv.getBanner().getCampaign())).intValue()-1));

			if (bannerCountMap.get(bv.getBanner()).intValue() < 0 ||
					campaignCountMap.get(bv.getBanner().getCampaign()).intValue() < 0){
				throw new UnsupportedOperationException();
			}
		}
		if (b != null){
			this.bannerCountMap.put(b, 
					new Integer((bannerCountMap.get(b)).intValue()+1));
			this.campaignCountMap.put(b.getCampaign(), 
					new Integer((campaignCountMap.get(b.getCampaign())).intValue()+1));
		}
	}

	public abstract void fillInstance(I_Policy pol);
	protected abstract BannerView getView(int i);
	public abstract int getViewCount();


}
