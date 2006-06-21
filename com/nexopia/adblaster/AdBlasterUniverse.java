/**
 * 
 */
package com.nexopia.adblaster;

import java.util.Random;

/* Represents a whole system (typically a website) of users with a set of banners, and \
 * targetting info that can relate the banners and users.
 * 
 */
public class AdBlasterUniverse extends AbstractAdBlasterUniverse {
	
	public AdBlasterUniverse(int interests, int num_banners, int num_users){
		u = new User[num_users];
		for (int i = 0; i < num_users; i++){
			u[i] = new User(i, null); //TODO we're passing in a null database here which is bad in the long run
		}
		
		b = new Banner[num_banners];
		for(int i = 0; i < num_banners; i++){
			b[i] = new Banner();
		}
		num_interests = interests;
	}
	
	public static AdBlasterUniverse generateTestData(int num_banners, int num_users){
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
		AdBlasterUniverse ac = new AdBlasterUniverse(20,num_banners,num_users);
		for (int i = 0; i < num_users; i++){
			for (int j = 0; j < ac.num_interests; j++){
				if (Math.random() > 0.5){
					ac.u[i].interests.add(new Integer(j));
				}
			}
		}
		
		/* Add a foolproof banner that never pays and never runs out.
		 * 
		 */
		ac.b[0].setPayrate(0);
		ac.b[0].setMaxHits(Integer.MAX_VALUE);

		ac.b[0].interests.getChecked().clear();

		for(int i = 1; i < num_banners; i++){
			ac.b[i].setPayrate((int)(Math.random()*10));
			for (int j = 0; j < ac.num_interests; j++){
				if (Math.random() > 0.95){
					ac.b[i].interests.add(new Integer(j));
				}
			}
		}
		return ac;
	}
	

}