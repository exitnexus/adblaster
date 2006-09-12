package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCConfig {
	public static final String BANNERSTAT_TABLE = "testbannerstats";	
	public static final String BANNERTYPESTAT_TABLE = "testbannertypestats";
	public static final String BANNER_TABLE = "testbanners";
	public static final String CAMPAIGN_TABLE = "testbannercampaigns";

	private static Connection con; 
	private static SQLQueue sqlQueue;
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			con = DriverManager.getConnection(url, "nathan", "nathan");
			sqlQueue = new SQLQueue(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load JDBC driver.");
			e.printStackTrace();
			System.exit(-1);
		} catch (SQLException e) {
			System.err.println("Unable to establish connection to banner database.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static Statement createStatement() throws SQLException {
		return con.createStatement();
	}
	
	public static void queueQuery(String query) {
		sqlQueue.execute(query);
	}
}
