/*
 * Created on Jun 19, 2006
 */
package com.nexopia.adblaster;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.SecondaryMultiKeyCreator;

public class BannerKeyCreator implements SecondaryMultiKeyCreator {
	Collection banners;
	public BannerKeyCreator(Collection banners) {
		this.banners = banners;
	}
	
	/* (non-Javadoc)
	 * @see com.sleepycat.je.SecondaryMultiKeyCreator#createSecondaryKeys(com.sleepycat.je.SecondaryDatabase, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry, java.util.Set)
	 */
	public void createSecondaryKeys(SecondaryDatabase db, DatabaseEntry key, DatabaseEntry data, Set secondaryKeys) throws DatabaseException {
		UserBinding ub = new UserBinding();
		IntegerBinding ib = new IntegerBinding();
		User u = (User)ub.entryToObject(data);
		for (Iterator i=banners.iterator(); i.hasNext(); ) {
			Banner banner=(Banner)i.next();
			if (banner.validUser(u)) {
				DatabaseEntry secondaryKey = new DatabaseEntry();
				ib.objectToEntry(new Integer(banner.getID()), secondaryKey);
				secondaryKeys.add(secondaryKey);
			}
		}
	}
}
