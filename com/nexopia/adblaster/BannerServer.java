package com.nexopia.adblaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.nexopia.adblaster.Campaign.CampaignDB;
import com.nexopia.adblaster.Utilities.PageValidator;
import com.sleepycat.je.DatabaseException;

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
	
		public BannerDatabase db;
		public CampaignDB cdb;
		public HashMap<String,Integer> sizes;

		private static int numservers;

		//public banners;
		//public bannerids;
		//public bannersizes;
		//public HashMap<Integer, Campaign> bannercampaigns;
		//public HashMap<Integer, Integer> campaignids; // array( bannerid => campaignid );
		public Object[] dailyviews;
		public Object[] dailyclicks;

		public int time;

		public BannerServer(BannerDatabase db, CampaignDB cdb, int numservers) {
			this.db = db;
			this.cdb = cdb;
			BannerServer.numservers = numservers;
			this.sizes = new HashMap<String, Integer>();
			this.sizes.put("468x60", BANNER_BANNER);
			this.sizes.put("728x90", BANNER_LEADERBOARD);
			this.sizes.put("300x250", BANNER_BIGBOX);
			this.sizes.put("120x600", BANNER_SKY120);
			this.sizes.put("160x600", BANNER_SKY160);
			this.sizes.put("120x60", BANNER_BUTTON60);
			this.sizes.put("Voken", BANNER_VULCAN);
			this.sizes.put("Link", BANNER_LINK);
			
			this.dailyviews = new Object[this.sizes.size()];
			this.dailyclicks= new Object[this.sizes.size()];

			for(Integer size : this.sizes.values()) {
				this.dailyviews[size.intValue()] = new Object();
				this.dailyclicks[size.intValue()]= new Object();
			}

			this.time = (int) (System.currentTimeMillis()/1000);
		}
		
		public boolean addCampaign(int id){
			Campaign c = cdb.add(id);
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
		
		public int getBanner(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, String page, boolean debug){
			//String debugLog = "";
			//if (debug) debugLog += usertime+", "+size+", "+userid+", "+age+", "+sex+", "+loc+", "+page+", "+debug;
			
			Vector<Banner> valid = new Vector<Banner>();
			for (Campaign campaign : cdb.getCampaigns()){
				valid.addAll(campaign.getBanners(usertime, size, userid, age, sex, location, interests, page, debug));
			}
			if (!valid.isEmpty()) {
				Banner b = Utilities.priorityChoose(valid);
				b.hit(userid, usertime);
				return b.getID();
			} else {
				return 0;
			}
		}
		
		/*
		function addBanner(id){
			res = this->db->prepare_query("SELECT * FROM banners WHERE id = #", id);
			banner = res->fetchrow();
			unset(res);
			
			if(banner){
				newBanner = new banner(banner, this->numservers, this->bannercampaigns[banner['campaignid']]);
				this->bannercampaigns[banner['campaignid']]->addBanner(newBanner);
				this->campaignids[banner['id']] = banner['campaignid'];
				return true;
			}else
				return false;
		}


		function updateBanner(id){
			res = this->db->prepare_query("SELECT * FROM banners WHERE id = #", id);
			banner = res->fetchrow();
			unset(res);
			
			if(banner){
				if (banner['campaignid'] == this->campaignids[id]) { //same campaign just update the banner in it
					this->bannercampaigns[this->campaignids[id]]->getBannerID(id)->update(banner, this->numservers, this->bannercampaigns[this->campaignids[id]]);
				} else { //moved to a new campaign
					this->bannercampaigns[this->campaignids[id]]->deleteBanner(id);
					newBanner = new banner(banner, this->numservers, this->bannercampaigns[banner['campaignid']]);
					this->bannercampaigns[banner['campaignid']]->addBanner(newBanner);
					this->campaignids[banner['id']] = banner['campaignid'];
				}
				return true;
				if(isset(this->banners[banner['bannersize']][banner['id']])){ //exists where expected
			}else
				return false;
		}

		function deleteBanner(id){
			banner = this->bannercampaigns[this->campaignids[id]]->getBannerID(id);
			banner->minutely(this->db, this->time, false); //update stats
			this->bannercampaigns[this->campaignids[id]]->deleteBanner(id);
			unset(this->campaignids[id]);
		}

		function deleteCampaign(id) {
			if (isset(this->bannercampaigns[id])) {
				this->bannercampaigns[id]->minutely(this->db, this->time, false);
				foreach(this->bannercampaigns[id]->banners as banner) {
					unset(this->campaignids[banner->id]);
				}
				unset(this->bannercampaigns[id]);
			}
		}

		function passbackBanner(id, userid){
			if(isset(this->campaignids[id])) {
					this->bannercampaigns[this->campaignids[id]]->getBannerID(id)->passback(userid, this->time);
			}
		}

		function getBanner(usertime, size, userid, age, sex, loc, interests, page, debug = 0, id = null){
			debugLog = "";
			if (debug) debugLog .= "usertime, size, userid, age, sex, loc, page, debug";
			if(id === null){
				valid = array();
				foreach(this->bannercampaigns as &campaign){
					//check campaign size and campaign enabled
					if (debug) debugLog .= "\nPre-checking campaign campaign->id:"; 
					if(isset(campaign->sizes[size]) && campaign->enabled) {
						//check valid start/end date and valid sex
						if (debug) debugLog .= " 1"; 
						if(!(campaign->startdate >= this->time || (campaign->enddate && campaign->enddate <= this->time)) && campaign->sex[sex]) {
							if (debug) debugLog .= " 2"; 
							//bannerDebug("Passed prescreening for campaign->id");
							if(validBanners = campaign->valid(userid, age, sex, loc, interests, page, this->time, size, usertime, debug)){
								valid[campaign->id] = campaign->priority(userid, validBanners, this->time);
								banners[campaign->id] = validBanners;
								foreach (validBanners as bannerid) {
									this->bannercampaigns[campaign->id]->getBannerID(bannerid)->potentialHit();
								}
							}
						} else {
							//bannerDebug("Failed date check for campaign:campaign->id");
						}
					} else {
						//bannerDebug("Failed size check for campaign:campaign->id");
					}
				}
				
				if(count(valid) == 0) {
					if (debug) bannerDebug(debugLog); 
					return 0;
				}

				
				campaignID = chooseWeight(valid, false);
				id = this->bannercampaigns[campaignID]->getBanner(userid, banners[campaignID], this->time);

			}


			this->dailyviews[size]->hit(age, sex, loc, interests, page, usertime);

			this->bannercampaigns[campaignID]->hit(id, userid, this->time);

			if (debug) debugLog .= "\nChose banner id";
			if (debug) bannerDebug(debugLog); 

			return id;
		}

		function Banner(id, age, sex, loc, interests, page, time){
//			echo "id: id, age: age, sex: sex, loc: loc, page: page\n";

			if(!isset(this->campaignids[id])){
				echo "banner doesn't exist!\n";
				return;
			}

			this->bannercampaigns[this->campaignids[id]]->getBannerID(id)->();
			this->dailys[this->bannercampaigns[this->campaignids[id]]->getBannerID(id)->size]->hit(age, sex, loc, interests, page, time);
		}

		function minutely(debug){
			foreach(this->bannercampaigns as campaign) {
				campaign->minutely(this->db, this->time, debug);
			}

			return 1;
		}

		function hourly(debug){
			foreach(this->bannercampaigns as campaign) {
				campaign->hourly(this->db, this->time, debug);
			}
			return 2;
		}

		function daily(debug){
			if(debug)
			bannerDebug("daily");

			foreach(this->bannercampaigns as campaign){
				campaign->daily(debug);
			}
			foreach(this->sizes as size) {
				viewsdump = gzcompress(serialize(this->dailyviews[size]));
				sdump= gzcompress(serialize(this->dailys[size]));

				this->db->prepare_query("INSERT INTO bannertypestats SET size = #, time = #, views = #, s = #, viewsdump = ?, sdump = ?", size, this->time, this->dailyviews[size]->total, this->dailys[size]->total,  viewsdump, sdump);

				unset(viewsdump, sdump);

				this->dailyviews[size] = new bannerstats();
				this->dailys[size]= new bannerstats();
			}

			return 4;
		}

		function settime(time, debug){
			this->time = time;

			ret = 0;

			if(time % 60 == 0)
				ret |= this->minutely(debug);

			if(time % 3600 == 0)
				ret |= this->hourly(debug);

			if(time % 86400 == 3600*BANNER_DAILY_HOUR)
				ret |= this->daily(debug);

			return ret;
		}

		function getBanners(){
			/*res = this->db->prepare_query("SELECT * FROM banners WHERE moded = 'y'");
			result = this->db->prepare_query('SELECT * FROM bannercampaigns');
			while (line = result->fetchrow()) {
				this->bannercampaigns[line['id']] = new bannercampaign(line, this->numservers);
				foreach(this->bannercampaigns as campaign) {
					foreach(campaign->banners as banner) {
						this->campaignids[banner->id] = campaign->id;
					}
				}
			}
			unset(result);
		}
	
	
	*/
		
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
		FastMap(K key, V val){
			map = new HashMap<K,V>();
			map.put(key, val);
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
			} else {
				params[i] = s;
				i++;
			}
		}
		
		receive(cmd, params);
		return command + " returned.";
	}
	
	public void receive(int cmd, String[] params){
		
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
				boolean debugGet=Boolean.parseBoolean(params[9]);
				
				Interests interests = new Interests(interestsStr, false);
	
				//if(passback != "")
				//	passbackBanner(passback, userid);
								
				int ret = getBanner(usertime, size, userid, age, sex, loc, interests, page, debugGet);
	
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
	
				//socket_write(sock, "ret\n");
	
				//unset(ret, size, userid, age, sex, loc, interests, page, passback);
	
				break;
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
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Object args1[] = {};
		BannerDatabase bdb = new BannerDatabase(new CampaignDB(), new PageValidatorFactory(Utilities.PageValidator1.class, args1));
		bdb.loadCoefficients(new HashMap<Banner, Float>());
		
		String params[] = 
		{"11111",//int usertime=Integer.parseInt(params[0]);
		"640x480",//int size=Integer.parseInt(params[1]); 
		"203",//int userid=Integer.parseInt(params[2]); 
		"23",//byte age=Byte.parseByte(params[3]); 
		"1",//byte sex=Byte.parseByte(params[4]); 
		"1",//short loc=Short.parseShort(params[5]); 
		"0",//String interestsStr=params[6]; 
		"index",//int page=Integer.parseInt(params[7]); 
		"???",//passback=params[8]; 
		"false"};//boolean debugGet=Boolean.parseBoolean(params[9]);
		BannerServer bs = new BannerServer(bdb, new CampaignDB(), 1);
		bs.receive(GET, params);
		
	}

}
