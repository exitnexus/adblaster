package com.nexopia.adblaster;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import com.thoughtworks.xstream.XStream;

import com.nexopia.adblaster.Campaign.CampaignDB;

public class BannerServer {
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
	private static final int CONDUMP = 8;
	private static final int BUFFDUMP = 9;
	private static final int BANNERDUMP = 10;
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
	public static final int BANNER_SLIDE_SIZE = 8;
	public static final double BANNER_MIN_CLICKRATE = 0.0002;
	public static final double BANNER_MAX_CLICKRATE = 0.005;
	
	
	public BannerDatabase db;
	public CampaignDB cdb;
	public HashMap<String,Integer> sizes;
	
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
	private HashMap<Integer, TypeStat> viewstats = new HashMap<Integer,TypeStat>();
	private HashMap<Integer, TypeStat> clickstats = new HashMap<Integer,TypeStat>();
	private FastMap<Campaign, BannerStat> campaignstats = new FastMap<Campaign,BannerStat>();
	private FastMap<Banner, HourlyStat> hourlystats = new FastMap<Banner,HourlyStat>();
	
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
		public String toXML() {
			return xstream.toXML(this);
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
	public void markBannerUsed(int userid, int time, Banner b){
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
			Banner b = banners.get(i);
			boolean b1 = isValidForUser(userid, usertime, b);
			boolean b2 = !hasReachedViewsPerDay(b);
			boolean b3 = !hasReachedClicksPerDay(b);
			System.out.println("" + b.id + ":" + b1 + ":"+b2+":"+b3);
			if ( b1 && b2 && b3){
				validBanners.add(b);
				//System.out.println(b);
				
			}
		}
		
		Banner chosen = weightedChoice(validBanners, userid, usertime);
		if (chosen != null) {
			markBannerUsed(userid, usertime, chosen);
			this.bannerstats.get(chosen).dailyviews++;
			System.out.println("PICKED:" + chosen.id);
			return chosen.id;
		}
		System.out.println("IMPORTANT:" + validBanners.size());
			
		return 0;
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
			if (currentWeight > pick) {
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
		
		double viewsperday = Math.min(b.getViewsPerDay(), b.campaign.getViewsPerDay());
		if (viewsperday == 0) viewsperday = Math.max(b.getViewsPerDay(), b.campaign.getViewsPerDay());
		
		double clicksperday = Math.min(b.getClicksperday(), b.campaign.getClicksperday());
		if (clicksperday == 0) clicksperday = Math.max(b.getClicksperday(), b.campaign.getClicksperday());
		
		
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
		if (this.bannerstats.get(b) == null){
			this.bannerstats.put(b, new BannerStat());
		}
		if (this.bannerstats.get(b).dailyviews > b.getIntegerMaxViewsPerDay())
			return true;
		if (this.campaignstats.get(b.campaign) == null){
			this.campaignstats.put(b.campaign, new BannerStat());
		}
		if (this.campaignstats.get(b.campaign).dailyviews > b.campaign.getIntegerMaxViewsPerDay())
			return true;
		return false;
	}
	
	private boolean hasReachedClicksPerDay(Banner b) {
		if (this.bannerstats.get(b) == null){
			this.bannerstats.put(b, new BannerStat());
		}
		if (this.bannerstats.get(b).dailyclicks > b.getIntegerMaxClicksperday())
			return true;
		if (this.campaignstats.get(b.campaign) == null){
			this.campaignstats.put(b.campaign, new BannerStat());
		}
		if (this.campaignstats.get(b.campaign).dailyclicks > b.campaign.getIntegerMaxClicksperday())
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
			markBannerUsed(userid, usertime, b);
			return b.getID();
		} else {
			return 0;
		}
	}
	
	
	public static void passbackBanner(String s, String y){}
	public static int debugGet(){return 1;}
	public static void addBanner(){};
	public static void bannerDebug(String str){}
	public static String str_pad(int s, int i){
		return "" + s;
	}
	
	static FastVec <FastMap<String, FastVec>>recentviews;
	
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
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
	
	static boolean debug[] = new boolean[1000];//should be map?
	static int stats[] = new int[1000];//should be map?
	
	static StringBuffer logsock = new StringBuffer();
	static int slidingstats[][] = new int[1000][1000];
	static int statstime = 0;
	static String currentwindow = "";
	
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
		} else if (command.toUpperCase().equals("CONDUMP")) {
			cmd = CONDUMP;
		} else if (command.toUpperCase().equals("BANNERDUMP")) {
			cmd = BANNERDUMP;
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
		
		return new Integer(receive(cmd, params)).toString();
	}
	

	@SuppressWarnings("unchecked")
	public void minutely(boolean debug) {
		Collection<Banner> banners = (Collection<Banner>)this.db.getBanners();
		for (Banner b: banners) {
			minutely(b, (int)(System.currentTimeMillis()/1000), debug);
		}
	}
	
	private void minutely(Banner b, int time, boolean debug) {
		BannerStat bannerstat = this.bannerstats.get(b);
		
		if (debug) {
			Utilities.bannerDebug("minutely " + b.id + " " + bannerstat.dailyviews + 
					" " + bannerstat.dailyclicks + " " + bannerstat.passbacks);
		}
		
		if(bannerstat.hasChanged()) {
			JDBCConfig.queueQuery("UPDATE banners SET lastupdatetime = "+time+", views = views + "+bannerstat.current_views+", clicks = clicks + "+bannerstat.current_clicks+", passbacks = passbacks + "+bannerstat.passbacks+" WHERE id = " + b.id);
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
		JDBCConfig.queueQuery("REPLACE INTO bannerstats (bannerid, time, views, potentialviews, clicks, passbacks) SELECT id, lastupdatetime, views, potentialviews, clicks, passbacks FROM banners WHERE id = " + b.id);
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
	}
	public void daily(Banner b, boolean debug) {
		bannerstats.get(b).dailyclicks = 0;
		bannerstats.get(b).dailyviews = 0;
	}
	
	public int receive(int cmd, String[] params){
		
		switch(cmd){
		case GET:
		{
			stats[GET]++;
			slidingstats[statstime][GET]++;
			
			int usertime=Integer.parseInt(params[0]);
			int size=Integer.parseInt(params[1]); 
			int userid=Integer.parseInt(params[2]); 
			byte age=Byte.parseByte(params[3]); 
			byte sex=Byte.parseByte(params[4]); 
			short loc=Short.parseShort(params[5]); 
			String interestsStr=params[6]; 
			String page=params[7]; 
			//passback=params[8]; 
			//boolean debugGet=Boolean.parseBoolean(params[9]);
			
			Interests interests = new Interests(interestsStr, false);
			
			//if(passback != "")
			//	passbackBanner(passback, userid);
			
			int ret = getBanner(usertime, size, userid, age, sex, loc, interests, page, true);
			
			/*if (debug[PASSBACK]) {
			 if (passback != "") {
			 boolean hasSeen = false;
			 String viewsstring = "";
			 for (Object vi : recentviews ) { //reference
			 FastMap<String, FastVec>viewswindow = (FastMap<String, FastVec>)vi;
			 if (viewswindow.get(userid) != null && !viewswindow.get(userid).isEmpty()) {
			 for (Object view : viewswindow.get(userid) ) {
			 viewsstring += "view ";
			 if (passback == view) {
			 hasSeen = true;
			 }
			 }
			 }
			 }
			 if (!hasSeen) {
			 //bannerDebug("Invalid Passback: passback, Recently Viewed: viewsstring");
			  }
			  }
			  //currentwindow
			   recentviews.add(new FastMap<String, FastVec>(userid, new FastVec(ret)));
			   }*/
			if(debug[GET] || (debug[GETFAIL] && (ret == -1)))
				bannerDebug("get params => ret");
			
			if(debug[GETLOG] && (logsock != null)){
				if(logsock.append("get params => ret\n") == null){
					bannerDebug("log server connection error: errstr (errno)<br />");
					//CLOSE Logsock// logsock.close();
					logsock = null;
				}
			}
			
			if(ret == -1){
				stats[GETFAIL]++;
				slidingstats[statstime][GETFAIL]++;
			}
			//System.out.println(ret);
			//socket_write(sock, "ret\n");
			
			//unset(ret, size, userid, age, sex, loc, interests, page, passback);
			
			return ret;
		}
		case BLANK:
		{
			if(debug[0])
				bannerDebug(" params");
			
			stats[0]++;
			slidingstats[statstime][0]++;
			
			//(id, age, sex, loc, interests, page, time) = explode(' ', params);
			String id="", 
			age="", 
			sex="", 
			loc="", 
			interestsStr="", 
			page="", 
			time="";
			Interests interests = new Interests(interestsStr, false);
			
			
			//new Banner(id, age, sex, loc, interests, page, time);
			
			//unset(id, age, sex, loc, interests, page);
			
			break;
		}
		case ADD: // "add id"
		{
			//addBannerD(params);
			bannerDebug("add params");
			break;
		}
		case UPDATE: // "update id"
			//updateBannerD(params);
			bannerDebug("update params");
			break;
			
		case DEL: // "del id"
			//deleteBannerD(params);
			bannerDebug("delete params");
			break;
			
		case ADDCAMPAIGN: // "addcampaign id"
			//addCampaignD(params);
			bannerDebug("addcampaign params");
			break;
			
		case UPDATECAMPAIGN: // "updatecampaign id"
			//updateCampaignD(params);
			bannerDebug("updatecampaign params");
			break;
			
		case DELCAMPAIGN: // "delcampaign id"
			//deleteCampaignD(params);
			bannerDebug("deletecampaign params");
			break;
			
		case QUIT: //disconnects this connection
			//			@socket_shutdown(clients[sock]);
			//socket_close(clients[(int)sock]);
			//unset(clients[(int)sock]); 	// remove client from arrays
			//unset(clientdata[(int)sock]);
			break;
			
		case CONDUMP:
			//socket_write(sock, print_r(clients, true) + "\n");
			break;
			
		case BUFFDUMP:
			//socket_write(sock, print_r(clientdata, true) + "\n");
			break;
			
		case BANNERDUMP:
			//socket_write(sock, print_r(bannerserver, true) + "\n");
			break;
			
		case STATS:
			int total[] = new int[109000];
			int k=0;
			for(int i=0; i<slidingstats.length; i++){
				int[] stat = slidingstats[i];
				for(int j=0; j<stat.length; j++){
					int v = stat[j]; 
					if(total[k] != 0)
						total[k] = v;
					else
						total[k] += v;
				}
				k++;
			}
			final int STARTTIME = 0;
			final int CONNECT = 1;
			int window = 1;
			
			String out  = "Uptime: " + (System.currentTimeMillis() - stats[STARTTIME]) + "\n";
			out += "Connect:  " + str_pad(stats[CONNECT], 9) + str_pad(total[CONNECT], 7) + (slidingstats[(statstime+window-1)%window][CONNECT]) + "\n";
			out += "Get:      " + str_pad(stats[GET], 9) +     str_pad(total[GET], 7) +     (slidingstats[(statstime+window-1)%window][GET]) + "\n";
			out += "Get Fail: " + str_pad(stats[GETFAIL], 9) + str_pad(total[GETFAIL], 7) + (slidingstats[(statstime+window-1)%window][GETFAIL]) + "\n";
			out += ":    " + str_pad(stats[0], 9) +   str_pad(total[0], 7) +   (slidingstats[(statstime+window-1)%window][0]) + "\n";
			//out += "Connections: " + clientdata.size() + "\n";
			
			//socket_write(sock, out + "\n");
			
			//unset(out, total, stat, i, k, v);
			
			break;
			
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
			//daily(debug[TIMEUPDATES]);
			System.exit(0);
			
		case VERSION:
			//socket_write(sock, "version\n");
			break;
			
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
			
		default:
			//myerror("unknown command: 'msg'", __LINE__);
			break;
		}
		return -1;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Object args1[] = {};
		BannerDatabase bdb = new BannerDatabase(new CampaignDB(), new PageValidatorFactory(Utilities.PageValidator1.class, args1));
		bdb.loadCoefficients(new HashMap<Banner, Float>());
		
		BannerServer bs = new BannerServer(bdb, new CampaignDB(), 1);
		int time = 0;
		for (int i = 0; i < 10000; i++){
			time += (int)(Math.random()*100);
			int size = (int) ((Math.random()*7.0) + 1);
			int userid = (int)(Math.random()*10);
			
			String params[] = 
			{String.valueOf(time),//int usertime=Integer.parseInt(params[0]);
					String.valueOf(size),//int size=Integer.parseInt(params[1]); 
					String.valueOf(userid),//int userid=Integer.parseInt(params[2]); 
					"23",//byte age=Byte.parseByte(params[3]); 
					"1",//byte sex=Byte.parseByte(params[4]); 
					"1",//short loc=Short.parseShort(params[5]); 
					"0",//String interestsStr=params[6]; 
					"index",//int page=Integer.parseInt(params[7]); 
					"???",//passback=params[8]; 
			"false"};//boolean debugGet=Boolean.parseBoolean(params[9]);
			bs.receive(GET, params);
		}
		
	}

	
}
