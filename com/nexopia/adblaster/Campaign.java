/*
 * Created on Jun 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;


class Campaign{
	private static HashMap<Integer, Campaign> campaigns;
	
	static {
		System.out.println("Initing campagins.");
		campaigns = new HashMap<Integer, Campaign>();
		//Database connection stuff here.
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			Connection con = DriverManager.getConnection(url, "nathan", "nathan");
			String sql = "SELECT * FROM bannercampaigns";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int i = 0;
			while (rs.next()) {
				int id = rs.getInt("ID");
				campaigns.put(Integer.valueOf(id), new Campaign(rs));
				i++;
			}
			System.out.println("Campaigns Total: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Campaign get(int campaignID) {
		Integer I = Integer.valueOf(campaignID);
		Campaign c = campaigns.get(I);
		I.free();
		return c;
	}
	
	
	private Interests interests;
	private int id;
	private int payrate;
	private byte paytype;
	private int maxHits;
	private int viewsPerUser;
	private int limitByPeriod;
	private boolean enabled;
	private long startdate;
	private long enddate;
	
	
	Vector<Integer> locations;
	Vector<Integer> ages;
	Vector<Integer> sexes;
	
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	Campaign(ResultSet rs) throws SQLException {
		this.id = rs.getInt("ID");
		this.payrate = rs.getInt("PAYRATE");
		this.maxHits = rs.getInt("VIEWSPERDAY");
		this.maxHits = (maxHits==0?Integer.MAX_VALUE:maxHits);
		this.locations = Utilities.stringToNegationVector(rs.getString("LOC"));
		this.ages = Utilities.stringToNegationVector(rs.getString("AGE"));
		this.sexes = Utilities.stringToVector(rs.getString("SEX"));
		this.viewsPerUser = rs.getInt("VIEWSPERUSER"); 
		this.limitByPeriod = rs.getInt("LIMITBYPERIOD"); 
		this.interests = new Interests(rs.getString("INTERESTS"), true);
		this.enabled = rs.getString("ENABLED").equals("y");
		this.paytype = rs.getByte("PAYTYPE");
		this.startdate = rs.getLong("STARTDATE");
		this.enddate = rs.getLong("ENDDATE");
	}
	
	int getID() {
		return id;
	}
	public String toString(){
		return "" + this.id + "," + this.getPayrate();
	}
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
	public int getMaxHits() {
		return maxHits;
	}
	public void setMaxHits(int maxHits) {
		this.maxHits = maxHits;
	}
	public Vector getSexes() {
		return sexes;
	}
	public void setSexes(Vector<Integer> sexes) {
		this.sexes = sexes;
	}
	public int getPayrate() {
		return payrate;
	}
	public void setPayrate(int payrate) {
		this.payrate = payrate;
	}
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
		return userInterests.matches(interests);
	}

	/**
	 * @param sex
	 * @return
	 */
	private boolean validSex(byte sex) {
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
	private boolean validLocation(short location) {
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
	private boolean validAge(byte age) {
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

	//this does basic checks like enabled and within date range
	//if it returns false then its banners will never be displayable today
	public boolean precheck() {
		boolean validDate = true;
		if (!(startdate < System.currentTimeMillis())) {
			validDate = false;
		} else if (!(enddate > System.currentTimeMillis())) {
			validDate = false;
		}
		return enabled && validDate;
	}

	public byte getPayType() {
		return this.paytype;
	}

	public int getLimitByPeriod() {
		return this.limitByPeriod;
	}

	public int getViewsPerUser() {
		return this.viewsPerUser;
	}
	
}