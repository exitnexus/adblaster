package com.nexopia.adblaster.util;

import java.util.HashMap;

import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.util.LowMemMap.LowMemArray;

public class LowMemMultiMap {
	HashMap<ServablePropertyHolder,LowMemMap> multiMap;
	
	public LowMemMultiMap(){
		multiMap = new HashMap<ServablePropertyHolder, LowMemMap>();
	}
	
	public LowMemArray get(int userid, ServablePropertyHolder b){
		/* Get records of all views for the banner.*/
		LowMemMap userViewMap = multiMap.get(b);
		if (userViewMap == null){
			userViewMap = new LowMemMap(b.getViewsPerUser());
			multiMap.put(b, userViewMap);
		}
		
		/* From the above records, get all views for this user.*/
		LowMemArray views = userViewMap.getOrCreate(userid);
		return views;
	}

}
