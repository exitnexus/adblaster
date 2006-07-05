/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.EnvironmentConfig;

/* Represents a whole system (typically a website) of users with a set of banners, and \
 * targetting info that can relate the banners and users.
 * 
 */
public class AdBlasterUniverse extends AbstractAdBlasterUniverse {
	protected int num_interests;
	private User u[];
	private Banner b[];

	protected void init(int u_num, int b_num){
		u = new User[u_num];
		b = new Banner[b_num];
	}
	
	public int getBannerCount(){
		return b.length;
	}
	protected void setBanner(int i, Banner banner) {
		b[i] = banner;
	}

	public User getUser(int i) {
		return u[i];
	}

	public int getUserCount(){
		return u.length;
	}
	protected void setUser(int i, User user) {
		u[i] = user;
	}

	public AdBlasterUniverse(int interests, int num_banners, int num_users){
		this.init(num_users, num_banners);
		
		for (int i = 0; i < num_users; i++){
			Random r = new Random(System.currentTimeMillis());
			int userid = i;
			byte age = (byte)(14+r.nextInt(86));
			byte sex = (byte)(r.nextBoolean()?1:0);
			short loc = (short)r.nextInt(1000);
			String inter = "1,4";
			System.out.println(userid + ":" + age + ":" + sex + ":" + loc + ":" + inter);
			
			setUser(i, new User(userid, age, sex, loc, inter)); //TODO we're passing in a null database here which is bad in the long run
		}
		
		for(int i = 0; i < num_banners; i++){
			Random r = new Random(System.currentTimeMillis());
			int payrate = (1+r.nextInt(99));
			Interests inter = new Interests("1,4");
			//System.out.println(userid + ":" + age + ":" + sex + ":" + loc + ":" + inter);
			
			int campaignID=1;
			int maxHits=1;
			//Vector location = new Vector();
			Vector sexes = new Vector();
			Vector loc = new Vector();
			Vector ages = new Vector();
			
			setBanner(i, new Banner(i, payrate, maxHits, campaignID, loc, sexes, ages, inter));
		}
		num_interests = interests;
	}
	
	public void makeMeADatabase(){
		try {			

			EnvironmentConfig envConf = new EnvironmentConfig();
			envConf.setAllowCreate(true);
			
			UserDatabase db = new UserDatabase();

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

	public static AdBlasterUniverse generateTestData(int num_banners, int num_users){
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
			for (int j = 0; j < ac.num_interests; j++){
				if (Math.random() > 0.5){
					ac.getUser(i).interests.add(new Integer(j));
				}
			}
		}
		
		
		/* Add a foolproof banner that never pays and never runs out.
		 * 
		 */
		ac.getBanner(0).setPayrate(0);
		ac.getBanner(0).setMaxHits(Integer.MAX_VALUE);

		ac.getBanner(0).interests.getChecked().clear();

		for(int i = 1; i < num_banners; i++){
			ac.getBanner(i).setPayrate((int)(Math.random()*10));
			for (int j = 0; j < ac.num_interests; j++){
				if (Math.random() > 0.95){
					ac.getBanner(i).interests.add(new Integer(j));
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

	public Banner getBanner(int i) {
		return b[i];
	}
	public Banner getBannerByIndex(int i) {
		return b[i];
	}

	public Banner getBannerByID(int i) {
		return b[i];
	}

	public User getRandomUser() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
