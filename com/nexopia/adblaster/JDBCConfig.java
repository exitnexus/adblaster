package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCConfig {
	public static final String BANNERSTAT_TABLE = "bannerstats";	
	public static final String BANNERTYPESTAT_TABLE = "bannertypestats";
	public static final String BANNER_TABLE = "banners";
	public static final String CAMPAIGN_TABLE = "bannercampaigns";

	private static Connection con; 
	private static SQLQueue sqlQueue;
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3306/testbanner";
			con = DriverManager.getConnection(url, "root", "Hawaii");
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

	public static void queueQuery(String query, byte[] a, byte[] b) {
		sqlQueue.execute(query, a, b);
	}

	public static PreparedStatement prepareStatement(String sql) throws SQLException {
		return con.prepareStatement(sql);
	}
}
