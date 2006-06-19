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
		AdCampaign ac = new AdCampaign(10,num_banners,num_users);
		for (int i = 0; i < num_users; i++){
			for (int j = 0; j < ac.num_interests; j++){
				if (Math.random() > 0.5){
					ac.u[i].interests.checked.add(new Integer(j));
				}
			}
		}
		
		for(int i = 0; i < num_banners; i++){
			ac.b[i].profit = Math.random();
			for (int j = 0; j < ac.num_interests; j++){
				if (Math.random() > 0.5){
					ac.b[i].interests.checked.add(new Integer(j));
				}
			}
		}
		return ac;
	}
	

}
