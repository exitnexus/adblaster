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

import com.nexopia.adblaster.Utilities.PageValidator;


class Campaign extends ServablePropertyHolder{
	
	static class CampaignDB{
		private HashMap<Integer, Campaign> campaigns;
		
		public CampaignDB() {
			System.out.println("Initing campaigns.");
			campaigns = new HashMap<Integer, Campaign>();
			//Database connection stuff here.
			try {
				String sql = "SELECT * FROM " + JDBCConfig.CAMPAIGN_TABLE;
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
		public ServablePropertyHolder add(int campaignID) {
			try {
				String sql = "SELECT * FROM " + JDBCConfig.CAMPAIGN_TABLE + " WHERE id = " + campaignID;
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
		
		public ServablePropertyHolder update(int campaignID) {
			Integer id = Integer.valueOf(campaignID);
			ServablePropertyHolder c = campaigns.get(id);
			id.free();
			id = null;
			if (c != null) {
				try {
					String sql = "SELECT * FROM " + JDBCConfig.CAMPAIGN_TABLE + " WHERE id = " + campaignID;
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
				return add(campaignID);
			}
		}
		public Collection<Campaign> getCampaigns() {
			return campaigns.values();
		}
		public Campaign get(int campaignID) {
			Integer I = Integer.valueOf(campaignID);
			Campaign c = campaigns.get(I);
			I.free();
			return c;
		}
		
		public Campaign getByIndex(int index) {
			Campaign c = get(((Integer)campaigns.keySet().toArray()[index]).intValue());
			return c;
		}

		public int getCampaignCount() {
			return campaigns.size();
		}

		public void delete(int campaignID) {
			Integer id = Integer.valueOf(campaignID);
			campaigns.remove(id);
			id.free();
		}

	}

	int id;
	int payrate;
	byte paytype;
	Set<Banner> banners;
	private int views;
	private int clicks;
	static int count = 0;
	public static int counter(){
		return count++;
	}
	
	public static int []loadViewsAndClicks(int campaignID){
		int totalViews = 0;
		int totalClicks = 0;
		try {
			Statement s = JDBCConfig.createStatement();
			ResultSet rs = s.executeQuery("SELECT id FROM " + JDBCConfig.BANNER_TABLE + " WHERE campaignid = " + campaignID);
			int i = 0;
			while (rs.next()) {
				int id = rs.getInt("ID");
				Statement s2 = JDBCConfig.createStatement();
				ResultSet rs2 = s2.executeQuery("SELECT views, clicks FROM " + JDBCConfig.BANNERSTAT_TABLE + " WHERE id = " + id);
				while (rs2.next()) {
					if (rs2.first()){
						totalViews += rs2.getInt("VIEWS");
						totalClicks += rs2.getInt("CLICKS");
					}
				}
				
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int ret[] = {totalViews,totalClicks};
		return ret;
	}
	
	Campaign(ResultSet rs) throws SQLException {
		banners = new HashSet<Banner>();
		this.update(rs);
		int viewsAndClicks[] = loadViewsAndClicks(this.id);
		this.views = viewsAndClicks[0];
		this.clicks = viewsAndClicks[0];
	}
	
	public void update(ResultSet rs) throws SQLException{
		super.update(rs);
		this.id = rs.getInt("ID");
		this.payrate = rs.getInt("PAYRATE");
		this.paytype = rs.getByte("PAYTYPE");
	}
	
	int getID() {
		return id;
	}
	public String toString(){
		return "" + this.id + " at " + this.getPayrate() + "cpm";
	}
	public int getPayrate() {
		return payrate;
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

	public void minutely() {
		// TODO Auto-generated method stub
		
	}

	public void hourly() {
		// TODO fill this
	}
	
	public void daily() {
		// TODO fill this
	}

	public Set<Banner> getBanners(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, String page, boolean debug) {
		HashSet<Banner> hs = new HashSet<Banner>();
		boolean b1 = this.banners.isEmpty();
		boolean b2 = !this.valid(usertime, size, userid, age, sex, location, interests, page, debug);		
		if (b1 || b2) {
			if (BannerServer.debug.get("development").booleanValue())
				System.out.println("Campaign " + this.toString() + " is: " + b1 + ":" + b2);
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

	public void addBanner(Banner banner) {
		this.banners.add(banner);
	}
	
	public void removeBanner(Banner banner) {
		this.banners.remove(banner);
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
	

}