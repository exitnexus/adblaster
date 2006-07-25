/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;



class Banner {
	public static final int PAYRATE_INHERIT = -1;
	public static final int PAYTYPE_CPM = 0;
	public static final int PAYTYPE_CPC = 1;
	public static final int PAYTYPE_INHERIT = 2;
	public static final int MAXRATE = 1000;
	
	Interests interests;
	int id;
	int payrate;
	byte paytype;
	private int viewsperday;
	Vector<Integer> locations;
	Vector<Integer> ages;
	Vector<Integer> sexes;
	Vector<Integer> pages;
	private int viewsperuser;
	private int limitbyperiod;
	Campaign campaign;
	int index;
	private byte size;
	private int minviewsperday;
	private int startdate;
	private int enddate;
	private boolean enabled;
	private int dailyviews;
	private int dailyclicks;
	private int clicksperday;
	private HashMap<Integer, int[]> userViewTimes; //int[0] is the position of the oldest view, int[1-viewsperuser] are timestamps
	
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	/*Banner() {
		interests = new Interests();
		id = counter(); //TODO Banners need an ID we can track them by
		maxHits = (int) (Math.pow((Math.random()-0.5) * 2,2) * 200) + 1;
		this.payrate = (int)(Math.random()*10);
	}*/
	
	/**
	 * @return Returns the campaignID.
	 */
	public Campaign getCampaign() {
		return campaign;
	}
	/**
	 * @param campaignID The campaignID to set.
	 */
	public void setCampaign(Campaign campaign) {
		this.campaign = campaign;
	}
	
	
	Banner(int id, int payrate, int maxHits, int campaignID, Vector<Integer> locations, Vector<Integer> ages, Vector<Integer> sexes, Interests interests) {
		this.index = counter();
		this.id = id;
		this.payrate = payrate;
		this.viewsperday = (maxHits==0?Integer.MAX_VALUE:maxHits);
		this.campaign = Campaign.get(campaignID);
		this.locations = locations;
		this.ages = ages;
		this.sexes = sexes;
		this.interests = interests;
	}
	
	@SuppressWarnings("unchecked") Banner(Banner b) {
		this.index = counter();
		this.id = b.id;
		this.viewsperday = b.viewsperday;
		this.campaign = b.campaign;
		this.locations = (Vector<Integer>) b.locations.clone();
		this.ages = (Vector<Integer>) b.ages.clone();
		this.pages = (Vector<Integer>) b.pages.clone();
		this.size = b.size;
		this.paytype = b.paytype;
		this.payrate = b.payrate;
		this.viewsperuser = b.viewsperuser;
		this.limitbyperiod = b.limitbyperiod;
		this.interests = new Interests(b.interests);
		this.startdate = b.startdate;
		this.enddate = b.enddate;
		this.enabled = b.enabled;
	}
	
	
	Banner(ResultSet rs) throws SQLException {
		this.index = counter();
		this.campaign = null;
		this.update(rs);
	}
	
	int getID() {
		return id;
	}
	
	public String toString(){
		String s = "Index: " + this.index + "\n";
		s += "ID: " + this.id + '\n';
		s += "Payrate:" + this.getRealPayrate() + '\n' ;
		s += "max hits:" + this.viewsperday + '\n' ;
		s += "locations:" + this.locations + '\n' ;
		s += "ages:" + this.ages + this.ages.isEmpty() + '\n' ;
		s += "sexes:" + this.sexes + this.sexes.isEmpty() + '\n' ;
		s += "views per user:" + this.viewsperuser + '\n' ;
		s += "views period:" + this.limitbyperiod + '\n' ;
		s += "size: " + this.size + '\n';
		return s;
	}
	public Vector<Integer> getAges() {
		return ages;
	}
	public void setAges(Vector<Integer> ages) {
		this.ages = ages;
	}
	public Interests getInterests() {
		return interests;
	}
	public void setInterests(Interests interests) {
		this.interests = interests;
	}
	public Vector<Integer> getLocations() {
		return locations;
	}
	public void setLocations(Vector<Integer> locations) {
		this.locations = locations;
	}
	public int getViewsperday() {
		return viewsperday;
	}
	public void setViewsperday(int maxHits) {
		this.viewsperday = maxHits;
	}
	public Vector<Integer> getSexes() {
		return sexes;
	}
	public void setSexes(Vector<Integer> sexes) {
		this.sexes = sexes;
	}
	
	/* The banner's value as it relates to a particular daily instance;
	 * In other words, its value as related to the learning algorithm.
	 * The value is usually the same as the payrate, unless we need to 
	 * reach a minimum number of views.
	 */
	public int getPayrate(AbstractAdBlasterInstance i) {
		if (i.bannerCount(this) < this.minviewsperday || 
				i.bannerCount(this) < this.getCampaign().getMinViewsPerDay() ){
			return MAXRATE;
		}
		if (payrate == PAYRATE_INHERIT) {
			return campaign.getPayrate();
		} else {
			return payrate;
		}
	}
	
	/*The payrate that is actually assigned to the banner.*/
	public int getRealPayrate() {
		if (payrate == PAYRATE_INHERIT) {
			return campaign.getPayrate();
		} else {
			return payrate;
		}
	}
	public void setPayrate(int payrate) {
		this.payrate = payrate;
	}

	public int getLimitbyperiod() {
		return limitbyperiod;
	}

	public void setLimitbyperiod(int limitbyperiod) {
		this.limitbyperiod = limitbyperiod;
	}

	public int getViewsperuser() {
		return viewsperuser;
	}

	public void setViewsperuser(int viewsperuser) {
		this.viewsperuser = viewsperuser;
	}

	/**
	 * @param u
	 * @return
	 */
	public boolean validUser(User u) {
		return (campaign.validUser(u) && 
				validLocation(u.getLocation()) &&
				validAge(u.getAge()) &&
				validSex(u.getSex()) &&
				validInterests(u.getInterests()));
	}

	/**
	 * @param interests2
	 * @return
	 */
	public boolean validInterests(Interests userInterests) {
		return userInterests.matches(interests);
	}

	/**
	 * @param sex
	 * @return
	 */
	public boolean validSex(byte sex) {
		if (sexes.isEmpty()) {
			return true;
		}
		Integer I = Integer.valueOf(sex);
		boolean valid = sexes.contains(I); 
		I.free();
		return valid;
		
	}

	/**
	 * @param location
	 * @return
	 */
	public boolean validLocation(short location) {
		Integer I = Integer.valueOf(location);
		boolean valid;
		if (locations.get(0).equals(Integer.NEGATE)) {
			valid = !locations.contains(I); 
		} else {
			valid = locations.contains(I);
		}
		I.free();
		return valid;
	}

	/**
	 * @param age
	 * @return
	 */
	public boolean validAge(byte age) {
		Integer I = Integer.valueOf(age);
		boolean valid;
		if (ages.get(0).equals(Integer.NEGATE)) {
			valid = !ages.contains(I); 
		} else {
			valid = ages.contains(I);
		}
		I.free();
		return valid;
	}
	
	private byte getPayType() {
		if (this.paytype == Banner.PAYTYPE_INHERIT) {
			return campaign.getPayType();
		} else {
			return this.paytype;
		}
	}

	public byte getSize() {
		return this.size;
	}

	public boolean validPage(int page) {
		if (pages.get(0) == Integer.NEGATE) {
			for (int i=1; i<pages.size(); i++) {
				if (pages.get(i).intValue() == page) {
					return false;
				}
			}
			return true;
		} else {
			for (int i=1; i<pages.size(); i++) {
				if (pages.get(i).intValue() == page) {
					return true;
				}
			}
			return false;
		}
	}

	public int getMinviewsperday() {
		return minviewsperday;
	}

	public static boolean precheck(ResultSet rs) {
		try {
			boolean enabled = rs.getString("ENABLED").equals("y");
			long startdate = rs.getLong("STARTDATE")*1000;
			long enddate = rs.getLong("ENDDATE")*1000;
			boolean validDate = true;
			if (startdate != 0){
				if (startdate < System.currentTimeMillis()){
					validDate = false;
				}
			}
			
			if (enddate != 0){
				if (enddate < System.currentTimeMillis()) {
					validDate = false;
				}
			}
			boolean validViews = true;
			int maxviews = rs.getInt("MAXVIEWS");
			int views = rs.getInt("VIEWS");
			if ((maxviews != 0 && views >= maxviews)) {
				validViews = false;
			}
			return enabled && validDate && validViews;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public Banner update(ResultSet rs) throws SQLException {
		this.id = rs.getInt("ID");
		this.viewsperday = rs.getInt("VIEWSPERDAY");
		viewsperday = (viewsperday==0?Integer.MAX_VALUE:viewsperday);
		int ci = rs.getInt("CAMPAIGNID");
		if (this.campaign == null) {
			this.campaign = Campaign.get(ci);
			if (campaign == null){
				System.out.println(ci + ":" + this.id);
				throw new SQLException();
			}
			this.campaign.addBanner(this);
		} else if (this.campaign.getID() != rs.getInt("CAMPAIGNID")) {
			this.campaign.removeBanner(this);
			this.campaign = Campaign.get(ci);
			if (campaign == null){
				System.out.println(ci + ":" + this.id);
				throw new SQLException();
			}
			this.campaign.addBanner(this);
		} else {
			this.campaign = Campaign.get(ci);
			if (campaign == null){
				System.out.println(ci + ":" + this.id);
				throw new SQLException();
			}
		}
		this.locations = Utilities.stringToNegationVector(rs.getString("LOC"));
		this.ages = Utilities.stringToNegationVector(rs.getString("AGE"));
		this.sexes = Utilities.stringToVector(rs.getString("SEX"));
		this.pages = Utilities.stringToPageNegationVector(rs.getString("PAGE"));
		this.size = rs.getByte("BANNERSIZE");
		this.paytype = rs.getByte("PAYTYPE");
		this.payrate = rs.getInt("PAYRATE");
		this.minviewsperday = rs.getInt("MINVIEWSPERDAY");
		this.startdate = rs.getInt("STARTDATE");
		this.enddate = rs.getInt("ENDDATE");
		this.enabled = rs.getString("ENABLED").equals("y");
		
		if (this.getPayType() == Banner.PAYTYPE_CPC) {
			try {
				this.payrate = (int)(this.getRealPayrate()*((double)rs.getInt("CLICKS")/(double)rs.getInt("VIEWS")));
			} catch (Exception e) {
				e.printStackTrace();
				this.payrate = rs.getInt("PAYRATE")/100; //assume 1% clickthrough if we have no data
			}
		}
		/* INTEGER viewsperday
		 * INTEGER clicksperday
		 * INTEGER UNSIGNED viewsperuser
		 * CHAR limitbyhour
		 * INTEGER UNSIGNED limitbyperiod
		 */
		this.viewsperuser = rs.getInt("VIEWSPERUSER"); 
		this.limitbyperiod = rs.getInt("LIMITBYPERIOD"); 

		this.interests = new Interests(rs.getString("INTERESTS"), true);
		return this;
	}

	public void hit() {
		// TODO Auto-generated method stub
		
	}

	public boolean valid(int time, int size, int userid, byte age, byte sex, short location, Interests interests2, int page, boolean debug) {
		String debugLog = "";
		if (debug) debugLog += "Checking banner this.id:";

		//Utilities.bannerDebug("Testing banner: this.id");
		if(!this.enabled) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 1";
		//if(!this.moded) {
		//	return false;
		//}
		
		//date
		if(this.startdate >= time || (this.enddate != 0 && this.enddate <= time)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 2";
		//Utilities.bannerDebug("testing size: this.size == size");
		//size
		if(this.size != size) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 3";

		//targetting
		//age
		//Utilities.bannerDebug("testing age");
		if (!this.validAge(age)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 4";

		//sex
		//Utilities.bannerDebug("testing sex");
		if(!this.validSex(sex)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 5";
		
		//location
		//Utilities.bannerDebug("testing location");
		if(!this.validLocation(location)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 6";
		//page
		//Utilities.bannerDebug("testing page");
		if(this.validPage(page)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 7";
		//interests
		//Utilities.bannerDebug("testing interests");
		if(this.validInterests(interests2)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 8";
		//time
		//Utilities.bannerDebug("testing time");
		
		if (!this.validTime(time)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		//day = gmdate("w", usertime);
		//hour = gmdate("G", usertime);
		//if (!this.allowedtimes.validHours[day][hour]) {
		//	if (debug) Utilities.bannerDebug(debugLog);
		//	return false;
		//}
		if (debug) debugLog += " 9";
		//payment available
		//if (this.campaign.clienttype == "payinadvance") {
		//	if (this.credits + this.campaign.credits < this.payrate()) {
		//		return false;
		//	}
		//}
		
		//frequency capping
		//this period (day/hour)
		//Utilities.bannerDebug("testing frequency capping");
		if(this.viewsperday != 0 && this.dailyviews >= this.viewsperday) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 10";
		if(this.clicksperday != 0 && this.dailyclicks >= this.clicksperday) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 11";
		//views per user
		if(!this.validUserTime(userid, time)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 12";
		//Utilities.bannerDebug("valid banner: this.id");
		//all else works
		if (debug) Utilities.bannerDebug(debugLog);
		return true;
	}

	private boolean validTime(int time) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean validUserTime(int userid, int time) {
		if (this.viewsperuser == 0) {
			return true;
		} 
		Integer uid = Integer.valueOf(userid);
		int[] times = this.userViewTimes.get(uid);
		uid.free();
		if (times == null) {
			return true;
		} else {
			if (times[times[0]] > time-this.limitbyperiod) {
				return false;
			} else {
				return true;
			}
		}
	}

}