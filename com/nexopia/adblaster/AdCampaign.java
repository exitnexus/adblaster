/**
 * 
 */
package com.nexopia.adblaster;

class AdCampaign{
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
	
	int num_interests;
	User u[];
	Banner b[];
	
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
}
