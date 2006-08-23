package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCConfig {
	private static Connection con; 
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3307/banner";
			con = DriverManager.getConnection(url, "nathan", "nathan");
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
}
