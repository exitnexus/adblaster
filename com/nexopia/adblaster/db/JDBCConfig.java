package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.nexopia.adblaster.SQLQueue;

public class JDBCConfig {
	public static final String BANNERSTAT_TABLE = "bannerstats";	
	public static final String BANNERTYPESTAT_TABLE = "bannertypestats";
	public static final String BANNER_TABLE = "banners";
	public static final String CAMPAIGN_TABLE = "bannercampaigns";

	private static Connection con; 
	private static SQLQueue sqlQueue;
	
	public static String inputString(String s) {
		BufferedReader input = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.print(s);
		try {
			return input.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3306/newbanners";
			con = DriverManager.getConnection(url, "root", "Hawaii");
			sqlQueue = new SQLQueue(1);
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load JDBC driver.");
			e.printStackTrace();
			System.exit(-1);
		} catch (SQLException e) {
			System.err.println("Unable to establish connection to banner database.");
			if (inputString("Display stack trace? (y)").equals("y"))
				e.printStackTrace();
			System.err.println("Run the following command (or something similar): ");
			System.err.println("ssh -nNT -R 3306:192.168.0.50:3306 root@192.168.0.50 -p 3022.");
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
