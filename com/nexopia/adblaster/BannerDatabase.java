/*
 * Created on Jun 19, 2006
 *
 */
package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
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
	Vector banners;
	
	public BannerDatabase() {
		banners = new Vector();
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
				String title = rs.getString("TITLE");
				int id = rs.getInt("ID");
				System.out.println(id + ": " + title);
				int payrate = rs.getInt("PAYRATE");
				int maxHits = rs.getInt("MAXVIEWS");
				int campaignID = rs.getInt("CAMPAIGNID");
				//System.out.println(parseCommaDelimitedInt(rs.getString("LOC")));
				//System.out.println(rs.getString("AGE"));
				//System.out.println(rs.getString("SEX"));
				//System.out.println(rs.getString("INTERESTS"));
				Vector location = parseCommaDelimitedInt(rs.getString("LOC"));
				Vector ages = parseCommaDelimitedInt(rs.getString("AGE"));
				Vector sexes = parseCommaDelimitedInt(rs.getString("SEX"));
				Interests interests = new Interests(rs.getString("INTERESTS"));
				banners.add(new Banner(id,payrate,maxHits,campaignID,location, ages, sexes, interests));
			}
			System.out.println("Total: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Vector parseCommaDelimitedInt(String string) {
		Vector v = new Vector();
		StringTokenizer st = new StringTokenizer(string, ",");
		while (st.hasMoreTokens()) {
			v.add(new Integer(Integer.parseInt(st.nextToken())));
		}
		return v;
	}

	public static void main(String args[]){
		new BannerDatabase();	
	}
	
}
