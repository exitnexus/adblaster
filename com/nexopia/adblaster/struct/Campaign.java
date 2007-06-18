/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.struct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.nexopia.adblaster.AbstractAdBlasterInstance;
import com.nexopia.adblaster.BannerServer;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.util.Interests;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.Utilities;



public class Campaign extends ServablePropertyHolder{
	
	int id;
	int payrate;
	byte paytype;
	private Set<Banner> banners;
	private Vector<Banner> payRateSortedBanners = null;
	private int views;
	private int clicks;
	private boolean pageDominance;
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	public static int []loadViewsAndClicks(int campaignID){
		int totalViews = 0;
		int totalClicks = 0;
		try {
			Statement s = JDBCConfig.createStatement();
			ResultSet rs = s.executeQuery("SELECT views,clicks FROM " + JDBCConfig.BANNER_TABLE + " WHERE campaignid = " + campaignID);
			while (rs.next()) {
				totalViews += rs.getInt("VIEWS");
				totalClicks += rs.getInt("CLICKS");
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int ret[] = {totalViews,totalClicks};
		return ret;
	}
	
	public Campaign(ResultSet rs, PageValidatorFactory pvf) throws SQLException {
		banners = new HashSet<Banner>();
		this.update(rs, pvf);
		int viewsAndClicks[] = loadViewsAndClicks(this.id);
		this.views = viewsAndClicks[0];
		this.clicks = viewsAndClicks[0];
		this.pageDominance = rs.getString("pagedominance").equals("y");
	}
	
	public void update(ResultSet rs, PageValidatorFactory pvf) throws SQLException{
		super.update(rs, pvf);
		this.id = rs.getInt("ID");
		this.payrate = rs.getInt("PAYRATE");
		this.paytype = rs.getByte("PAYTYPE");
	}
	
	public int getID() {
		return id;
	}
	public String toString(){
		return "" + this.id + " at " + this.getPayrate() + "cpm";
	}
	public int getPayrate() {
		return payrate;
	}
	
	

	//this does basic checks like enabled and within date range
	//if it returns false then its banners will never be displayable today
	public boolean precheck() {
		boolean validDate = true;
		if (startdate != 0){
			if ((long)startdate*1000 > System.currentTimeMillis() + 86400*1000) {
				//System.err.println(startdate*1000 + ">"+(System.currentTimeMillis() + 86400*1000));
				validDate = false;
			}
		}
		
		if (enddate != 0){
			if ((long)enddate*1000 < System.currentTimeMillis()) {
				//System.err.println((long)enddate*1000 + "<"+(System.currentTimeMillis() + 86400*1000));
				validDate = false;
			}
		}
		
		return enabled && validDate;
	}

	public byte getPayType() {
		return this.paytype;
	}

	
	public Set<Banner> getBanners(int usertime, int size, int userid, byte age, 
			byte sex, short location, Interests interests, String page, 
			int pageDominance, 
			boolean debug) {
		
		HashSet<Banner> hs = new HashSet<Banner>();
		boolean b1 = this.banners.isEmpty();
		boolean b2 = !this.valid(usertime, size, userid, age, sex, location, 
				interests, page, pageDominance, debug);		
		
		if (b1) {
			if (BannerServer.debug.get("development").booleanValue())
				System.out.println("Campaign " + this.toString() + " is: " + (b1?"empty":"non-empty") + ":" + (b2?"invalid":"valid"));
			if (debug)
				Utilities.bannerDebug("Bannerset is empty.");
			return hs;
		}
		
		if (b2){
			if (BannerServer.debug.get("development").booleanValue())
				System.out.println("Campaign " + this.toString() + " is: " + (b1?"empty":"non-empty") + ":" + (b2?"invalid":"valid"));
			if (debug)
				Utilities.bannerDebug("Campaign doesn't match parameters.");
			return hs;
		}
		
		for (Banner b : this.banners) {
			if (b.valid(usertime, size, userid, age, sex, location, interests, page, pageDominance, debug)) {
				hs.add(b);
			}
		}
		return hs;
	}

	public void addBanner(Banner banner) {
		this.banners.add(banner);
		this.payRateSortedBanners = null;
	}
	
	public void removeBanner(Banner banner) {
		this.banners.remove(banner);
		this.payRateSortedBanners.remove(banner);
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

	@Override
	protected boolean valid(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests2, String page, int pageDominance, boolean debug) {
		String debugLog = "";
		if (debug) debugLog += "Checking campaign " + this.toString() + ": ";
		if(!this.enabled)
			return false;
		
		//date
		if (debug) debugLog += " Date";
		if(this.startdate >= usertime || (this.enddate != 0 && this.enddate <= usertime))
			return false;
	
		if (debug) debugLog += " Age";
		//targetting
		//age
		if (!this.validAge(age)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " Sex";
		//sex
		if(!this.validSex(sex)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
	
		if (debug) debugLog += " PageDominance";
		if (!this.validPageDominance(pageDominance)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		//location
		if (debug) debugLog += " Location";
		if(!this.validLocation(location)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " Page";
		//page
		if(!this.validPage(page)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " Interests";
		//interests
		if(!this.validInterests(interests2)){
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " Time";
		if (!this.validTime((long)usertime*1000)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " Done";
		//Utilities.bannerDebug("Campaign valid: this.id");
		if (debug) Utilities.bannerDebug(debugLog);
		return true;
	}

	private boolean validPageDominance(int pageDominanceID) {
		if (pageDominanceID == BannerServer.PAGE_DOMINANCE_POSSIBLE) {
			return true;
		} else if (pageDominanceID == BannerServer.PAGE_DOMINANCE_OFF) {
			return !this.pageDominance;
		} else {
			return (pageDominanceID == this.id);
		}
	}

	public boolean getPageDominance() {
		return pageDominance;
	}

	public Vector<Banner> getPayRateSortedBanners(AbstractAdBlasterInstance instance) {
		if (this.payRateSortedBanners == null) {
			this.payRateSortedBanners = new Vector<Banner>(banners);
			Collections.sort(this.payRateSortedBanners, new BannerPayRateComparator(instance));
		}
		return this.payRateSortedBanners;
	}

	public Set<Banner> getBanners() {
		return banners;
	}

}

/*
I left these functions commented out when I extracted these features to the superclass
because some of them might have slight differences and you may want to compare the
old implementation if you detect something going wrong.

	public Vector getAges() {
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
	public Vector getLocations() {
		return locations;
	}
	public void setLocations(Vector<Integer> locations) {
		this.locations = locations;
	}
	public int getViewsperuser() {
		return viewsperuser;
	}
	public void setViewsperuser(int maxHits) {
		this.viewsperuser = maxHits;
	}
	public Vector getSexes() {
		return sexes;
	}
	public void setSexes(Vector<Integer> sexes) {
		this.sexes = sexes;
	}
	public void setPayrate(int payrate) {
		this.payrate = payrate;
	}


	public boolean validInterests(Interests userInterests) {
		return userInterests.matches(interests);
	}

	public boolean validSex(byte sex) {
		if (sexes.isEmpty()) {
			return true;
		}
		Integer I = Integer.valueOf(sex);
		boolean valid = sexes.contains(I); 
		I.free();
		return valid;
		
	}

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

	public int getViewsPerUser() {
		return this.viewsPerUser;
	}

	public int getMinViewsPerDay() {
		return this.minviewsperday;
	}
	
	public boolean validTime(long usertime) {
		return allowedTimes.getValid(usertime);
	}

	public int getViewsPerDay() {
		return this.maxviews;
	}

	public int getClicksperday() {
		return clicksperday;
	}

	public int getViewsperday() {
		return viewsperday;
	}
*/
