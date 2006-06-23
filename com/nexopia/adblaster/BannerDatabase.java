/*
 * Created on Jun 19, 2006
 *
 */
package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;
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
	private HashMap banners;
	
	public BannerDatabase() {
		banners = new HashMap();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			Connection con = DriverManager.getConnection(url, "nathan", "nathan");
			String sql = "SELECT * FROM banners";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			/*for (int i = 0; i < rs.getMetaData().getColumnCount(); i++){
				System.out.println(rs.getMetaData().getColumnTypeName(i+1) + " " 
						+ rs.getMetaData().getColumnName(i+1) );
			}*/
			int i = 0;
			while (rs.next()) {
				i++;
				int id = rs.getInt("ID");
				banners.put(new Integer(id), new Banner(rs));
			}
			System.out.println("Total: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		public Banner getBanner(int i) {
		return (Banner)this.banners.get(new Integer(i));
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
	
}
