/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Vector;



class Banner{
	Interests interests;
	int id;
	int payrate;
	private int maxHits;
	int campaignID;
	Vector locations;
	Vector ages;
	Vector sexes;
	private int viewsperuser;
	private int limitbyperiod;
	
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
	public int getCampaignID() {
		return campaignID;
	}
	/**
	 * @param campaignID The campaignID to set.
	 */
	public void setCampaignID(int campaignID) {
		this.campaignID = campaignID;
	}
	/*Banner(int id) {
		this();
		this.id = id;
	}*/
	
	int index;
	private int size;
	
	Banner(int id, int payrate, int maxHits, int campaignID, Vector locations, Vector ages, Vector sexes, Interests interests) {
		this.index = counter();
		this.id = id;
		this.payrate = payrate;
		this.maxHits = (maxHits==0?Integer.MAX_VALUE:maxHits);
		this.campaignID = campaignID;
		this.locations = locations;
		this.ages = ages;
		this.sexes = sexes;
		this.interests = interests;
	}
	
	Banner(ResultSet rs) throws SQLException {
		this.index = counter();
		this.id = rs.getInt("ID");
		this.payrate = rs.getInt("PAYRATE");
		this.maxHits = rs.getInt("VIEWSPERDAY");
		maxHits = (maxHits==0?Integer.MAX_VALUE:maxHits);
		this.campaignID = rs.getInt("CAMPAIGNID");
		this.locations = Utilities.stringToVector(rs.getString("LOC"));
		this.ages = Utilities.stringToVector(rs.getString("AGE"));
		this.sexes = Utilities.stringToVector(rs.getString("SEX"));
		this.size = rs.getInt("BANNERSIZE");
		/* INTEGER viewsperday
		 * INTEGER clicksperday
		 * INTEGER UNSIGNED viewsperuser
		 * CHAR limitbyhour
		 * INTEGER UNSIGNED limitbyperiod
		 */
		this.viewsperuser = rs.getInt("VIEWSPERUSER"); 
		this.limitbyperiod = rs.getInt("LIMITBYPERIOD"); 

		this.interests = new Interests(rs.getString("INTERESTS"));
	}
	
	int getID() {
		return id;
	}
	public String toString(){
		String s = "" + this.index + ":" + this.id + '\n';
		s += "Payrate:" + this.getPayrate() + '\n' ;
		s += "max hits:" + this.maxHits + '\n' ;
		s += "locations:" + this.locations + '\n' ;
		s += "ages:" + this.ages + this.ages.isEmpty() + '\n' ;
		s += "sexes:" + this.sexes + this.sexes.isEmpty() + '\n' ;
		s += "views per user:" + this.viewsperuser + '\n' ;
		s += "views period:" + this.limitbyperiod + '\n' ;
		s += this.size;
		return s;
	}
	public Vector getAges() {
		return ages;
	}
	public void setAges(Vector ages) {
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
	public void setLocations(Vector locations) {
		this.locations = locations;
	}
	public int getMaxHits() {
		return maxHits;
	}
	public void setMaxHits(int maxHits) {
		this.maxHits = maxHits;
	}
	public Vector getSexes() {
		return sexes;
	}
	public void setSexes(Vector sexes) {
		this.sexes = sexes;
	}
	public int getPayrate() {
		return payrate;
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
		return (validLocation(u.getLocation()) &&
				validAge(u.getAge()) &&
				validSex(u.getSex()) &&
				validInterests(u.getInterests()));
	}

	/**
	 * @param interests2
	 * @return
	 */
	private boolean validInterests(Interests userInterests) {
		return interests.hasAnyIn(userInterests);
	}

	/**
	/**
	 * @param sex
	 * @return
	 */
	private boolean validSex(byte sex) {
		Integer I = Integer.valueOf(sex);
		boolean valid = sexes.contains(I); 
		I.free();
		return valid;
	}

	/**
	 * @param location
	 * @return
	 */
	private boolean validLocation(short location) {
		Integer I = Integer.valueOf(location);
		boolean valid = locations.contains(I); 
		I.free();
		return valid;
	}

	/**
	 * @param age
	 * @return
	 */
	private boolean validAge(byte age) {
		Integer I = Integer.valueOf(age);
		boolean valid = ages.contains(I); 
		I.free();
		return valid;
	}
}