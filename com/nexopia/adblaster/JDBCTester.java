/*
 * Created on Jun 20, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class JDBCTester {

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			Connection con = DriverManager.getConnection(url, "nathan", "nathan");
			String sql = "SELECT * FROM banners";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String title = rs.getString("TITLE");
				int id = rs.getInt("ID");
				System.out.println(id + ": " + title);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
