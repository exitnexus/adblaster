/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Vector;

class AdCampaign{
	int num_interests;
	User u[];
	Banner b[];

	AdCampaign(int interests, int num_users, int num_banners){
		u = new User[num_users];
		for (int i = 0; i < num_users; i++){
			u[i] = new User("User" + i);
		}
		
		b = new Banner[num_banners];
		for(int i = 0; i < num_banners; i++){
			b[i] = new Banner();
		}
		num_interests = interests;
	}
	
	class Interests{
		private Interests(){
		  checked = new boolean[num_interests];
		}
		public boolean checked[];
	}
	class User{
		String name;
		Interests i;
		User(String s){
			name = s;
			i = new Interests();
		}
	}
	
	class Banner{
		Interests i;
		Banner(){
			i = new Interests();
		}
	}

	public static AdCampaign generateTestData(int num_banners, int num_users){
		/*Generate a set of test banners and parameters
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
		 *   targetting data
		 *  
		 * 
		 * */
		return new AdCampaign(1,1,1);
	}

	public BannerView randomView(AdBlasterPolicy pol) {
		// TODO Auto-generated method stub
		User randomPick = u[(int) (Math.random()*u.length)];
		int time = (int) (Math.random()*60*60*24);
		Banner banner = b[(int) (Math.random()*b.length)];
		return new BannerView(randomPick, banner, time);
	}
	

}
