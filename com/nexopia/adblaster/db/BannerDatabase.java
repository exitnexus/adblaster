/*
 * Created on Jun 19, 2006
 *
 */
package com.nexopia.adblaster.db;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.IntObjectHashMap;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.PageValidator;
import com.nexopia.adblaster.util.PageValidatorFactory;

/**
 * @author wolfe
 *
 *
 * INTEGER UNSIGNED id
 * INTEGER UNSIGNED clientid
 * TINYINT UNSIGNED bannersize
 * TINYINT UNSIGNED bannertype
 * INTEGER UNSIGNED views
 * INTEGER UNSIGNED potentialviews
 * INTEGER UNSIGNED clicks
 * INTEGER maxviews
 * INTEGER maxclicks
 * INTEGER viewsperday
 * INTEGER clicksperday
 * INTEGER UNSIGNED viewsperuser
 * CHAR limitbyhour
 * INTEGER UNSIGNED limitbyperiod
 * INTEGER startdate
 * INTEGER enddate
 * SMALLINT payrate
 * TINYINT UNSIGNED paytype
 * TEXT age
 * TEXT sex
 * TEXT loc
 * TEXT page
 * TEXT interests
 * CHAR moded
 * CHAR enabled
 * VARCHAR title
 * VARCHAR image
 * VARCHAR link
 * TEXT alt
 * INTEGER dateadded
 * INTEGER lastupdatetime
 * SMALLINT refresh
 * INTEGER UNSIGNED passbacks
 * INTEGER UNSIGNED campaignid
 *
 */

public class BannerDatabase {
	
	private IntObjectHashMap<Banner> banners;
	private Vector<Banner> bannerList;
	CampaignDB cdb;
	
	public BannerDatabase(Campaign.CampaignDB cdb, PageValidatorFactory pvfactory) {
		this.cdb = cdb;
		banners = new IntObjectHashMap<Banner>();
		bannerList = new Vector<Banner>();
		try {
			String sql = "SELECT * FROM " + JDBCConfig.BANNER_TABLE;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			/*for (int i = 0; i < rs.getMetaData().getColumnCount(); i++){
				System.out.println(rs.getMetaData().getColumnTypeName(i+1) + " " 
						+ rs.getMetaData().getColumnName(i+1) );
			}*/
			int i = 0;
			while (rs.next()) {
				int id = rs.getInt("ID");
				if (Banner.precheck(rs) &&
						cdb.get(rs.getInt("CAMPAIGNID")).precheck()) {
					try {
						Banner b = new Banner(rs, cdb, pvfactory.make());
						banners.put(id, b);
						bannerList.add(b);
					} catch (SQLException e){
						System.out.println("This probably indicates a bad campaign. Continue if you know what you're doing.");
						e.printStackTrace();
					}
					i++;
				}
			}
			System.out.println("Total: " + i + " banners.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Banner getBannerByID(int i) {
		return this.banners.get(i);
	}
	public Banner getBannerByIndex(int i) {
		return this.banners.get(this.banners.getKeyArray()[i]);
	}
	
	public int getBannerCount() {
		return banners.size();
	}
	
	public Vector<Banner> getBanners() {
		return bannerList; 
	}

	public static void loadCoefficients(HashMap<Banner, Float> coefficients) {
		System.out.println("Loading coefficients...");
		Statement stmt;
		try {
			stmt = JDBCConfig.createStatement();
			for (Banner banner: coefficients.keySet()) {
				System.out.println("Loading banner " + banner.getID());
				try {
					Float f = coefficients.get(banner);
					String st = "SELECT c.* " 
					+ "FROM coefficients c LEFT JOIN coefficients AS c2 "
					+ "ON c.bannerid=c2.bannerid AND c.time<c2.time "
					+ "WHERE c2.bannerid IS NULL "
					+ "AND c.bannerid = " + banner.getID();
					stmt.execute(st);
					ResultSet rs = stmt.getResultSet();
					if (rs.first()){
						f = rs.getFloat("coefficient");
						System.out.println(rs.getInt("bannerid") + ":" + f);
						coefficients.put(banner, f);
					} else {
						coefficients.put(banner, Float.valueOf(0));
					}
					
				} catch (SQLException sqle) {
					System.err.println("Error during coefficient retrieval.");
					sqle.printStackTrace();
				}
			}
		} catch (SQLException sqle) {
			System.err.println("Unable to load coefficients from database.");
			sqle.printStackTrace();
		}
	
	}

	public void saveCoefficients(HashMap<Banner, Float> coefficients) {
		Statement stmt;
		try {
			stmt = JDBCConfig.createStatement();
			for (Banner banner: bannerList) {
				try {
					if (coefficients.get(banner) != null) {
						Float f = coefficients.get(banner);
						stmt.executeUpdate("INSERT INTO `coefficients` SET `coefficient` = " 
								+ f 
								+ ", bannerid = " + banner.getID()
								+ ", time = " + System.currentTimeMillis()/1000);
					}
				} catch (SQLException sqle) {
					System.err.println("Error during coefficient insert.");
					sqle.printStackTrace();
				}
			}
		} catch (SQLException sqle) {
			System.err.println("Unable to store coefficients in database.");
			sqle.printStackTrace();
		}
	
	}

	public HashMap<Banner, Float> getCoefficientMap() {
		HashMap<Banner,Float> coefficients = new HashMap<Banner,Float>();
		Statement stmt;
		try {
			stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM coefficients");
			while (rs.next()) {
				int bannerid = rs.getInt("BANNERID");
				coefficients.put(banners.get(bannerid), new Float(rs.getFloat("COEFFICIENT")));
			}
		} catch (SQLException sqle) {
			System.err.println("Unable to load coefficients from database.");
			sqle.printStackTrace();
		}
		return coefficients;
	}

	public ServablePropertyHolder add(int id, PageValidator pv) {
		try {
			String sql = "SELECT * FROM banners WHERE id = " + id;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				if (Banner.precheck(rs) &&
						cdb.get(rs.getInt("CAMPAIGNID")).precheck()) {
					try {
						Banner b = new Banner(rs, cdb, pv);
						if (banners.containsKey(id)){
							bannerList.remove(banners.get(id));
						}
						banners.put(id, b);
						bannerList.add(b);
						return b;
					} catch (SQLException e){
						System.out.println("This probably indicates a bad campaign. Continue if you know what you're doing.");
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ServablePropertyHolder update(int bannerID, PageValidator pv) {
		Banner b = banners.get(bannerID);
		if (b != null) {
			try {
				String sql = "SELECT * FROM banners WHERE id = " + bannerID;
				Statement stmt = JDBCConfig.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					return b.update(rs, cdb);
				} else {
					if (banners.containsKey(bannerID)){
						bannerList.remove(banners.get(bannerID));
					}
					banners.remove(bannerID);
					return null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			return add(bannerID, pv);
		}
		return null;
	}

	public void deleteCampaign(int campaignID) {
		try {
			String sql = "SELECT * FROM banners WHERE campaignid = " + campaignID;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int bannerID = rs.getInt("ID");
				this.delete(bannerID);
			}
		} catch (SQLException e) {
			System.err.println("Error deleting banners with campaign id " + campaignID + ".");
			e.printStackTrace();
		}
		
	}

	public void delete(int bannerID) {
		Banner b = this.banners.get(bannerID);
		b.getCampaign().removeBanner(b);
		if (banners.containsKey(bannerID)){
			bannerList.remove(banners.get(bannerID));
		}
		banners.remove(bannerID);
	}
	
}
