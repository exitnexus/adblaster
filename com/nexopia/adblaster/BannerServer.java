package com.nexopia.adblaster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import com.thoughtworks.xstream.XStream;

import com.nexopia.adblaster.Campaign.CampaignDB;

public class BannerServer {
	public static final String CURRENT_VERSION = "0.0";
	public static final Integer BANNER_BANNER = new Integer(1);
	public static final Integer BANNER_LEADERBOARD = new Integer(2);
	public static final Integer BANNER_BIGBOX = new Integer(3);
	public static final Integer BANNER_SKY120 = new Integer(4);
	public static final Integer BANNER_SKY160 = new Integer(5);
	public static final Integer BANNER_BUTTON60 = new Integer(6);
	public static final Integer BANNER_VULCAN = new Integer(7);
	public static final Integer BANNER_LINK = new Integer(8);
	private static final int BLANK = 0;
	private static final int ADD = 1;
	private static final int UPDATE = 2;
	private static final int ADDCAMPAIGN = 3;
	private static final int QUIT = 4;
	private static final int DELCAMPAIGN = 5;
	private static final int UPDATECAMPAIGN = 6;
	private static final int DEL = 7;
	private static final int STATS = 11;
	private static final int UPTIME = 12;
	private static final int SHOW = 13;
	private static final int HIDE = 14;
	private static final int SHUTDOWN = 15;
	private static final int VERSION = 16;
	private static final int RECONNECT = 17;
	private static final int LOGSTAT = 18;
	static final int GET = 19;
	static final int PASSBACK = 20;
	static final int GETFAIL = 21;
	static final int GETLOG = 22;
	static final int MINUTELY = 23;
	static final int HOURLY = 24;
	static final int DAILY = 25;
	static final int CLICK = 26;
	public static final int BANNER_SLIDE_SIZE = 8;
	public static final double BANNER_MIN_CLICKRATE = 0.0002;
	public static final double BANNER_MAX_CLICKRATE = 0.005;
	public static final int STATS_WINDOW = 60;
	private static final int NO_BANNER = 0;
	private static final int VIEW_WINDOWS = 5;
	
	//static boolean debugFields[] = new boolean[1000];//should be map?
	
	static HashMap<String, Boolean> debug;
	{
		debug.put("tick", Boolean.FALSE);
		debug.put("connect", Boolean.FALSE);
		debug.put("get", Boolean.FALSE);
		debug.put("getlog", Boolean.TRUE);
		debug.put("click", Boolean.TRUE);
		debug.put("timeupdates", Boolean.FALSE);
		debug.put("dailyrestart", Boolean.TRUE);
		debug.put("passback", Boolean.TRUE);
		debug.put("development", Boolean.TRUE);
	}
	
	static ServerStat stats = new ServerStat();
	static ServerStat slidingstats[] = new ServerStat[STATS_WINDOW];
	{
		for (int i = 0; i < slidingstats.length; i++){
			slidingstats[i] = new ServerStat();
		}
	}
	
	static StringBuffer logsock = new StringBuffer();
	static int currentwindow = 0;
	
	public BannerDatabase db;
	public CampaignDB cdb;
	public HashMap<String,Integer> sizes;
	Integer sizes_array[] = {BANNER_BANNER, BANNER_LEADERBOARD,
			BANNER_BIGBOX, BANNER_SKY120, BANNER_SKY160,
			BANNER_BUTTON60, BANNER_VULCAN, BANNER_LINK};
	
	private static int numservers;
	private static Random rand = new Random();
	//public banners;
	//public bannerids;
	//public bannersizes;
	//public HashMap<Integer, Campaign> bannercampaigns;
	//public HashMap<Integer, Integer> campaignids; // array( bannerid => campaignid );
	
	//public int time;
	private HashMap<ServablePropertyHolder,IntObjectHashMap> viewMap = new HashMap<ServablePropertyHolder, IntObjectHashMap>();
	Vector<Banner> banners = new Vector<Banner>();
	private FastMap<Banner, BannerStat> bannerstats;
	private FastMap<Integer, TypeStat> viewstats = new FastMap<Integer,TypeStat>();
	private FastMap<Integer, TypeStat> clickstats = new FastMap<Integer,TypeStat>();
	private FastMap<Campaign, BannerStat> campaignstats = new FastMap<Campaign,BannerStat>();
	private FastMap<Banner, HourlyStat> hourlystats = new FastMap<Banner,HourlyStat>();
	
	static class ServerStat {
		int starttime;
		public ServerStat() {
			starttime = (int)(System.currentTimeMillis()/1000);
		}
		int connect = 0; 
		int get = 0;
		int getfail = 0; 
		int click = 0;
	}
	
	static class BannerStat{
		int dailyviews;
		int dailyclicks;
		int passbacks;
		int current_views;
		int current_clicks;
		public boolean hasChanged() {
			return (passbacks > 0 || current_views > 0 || current_clicks > 0);
		}
		
		public void view() {
			dailyviews++;
			current_views++;
		}
		
		public void click() {
			dailyclicks++;
			current_clicks++;
		}
	}
	
	static class TypeStat {
		private static XStream xstream = new XStream();
		private static final boolean COMPRESS = true; 
		private static final String GZIP_ENCODING = "ISO8859_1";
		
		public String toXML() {
			xstream.alias("typestat", TypeStat.class);
			xstream.alias("integer", Integer.class);
			if (!COMPRESS) {
				return xstream.toXML(this);
			} else {
				GZIPOutputStream gz = null;
				ByteArrayOutputStream bytes = null;
				try {
					bytes = new ByteArrayOutputStream(); 
					gz = new GZIPOutputStream(bytes);
					gz.write(xstream.toXML(this).getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					return bytes.toString(GZIP_ENCODING);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		
		public static int INITIAL_ARRAY_SIZE = 100;
		public int starttime;
		private int total;
		private int[][] agesex;
		private int[] loc;
		private int[] interests;
		private int[][] hittimes;
		private HashMap<String, Integer> pages;
		
		public TypeStat() {
			total = 0;
			starttime = (int)(System.currentTimeMillis()/1000);
			loc = new int[INITIAL_ARRAY_SIZE];
			interests = new int[INITIAL_ARRAY_SIZE];
			agesex = new int[INITIAL_ARRAY_SIZE][3];
			hittimes = new int[7][24];
			pages = new HashMap<String, Integer>();
		}
		
		public void hit(int age, int sex, int loc, Interests interests, String page, int time) {
			total++;
			Calendar c = Calendar.getInstance();
			c.setTime(new Timestamp((long)time*1000));
			hittimes[c.get(Calendar.DAY_OF_WEEK)][c.get(Calendar.HOUR_OF_DAY)]++;
			if (age < 100 && age >= 0) {
				agesex[age][sex]++;
			}
			this.loc = expandArray(this.loc, loc);
			this.loc[loc]++;
			for(int i=interests.checked.nextSetBit(0); i>=0; i=interests.checked.nextSetBit(i+1)) { 
				this.interests = expandArray(this.interests, i);
				this.interests[i]++;
			}
			Integer pageviews = pages.get(page);
			if (pageviews == null) {
				pageviews = Integer.valueOf(1);
			} else {
				int i = pageviews.intValue();
				i++;
				pageviews.free();
				pageviews = Integer.valueOf(i);
			}
			pages.put(page, pageviews);
				
		}

		private int[] expandArray(int[] array, int new_val) {
			if (array.length > new_val) {
				return array;
			} else {
				int new_size = Math.max(array.length*2, new_val+1);
				int[] new_array = new int[new_size];
				for (int i=0; i<array.length; i++) {
					new_array[i] = array[i];
				}
				return new_array;
			}
		}
	}
	
	static class HourlyStat {
		private int[] views = new int[BANNER_SLIDE_SIZE];
		private int[] clicks = new int[BANNER_SLIDE_SIZE];
		private int current_pos = 0;
		
		public double getClickRate() {
			int totalviews = 0;
			int totalclicks = 0;
			for (int i=0; i < views.length; i++) {
				totalviews += views[i];
				totalclicks += clicks[i];
			}
			double clickrate = Math.max(BANNER_MIN_CLICKRATE, (double)totalviews/totalclicks);
			clickrate = Math.min(clickrate, BANNER_MAX_CLICKRATE);
			return clickrate;
		}

		public void shift() {
			current_pos = (current_pos+1)%BANNER_SLIDE_SIZE;
			views[current_pos] = 0;
			clicks[current_pos] = 0;
		}
		
		public void view() {
			views[current_pos]++;
		}
		public void click() {
			clicks[current_pos]++;
		}
		
	}
	
	public BannerServer(BannerDatabase db, CampaignDB cdb, int numservers) {
		this.db = db;
		this.cdb = cdb;
		BannerServer.numservers = numservers;
		Integer sizes[] = {BANNER_BANNER, BANNER_LEADERBOARD,
				BANNER_BIGBOX, BANNER_SKY120, BANNER_SKY160,
				BANNER_BUTTON60, BANNER_VULCAN, BANNER_LINK};
		this.bannerstats = new FastMap<Banner, BannerStat>();
		for(int i = 0; i < sizes.length; i++) {
			Integer size = sizes[i];
			this.viewstats.put(size, new TypeStat());
			this.clickstats.put(size, new TypeStat());
		}
		
		//this.time = (int) (System.currentTimeMillis()/1000);
	}
	
	public boolean addCampaign(int id){
		ServablePropertyHolder c = cdb.add(id);
		if (c != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean updateCampaign(int id) {
		return cdb.update(id) != null;
	}
	
	public void deleteCampaign(int id) {
		cdb.get(id).minutely();
		cdb.delete(id);
		this.db.deleteCampaign(id);
	}
	public boolean addBanner(int id) {
		return this.db.add(id,new Utilities.PageValidator1()) != null;
	}
	
	public boolean updateBanner(int id) {
		return this.db.update(id,new Utilities.PageValidator1()) != null;
	}
	
	public void deleteBanner(int id) {
		this.db.delete(id);
	}
	
	/**
	 * Determine whether it is valid for a user to view this banner
	 * at this time. 
	 */
	public boolean isValidForUser(int userid, int time, ServablePropertyHolder b){
		if (b.getViewsPerUser() <= 0)
			return true;
		
		/* Find the oldest view*/
		int[] views = getViewsForUser(userid, b);

		if (views[0] != 0){
			System.out.println(time + ":" + views[0] + " > " + b.getLimitByPeriod());
			if (time - views[0] > b.getLimitByPeriod())
				/* If the oldest view is outside of limit period, we're golden */
				return true;
			else
				return false;
		}
		
		/* If we've fallen through, then there are no views.*/
		return true;
		
	}
	
	/**
	 * Indicate that a user used a banner at a certain time. 
	 */
	public void markBannerUsed(int age, int sex, int loc, Interests interests, String page, int time, int userid, Banner b){
		if (b.getViewsPerUser() != 0){
			int[] views = getViewsForUser(userid, b);
			
			/* Throw out the oldest, insert the new view. */
			for (int i = 0; i < views.length - 1; i++){
				views[i] = views[i+1];
			}
			views[views.length - 1] = time;
			System.out.println(Arrays.toString(views));
		}
		
		if (b.getCampaign().getViewsPerUser() != 0){
			int[] cviews = getViewsForUser(userid, b.getCampaign());
			
			/* Throw out the oldest, insert the new view. */
			for (int i = 0; i < cviews.length - 1; i++){
				cviews[i] = cviews[i+1];
			}
			cviews[cviews.length - 1] = time;
		}

		hourlystats.getOrCreate(b, HourlyStat.class).view();
		bannerstats.getOrCreate(b, BannerStat.class).view();
		campaignstats.getOrCreate(b.campaign, BannerStat.class).view();
		viewstats.getOrCreate(Integer.valueOf(b.getSize()), TypeStat.class).hit(age, sex, loc, interests, page, time);
		clickstats.getOrCreate(Integer.valueOf(b.getSize()), TypeStat.class).hit(age, sex, loc, interests, page, time);
	}
	
	/**
	 *  Return an int array of the times the user has viewed the banner. 
	 */
	private int[] getViewsForUser(int userid, ServablePropertyHolder b) {
		/* Get records of all views for the banner.*/
		IntObjectHashMap userViewMap = viewMap.get(b);
		if (userViewMap == null){
			userViewMap = new IntObjectHashMap();
			viewMap.put(b, userViewMap);
		}
		
		/* From the above records, get all views for this user.*/
		int []views = (int[])userViewMap.get(userid);
		if (views == null){
			views = new int[b.getViewsPerUser()];
			userViewMap.put(userid, views);
		}
		return views;
	}
	
	private Vector<Banner> orderBannersByScore(Vector<Banner> old) {
		Vector<Banner> vec = new Vector<Banner>();
		for (int j = 0; j < old.size(); j++){
			Banner b = old.get(j);
			float score = b.payrate;
			int i = -1;
			while (true){
				i++;
				if (i >= vec.size()){
					break;
				}
				Banner b2 = vec.get(i);
				if (b2.payrate < score)
					break;
			}
			vec.insertElementAt(b, i);
			
		}
		return vec;
	}
	
	/**
	 * Return a valid banner with the best score.  Currently, doesn't check
	 * many constraints.
	 * 
	 * @param usertime
	 * @param size
	 * @param userid
	 * @param age
	 * @param sex
	 * @param location
	 * @param interests
	 * @param page
	 * @param debug
	 * @return
	 */
	
	public int getBanner(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, String page, boolean debug){
		Vector<Banner> banners = new Vector<Banner>();
		for (Campaign campaign : cdb.getCampaigns()){
			banners.addAll(campaign.getBanners(usertime, size, userid, age, sex, location, interests, page, debug));
		}
		Vector<Banner> validBanners = new Vector<Banner>();
		
		for (int i = 0; i < banners.size(); i++){
			Banner banner_i = banners.get(i);
			boolean b1 = isValidForUser(userid, usertime, banner_i);
			boolean b2 = isValidForUser(userid, usertime, banner_i.campaign);
			boolean b3 = !hasReachedViewsPerDay(banner_i);
			boolean b4 = !hasReachedClicksPerDay(banner_i);
			boolean b5 = !hasReachedMaxViews(banner_i);
			
			if (debug) System.out.println("" + banner_i.id + ":" + b1 + ":"+b2+":"+b3+":"+b4);
			if ( b1 && b2 && b3 && b4 && b5){
				validBanners.add(banner_i);
				//System.out.println(b);
				
			}
		}
		
		Banner chosen = weightedChoice(validBanners, userid, usertime);
		if (chosen != null) {
			markBannerUsed(age, sex, location, interests, page, usertime, userid, chosen);
			System.out.println("PICKED:" + chosen.id);
			return chosen.id;
		}
		System.out.println("Number of valid banners:" + validBanners.size());
			
		return 0;
	}
	
	private boolean hasReachedMaxViews(Banner b) {
		if ((b.getViews() + this.bannerstats.getOrCreate(b, BannerStat.class).dailyviews) >= b.getIntegerMaxViews())
			return true;
		if ((b.getCampaign().getViews() + this.campaignstats.getOrCreate(b.campaign, BannerStat.class).dailyviews) >= b.campaign.getIntegerMaxViews())
			return true;
		if (debug.get("development").booleanValue()) System.out.println("" + b.getCampaign().getViews() + " + " + this.campaignstats.getOrCreate(b.campaign, BannerStat.class).dailyviews + " < " + b.campaign.getIntegerMaxViews()); 
		return false;
			
	}

	private Banner weightedChoice(Vector<Banner> validBanners, int uid, int time) {
		double total = 0;
		double[] priorities = new double[validBanners.size()];
		for (int i=0; i<validBanners.size(); i++) {
			Banner b = validBanners.get(i);
			double priority = getPriority(b, uid, time);
			priorities[i] = priority;
			total += priority;
		}
		double pick = rand.nextDouble()*total;
		
		double currentWeight = 0;
		for (int i=0;i<priorities.length;i++) {
			currentWeight += priorities[i];
			if (currentWeight >= pick) {
				return validBanners.get(i);
			}
		}
		return null;
	}


	/*weighting based on
				(1 + priority) *
				(2 - (1-(time since last view/max view rate)) *
				(3 - ((views today)/(max views per day))) *
				(3 - ((clicks today)/(max clicks per day)))
	*/
	private double getPriority(Banner b, int uid, int time) {
		double priority = b.getCoefficient() + 1;
		
		double period = Math.min(b.getLimitByPeriod(), b.campaign.getLimitByPeriod());
		if (period == 0) period = Math.max(b.getLimitByPeriod(), b.campaign.getLimitByPeriod());
		
		double viewsperuser = Math.min(b.getViewsPerUser(), b.campaign.getViewsPerUser());
		if (viewsperuser == 0) viewsperuser = Math.max(b.getViewsPerUser(), b.campaign.getViewsPerUser());
		
		double viewsperday = Math.min(b.getViewsPerDay(), b.campaign.getViewsPerDay())/numservers;
		if (viewsperday == 0) viewsperday = Math.max(b.getViewsPerDay(), b.campaign.getViewsPerDay())/numservers;
		
		double clicksperday = Math.min(b.getClicksperday(), b.campaign.getClicksperday())/numservers;
		if (clicksperday == 0) clicksperday = Math.max(b.getClicksperday(), b.campaign.getClicksperday())/numservers;
		
		
		if (viewsperuser != 0) {
			int[] viewTimes = getViewsForUser(uid, b);
			if (viewTimes.length != 0 && viewTimes[viewTimes.length-1] != 0 && period != 0) {
				priority *= 2 - Math.max(1-((time - viewTimes[viewTimes.length-1])/period)*viewsperuser, 0);
			} else {
				priority *= 2;
			}
		}
		
		if (viewsperday != 0) {
			priority *= (3-this.bannerstats.get(b).dailyviews/viewsperday);
		}
		
		if (clicksperday != 0) {
			priority *= (3-this.bannerstats.get(b).dailyviews/clicksperday);
		}
		
		return priority;
	}

	private boolean hasReachedViewsPerDay(Banner b) {

		if (this.bannerstats.getOrCreate(b, BannerStat.class).dailyviews >= b.getIntegerMaxViewsPerDay()/numservers)
			return true;
		if (this.campaignstats.getOrCreate(b.campaign, BannerStat.class).dailyviews >= b.campaign.getIntegerMaxViewsPerDay()/numservers)
			return true;
		if (debug.get("development").booleanValue()) System.out.println("Banner Views: " + this.bannerstats.getOrCreate(b, BannerStat.class).dailyviews + ":" + b.getIntegerMaxViewsPerDay()/numservers);
		if (debug.get("development").booleanValue()) System.out.println("Campaign Views: " + this.campaignstats.getOrCreate(b.campaign, BannerStat.class).dailyviews + ":" + b.campaign.getIntegerMaxViewsPerDay()/numservers);
		return false;
	}
	
	private boolean hasReachedClicksPerDay(Banner b) {
		if (this.bannerstats.get(b) == null){
			this.bannerstats.put(b, new BannerStat());
		}
		if (this.bannerstats.get(b).dailyclicks > b.getIntegerMaxClicksperday()/numservers)
			return true;
		if (this.campaignstats.get(b.campaign) == null){
			this.campaignstats.put(b.campaign, new BannerStat());
		}
		if (this.campaignstats.get(b.campaign).dailyclicks > b.campaign.getIntegerMaxClicksperday()/numservers)
			return true;
		return false;
	}
	
	/** 
	 * Not used yet
	 */
	public int getBannerByCoef(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, String page, boolean debug){
		//String debugLog = "";
		//if (debug) debugLog += usertime+", "+size+", "+userid+", "+age+", "+sex+", "+loc+", "+page+", "+debug;
		
		Vector<Banner> valid = new Vector<Banner>();
		for (Campaign campaign : cdb.getCampaigns()){
			valid.addAll(campaign.getBanners(usertime, size, userid, age, sex, location, interests, page, debug));
		}
		if (!valid.isEmpty()) {
			Banner b = Utilities.priorityChoose(valid);
			markBannerUsed(age, sex, location, interests, page, usertime, userid, b);
			return b.getID();
		} else {
			return 0;
		}
	}
	
	
	public static int debugGet(){return 1;}
	public static void bannerDebug(String str) {
		Date now = new Date();
		System.out.println("[ " + now + " ] " + str);
	}
	
	public static String str_pad(int s, int i){
		return "" + s;
	}
	
	static Vector<HashMap<Integer, Vector<Integer>>> recentviews = new Vector<HashMap<Integer, Vector<Integer>>>();
	
	static class FastMap <K, V> {
		HashMap <K,V>map;
		
		FastMap(){
			map = new HashMap<K,V>();
		}
		
		FastMap(K key, V val){
			map = new HashMap<K,V>();
			map.put(key, val);
		}
		
		public V getOrCreate(K k, Class defaultValue) {
			V elem = this.get(k);
			if(elem == null){
				try {
					elem = (V) defaultValue.newInstance();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				this.put(k,elem);
			}
			return elem;
		}
		HashMap getMap(){
			return map;
		}
		void put(K k, V v){
			map.put(k,v);
		}
		V get(K k){
			return map.get(k);
		}
		
	}
	
	static class FastVec <T> implements Iterable{
		Vector <T>v;
		public FastVec(T obj){
			v = new Vector<T>();
			v.add(obj);
		}
		public FastVec add(T obj){
			v.add(obj);
			return this;
		}
		public Iterator iterator() {
			return v.iterator();
		}
		public boolean isEmpty(){
			return v.isEmpty();
		}
		
	}
	
	
	private int parseCommand(String command) {
		int cmd;
		if (command.toUpperCase().equals("GET")) {
			cmd = GET;
		} else if (command.toUpperCase().equals("PASSBACK")) {
			cmd = PASSBACK;
		} else if (command.toUpperCase().equals("ADD")) {
			cmd = ADD;
		} else if (command.toUpperCase().equals("UPDATE")) {
			cmd = UPDATE;
		} else if (command.toUpperCase().equals("ADDCAMPAIGN")) {
			cmd = ADDCAMPAIGN;
		} else if (command.toUpperCase().equals("QUIT")) {
			cmd = QUIT;
		} else if (command.toUpperCase().equals("DELCAMPAIGN")) {
			cmd = DELCAMPAIGN;
		} else if (command.toUpperCase().equals("UPDATECAMPAIGN")) {
			cmd = UPDATECAMPAIGN;
		} else if (command.toUpperCase().equals("DEL")) {
			cmd = DEL;
		} else if (command.toUpperCase().equals("STATS")) {
			cmd = STATS;
		} else if (command.toUpperCase().equals("UPTIME")) {
			cmd = UPTIME;
		} else if (command.toUpperCase().equals("SHOW")) {
			cmd = SHOW;
		} else if (command.toUpperCase().equals("HIDE")) {
			cmd = HIDE;
		} else if (command.toUpperCase().equals("SHUTDOWN")) {
			cmd = SHUTDOWN;
		} else if (command.toUpperCase().equals("VERSION")) {
			cmd = VERSION;
		} else if (command.toUpperCase().equals("RECONNECT")) {
			cmd = RECONNECT;
		} else if (command.toUpperCase().equals("LOGSTAT")) {
			cmd = LOGSTAT;
		} else if (command.toUpperCase().equals("GETFAIL")) {
			cmd = GETFAIL;
		} else if (command.toUpperCase().equals("GETLOG")) {
			cmd = GETLOG;
		} else if (command.toUpperCase().equals("MINUTELY")) {
			cmd = MINUTELY;
		} else if (command.toUpperCase().equals("HOURLY")) {
			cmd = HOURLY;
		} else if (command.toUpperCase().equals("DAILY")) {
			cmd = DAILY;
		} else if (command.toUpperCase().equals("CLICK")) {
			cmd = CLICK;
		} else {
			cmd = BLANK;
			System.out.println(command + " not found.");
		}
		return cmd;
	}
	
	public String receive(String command){
		String[] split = command.split(" ");
		int cmd = BLANK;
		String[] params = new String[split.length-1];
		int i = 0;
		boolean first = true;
		for (String s : split) {
			if (first) {
				cmd = parseCommand(s);
				first = false;
			} else {
				params[i] = s;
				i++;
			}
		}
		
		//System.out.println(cmd);
		
		return receive(cmd, params);
	}
	
	public void secondly() {
		currentwindow = (currentwindow+1)%VIEW_WINDOWS;
		recentviews.set(currentwindow, new HashMap<Integer, Vector<Integer>>());
	}

	@SuppressWarnings("unchecked")
	public void minutely(boolean debug) {
		Collection<Banner> banners = (Collection<Banner>)this.db.getBanners();
		for (Banner b: banners) {
			minutely(b, (int)(System.currentTimeMillis()/1000), debug);
		}
	}
	
	private void minutely(Banner b, int time, boolean debug) {
		BannerStat bannerstat = this.bannerstats.getOrCreate(b, BannerStat.class);
		
		if (debug) {
			Utilities.bannerDebug("minutely " + b.id + " " + bannerstat.dailyviews + 
					" " + bannerstat.dailyclicks + " " + bannerstat.passbacks);
		}
		
		if(bannerstat.hasChanged()) {
			JDBCConfig.queueQuery("UPDATE " + JDBCConfig.BANNER_TABLE + " SET lastupdatetime = "+time+", views = views + "+bannerstat.current_views+", clicks = clicks + "+bannerstat.current_clicks+", passbacks = passbacks + "+bannerstat.passbacks+" WHERE id = " + b.id);
			bannerstat.current_views = 0;
			bannerstat.current_clicks = 0;
			bannerstat.passbacks = 0;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public void hourly(boolean debug) {
		Collection<Banner> banners = (Collection<Banner>)this.db.getBanners();
		for (Banner b: banners) {
			hourly(b, debug);
		}
	}
	
	private void hourly(Banner b, boolean debug) {
		if (debug) {
			Utilities.bannerDebug("hour " + b.id);
		}
		JDBCConfig.queueQuery("REPLACE INTO " + JDBCConfig.BANNERSTAT_TABLE + " (bannerid, time, views, potentialviews, clicks, passbacks) SELECT id, lastupdatetime, views, potentialviews, clicks, passbacks FROM banners WHERE id = " + b.id);
		if (b.getPayType() == Banner.PAYTYPE_CPC) {
			HourlyStat hourlystat = hourlystats.get(b);
			b.setCoefficient(hourlystat.getClickRate()*b.getPayRate());
			hourlystat.shift();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void daily(boolean debug) {
		Collection<Banner> banners = (Collection<Banner>)this.db.getBanners();
		for (Banner b: banners) {
			daily(b, debug);
		}
		long time = System.currentTimeMillis()/1000;
		for (Integer size: sizes_array) {
			TypeStat views = viewstats.get(size);
			TypeStat clicks = clickstats.get(size);
			JDBCConfig.queueQuery("INSERT INTO " + JDBCConfig.BANNERTYPESTAT_TABLE + " SET size = "+size+", time = "+time+", views = "+views.total+", clicks = "+clicks.total+", viewsdump = '"+views.toXML()+"', clicksdump = '"+clicks.toXML()+"'");
		}
	}
	
	public void daily(Banner b, boolean debug) {
		bannerstats.get(b).dailyclicks = 0;
		bannerstats.get(b).dailyviews = 0;
	}
	
	public String receive(int cmd, String[] params){
		int id;
		int t_sec = (int)(System.currentTimeMillis() / 1000);
		int statstime = (t_sec % STATS_WINDOW);
		
		switch(cmd){
		case GET:
		{
			stats.get++;
			slidingstats[statstime].get++;
			
			int usertime=Integer.parseInt(params[0]);
			int size=Integer.parseInt(params[1]); 
			int userid=Integer.parseInt(params[2]); 
			byte age=Byte.parseByte(params[3]); 
			byte sex=Byte.parseByte(params[4]); 
			short loc=Short.parseShort(params[5]); 
			String interestsStr=params[6]; 
			String page=params[7]; 
			int passback=Integer.parseInt(params[8]); 
			//boolean debugGet=Boolean.parseBoolean(params[9]);
			
			Interests interests = new Interests(interestsStr, false);
			
			if(passback != 0)
				passbackBanner(passback, userid);
			
			int ret = getBanner(usertime, size, userid, age, sex, loc, interests, page, true);
			
			if (debug.get("passback").booleanValue()) {
				if (passback != 0) {
					boolean hasSeen = false;
					String viewsstring = "";
					Integer uid = Integer.valueOf(userid);
					for (HashMap<Integer, Vector<Integer>> uidtoviews : recentviews) {
						if (uidtoviews.get(uid) != null && !uidtoviews.get(uid).isEmpty()) {
							for (Integer view : uidtoviews.get(userid)) {
								viewsstring += " " + view;
								if (passback == view.intValue()) {
									hasSeen = true;
								}
							}
						}
					}
					if (!hasSeen) {
						bannerDebug("Invalid Passback: " + passback + ", Recently Vied: " + viewsstring);
					}
					Vector<Integer> currentUserWindow = recentviews.get(currentwindow).get(uid);
					if (currentUserWindow == null) {
						currentUserWindow = new Vector<Integer>();
					}
					currentUserWindow.add(Integer.valueOf(passback));
				}
			}
			
			if(debug.get("get").booleanValue() || (debug.get("getfail").booleanValue() && (ret == NO_BANNER)))
				bannerDebug("get params => ret");

			if(debug.get("getlog").booleanValue() && (logsock != null)){
				if(logsock.append("get params => ret\n") == null){
					bannerDebug("log server connection error: errstr (errno)<br />");
					//CLOSE Logsock// logsock.close();
					logsock = null;
				}
			}
			
			if(ret == 0){
				stats.getfail++;
				slidingstats[statstime].getfail++;
			}
			//System.out.println(ret);
			//socket_write(sock, "ret\n");
			
			//unset(ret, size, userid, age, sex, loc, interests, page, passback);
			Integer retInt = Integer.valueOf(ret);
			String retString = retInt.toString();
			retInt.free();
			return retString;
		}
		case ADD: // "add id"
			id = Integer.parseInt(params[0]);
			db.add(id, new Utilities.PageValidator1()); //addBannerD(params);
			bannerDebug("add params");
			break;
		case UPDATE: // "update id"
			//updateBannerD(params);
			id = Integer.parseInt(params[0]);
			db.add(id, new Utilities.PageValidator1()); 
			bannerDebug("update params");
			break;
			
		case DEL: // "del id"
			//deleteBannerD(params);
			id = Integer.parseInt(params[0]);
			db.delete(id);
			bannerDebug("delete params");
			break;
			
		case ADDCAMPAIGN: // "addcampaign id"
			id = Integer.parseInt(params[0]);
			cdb.add(id);
			try {
				Statement st = JDBCConfig.createStatement();
				ResultSet rs = st.executeQuery("SELECT id FROM banners WHERE campaignid = " + id);
				while (rs.next()) {
					int bannerid = rs.getInt("id");
					db.add(bannerid, new Utilities.PageValidator1());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			bannerDebug("addcampaign params");
			break;
			
		case UPDATECAMPAIGN: // "updatecampaign id"
			id = Integer.parseInt(params[0]);
			cdb.add(id);
			try {
				Statement st = JDBCConfig.createStatement();
				ResultSet rs = st.executeQuery("SELECT id FROM banners WHERE campaignid = " + id);
				while (rs.next()) {
					int bannerid = rs.getInt("id");
					db.add(bannerid, new Utilities.PageValidator1());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			bannerDebug("updatecampaign params");
			break;
			
		case DELCAMPAIGN: // "delcampaign id"
			id = Integer.parseInt(params[0]);
			for (Banner b: cdb.get(id).banners) {
				db.delete(b.id);
			}
			cdb.delete(id);
			bannerDebug("deletecampaign params");
			break;
			
		case QUIT: 
			//Nothing needs to be done to cleanup a given connection right now, the client simply needs to drop connection.
			break;
			
		case STATS:
			/*
			$total = array();
			foreach($slidingstats as $i => $stat)
				foreach($stat as $k => $v)
					if(!isset($total[$k]))
						$total[$k] = $v;
					else
						$total[$k] += $v;
			*/
			ServerStat totalstat = new ServerStat();
			for (ServerStat slidingstat: slidingstats) {
				totalstat.get += slidingstat.get;
				totalstat.getfail += slidingstat.getfail;
				totalstat.connect += slidingstat.connect;
				totalstat.click += slidingstat.click;
			}
			
			String out  = "Uptime: " + (System.currentTimeMillis()/1000 - stats.starttime) + "\n";
			out += "Connect:  " + str_pad(stats.connect, 9) + str_pad(totalstat.connect, 7) + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].connect) + "\n";
			out += "Get:  " + str_pad(stats.get, 9) + str_pad(totalstat.get, 7) + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].get) + "\n";
			out += "Get Fail:  " + str_pad(stats.getfail, 9) + str_pad(totalstat.getfail, 7) + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].getfail) + "\n";
			out += "Click:  " + str_pad(stats.click, 9) + str_pad(totalstat.click, 7) + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].click) + "\n";
			
			return out;
			
		case UPTIME:
			//socket_write(sock,  "Uptime: " . (time - stats[STARTTIME]) + "\n");
			break;
			
		case SHOW:
			/*I don't know how we're going to do this command yet. -Thomas*/
			/*if(isset(debug[params])){
			 debug[params] = true;
			 socket_write(sock, "debug enabled for params\n");
			 }else{
			 socket_write(sock, "unknown debug variable: params\n");
			 }*/
			break;
			
		case HIDE:
			/*I don't know how we're going to do this command yet. -Thomas*/
			//if(debug[params]){
			//debug[params] = false;
			//socket_write(sock, "debug disabled for params\n");
			//}else{
			//socket_write(sock, "unknown debug variable: params\n");
			//}
			break;
			
		case SHUTDOWN: //dump stats, clean up most memory, and quit. Good for upgrading the server early :p
			//socket_write(sock, "shutting down\n");
			bannerDebug("shutting down");
			final int TIMEUPDATES = 12;
			daily(true);
			//daily(debug[TIMEUPDATES]);
			System.exit(0);
			
		case VERSION:
			return CURRENT_VERSION;
			
		case RECONNECT:
			/*if(logsock)
			 fclose(logsock);
			 
			 if(params)
			 list(logserver, logserver_port) = explode(':', params);
			 
			 if(logserver && logserver_port){
			 if(logsock = fsockopen(logserver, logserver_port, errno, errstr, 0.05)){
			 stream_set_timeout(logsock, 0.02);
			 //					stream_set_blocking(logsock, 0); //non blocking
			  socket_write(sock, "success: logserver, logserver_port\n");
			  }else{
			  socket_write(sock, "failed: logserver, logserver_port\n");
			  logsock = null;
			  }
			  }else{
			  socket_write(sock, "no logserver defined\n");
			  }
			  
			  break;*/
		case LOGSTAT:
			//socket_write(sock, (logsock ? "connected" : "not") + ": logserver, logserver_port\n");
			break;
		case MINUTELY:
			minutely(true);
			break;
		case HOURLY:
			hourly(true);
			break;
		case DAILY:
			daily(true);
			break;
		case CLICK:
			int bannerid=Integer.parseInt(params[0]); 
			Banner b = db.getBannerByID(bannerid);
			
			this.bannerstats.getOrCreate(b, BannerStat.class).click();
			this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).click();
			
			stats.click++;
			slidingstats[statstime%STATS_WINDOW].click++;
			
			break;
		default:
			//myerror("unknown command: 'msg'", __LINE__);
			break;
		}
		return null;
	}

	private void passbackBanner(int passback, int userid) {
		Banner b = db.getBannerByID(passback);
		bannerstats.get(b).passbacks++;
		int[] userviews = getViewsForUser(userid, b);
		for (int i=0; i<userviews.length; i++) {
			userviews[i] = (int)(System.currentTimeMillis()/1000);
		}
	}

	
	
}
