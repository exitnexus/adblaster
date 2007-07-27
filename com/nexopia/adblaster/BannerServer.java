package com.nexopia.adblaster;

import com.nexopia.adblaster.util.XMLIntIntHashMapConverter;
import com.nexopia.adblaster.util.XMLIntObjectHashMapConverter;
import com.nexopia.adblaster.util.XMLInterestsConverter;
import com.nexopia.adblaster.util.XMLTimeTableConverter;
import com.nexopia.adblaster.util.XMLTypeStatConverter;
import com.thoughtworks.xstream.XStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
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
import java.lang.reflect.Method;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.db.CampaignDB;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerStat;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.ConfigFile;
import com.nexopia.adblaster.struct.HourlyStat;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.ServerStat;
import com.nexopia.adblaster.struct.TypeStat;
import com.nexopia.adblaster.util.EasyDatagramSocket;
import com.nexopia.adblaster.util.FastMap;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.Interests;
import com.nexopia.adblaster.util.LowMemMultiMap;
import com.nexopia.adblaster.util.StringArrayPageValidator;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.Utilities;
import com.nexopia.adblaster.util.LowMemMap.LowMemArray;
import com.vladium.utils.ObjectProfiler;

public class BannerServer {
	///////////////////////////static members////////////////////////////
	private static Random rand = new Random();
	
	public static final String CURRENT_VERSION = "0.0";
	public static final Integer BANNER_BANNER = new Integer(1);
	public static final Integer BANNER_LEADERBOARD = new Integer(2);
	public static final Integer BANNER_BIGBOX = new Integer(3);
	public static final Integer BANNER_SKY120 = new Integer(4);
	public static final Integer BANNER_SKY160 = new Integer(5);
	public static final Integer BANNER_BUTTON60 = new Integer(6);
	public static final Integer BANNER_VULCAN = new Integer(7);
	public static final Integer BANNER_LINK = new Integer(8);

	public static Integer sizes_array[] = {BANNER_BANNER, BANNER_LEADERBOARD,
			BANNER_BIGBOX, BANNER_SKY120, BANNER_SKY160,
			BANNER_BUTTON60, BANNER_VULCAN, BANNER_LINK};
	
	public static final int PAGE_DOMINANCE_OFF = Integer.MIN_VALUE;
	public static final int PAGE_DOMINANCE_POSSIBLE = Integer.MIN_VALUE+1;
	public static final int PAGE_DOMINANCE_TYPE = 0; //array position
	public static final int PAGE_DOMINANCE_TIME = 1; //array position
	public static final int PAGE_DOMINANCE_RETENTION_TIME = 60; //seconds to keep page ids around
	public static final int PAGE_DOMINANCE_NO_PAGE_ID = -1; //page id passed if no page id exists
	
	public static final int BANNER_SLIDE_SIZE = 8;
	public static final double BANNER_MIN_CLICKRATE = 0.0002;
	public static final double BANNER_MAX_CLICKRATE = 0.005;
	public static final int STATS_WINDOW = 60;
	private static final int NO_BANNER = 0;
	private static final int VIEW_WINDOWS = 10;
	
	public static final String RELOAD_COEFFICIENTS = "RELOAD_COEFFICIENTS";
	public static final String COLLECT_GARBAGE = "GC";
	public static final String MEMORY_STATS = "MEMSTAT";
	public static final String BANNER_INFO = "BANNER_INFO";
	public static final String RECONNECT_DB = "RECONNECT_DB";
	public static final String SIMULATE_GET = "SIMULATE_GET";
	public static final String RELOAD_FROM_DB = "RELOAD";
	
	private static HashMap<String, Method> commands = new HashMap<String,Method>();
	private static void registerCommand(String command, String methodName) {
		Class[] parameters = {String[].class};
		command = command.toUpperCase();
		try {
			BannerServer.commands.put(command, BannerServer.class.getDeclaredMethod(methodName, parameters));
		} catch (NoSuchMethodException e) {
			System.err.println("Registered BannerServer method not found for command " + command + ".");
			e.printStackTrace();
			try {
				BannerServer.commands.put(command, BannerServer.class.getDeclaredMethod("cmdDefault", parameters));
			} catch (NoSuchMethodException e2) {
				System.err.println("No default method for banner server commands found.");
				e2.printStackTrace();
			}
		}
	}
	static {
		BannerServer.registerCommand("GET", "cmdGet");
		BannerServer.registerCommand("ADD", "cmdAdd");
		BannerServer.registerCommand("DELETE", "cmdDelete");
		BannerServer.registerCommand("UPDATE", "cmdUpdate");
		BannerServer.registerCommand("ADDCAMPAIGN", "cmdAddCampaign");
		BannerServer.registerCommand("DELCAMPAIGN", "cmdDelCampaign");
		BannerServer.registerCommand("UPDATECAMPAIGN", "cmdUpdateCampaign");
		BannerServer.registerCommand("QUIT", "cmdQuit");
		BannerServer.registerCommand("STATS", "cmdStats");
		BannerServer.registerCommand("UPTIME", "cmdUptime");
		BannerServer.registerCommand("SHOW", "cmdShow");
		BannerServer.registerCommand("HIDE", "cmdHide");
		BannerServer.registerCommand("SHUTDOWN", "cmdShutdown");
		BannerServer.registerCommand("VERSION", "cmdVersion");
		BannerServer.registerCommand("RECONNECT", "cmdReconnect");
		BannerServer.registerCommand("LOGSTAT", "cmdLogStat");
		BannerServer.registerCommand("MINUTELY", "cmdMinutely");
		BannerServer.registerCommand("HOURLY", "cmdHourly");
		BannerServer.registerCommand("DAILY", "cmdDaily");
		BannerServer.registerCommand("CLICK", "cmdClick");
		BannerServer.registerCommand(RELOAD_COEFFICIENTS, "cmdReloadCoefficients");
		BannerServer.registerCommand(COLLECT_GARBAGE, "cmdCollectGarbage");
		BannerServer.registerCommand(MEMORY_STATS, "cmdMemoryStats");
		BannerServer.registerCommand(BANNER_INFO, "cmdBannerInfo");
		BannerServer.registerCommand(RECONNECT_DB, "cmdReconnectDB");
		BannerServer.registerCommand(SIMULATE_GET, "cmdSimulateGet");
		BannerServer.registerCommand(RELOAD_FROM_DB, "cmdReloadFromDB");
		BannerServer.registerCommand("XML", "cmdXML");
	}
	
	
	
	//////////////////////////instance members/////////////////////////////
	
	public HashMap<String, Boolean> debug=new HashMap<String,Boolean>();
	
	private I_Policy policy;
	
	private Vector<HashMap<Integer, Vector<Integer>>> recentviews = new Vector<HashMap<Integer, Vector<Integer>>>(VIEW_WINDOWS);
	
	private ServerStat stats;
	private ServerStat[] slidingstats;
	
	private int currentwindow;
	
	private BannerDatabase db;
	private CampaignDB cdb;
	private long creationTime;
	
	private int numservers;
	
	private LowMemMultiMap viewMap; //maps userid to an array of recently viewed times for banners
	private FastMap<Banner, BannerStat> bannerstats; //bannerstat objects for banners
	private FastMap<Campaign, BannerStat> campaignstats; //bannerstat objects for campaigns
	private FastMap<Integer, TypeStat> viewstats; //viewing stats by banner size
	private FastMap<Integer, TypeStat> clickstats; //click stats by banner size
	private FastMap<Banner, HourlyStat> hourlystats;
	private IntObjectHashMap<IntObjectHashMap<int[]>> pageIDDominance;
	
	private EasyDatagramSocket logsock;
	private EasyDatagramSocket hitlogsock;
	private int logserver_port;
	private int hitlogserver_port;
	private String logserver;
	private String hitlogserver;
	private int currentConnected;
	private ConfigFile configFile;

	public BannerServer(int numservers, ConfigFile config) {
		debug.put("tick", config.getBoolean("tick"));
		debug.put("connect", config.getBoolean("connect"));
		debug.put("get", config.getBoolean("get"));
		debug.put("getlog", config.getBoolean("getlog"));
		debug.put("getfail", config.getBoolean("getfail"));
		debug.put("click", config.getBoolean("click"));
		debug.put("timeupdates", config.getBoolean("timeupdates"));
		debug.put("dailyrestart", config.getBoolean("dailyrestart"));
		debug.put("passback", config.getBoolean("passback"));
		debug.put("development", config.getBoolean("development"));
		
		configFile = config;
		
		this.currentConnected = 0;
		this.viewMap = new LowMemMultiMap();
		this.viewstats = new FastMap<Integer,TypeStat>();
		this.clickstats = new FastMap<Integer,TypeStat>();
		this.campaignstats = new FastMap<Campaign,BannerStat>();
		this.hourlystats = new FastMap<Banner,HourlyStat>();
		this.pageIDDominance = new IntObjectHashMap<IntObjectHashMap<int[]>>();
		
		while (recentviews.size() < VIEW_WINDOWS) {
			recentviews.add(new HashMap<Integer, Vector<Integer>>());
		}
		this.currentwindow = 0;
		this.policy = new OldPolicy(cdb);
		//this.policy = new AdBlasterPolicy(db.getBanners());
		
		Object args1[] = {};

		PageValidatorFactory factory = new PageValidatorFactory(StringArrayPageValidator.class,args1);
		
		this.cdb = new CampaignDB(factory);
		this.db = new BannerDatabase(cdb, factory);
		
		
		JDBCConfig.initThreadedSQLQueue();
		this.numservers = numservers;
		this.bannerstats = new FastMap<Banner, BannerStat>();
		for(int i = 0; i < sizes_array.length; i++) {
			Integer size = BannerServer.sizes_array[i];
			this.viewstats.put(size, new TypeStat());
			this.clickstats.put(size, new TypeStat());
		}
		stats = new ServerStat();
		slidingstats = new ServerStat[STATS_WINDOW];
		for (int i = 0; i < slidingstats.length; i++){
			slidingstats[i] = new ServerStat();
		}
		
		try {
			
			logsock = new EasyDatagramSocket();
			hitlogsock = new EasyDatagramSocket();
			
			logserver = config.getString("log_host");
			hitlogserver = config.getString("hit_log_host");
			logserver_port = config.getInt("log_port");
			hitlogserver_port = config.getInt("hit_log_port");
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
			LowMemArray views = getViewsForUser(userid, b);
			
			/* Throw out the oldest, insert the new view. */
			for (int i = 0; i < views.size() - 1; i++){
				views.set(i, views.get(i+1));
			}
			views.set(views.size()-1, time);
			if (debug.get("development").booleanValue()) System.out.println(views);
		}
		
		if (b.getCampaign().getViewsPerUser() != 0){
			LowMemArray cviews = getViewsForUser(userid, b.getCampaign());
			
			/* Throw out the oldest, insert the new view. */
			for (int i = 0; i < cviews.size() - 1; i++){
				cviews.set(i, cviews.get(i+1));
			}
			cviews.set(cviews.size() - 1, time);
		}

		hourlystats.getOrCreate(b, HourlyStat.class).view();
		bannerstats.getOrCreate(b, BannerStat.class).view();
		campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).view();
		viewstats.getOrCreate(Integer.valueOf(b.getSize()), TypeStat.class).hit(age, sex, loc, interests, page, time);
	}
	
	/**
	 *  Return an int array of the times the user has viewed the banner. 
	 */
	public LowMemArray getViewsForUser(int userid, ServablePropertyHolder b) {
		return viewMap.get(userid, b);
	}
	
	/**
	 * Return a valid banner with the best score.  Currently, doesn't check
	 * many constraints.
	 * @param pageDominance 
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
		if ((b.getViews() + this.bannerstats.getOrCreate(b, BannerStat.class).getDailyViews()) >= b.getIntegerMaxViews())
			return true;
		if ((b.getCampaign().getViews() + this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).getDailyViews()) >= b.getCampaign().getIntegerMaxViews())
			return true;
		if (debug.get("development").booleanValue()) System.out.println("" + b.getCampaign().getViews() + " + " + this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).getDailyViews() + " < " + b.getCampaign().getIntegerMaxViews()); 
		return false;
			
	}

	private Banner weightedChoice(Vector<Banner> validBanners, int uid, int time, boolean debug) {
		if (debug)
			Utilities.bannerDebug("Weighted choice:\n");

		double total = 0;
		double[] priorities = new double[validBanners.size()];
		for (int i=0; i<validBanners.size(); i++) {
			Banner b = validBanners.get(i);
			double priority = policy.getPriority(b, uid, time, this);
			priorities[i] = priority;
			total += priority;
			if (debug)
				Utilities.bannerDebug("Banner " + b.getID() + " : " + priority);

		}
		double pick = rand.nextDouble()*total;
		if (debug)
			Utilities.bannerDebug("Picked number: " + pick + " \n");
		
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
			byte sex, short location, Interests interests, String page, int pageid,
			boolean debug) {
		Vector<Banner> banners = new Vector<Banner>();
		
		int pageDominance;
		
		if (pageIDDominance.get(userid) != null && pageIDDominance.get(userid).get(pageid) != null) {
			pageDominance = pageIDDominance.get(userid).get(pageid)[PAGE_DOMINANCE_TYPE];
		} else if (pageid == PAGE_DOMINANCE_NO_PAGE_ID) {
			pageDominance = PAGE_DOMINANCE_OFF;
		} else {
			pageDominance = PAGE_DOMINANCE_POSSIBLE;
		}
		
		if (debug) Utilities.bannerDebug("Phase one.\n");
		
		for (Campaign campaign : cdb.getCampaigns()) {
			if (debug)
				Utilities.bannerDebug("Checking campaign " + campaign.getID() + "\n");

			banners.addAll(campaign.getBanners(usertime, size, userid, age,
					sex, location, interests, page, pageDominance, debug));
		}
		
		if (debug) Utilities.bannerDebug("Phase two.\n");
		Vector<Banner> validBanners = new Vector<Banner>();

		for (int i = 0; i < banners.size(); i++) {
			Banner banner_i = banners.get(i);
			if (debug) Utilities.bannerDebug("Checking banner " + banner_i.getID());
			
			boolean b1 = banner_i.isValidForUser(userid, usertime, debug, this);
			boolean b2 = banner_i.getCampaign().isValidForUser(userid, usertime,
					debug, this);
			boolean b3 = !hasReachedViewsPerDay(banner_i);
			boolean b4 = !hasReachedClicksPerDay(banner_i);
			boolean b5 = !hasReachedMaxViews(banner_i);
			boolean b6 = pageDominance > 0;
			if (debug){
				Utilities.bannerDebug("Banner/User: " + b1);
				Utilities.bannerDebug("Campaign/User: " + b2);
				Utilities.bannerDebug("ViewDaily: "	+ b3);
				Utilities.bannerDebug("ClicksPerDay: " + b4);
				Utilities.bannerDebug("MaxViews: " + b5);
				Utilities.bannerDebug("PageDominance: " + b6);
			}
			
			if (b1 && b2 && ((b3 && b4 && b5) || b6)) {
				validBanners.add(banner_i);
			}
		}

		if (debug) Utilities.bannerDebug("Phase three.\n");
		
		Banner chosen = weightedChoice(validBanners, userid, usertime, debug);
		if (chosen != null) {
			if (pageDominance == PAGE_DOMINANCE_POSSIBLE) {
				if (chosen.getCampaign().getPageDominance()) {
					int[] idTimePair = new int[2];
					idTimePair[PAGE_DOMINANCE_TYPE] = chosen.getCampaign().getID();
					idTimePair[PAGE_DOMINANCE_TIME] = (int)System.currentTimeMillis()/1000;
					if (pageIDDominance.get(userid) == null) {
						pageIDDominance.put(userid, new IntObjectHashMap<int[]>());
					}
					pageIDDominance.get(userid).put(pageid, idTimePair);
				} else {
					int[] idTimePair = new int[2];
					idTimePair[PAGE_DOMINANCE_TYPE] = PAGE_DOMINANCE_OFF;
					idTimePair[PAGE_DOMINANCE_TIME] = (int)System.currentTimeMillis()/1000;
					if (pageIDDominance.get(userid) == null) {
						pageIDDominance.put(userid, new IntObjectHashMap<int[]>());
					}
					pageIDDominance.get(userid).put(pageid, idTimePair);
				}
			} else if (pageDominance != PAGE_DOMINANCE_OFF) {
				int[] idTimePair = pageIDDominance.get(userid).get(pageid);
				idTimePair[PAGE_DOMINANCE_TIME] = (int)System.currentTimeMillis()/1000;
			}
			markBannerUsed(age, sex, location, interests, page,
					usertime, userid, chosen);
			if (debug)
				System.out.println("PICKED:" + chosen.getID());
			return chosen.getID();
		}
		if (debug)
			Utilities.bannerDebug("Number of valid banners:" + validBanners.size());

		return 0;

	}


	boolean hasReachedViewsPerDay(Banner b) {
		if (this.bannerstats.getOrCreate(b, BannerStat.class).getDailyViews() >= b.getIntegerMaxViewsPerDay()/numservers)
			return true;
		if (this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).getDailyViews() >= b.getCampaign().getIntegerMaxViewsPerDay()/numservers)
			return true;
		if (debug.get("development").booleanValue()) System.out.println("Banner Views: " + this.bannerstats.getOrCreate(b, BannerStat.class).getDailyViews() + ":" + b.getIntegerMaxViewsPerDay()/numservers);
		if (debug.get("development").booleanValue()) System.out.println("Campaign Views: " + this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).getDailyViews() + ":" + b.getCampaign().getIntegerMaxViewsPerDay()/numservers);
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
	public int getBannerByCoef(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, String page, int pageDominance, boolean debug){
		//String debugLog = "";
		//if (debug) debugLog += usertime+", "+size+", "+userid+", "+age+", "+sex+", "+loc+", "+page+", "+debug;
		
		Vector<Banner> valid = new Vector<Banner>();
		for (Campaign campaign : cdb.getCampaigns()){
			valid.addAll(campaign.getBanners(usertime, size, userid, age, sex, location, interests, page, pageDominance, debug));
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
	
	public String receive(String command) throws IOException{
		String[] split = command.split(" ");
		command = split[0];
		Method commandMethod = commands.get("DEFAULT");
		String[] params;
		if (split.length > 1){
			params = new String[split.length-1];
			int i = 0;
			boolean first = true;
			for (String s : split) {
				if (first) {
					first = false;
				} else {
					params[i] = s.trim();
					i++;
				}
			}
		} else {
			params = new String[0];
		}
		commandMethod = commands.get(command.trim().toUpperCase());

		try {
			Object[] methodArgs = {params};
			return (String)commandMethod.invoke(this, methodArgs);
		} catch (Exception e) {
			bannerDebug("Error attempting to handle command: " + command);
			e.printStackTrace();
			return "Error attempting to handle command: " + command;
		}
	}
	
	public void secondly() {
		int t_sec = (int)(System.currentTimeMillis() / 1000);
		int statstime = ((t_sec+1) % STATS_WINDOW); //clear the next seconds stats so when we get to it it is clear.
		slidingstats[statstime] = new ServerStat();
		
		currentwindow = (currentwindow+1)%VIEW_WINDOWS;
		recentviews.set(currentwindow, new HashMap<Integer, Vector<Integer>>());
	}

	@SuppressWarnings("unchecked")
	public void minutely(boolean debug) {
		prunePageIDs();
		Collection<Banner> banners = (Collection<Banner>)this.db.getBanners();
		for (Banner b: banners) {
			minutely(b, (int)(System.currentTimeMillis()/1000), debug);
		}
	}
	
	private void minutely(Banner b, int time, boolean debug) {
		BannerStat bannerstat = this.bannerstats.getOrCreate(b, BannerStat.class);
		
		if (debug) {
			Utilities.bannerDebug("minutely " + b.getID() + " " + bannerstat.getDailyViews() + 
					" " + bannerstat.getDailyClicks() + " " + bannerstat.getPassbacks());
		}
		
		if(bannerstat.hasChanged()) {
			JDBCConfig.queueQuery("UPDATE " + JDBCConfig.BANNER_TABLE + " SET lastupdatetime = "+time+", views = views + "+bannerstat.getCurrentViews()+", clicks = clicks + "+bannerstat.getCurrentClicks()+", passbacks = passbacks + "+bannerstat.getPassbacks()+" WHERE id = " + b.getID());
			bannerstat.clearCurrent();
		}
	}
	
	private void prunePageIDs() {
		int[] userids = pageIDDominance.getKeyArray();
		int time = (int)System.currentTimeMillis()/1000 - PAGE_DOMINANCE_RETENTION_TIME;
		for (int userid: userids) {
			if (userid != 0) { //Weirdness from IntObjectHashMap, 0 points to every unassigned bucket in the hash.
				IntObjectHashMap<int[]> pageIDMap = pageIDDominance.get(userid);
				if (pageIDMap != null) {
					int[] pageids = pageIDMap.getKeyArray();
					for (int pageid: pageids) {
						if (pageid != 0) {
							if (pageIDMap.get(pageid) == null || pageIDMap.get(pageid)[PAGE_DOMINANCE_TIME] < time) {
								pageIDMap.remove(pageid);
							}
						}
					}
				}
			}
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
		long time = System.currentTimeMillis()/1000;
		for (Integer size: sizes_array) {
			TypeStat views = viewstats.get(size);
			TypeStat clicks = clickstats.get(size);
			//views.toXML();
			//clicks.toXML();
			JDBCConfig.queueQuery("INSERT INTO " + JDBCConfig.BANNERTYPESTAT_TABLE + " SET size = "+size+", time = "+time+", views = "+views.total+", clicks = "+clicks.total+", viewsdump = ?, clicksdump = ?", views.toXML(), clicks.toXML());
			//System.err.println("****" + "INSERT INTO " + JDBCConfig.BANNERTYPESTAT_TABLE + " SET size = "+size+", time = "+time+", views = "+views.total+", clicks = "+clicks.total+", viewsdump = '"+views.toXML()+"', clicksdump = '"+clicks.toXML()+"'" + "****");
		}
		JDBCConfig.finishQueries(); //Waits until the queue is empty.
		this.reload();
	}
	
	//records statistics, indicates a new connection was made.
	public void connection() {
		int t_sec = (int)(System.currentTimeMillis() / 1000);
		int statstime = (t_sec % STATS_WINDOW);
		slidingstats[statstime].connect++;
		stats.connect++;
		currentConnected++;
	}
	
	public void connectionClosed() {
		currentConnected--;
	}
	
	public int statstime() {
		int t_sec = (int)(System.currentTimeMillis() / 1000);
		return (t_sec % STATS_WINDOW);
	}
	
	//banner server command functions
	private String cmdDefault(String[] params) {
		return "Command not found.";
	}
	
	private String cmdGet(String[] params) {
		stats.get++;
		slidingstats[statstime()].get++;
		
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
		int pageid=Integer.parseInt(params[10]);
		
		Interests interests = new Interests(interestsStr, false);
		
		if(passback != 0)
			passbackBanner(passback, userid);
		
		int ret = getBestBanner(usertime, size, userid, age, sex, loc, interests, page, pageid, debugGet);
		
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
				logsock = null;
				//throw(e);
			}
		}
		
		if(ret == 0){
			stats.getfail++;
			slidingstats[statstime()].getfail++;
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
	
	private String cmdAdd(String[] params) {
		int id = Integer.parseInt(params[0]);
		db.add(id, new StringArrayPageValidator());
		bannerDebug("add " + Arrays.toString(params));
		return "Banner " + id + " added.";
	}
	
	private String cmdUpdate(String[] params) {
		int id = Integer.parseInt(params[0]);
		db.update(id, new StringArrayPageValidator());
		bannerDebug("update " + Arrays.toString(params));
		return "Banner " + id + " updated.";
	}
	
	private String cmdDelete(String[] params) {
		int id = Integer.parseInt(params[0]);
		db.delete(id);
		bannerDebug("delete " + Arrays.toString(params));
		return "Banner " + id + " deleted.";
	}
	
	private String cmdAddCampaign(String[] params) {
		int id = Integer.parseInt(params[0]);
		cdb.update(id);
		try {
			Statement st = JDBCConfig.createStatement();
			ResultSet rs = st.executeQuery("SELECT id FROM banners WHERE campaignid = " + id);
			while (rs.next()) {
				int bannerid = rs.getInt("id");
				db.update(bannerid, new StringArrayPageValidator());
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		bannerDebug("updatecampaign " + Arrays.toString(params));
		return "Campaign " + id + " updated.";
	}
	
	private String cmdUpdateCampaign(String[] params) {
		int id = Integer.parseInt(params[0]);
		cdb.update(id);
		try {
			Statement st = JDBCConfig.createStatement();
			ResultSet rs = st.executeQuery("SELECT id FROM banners WHERE campaignid = " + id);
			while (rs.next()) {
				int bannerid = rs.getInt("id");
				db.update(bannerid, new StringArrayPageValidator());
			}
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		bannerDebug("updatecampaign " + Arrays.toString(params));
		return "Campaign " + id + " updated.";
	}
	
	private String cmdDelCampaign(String[] params) {
		int id = Integer.parseInt(params[0]);
		for (Banner delete_banner: cdb.get(id).getBanners()) {
			db.delete(delete_banner.getID());
		}
		cdb.delete(id);
		bannerDebug("deletecampaign "+ Arrays.toString(params));
		return "Campaign " + id + " deleted.";
	}
	
	private String cmdQuit(String[] params) {
		//Nothing needs to be done to cleanup a given connection right now, the client simply needs to drop connection.
		return "Disconnecting...";
	}
	
	private String cmdStats(String[] params) {
		ServerStat totalstat = new ServerStat();
		for (ServerStat slidingstat: slidingstats) {
			totalstat.get += slidingstat.get;
			totalstat.getfail += slidingstat.getfail;
			totalstat.connect += slidingstat.connect;
			totalstat.click += slidingstat.click;
		}
		int statstime = statstime();
		String out  = "Uptime: " + (System.currentTimeMillis()/1000 - stats.starttime) + "\n";
		out += "Connect:  " + str_pad(stats.connect, 9) + " " + str_pad(totalstat.connect, 7) + " " + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].connect) + "\n";
		out += "Get:  " + str_pad(stats.get, 9) + " " + str_pad(totalstat.get, 7) + " " +  (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].get) + "\n";
		out += "Get Fail:  " + str_pad(stats.getfail, 9) + " " + str_pad(totalstat.getfail, 7) + " " + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].getfail) + "\n";
		out += "Click:  " + str_pad(stats.click, 9) + " " + str_pad(totalstat.click, 7) + " " + (slidingstats[(statstime+STATS_WINDOW-1)%STATS_WINDOW].click) + "\n";
		out += "Connections: " + currentConnected + "\n";
		return out;
	}
	
	private String cmdUptime(String[] params) {
		long ms = System.currentTimeMillis() - this.creationTime;
		int hours = (int) ms/3600000;
		int minutes = (int) (ms-(hours*3600000))/60000;
		int seconds = (int) ((ms-hours*3600000)-(minutes*60000))/1000;
		return "Uptime: " + hours + ":" + minutes + ":" + seconds + "\n";
	}
	
	private String cmdShow(String[] params) {
		if (params.length >= 1) {
			if (debug.get(params[0]) != null) {
				debug.put(params[0], Boolean.TRUE);
				return params[0] + " is now " + debug.get(params[0]);
			} else {
				return params[0] + " is not a valid debug option.\n";
			}
		} else {
			return "Incorrect parameters, should be: show <flag>";
		}
	}
	
	private String cmdHide(String[] params) {
		if (params.length >= 1) {
			if (debug.get(params[0]) != null) {
				
				debug.put(params[0], Boolean.FALSE);
				return params[0] + " is now " + debug.get(params[0]);
			} else {
				return params[0] + " is not a valid debug option.\n";
			}
		} else {
			return "Incorrect parameters, should be: hide <flag>";
		}
	}
	
	//dump stats, clean up most memory, and quit. Good for upgrading the server early :p
	private String cmdShutdown(String[] params) {
		bannerDebug("shutting down");
		daily(true);
		System.exit(0);	
		return "System shutdown.";
	}
	
	private String cmdVersion(String[] params) {
		return CURRENT_VERSION;	
	}
	
	private String cmdReconnect(String[] params) {
		if(params.length >= 2){
			logserver = params[0];
			logserver_port = Integer.parseInt(params[1]);
		}
		try {
			if (logsock == null)

				logsock = new EasyDatagramSocket();

			logsock.connect(new InetSocketAddress(logserver, logserver_port));
			logsock.setSoTimeout(20);
			hitlogsock.connect(new InetSocketAddress(hitlogserver, hitlogserver_port));
			hitlogsock.setSoTimeout(20);
			return "success: " + logserver + "," + logserver_port + "\n" + "success: " + hitlogserver + "," + hitlogserver_port + "\n";
		} catch (Exception e) {
			return "Reconnect failed with exception: " + e;
		}
	}
	
	private String cmdLogStat(String[] params) {
		return (logsock.isConnected() ? "connected" : "not") + ": " + logserver + "," + logserver_port + "\n";
	}
	
	private String cmdMinutely(String[] params) {
		minutely(this.debug.get("timeupdates").booleanValue());
		return "Running minutely.";
	}
	
	private String cmdHourly(String[] params) {
		hourly(this.debug.get("timeupdates").booleanValue());
		return "Running hourly.";
	}
	private String cmdDaily(String[] params) {
		daily(this.debug.get("timeupdates").booleanValue());
		return "Running daily.";
	}
	
	private String cmdClick(String[] params) {
		if (debug.get("click").booleanValue()) {
			bannerDebug("click " + Arrays.toString(params));
		}
		
		int bannerid=Integer.parseInt(params[0]);
		Banner b = db.getBannerByID(bannerid);
		
		this.bannerstats.getOrCreate(b, BannerStat.class).click();
		this.campaignstats.getOrCreate(b.getCampaign(), BannerStat.class).click();
		
		stats.click++;
		slidingstats[statstime()].click++;
		try {
			hitlogsock.send("c");
		} catch (Exception e) {
			bannerDebug("Error connecting to hit log server for click.");
		}
		return "Clicked " + bannerid;
	}
	
	private String cmdReloadCoefficients(String[] params) {
		//this.policy = new AdBlasterPolicy(db.getBanners());
		//return "Coefficients reloaded.";
		return "Reloading of coefficients is disabled under classic bannerserver serving policy.";
	}
	
	private String cmdCollectGarbage(String[] params) {
		bannerDebug("Performing garbage collection...");
		System.gc();
		bannerDebug("Garbarge collection complete.");
		return "Garbage collected.";
	}
	
	private String cmdMemoryStats(String[] params) {
		String stats = "";
		stats += "bannerstats: " + ObjectProfiler.sizeof(bannerstats) + " bytes\n";
		stats += "campaignstats: " + ObjectProfiler.sizeof(campaignstats) + " bytes\n";
		stats += "viewstats: " + ObjectProfiler.sizeof(viewstats) + " bytes\n";
		stats += "clickstats: " + ObjectProfiler.sizeof(clickstats) + " bytes\n";
		stats += "hourlystats: " + ObjectProfiler.sizeof(hourlystats) + " bytes\n";
		stats += "viewMap: " + ObjectProfiler.sizeof(viewMap) + " bytes\n";
		stats += "pageIDDominance: " + ObjectProfiler.sizeof(pageIDDominance) + " bytes\n";
		stats += "sqlQueue: " + ObjectProfiler.sizeof(JDBCConfig.getSQLQueue()) + " bytes\n";
		stats += "Database connection: " + JDBCConfig.sizeofCon() + " bytes\n";
		stats += "recentviews: " + ObjectProfiler.sizeof(recentviews) + " bytes\n";
		stats += "slidingstats: " + ObjectProfiler.sizeof(slidingstats) + " bytes\n";
		return stats;
	}
	
	private String cmdBannerInfo(String[] params) {
		int bannerid=Integer.parseInt(params[0]);
		Banner b = db.getBannerByID(bannerid);
		if (b != null) {
			String bannerInfo = "Banner ID: " + b.getID() + "\n";
			bannerInfo += "Campaign ID: " + b.getCampaign().getID() + "\n";
			bannerInfo += "Size: " + b.getSize() + "\n";
			bannerInfo += "Interests: " + b.getInterests() + "\n";
			bannerInfo += "Locations: " + b.getLocations() + "\n";
			bannerInfo += "Ages: " + b.getAges() + "\n";
			bannerInfo += "Sexes: " + b.getSexes() + "\n";
			bannerInfo += "Min Views Per Day: " + b.getMinViewsPerDay() + "\n";
			bannerInfo += "Max Views Per Day: " + b.getIntegerMaxViewsPerDay() + "\n";
			bannerInfo += "Max Views: " + b.getIntegerMaxViews() + "\n";
			bannerInfo += "Views Per User: " + b.getIntegerMaxViewsPerUser() + "/" + b.getLimitByPeriod() + " seconds\n";
			bannerInfo += "Pay Rate: " + b.getRealPayrate() + "\n";
			bannerInfo += "Pay Type: " + b.getPayType() + "\n";
			return bannerInfo;
		} else {
			return "Banner " + bannerid + " doesn't exist.";
		}
	}
	
	private String cmdReconnectDB(String[] params) {
		if (JDBCConfig.initDBConnection(configFile)) {
			return "Successfully reconnected to database.";
		} else {
			return "Failed to reconnect to database.";
		}
	}
	
	private String cmdSimulateGet(String[] params) {
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
		int pageid=Integer.parseInt(params[10]);
		
		Interests interests = new Interests(interestsStr, false);
		
		if(passback != 0)
			passbackBanner(passback, userid);
		
		OutputStream str = new ByteArrayOutputStream();
		Utilities.setDebugLog(str);
		getBestBanner(usertime, size, userid, age, sex, loc, interests, page, pageid, true);
		return str.toString();
	}
	
	private String cmdReloadFromDB(String params[]) {
		minutely(true);
		hourly(true);
		daily(true);
		return "Banner and campaign data reloaded from the database.";
	}
	
	private void reload() {
		this.viewstats = new FastMap<Integer,TypeStat>();
		this.clickstats = new FastMap<Integer,TypeStat>();
		this.campaignstats = new FastMap<Campaign,BannerStat>();
		this.hourlystats = new FastMap<Banner,HourlyStat>();
		this.bannerstats = new FastMap<Banner, BannerStat>();
		
		Object args1[] = {};
		PageValidatorFactory factory = new PageValidatorFactory(StringArrayPageValidator.class,args1);
		this.cdb = new CampaignDB(factory);
		this.db = new BannerDatabase(cdb, factory);
		
		for(int i = 0; i < sizes_array.length; i++) {
			Integer size = BannerServer.sizes_array[i];
			this.viewstats.put(size, new TypeStat());
			this.clickstats.put(size, new TypeStat());
		}
	}
	
	private String cmdXML(String params[]) {
		return this.toXML();
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
			bannerstats.getOrCreate(b, BannerStat.class).passback();
			LowMemArray userviews = getViewsForUser(userid, b);
			for (int i=0; i<userviews.size(); i++) {
				userviews.set(i, (int)(System.currentTimeMillis()/1000));
			}
		} else {
			if (debug.get("passback").booleanValue()) {
				bannerDebug("Attempted to passback banner " +passback+ ", which doesn't exist.");
			}
		}
	}
	
	public int numservers() {
		return this.numservers;
	}
	
	public BannerStat getBannerStat(Banner b) {
		return this.bannerstats.get(b);
	}
	
	public String toXML() {
		XStream xstream = new XStream();
		xstream.registerConverter(new XMLInterestsConverter());
		xstream.registerConverter(new XMLTimeTableConverter());
		xstream.registerConverter(new XMLIntIntHashMapConverter());
		xstream.registerConverter(new XMLIntObjectHashMapConverter());
		xstream.registerConverter(new XMLTypeStatConverter());
		xstream.alias("integer", Integer.class);
		xstream.alias("banner", Banner.class);
		xstream.alias("typestat", TypeStat.class);
		
		String xml = "";
		xml += xstream.toXML(this);
		return xml;
	}

}
