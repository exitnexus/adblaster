package com.nexopia.adblaster.db;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.mysql.jdbc.ResultSet;
import com.nexopia.adblaster.SQLQueue;
import com.nexopia.adblaster.struct.ConfigFile;
import com.vladium.utils.ObjectProfiler;

public class JDBCConfig {
	public static final String BANNERSTAT_TABLE = "bannerstats";	
	public static final String BANNERTYPESTAT_TABLE = "bannertypestats";
	public static final String BANNER_TABLE = "banners";
	public static final String CAMPAIGN_TABLE = "bannercampaigns";
	public static String connectionLock = "CONNECTION LOCK";
	
	private static Connection con; 
	private static SQLQueue sqlQueue;
	private static ConfigFile configFile;
	
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
	
	/* null means to use the default user directory for the config file. */
	public static boolean initDBConnection(ConfigFile config){
		String url = "blank";
		String user = "blank";
		String pass = "blank";
		configFile = config;
		url = config.getString("db_url");
		user = config.getString("db_user");
		pass = config.getString("db_pass");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			if (con != null) {
				con.close();
			}
			con = DriverManager.getConnection(url, user, pass);
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load JDBC driver.");
			e.printStackTrace();
			System.exit(-1);
		} catch (SQLException e) {
			System.err.println("Unable to establish connection to banner database.");
			//if (inputString("Display stack trace? (y)").equals("y"))
			e.printStackTrace();
			System.err.println("Run the following command (or something similar) if this is on the adblaster dev machine: ");
			System.err.println("ssh -nNT -R 3306:192.168.0.50:3306 root@192.168.0.50 -p 3022.");
			//System.exit(-1);
			return false;
		}
		return true;
	}
	
	private static void ensureConnectionOpen() throws SQLException {
		if (con.isClosed()) {
			initDBConnection(configFile);
		}
	}
	
	public static void initThreadedSQLQueue() {
		sqlQueue = new SQLQueue(1);
	}

	public static Statement createStatement() throws SQLException {
		ensureConnectionOpen();
		return con.createStatement();
	}
	
	public static void queueQuery(String query) {
		sqlQueue.execute(query);
	}

	public static void queueQuery(String query, byte[] a, byte[] b) {
		sqlQueue.execute(query, a, b);
	}
	
	//Wait until the query queue is empty before returning.
	public static void finishQueries() {
		while (!sqlQueue.isEmpty()) {
		}
	}

	public static PreparedStatement prepareStatement(String sql) throws SQLException {
		ensureConnectionOpen();
		return con.prepareStatement(sql);
	}

	public static SQLQueue getSQLQueue() {
		return sqlQueue;
	}
	
	public static int sizeofCon() {
		return ObjectProfiler.sizeof(con);
	}
}
