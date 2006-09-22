package com.nexopia.adblaster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.nexopia.adblaster.Utilities.PageValidator;

public class ServablePropertyHolder {

	protected Vector<Integer> ages;
	protected TimeTable allowedTimes;
	protected int clicksperday;
	protected int enddate;
	protected boolean enabled;
	protected int index;
	protected Interests interests;
	protected int limitByPeriod;
	protected Vector<Integer> locations;
	protected int maxviews;
	protected int minviewsperday;
	protected PageValidator pages;
	protected Vector<Integer> sexes;
	protected int startdate;
	protected int viewsPerUser;
	protected int viewsperday;

	public ServablePropertyHolder() {
		super();
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
	
	public int getViewsPerDay() {
		return viewsperday;
	}

	public Vector<Integer> getSexes() {
		return sexes;
	}

	public void setSexes(Vector<Integer> sexes) {
		this.sexes = sexes;
	}

	public int getLimitByPeriod() {
		return limitByPeriod;
	}

	public int getViewsPerUser() {
		return viewsPerUser;
	}

	public void setViewsPerUser(int viewsperuser) {
		this.viewsPerUser = viewsperuser;
	}

	public void setViewsPerDay(int viewsperday) {
		this.viewsperday = viewsperday;
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

	public int getMinViewsPerDay() {
		return minviewsperday;
	}

	protected boolean validTime(long time) {
		return this.allowedTimes.getValid(time);
	}

	public int getClicksperday() {
		return clicksperday;
	}
	
	public int getIntegerMaxClicksperday() {
		return (clicksperday==0?Integer.MAX_VALUE:clicksperday);
	}

	public boolean validPage(String page) {
		return pages.validate(page);
	}

	public void update(ResultSet rs) throws SQLException {
		this.locations = Utilities.stringToNegationVector(rs.getString("LOC"));
		this.ages = Utilities.stringToNegationVector(rs.getString("AGE"));
		this.sexes = Utilities.stringToVector(rs.getString("SEX"));
		this.viewsPerUser = rs.getInt("VIEWSPERUSER"); 
		this.limitByPeriod = rs.getInt("LIMITBYPERIOD"); 
		this.interests = new Interests(rs.getString("INTERESTS"), true);
		this.enabled = rs.getString("ENABLED").equals("y");
		this.startdate = rs.getInt("STARTDATE");
		this.enddate = rs.getInt("ENDDATE");
		this.maxviews = rs.getInt("MAXVIEWS");
		this.minviewsperday = rs.getInt("MINVIEWSPERDAY");
		this.viewsperday = rs.getInt("VIEWSPERDAY");
		this.clicksperday = rs.getInt("CLICKSPERDAY");
		this.allowedTimes = new TimeTable(rs.getString("ALLOWEDTIMES"));
		this.pages = Utilities.stringToPageValidator(rs.getString("PAGE"));
	}

	public boolean validUser(User u) {
		return (validLocation(u.getLocation()) &&
				validAge(u.getAge()) &&
				validSex(u.getSex()) &&
				validInterests(u.getInterests()));
		
	}

	protected boolean valid(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests2, String page, boolean debug) {
		String debugLog = "";
		if (debug) debugLog += "Checking campaign " + this.toString() + ": ";
		if(!this.enabled)
			return false;
		//date
		if(this.startdate >= usertime || (this.enddate != 0 && this.enddate <= usertime))
			return false;
	
		if (debug) debugLog += " 0";
		//targetting
		//age
		if (!this.validAge(age)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 1";
		//sex
		if(!this.validSex(sex)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
	
		//location
		if(!this.validLocation(location)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 2";
		//page
		if(!this.validPage(page)){ //default true
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 3";
		//interests
		if(!this.validInterests(interests2)){
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 4";
		if (!this.validTime((long)usertime*1000)) {
			if (debug) Utilities.bannerDebug(debugLog);
			return false;
		}
		
		if (debug) debugLog += " 5";
		//Utilities.bannerDebug("Campaign valid: this.id");
		if (debug) Utilities.bannerDebug(debugLog);
		return true;
	}

	public int getIntegerMaxViewsPerDay(){
		if (this.viewsperday == 0)
			return Integer.MAX_VALUE;
		else
			return this.viewsperday;
	}

	public int getIntegerMaxViewsPerUser(){
		if (this.viewsPerUser == 0)
			return Integer.MAX_VALUE;
		else
			return this.viewsPerUser;
	}
	
	public int getIntegerMaxViews(){
		if (this.maxviews == 0)
			return Integer.MAX_VALUE;
		else
			return this.maxviews;
	}
	
	
	/**
	 * Determine whether it is valid for a user to view this banner
	 * at this time. 
	 */
	public boolean isValidForUser(int userid, int time, boolean debug, BannerServer server){
		if (getViewsPerUser() <= 0)
			return true;
		
		/* Find the oldest view*/
		int[] views = server.getViewsForUser(userid, this);

		if (views[0] != 0){
			if (debug) System.out.println(time + ":" + views[0] + " > " + getLimitByPeriod());
			if (time - views[0] > getLimitByPeriod())
				/* If the oldest view is outside of limit period, we're golden */
				return true;
			else
				return false;
		}
		
		/* If we've fallen through, then there are no views.*/
		return true;
		
	}
}