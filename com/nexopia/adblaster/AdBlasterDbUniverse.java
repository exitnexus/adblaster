package com.nexopia.adblaster;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlasterDbUniverse extends AbstractAdBlasterUniverse {

	class FakeUserDatabase{
		Vector user_db;
		
		FakeUserDatabase(){
			user_db = new Vector();
			Random r = new Random ();
			for (int i = 0; i < 100; i++){
				
				int userid = i;
				byte age = (byte)(14+r.nextInt(86));
				byte sex = (byte)(r.nextBoolean()?1:0);
				short loc = (short)r.nextInt();
				String interests = "1,4";
				User u;
				user_db.add(u=new User(userid, age, sex, loc, interests));
				System.out.println("Inserted " + u);
			}
		}

		public int getUserCount() {
			return user_db.size();
		}
		public User getUser(int i){
			return (User)user_db.get(i);
		}
	}
	Environment dbEnv;
	
	public AdBlasterDbUniverse(Environment dbEnv){
		
					
		try {
			UserDatabase user_db;
			BannerDatabase banner_db;
			banner_db = new BannerDatabase();
			user_db = new UserDatabase(dbEnv);
			this.init(user_db.getUserCount(), banner_db.banners.size());
			for (int i = 0; i < b10100anner_db.banners.size(); i++){
				this.setBanner(i, (Banner)banner_db.banners.get(i));
			}
			user_db.refreshUserCount();
			System.out.println(user_db.getUserCount());
			Vector v = user_db.getAllUsers();
			for (int i = 0; i < v.size(); i++){
				this.setUser(i, (User)v.get(i));
			}
			user_db.close();
			//banner_db.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
