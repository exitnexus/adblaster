/*
 * Created on Jun 19, 2006
 *
 */
package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

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
	private HashMap<Integer, Banner> banners;
	Vector<Integer> keyset = new Vector<Integer>();
	public BannerDatabase() {
		banners = new HashMap<Integer, Banner>();
		try {
			String sql = "SELECT * FROM banners";
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
						Campaign.get(rs.getInt("CAMPAIGNID")).precheck()) {
					try {
						banners.put(new Integer(id), new Banner(rs));
					} catch (SQLException e){
						System.out.println("This probably indicates a bad campaign. Continue if you know what you're doing.");
					}
					i++;
				}
			}
			keyset.addAll(this.banners.keySet());
			System.out.println("Total: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Banner getBannerByID(int i) {
		return (Banner)this.banners.get(new Integer(i));
	}
	public Banner getBannerByIndex(int i) {
		return (Banner)this.banners.get(keyset.get(i));
	}
	
	public int getBannerCount() {
		return banners.size();
	}
	
	public Collection getBanners() {
		return banners.values(); 
	}
	public static void main(String args[]){
		new BannerDatabase();	
	}

	public void saveCoefficients(HashMap<Banner, Float> coefficients) {
		Statement stmt;
		try {
			stmt = JDBCConfig.createStatement();
			for (Banner banner: banners.values()) {
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
				Integer bannerid = Integer.valueOf(rs.getInt("BANNERID"));
				coefficients.put(banners.get(bannerid), new Float(rs.getFloat("COEFFICIENT")));
				bannerid.free();
			}
		} catch (SQLException sqle) {
			System.err.println("Unable to load coefficients from database.");
			sqle.printStackTrace();
		}
		return coefficients;
	}

	public Banner add(int id) {
		try {
			String sql = "SELECT * FROM banners WHERE id = " + id;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				if (Banner.precheck(rs) &&
						Campaign.get(rs.getInt("CAMPAIGNID")).precheck()) {
					try {
						Banner b = new Banner(rs);
						Integer i = Integer.valueOf(id);
						banners.put(i, b);
						keyset.add(i);
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

	public Banner update(int bannerID) {
		Integer id = Integer.valueOf(bannerID);
		Banner b = banners.get(id);
		id.free();
		id = null;
		if (b != null) {
			try {
				String sql = "SELECT * FROM banners WHERE id = " + bannerID;
				Statement stmt = JDBCConfig.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					return b.update(rs);
				} else {
					id = Integer.valueOf(bannerID);
					banners.remove(id);
					keyset.remove(id);
					id.free();
					id = null;
					return null;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			return add(bannerID);
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
		Integer id = Integer.valueOf(bannerID);
		banners.remove(id);
		keyset.remove(id);
		id.free();
	}
	
}
