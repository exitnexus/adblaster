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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;


class Campaign{

	private static HashMap<Integer, Campaign> campaigns;
	
	static {
		System.out.println("Initing campaigns.");
		campaigns = new HashMap<Integer, Campaign>();
		//Database connection stuff here.
		try {
			String sql = "SELECT * FROM bannercampaigns";
			Statement stmt = JDBCConfig.createStatement();
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
	
	public static Campaign add(int campaignID) {
		try {
			String sql = "SELECT * FROM bannercampaigns WHERE id = " + campaignID;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				int id = rs.getInt("ID");
				Campaign c = new Campaign(rs);
				campaigns.put(Integer.valueOf(id), c);
				return c;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Campaign update(int campaignID) {
		Integer id = Integer.valueOf(campaignID);
		Campaign c = campaigns.get(id);
		id.free();
		id = null;
		if (c != null) {
			try {
				String sql = "SELECT * FROM bannercampaigns WHERE id = " + campaignID;
				Statement stmt = JDBCConfig.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					c.update(rs);
					return c;
				} else {
					id = Integer.valueOf(campaignID);
					campaigns.remove(id);
					id.free();
					id = null;
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return Campaign.add(campaignID);
		}
	}
	
	public Campaign update(ResultSet rs) throws SQLException {
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
		this.maxviews = rs.getInt("MAXVIEWS");
		this.minviewsperday = rs.getInt("MINVIEWSPERDAY");
		return this;
	}
	
	public static Collection<Campaign> getCampaigns() {
		return campaigns.values();
	}
	public static Campaign get(int campaignID) {
		Integer I = Integer.valueOf(campaignID);
		Campaign c = campaigns.get(I);
		I.free();
		return c;
	}
	
	public static Campaign getByIndex(int index) {
		Campaign c = get(((Integer)campaigns.keySet().toArray()[index]).intValue());
		return c;
	}

	public static int getCampaignCount() {
		return campaigns.size();
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
	private int maxviews;
	public int minviewsperday;
	
	Vector<Integer> locations;
	Vector<Integer> ages;
	Vector<Integer> sexes;
	
	Set<Banner> banners;
	
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	Campaign(ResultSet rs) throws SQLException {
		banners = new HashSet<Banner>();
		this.update(rs);
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
		if (startdate != 0){
			if (startdate < System.currentTimeMillis()) {
				validDate = false;
			}
		}
		
		if (enddate != 0){
			if (enddate > System.currentTimeMillis()) {
				validDate = false;
			}
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

	public static void delete(int campaignID) {
		Integer id = Integer.valueOf(campaignID);
		campaigns.remove(id);
		id.free();
	}

	public void minutely() {
		// TODO Auto-generated method stub
		
	}

	public void hourly() {
		// TODO fill this
	}
	
	public void daily() {
		// TODO fill this
	}

	public Set<Banner> getBanners(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, int page, boolean debug) {
		HashSet<Banner> hs = new HashSet<Banner>();
		if (this.banners.isEmpty() || !this.valid(usertime, size, userid, age, sex, location, interests, page, debug)) {
			return hs;
		} else {
			for (Banner b : this.banners) {
				if (b.valid(usertime, size, userid, age, sex, location, interests, page, debug)) {
					hs.add(b);
				}
			}
			return hs;
		}
	}

	private boolean valid(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests2, int page, boolean debug) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addBanner(Banner banner) {
		this.banners.add(banner);
	}
	
	public void removeBanner(Banner banner) {
		this.banners.remove(banner);
	}
}