package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sleepycat.je.DatabaseException;

public class TextReader {
	
	public static void main(String args[]){
		try {
			File f = new File("newbanner.log");
			FileInputStream fis = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader buf = new BufferedReader(isr);
			String s = "";
			int i = 0;
			AdBlasterUniverse universe = new AdBlasterUniverse();
			AdBlasterInstance instance = new AdBlasterInstance(universe);
			AdBlaster.instanceBinding = new BannerViewBinding(universe, instance);
			BannerDatabase bannerDB = new BannerDatabase();
			for (int j = 0; j < bannerDB.getBannerCount(); j++){
				universe.addBanner(bannerDB.getBannerByIndex(j));
			}
			BannerViewDatabase db = null;
			UserDatabase userDB = null;
			try {
				db = new BannerViewDatabase();
				userDB = new UserDatabase();
			} catch (DatabaseException e1) {
				e1.printStackTrace();
			}
			
			int index = 0;
			while ((s = buf.readLine()) != null){
				if (s.indexOf("get") >= 0){
					i++;
					String array[] = s.split("\\s");
					int timestamp = Integer.parseInt(array[1]);
					int size = Integer.parseInt(array[2]);
					int userid = Integer.parseInt(array[3]);
					if (userid % 3 == 2){
						byte age = Byte.parseByte(array[4]);
						byte sex = Byte.parseByte(array[5]);
						short location = Short.parseShort(array[6]);
						Interests in = new Interests(array[7]);
						String page = array[8];
						int passback = Integer.parseInt(array[9]);
						int bannerid = Integer.parseInt(array[11]);
						User u = userDB.getUser(userid);
						if (u == null){
							u = new User(userid, age, sex, location, in);
							universe.addUser(u);
							try {
								userDB.insert(u);
							} catch (DatabaseException e) {
								e.printStackTrace();
							}
						}
						Banner b = universe.getBannerByID(bannerid);
						BannerView bv = new BannerView(instance, index++, u, b, timestamp, b.getSize(), bannerid);
						try {
						  db.insert(bv);
						} catch (DatabaseException e) {
							e.printStackTrace();
						}
					}
				}
				if (i % 1000 == 0){
					System.out.println(i);
				}
			}
			try {
				db.close();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			System.out.println(i);
			System.exit(0);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
