package com.nexopia.adblaster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerStat;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.HourlyStat;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.ServerStat;
import com.nexopia.adblaster.struct.TypeStat;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.EasyDatagramSocket;
import com.nexopia.adblaster.util.FastMap;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.Interests;
import com.nexopia.adblaster.util.StringArrayPageValidator;
import com.nexopia.adblaster.util.Utilities;

public class BannerServer {
	private static final int LOG_PORT = 5556;
	private static final int HIT_LOG_PORT = 6666;
	private static final String LOG_HOST = "localhost";
//	private static final String HIT_LOG_HOST = "10.0.0.85";
	private static final String HIT_LOG_HOST = "localhost";
	
	
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
	private final I_Policy policy;
	
	public static HashMap<String, Boolean> debug=new HashMap<String,Boolean>();
	{
		debug.put("tick", Boolean.FALSE);
		debug.put("connect", Boolean.FALSE);
		debug.put("get", Boolean.FALSE);
		debug.put("getlog", Boolean.TRUE);
		debug.put("getfail", Boolean.FALSE);
		debug.put("click", Boolean.TRUE);
		debug.put("timeupdates", Boolean.TRUE);
		debug.put("dailyrestart", Boolean.TRUE);
		debug.put("passback", Boolean.TRUE);
		debug.put("development", Boolean.FALSE);
	}
	
	static ServerStat stats = new ServerStat();
	static ServerStat slidingstats[] = new ServerStat[STATS_WINDOW];
	static {
		for (int i = 0; i < slidingstats.length; i++){
			slidingstats[i] = new ServerStat();
		}
	}
	
	static int currentwindow = 0;
	
	public BannerDatabase db;
	public CampaignDB cdb;
	private long creationTime;
	public HashMap<String,Integer> sizes;
	Integer sizes_array[] = {BANNER_BANNER, BANNER_LEADERBOARD,
			BANNER_BIGBOX, BANNER_SKY120, BANNER_SKY160,
			BANNER_BUTTON60, BANNER_VULCAN, BANNER_LINK};
	
	static int numservers;
	private static Random rand = new Random();
	//public banners;
	//public bannerids;
	//public bannersizes;
	//public HashMap<Integer, Campaign> bannercampaigns;
	//public HashMap<Integer, Integer> campaignids; // array( bannerid => campaignid );
	
	//public int time;
	private HashMap<ServablePropertyHolder,IntObjectHashMap<int[]>> viewMap = new HashMap<ServablePropertyHolder, IntObjectHashMap<int[]>>();
	Vector<Banner> banners = new Vector<Banner>();
	FastMap<Banner, BannerStat> bannerstats;
	private FastMap<Integer, TypeStat> viewstats = new FastMap<Integer,TypeStat>();
	private FastMap<Integer, TypeStat> clickstats = new FastMap<Integer,TypeStat>();
	private FastMap<Campaign, BannerStat> campaignstats = new FastMap<Campaign,BannerStat>();
	private FastMap<Banner, HourlyStat> hourlystats = new FastMap<Banner,HourlyStat>();

	private EasyDatagramSocket logsock;
	private EasyDatagramSocket hitlogsock;
	private int logserver_port;
	private int hitlogserver_port;
	private String logserver;
	private String hitlogserver;

	public BannerServer(BannerDatabase db, CampaignDB cdb, int numservers) {
		//this.policy = new OldPolicy(cdb);
		this.policy = new AdBlasterPolicy(db.getBanners());
		this.db = db;
		this.cdb = cdb;
		JDBCConfig.initThreadedSQLQueue();
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
		
		 try {
			logsock = new EasyDatagramSocket();
			hitlogsock = new EasyDatagramSocket();
			logserver = LOG_HOST;
			hitlogserver = HIT_LOG_HOST;
			logserver_port = LOG_PORT;
			hitlogserver_port = HIT_LOG_PORT;
			logsock.connect(new InetSocketAddress(logserver, logserver_port));
			hitlogsock.connect(new InetSocketAddress(hitlogserver, hitlogserver_port));
			logsock.setSoTimeout(20);
			hitlogsock.setSoTimeout(20);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		 
		 this.creationTime = System.currentTimeMillis();
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
		return this.db.add(id,new StringArrayPageValidator()) != null;
	}
	
	public boolean updateBanner(int id) {
		return this.db.update(id,new StringArrayPageValidator()) != null;
	}
	
	public void deleteBanner(int id) {
		this.db.delete(id);
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
			if (debug.get("development").booleanValue()) System.out.println(Arrays.toString(views));
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
		campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).view();
		viewstats.getOrCreate(Integer.valueOf(b.getSize()), TypeStat.class).hit(age, sex, loc, interests, page, time);
	}
	
	/**
	 *  Return an int array of the times the user has viewed the banner. 
	 */
	public int[] getViewsForUser(int userid, ServablePropertyHolder b) {
		/* Get records of all views for the banner.*/
		IntObjectHashMap<int[]> userViewMap = viewMap.get(b);
		if (userViewMap == null){
			userViewMap = new IntObjectHashMap<int[]>();
			viewMap.put(b, userViewMap);
		}
		
		/* From the above records, get all views for this user.*/
		int []views = userViewMap.get(userid);
		if (views == null){
			views = new int[b.getViewsPerUser()];
			userViewMap.put(userid, views);
		}
		return views;
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
	
	boolean hasReachedMaxViews(Banner b) {
		if ((b.getViews() + this.bannerstats.getOrCreate(b, BannerStat.class).dailyviews) >= b.getIntegerMaxViews())
			return true;
		if ((b.getCampaign().getViews() + this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).dailyviews) >= b.getCampaign().getIntegerMaxViews())
			return true;
		if (debug.get("development").booleanValue()) System.out.println("" + b.getCampaign().getViews() + " + " + this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).dailyviews + " < " + b.getCampaign().getIntegerMaxViews()); 
		return false;
			
	}

	private Banner weightedChoice(Vector<Banner> validBanners, int uid, int time) {
		double total = 0;
		double[] priorities = new double[validBanners.size()];
		for (int i=0; i<validBanners.size(); i++) {
			Banner b = validBanners.get(i);
			double priority = policy.getPriority(b, uid, time, this);
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

	public int getBestBanner(int usertime, int size, int userid, byte age,
			byte sex, short location, Interests interests, String page,
			boolean debug) {
		Vector<Banner> banners = new Vector<Banner>();
		for (Campaign campaign : cdb.getCampaigns()) {
			banners.addAll(campaign.getBanners(usertime, size, userid, age,
					sex, location, interests, page, debug));
		}
		Vector<Banner> validBanners = new Vector<Banner>();

		for (int i = 0; i < banners.size(); i++) {
			Banner banner_i = banners.get(i);
			boolean b1 = banner_i.isValidForUser(userid, usertime, debug, this);
			boolean b2 = banner_i.getCampaign().isValidForUser(userid, usertime,
					debug, this);
			boolean b3 = !hasReachedViewsPerDay(banner_i);
			boolean b4 = !hasReachedClicksPerDay(banner_i);
			boolean b5 = !hasReachedMaxViews(banner_i);

			if (debug)
				System.out.println("" + banner_i.getID() + ":" + b1 + ":" + b2 + ":"
						+ b3 + ":" + b4);
			if (b1 && b2 && b3 && b4 && b5) {
				validBanners.add(banner_i);
			}
		}

		Banner chosen = weightedChoice(validBanners, userid, usertime);
		if (chosen != null) {
			markBannerUsed(age, sex, location, interests, page,
					usertime, userid, chosen);
			if (debug)
				System.out.println("PICKED:" + chosen.getID());
			return chosen.getID();
		}
		if (debug)
			System.out.println("Number of valid banners:" + validBanners.size());

		return 0;

	}


	boolean hasReachedViewsPerDay(Banner b) {

		if (this.bannerstats.getOrCreate(b, BannerStat.class).dailyviews >= b.getIntegerMaxViewsPerDay()/numservers)
			return true;
		if (this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).dailyviews >= b.getCampaign().getIntegerMaxViewsPerDay()/numservers)
			return true;
		if (debug.get("development").booleanValue()) System.out.println("Banner Views: " + this.bannerstats.getOrCreate(b, BannerStat.class).dailyviews + ":" + b.getIntegerMaxViewsPerDay()/numservers);
		if (debug.get("development").booleanValue()) System.out.println("Campaign Views: " + this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).dailyviews + ":" + b.getCampaign().getIntegerMaxViewsPerDay()/numservers);
		return false;
	}
	
	boolean hasReachedClicksPerDay(Banner b) {
		if (this.bannerstats.get(b) == null){
			this.bannerstats.put(b, new BannerStat());
		}
		if (this.bannerstats.get(b).getDailyClicks() >= b.getIntegerMaxClicksperday()/numservers) {
			return true;
		}
		
		
		if (this.campaignstats.get(b.getCampaign()) == null){
			this.campaignstats.put(b.getCampaign(), new BannerStat());
		}
		if (this.campaignstats.get(b.getCampaign()).getDailyClicks() >= b.getCampaign().getIntegerMaxClicksperday()/numservers) {
			return true;
		}
		
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
	
	
	public static void bannerDebug(String str) {
		Date now = new Date();
		System.out.println("[ " + now + " ] " + str);
	}
	
	public static String str_pad(int s, int i){
		return "" + s;
	}
	
	static Vector<HashMap<Integer, Vector<Integer>>> recentviews = new Vector<HashMap<Integer, Vector<Integer>>>(VIEW_WINDOWS);
	{
		for (int i=0; i<VIEW_WINDOWS; i++) {
			recentviews.add(new HashMap<Integer, Vector<Integer>>());
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
			System.out.println("'" + command + "'" + " not found.");
		}
		return cmd;
	}
	
	public String receive(String command) throws IOException{
		String[] split = command.split(" ");
		int cmd = BLANK;
		String[] params;
		if (split.length > 0){
			params = new String[split.length-1];
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
		} else {
			params = new String[0];
			cmd = parseCommand(command);
		}
		
		//System.out.println(cmd);
		
		return receive(cmd, params);
	}
	
	public void secondly() {
		currentwindow = (currentwindow+1)%VIEW_WINDOWS;
		if (recentviews.size() < currentwindow+1) {
			recentviews.add(new HashMap<Integer, Vector<Integer>>());
		} else {
			recentviews.set(currentwindow, new HashMap<Integer, Vector<Integer>>());
		}
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
			Utilities.bannerDebug("minutely " + b.getID() + " " + bannerstat.dailyviews + 
					" " + bannerstat.getDailyClicks() + " " + bannerstat.passbacks);
		}
		
		if(bannerstat.hasChanged()) {
			JDBCConfig.queueQuery("UPDATE " + JDBCConfig.BANNER_TABLE + " SET lastupdatetime = "+time+", views = views + "+bannerstat.getCurrentViews()+", clicks = clicks + "+bannerstat.getCurrentClicks()+", passbacks = passbacks + "+bannerstat.passbacks+" WHERE id = " + b.getID());
			bannerstat.SetCurrentViews(0);
			bannerstat.setCurrentClicks(0);
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
			Utilities.bannerDebug("hour " + b.getID());
		}
		JDBCConfig.queueQuery("REPLACE INTO " + JDBCConfig.BANNERSTAT_TABLE + " (bannerid, time, views, potentialviews, clicks, passbacks) SELECT id, lastupdatetime, views, potentialviews, clicks, passbacks FROM banners WHERE id = " + b.getID());
		if (b.getPayType() == Banner.PAYTYPE_CPC) {
			HourlyStat hourlystat = hourlystats.get(b);
			b.setCoefficient(hourlystat.getClickRate()*b.getRealPayrate());
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
			JDBCConfig.queueQuery("INSERT INTO " + JDBCConfig.BANNERTYPESTAT_TABLE + " SET size = "+size+", time = "+time+", views = "+views.total+", clicks = "+clicks.total+", viewsdump = ?, clicksdump = ?", views.toXML(), clicks.toXML());
			//System.err.println("****" + "INSERT INTO " + JDBCConfig.BANNERTYPESTAT_TABLE + " SET size = "+size+", time = "+time+", views = "+views.total+", clicks = "+clicks.total+", viewsdump = '"+views.toXML()+"', clicksdump = '"+clicks.toXML()+"'" + "****");
		}
	}
	
	public void daily(Banner b, boolean debug) {
		bannerstats.get(b).dailyclicks = 0;
		bannerstats.get(b).dailyviews = 0;
	}
	
	public String receive(int cmd, String[] params) throws IOException{
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
			boolean debugGet=Boolean.parseBoolean(params[9]);

			Interests interests = new Interests(interestsStr, false);
			
			if(passback != 0)
				passbackBanner(passback, userid);
			
			int ret = getBestBanner(usertime, size, userid, age, sex, loc, interests, page, debugGet);
			
			if (debug.get("passback").booleanValue()) {
				Integer uid = Integer.valueOf(userid);
				if (passback != 0) {
					boolean hasSeen = false;
					String viewsstring = "";
					for (HashMap<Integer, Vector<Integer>> uidtoviews : recentviews) {
						if (uidtoviews.get(uid) != null && !uidtoviews.get(uid).isEmpty()) {
							Vector<Integer> userviews = uidtoviews.get(uid);
							for (Integer view : userviews) {
								viewsstring += " " + view;
								if (passback == view.intValue()) {
									hasSeen = true;
								}
							}
						}
					}
					if (!hasSeen) {
						bannerDebug("Invalid Passback: " + passback + ", Recently Viewed: " + viewsstring);
					}
				}
				Vector<Integer> currentUserWindow = recentviews.get(currentwindow).get(uid);
				if (currentUserWindow == null) {
					currentUserWindow = new Vector<Integer>();
					recentviews.get(currentwindow).put(uid, currentUserWindow);
				}
				currentUserWindow.add(Integer.valueOf(ret));
				uid.free();
				uid=null;
			}
			
			if(debug.get("get").booleanValue() || (debug.get("getfail").booleanValue() && (ret == NO_BANNER)))
				bannerDebug("get " + format(params) + " => " + ret);
			
			if(debug.get("getlog").booleanValue() && (logsock != null)){
				try {
					logsock.send("get " + format(params) + " => " + ret + "\n");
				} catch (IOException e) {
					logsock.disconnect();
					throw(e);
				}
			}
			
			if(ret == 0){
				stats.getfail++;
				slidingstats[statstime].getfail++;
			}
			
			Integer retInt = Integer.valueOf(ret);
			String retString = retInt.toString();
			
			//Hit logging
			try {

				hitlogsock.send("b");
				if (passback != 0) {
					hitlogsock.send("p");
				}
				if (retInt.intValue() == 0) {
					hitlogsock.send("f");
				}
			} catch (Exception ignored){}
			
			
			retInt.free();
			return retString;
		}
		case ADD: // "add id"
			id = Integer.parseInt(params[0]);
			db.add(id, new StringArrayPageValidator()); //addBannerD(params);
			bannerDebug("add " + Arrays.toString(params));
			break;
		case UPDATE: // "update id"
			//updateBannerD(params);
			id = Integer.parseInt(params[0]);
			db.add(id, new StringArrayPageValidator()); 
			bannerDebug("update " + Arrays.toString(params));
			break;
			
		case DEL: // "del id"
			//deleteBannerD(params);
			id = Integer.parseInt(params[0]);
			db.delete(id);
			bannerDebug("delete " + Arrays.toString(params));
			break;
			
		case ADDCAMPAIGN: // "addcampaign id"
			id = Integer.parseInt(params[0]);
			cdb.add(id);
			try {
				Statement st = JDBCConfig.createStatement();
				ResultSet rs = st.executeQuery("SELECT id FROM banners WHERE campaignid = " + id);
				while (rs.next()) {
					int bannerid = rs.getInt("id");
					db.add(bannerid, new StringArrayPageValidator());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			bannerDebug("addcampaign " + Arrays.toString(params));
			break;
			
		case UPDATECAMPAIGN: // "updatecampaign id"
			id = Integer.parseInt(params[0]);
			cdb.add(id);
			try {
				Statement st = JDBCConfig.createStatement();
				ResultSet rs = st.executeQuery("SELECT id FROM banners WHERE campaignid = " + id);
				while (rs.next()) {
					int bannerid = rs.getInt("id");
					db.add(bannerid, new StringArrayPageValidator());
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			bannerDebug("updatecampaign " + Arrays.toString(params));
			break;
			
		case DELCAMPAIGN: // "delcampaign id"
			id = Integer.parseInt(params[0]);
			for (Banner b: cdb.get(id).banners) {
				db.delete(b.getID());
			}
			cdb.delete(id);
			bannerDebug("deletecampaign "+ Arrays.toString(params));
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
			long ms = System.currentTimeMillis() - this.creationTime;
			int hours = (int) ms/3600000;
			int minutes = (int) (ms-(hours*3600000))/60000;
			int seconds = (int) ((ms-hours*3600000)-(minutes*60000))/1000; 
			return "Uptime: " + hours + ":" + minutes + ":" + seconds + "\n";  
			
		case SHOW:
			if (debug.get(params[0]) != null) {
				debug.put(params[0], Boolean.TRUE);
			} else {
				return params[0] + " is not a valid debug option.\n";
			}
			break;
			
		case HIDE:
			if (debug.get(params[0]) != null) {
				debug.put(params[0], Boolean.FALSE);
			} else {
				return params[0] + " is not a valid debug option.\n";
			}
			break;
			
		case SHUTDOWN: //dump stats, clean up most memory, and quit. Good for upgrading the server early :p
			//socket_write(sock, "shutting down\n");
			bannerDebug("shutting down");
			daily(true);
			//daily(debug[TIMEUPDATES]);
			System.exit(0);
			
		case VERSION:
			return CURRENT_VERSION;
			
		case RECONNECT:
			if(params != null){
				logserver = params[0];
				logserver_port = Integer.parseInt(params[1]);
			}
			
			logsock.connect(new InetSocketAddress(logserver, logserver_port));
			logsock.setSoTimeout(20);
			hitlogsock.connect(new InetSocketAddress(hitlogserver, hitlogserver_port));
			hitlogsock.setSoTimeout(20);
			return "success: " + logserver + "," + logserver_port + "\n" + "success: " + hitlogserver + "," + hitlogserver_port + "\n";
		case LOGSTAT:
			return (logsock.isConnected() ? "connected" : "not") + ": " + logserver + "," + logserver_port + "\n";
		case MINUTELY:
			minutely(BannerServer.debug.get("timeupdates").booleanValue());
			break;
		case HOURLY:
			hourly(BannerServer.debug.get("timeupdates").booleanValue());
			break;
		case DAILY:
			daily(BannerServer.debug.get("timeupdates").booleanValue());
			break;
		case CLICK:
			if (debug.get("click").booleanValue()) {
				bannerDebug("click " + Arrays.toString(params));
			}
			
			int bannerid=Integer.parseInt(params[0]); 
			Banner b = db.getBannerByID(bannerid);
			
			this.bannerstats.getOrCreate(b, BannerStat.class).click();
			this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).click();
			
			stats.click++;
			slidingstats[statstime%STATS_WINDOW].click++;
			hitlogsock.send("c");
			break;
		default:
			//myerror("unknown command: 'msg'", __LINE__);
			throw new UnsupportedOperationException("Command:" + cmd + " : Params: " + Arrays.toString(params));			
		}
		return null;
	}

	private String format(String[] params) {
		StringBuffer str = new StringBuffer("");
		for (int i = 0; i < params.length; i++){
			str.append(params[i]);
			if (i < params.length-1){
				str.append(" ");
			}
		}
		return str.toString();
	}

	private void passbackBanner(int passback, int userid) {
		Banner b = db.getBannerByID(passback);
		if (b != null) {
			bannerstats.getOrCreate(b, BannerStat.class).passbacks++;
			int[] userviews = getViewsForUser(userid, b);
			for (int i=0; i<userviews.length; i++) {
				userviews[i] = (int)(System.currentTimeMillis()/1000);
			}
		} else {
			if (debug.get("passback").booleanValue()) {
				bannerDebug("Attempted to passback banner " +passback+ ", which doesn't exist.");
			}
		}
	}

}
