/*
 * Created on Jun 20, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.nexopia.adblaster.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.util.Integer;

public class JDBCTester {

	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://192.168.0.50:3307/banner";
			Connection con = DriverManager.getConnection(url, "nathan", "nathan");
			String sql = "SELECT * FROM banners";
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			System.out.println(rs.getFetchSize());
			HashMap<Integer, Banner> banners = new HashMap<Integer, Banner>();
			for (int i=0; rs.next(); i++) {
				if (true) throw new UnsupportedOperationException("Hasn't been updated.");
				//banners.put(new Integer(rs.getInt("ID")), new Banner(rs, null));
			}

			for (Iterator it = banners.values().iterator(); it.hasNext(); ){
				Banner b = (Banner)it.next();
				System.out.println(b.getID() + " " + b.getInterests());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
