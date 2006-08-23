/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;

/* Represents a whole system (typically a website) of users with a set of banners, and \
 * targetting info that can relate the banners and users.
 * 
 */
public class AdBlasterUniverse extends AbstractAdBlasterUniverse {
	private HashMap <Integer, User> u;
	private HashMap <Integer, Banner> b;

	public int getBannerCount(){
		return b.size();
	}
	protected void addBanner(Banner banner) {
		b.put(new Integer(banner.id), banner);
	}

	public User getUser(int i) {
		return u.get(new Integer(i));
	}

	public int getUserCount(){
		return u.size();
	}
	
	protected void addUser(User user) {
		u.put(new Integer(user.id), user);
	}

	public AdBlasterUniverse(int interests, int num_banners, int num_users){
		u = new HashMap<Integer, User>();
		b = new HashMap<Integer, Banner>();
		
		for (int i = 0; i < num_users; i++){
			Random r = new Random(System.currentTimeMillis());
			int userid = i;
			byte age = (byte)(14+r.nextInt(86));
			byte sex = (byte)(r.nextBoolean()?1:0);
			short loc = (short)r.nextInt(1000);
			String inter = "1,4";
			System.out.println(userid + ":" + age + ":" + sex + ":" + loc + ":" + inter);
			
			addUser(new User(userid, age, sex, loc, inter)); //TODO we're passing in a null database here which is bad in the long run
		}
		
		for(int i = 0; i < num_banners; i++){
			Random r = new Random(System.currentTimeMillis());
			int payrate = (1+r.nextInt(99));
			Interests inter = new Interests("1,4", true);
			//System.out.println(userid + ":" + age + ":" + sex + ":" + loc + ":" + inter);
			
			int campaignID=1;
			int maxHits=1;
			//Vector location = new Vector();
			Vector<Integer> sexes = new Vector<Integer>();
			Vector<Integer> loc = new Vector<Integer>();
			Vector<Integer> ages = new Vector<Integer>();
			
			addBanner(new Banner(i, payrate, maxHits, campaignID, loc, sexes, ages, inter, null));
		}
	}
	
	public AdBlasterUniverse() {
		u = new HashMap<Integer, User>();
		b = new HashMap<Integer, Banner>();
	}

	public void makeMeADatabase(){
		try {			

			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			
			UserDatabase db = new UserDatabase("test");

			System.out.println("Should be inserting " + this.getUserCount() + " users.");
			for (int i=0; i<this.getUserCount(); i++) {
				db.insert((User)this.getUser(i));
			}
			
			db.close();
		} catch (DatabaseException e) {
			System.err.println("DatabaseException: " + e);
			e.printStackTrace();
		}
	}

	public static AdBlasterUniverse generateTestData(int num_banners, int num_users, int num_interests){
		/*Generate ab set of test banners and parameters
		 * 
		 * data: 
		 * 	num_banners (int)
		 *  banners (array)
		 *   targetting data:
		 *    max views daily (int)
		 *    viewing ranges (time)
		 *    profitability (float)
		 *   
		 *  num_users (int)
		 *  users (array)
		 *   targetting dataxmms divx
		 *  
		 * 
		 * */
		AdBlasterUniverse ac = new AdBlasterUniverse(20,num_banners,num_users);
		for (int i = 0; i < num_users; i++){
			for (int j = 0; j < num_interests; j++){
				if (Math.random() > 0.5){
					ac.getUser(i).interests.add(j);
				}
			}
		}
		
		
		/* Add a foolproof banner that never pays and never runs out.
		 * 
		 */
		ac.getBannerByIndex(0).setPayrate(0);
		ac.getBannerByIndex(0).setViewsperday(Integer.MAX_VALUE);
		ac.getBannerByIndex(0).interests.clear();

		for(int i = 1; i < num_banners; i++){
			ac.getBannerByIndex(i).setPayrate((int)(Math.random()*10));
			for (int j = 0; j < num_interests; j++){
				if (Math.random() > 0.95){
					ac.getBannerByIndex(i).interests.add(j);
				}
			}
		}
		return ac;
	}

	/* (non-Javadoc)
	 * @see com.nexopia.adblaster.AbstractAdBlasterUniverse#getBanners()
	 */
	public Collection getBanners() {
		return Arrays.asList(b);
	}

	public Banner getBannerByIndex(int i) {
		return (Banner) b.keySet().toArray()[i];
	}

	public Banner getBannerByID(int i) {
		return b.get(new Integer(i));
	}
	@Override
	public Campaign getCampaignByIndex(int i) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getCampaignCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
