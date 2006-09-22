/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.struct;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import com.nexopia.adblaster.AbstractAdBlasterInstance;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.Interests;
import com.nexopia.adblaster.util.PageValidator;
import com.nexopia.adblaster.util.Utilities;



public class Banner extends ServablePropertyHolder{
	public static final int PAYRATE_INHERIT = -1;
	public static final int PAYTYPE_CPM = 0;
	public static final int PAYTYPE_CPC = 1;
	public static final int PAYTYPE_INHERIT = 2;
	public static final int MAXRATE = 1000;
	
	int id;
	int payrate;
	byte paytype;
	Campaign campaign;
	private double coefficient;
	private byte size;
	static int count = 0;
	private int views;
	private int clicks;
	
	public static int counter(){
		return count++;
	}
	
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
	
/*	
	Banner(int id, int payrate, int maxHits, int campaignID, Vector<Integer> locations, Vector<Integer> ages, Vector<Integer> sexes, Interests interests, Campaign c, PageValidator pv, String allowed) {
		this.index = counter();
		this.id = id;
		this.payrate = payrate;
		this.viewsperday = (maxHits==0?Integer.MAX_VALUE:maxHits);
		this.campaign = c;
		this.locations = locations;
		this.ages = ages;
		this.sexes = sexes;
		this.interests = interests;
		this.pages = pv;
		this.allowedTimes = new TimeTable(allowed);
		this.coefficient = this.getPayRate();
	}
	
	@SuppressWarnings("unchecked") Banner(Banner b) {
		this.index = counter();
		this.id = b.id;
		this.viewsperday = b.viewsperday;
		this.campaign = b.campaign;
		this.locations = (Vector<Integer>) b.locations.clone();
		this.ages = (Vector<Integer>) b.ages.clone();
		this.pages = (PageValidator) b.pages.clone();
		this.size = b.size;
		this.paytype = b.paytype;
		this.payrate = b.payrate;
		this.viewsperuser = b.viewsperuser;
		this.limitbyperiod = b.limitbyperiod;
		this.interests = new Interests(b.interests);
		this.startdate = b.startdate;
		this.enddate = b.enddate;
		this.allowedTimes = (TimeTable) b.allowedTimes.clone();
		this.enabled = b.enabled;
		this.coefficient = this.getPayRate();
	}
*/	
	
	public Banner(ResultSet rs, CampaignDB cdb, PageValidator pv) throws SQLException {
		this.index = counter();
		this.pages = pv;
		this.campaign = null;
		this.update(rs, cdb);
		java.sql.Statement s = JDBCConfig.createStatement();
		s.execute("SELECT views FROM " + JDBCConfig.BANNERSTAT_TABLE + " WHERE id = " + this.id);
		ResultSet rs2 = s.getResultSet();
		if (rs2.first())
			views = rs2.getInt("VIEWS");

		s.execute("SELECT clicks FROM " + JDBCConfig.BANNERSTAT_TABLE + " WHERE id = " + this.id);
		ResultSet rs3 = s.getResultSet();
		if (rs3.first())
			clicks = rs3.getInt("clicks");
		
	}
	
	public int getID() {
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
		s += "views per user:" + this.viewsPerUser + '\n' ;
		s += "views period:" + this.limitByPeriod + '\n' ;
		s += "size: " + this.size + '\n';
		return s;
	}
	public Vector<Integer> getAges() {
		return ages;
	}
	/*The payrate that is actually assigned to the banner.*/
	public int getRealPayrate() {
		if (payrate == PAYRATE_INHERIT) {
			return campaign.getPayrate();
		} else {
			return payrate;
		}
	}
	public byte getPayType() {
		if (this.paytype == Banner.PAYTYPE_INHERIT) {
			return campaign.getPayType();
		} else {
			return this.paytype;
		}
	}

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

	public void setPayrate(int payrate) {
		this.payrate = payrate;
	}


	public int getPayRate() {
		if (this.payrate == Banner.PAYRATE_INHERIT) {
			return campaign.getPayrate();
		} else {
			return this.payrate;
		}
	}
	
	public byte getSize() {
		return this.size;
	}

	public static boolean precheck(ResultSet rs) {
		try {
			boolean enabled = rs.getString("ENABLED").equals("y");
			long startdate = rs.getLong("STARTDATE")*1000;
			long enddate = rs.getLong("ENDDATE")*1000;
			boolean validDate = true;
			if (startdate != 0){
				if (startdate > System.currentTimeMillis() + 86400*1000){
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
			e.printStackTrace();
			return false;
		}
	}


	public Banner update(ResultSet rs, CampaignDB cdb) throws SQLException {
		this.id = rs.getInt("ID");
		super.update(rs);
		int ci = rs.getInt("CAMPAIGNID");
		if (this.campaign == null) {
			this.campaign = cdb.get(ci);
			if (campaign == null){
				System.out.println(ci + ":" + this.id);
				throw new SQLException();
			}
			this.campaign.addBanner(this);
		} else if (this.campaign.getID() != rs.getInt("CAMPAIGNID")) {
			this.campaign.removeBanner(this);
			this.campaign = cdb.get(ci);
			if (campaign == null){
				System.out.println(ci + ":" + this.id);
				throw new SQLException();
			}
			this.campaign.addBanner(this);
		} else {
			this.campaign = cdb.get(ci);
			if (campaign == null){
				System.out.println(ci + ":" + this.id);
				throw new SQLException();
			}
		}
		this.size = rs.getByte("BANNERSIZE");
		this.paytype = rs.getByte("PAYTYPE");
		this.payrate = rs.getInt("PAYRATE");
		if (this.getPayType() == Banner.PAYTYPE_CPC) {
			try {
				this.payrate = (int)(this.getRealPayrate()*((double)rs.getInt("CLICKS")/(double)rs.getInt("VIEWS")));
			} catch (Exception e) {
				e.printStackTrace();
				this.payrate = rs.getInt("PAYRATE")/100; //assume 1% clickthrough if we have no data
			}
		}
		this.coefficient = this.getPayRate();
		return this;
	}

	public boolean valid(int time, int size, int userid, byte age, byte sex, short location, Interests interests2, String page, boolean debug) {
		String debugLog = "";
		if (debug) debugLog += "Checking banner " + this.id + ": ";

		if(!this.enabled) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 1";
		
		//date
		if(this.startdate >= time || (this.enddate != 0 && this.enddate <= time)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 2";
		
		//size
		if(this.size != size) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 3";

		//targetting
		//age
		if (!this.validAge(age)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 4";

		//sex
		if(!this.validSex(sex)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 5";
		
		//location
		if(!this.validLocation(location)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 6";
		//page
		if(!this.pages.validate(page)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 7";
		//interests
		//Utilities.bannerDebug("testing interests");
		if(!this.validInterests(interests2)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 8";
		//time
		//Utilities.bannerDebug("testing time");
		
		if (!this.validTime((long)time * 1000)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		if (debug) debugLog += " 9";
		

		//all else works
		if (debug) Utilities.bannerDebug(debugLog);
		return true;
	}

	public double getCoefficient() {
		return coefficient;
	}

	public void setCoefficient(double coefficient) {
		this.coefficient = coefficient;
		
	}

	public int getClicks() {
		return clicks;
	}

	public void setClicks(int clicks) {
		this.clicks = clicks;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public PageValidator getPageValidator() {
		return this.pages;
	}
}