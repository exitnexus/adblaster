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
	
	public static void init() {
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
				if (rs.getString("ENABLED").equals("y")) {
					campaigns.put(Integer.valueOf(id), new Campaign(rs));
					i++;
				}
			}
			System.out.println("Campaigns Total: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	Interests interests;
	int id;
	int payrate;
	int maxHits;
	int viewsPerUser;
	int limitByPeriod;
	
	Vector<Integer> locations;
	Vector<Integer> ages;
	Vector<Integer> sexes;
	
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	Campaign() {
		interests = new Interests();
		id = counter(); //TODO Banners need an ID we can track them by
		maxHits = (int) (Math.pow((Math.random()-0.5) * 2,2) * 200) + 1;
		this.payrate = (int)(Math.random()*10);
	}
	
	Campaign(int id) {
		this();
		this.id = id;
	}
	
	Campaign(int id, int payrate, int maxHits, Vector<Integer> locations, Vector<Integer> ages, Vector<Integer> sexes, Interests interests) {
		this.id = id;
		this.payrate = payrate;
		this.maxHits = maxHits;
		this.locations = locations;
		this.ages = ages;
		this.sexes = sexes;
		this.interests = interests;
	}
	
	Campaign(ResultSet rs) throws SQLException {
		this.id = rs.getInt("ID");
		this.payrate = rs.getInt("PAYRATE");
		this.maxHits = rs.getInt("VIEWSPERDAY");
		maxHits = (maxHits==0?Integer.MAX_VALUE:maxHits);
		this.locations = Utilities.stringToVector(rs.getString("LOC"));
		this.ages = Utilities.stringToVector(rs.getString("AGE"));
		this.sexes = Utilities.stringToVector(rs.getString("SEX"));
		this.viewsPerUser = rs.getInt("VIEWSPERUSER"); 
		this.limitByPeriod = rs.getInt("LIMITBYPERIOD"); 
		this.interests = new Interests(rs.getString("INTERESTS"));
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
		return interests.hasAnyIn(userInterests);
	}

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

	public static Campaign get(int campaignID) {
		Integer I = Integer.valueOf(campaignID);
		Campaign c = campaigns.get(I);
		I.free();
		return c;
	}
}