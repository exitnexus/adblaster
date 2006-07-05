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
import com.sleepycat.je.SecondaryMultiKeyCreator;

public class BannerKeyCreator implements SecondaryMultiKeyCreator {
	Collection banners;
	IntegerBinding ib = new IntegerBinding();
	UserBinding ub = new UserBinding();
	DatabaseEntry secondaryKey = new DatabaseEntry();

	public BannerKeyCreator(Collection banners) {
		this.banners = banners;
	}
	
	/* (non-Javadoc)
	 * @see com.sleepycat.je.SecondaryMultiKeyCreator#createSecondaryKeys(com.sleepycat.je.SecondaryDatabase, com.sleepycat.je.DatabaseEntry, com.sleepycat.je.DatabaseEntry, java.util.Set)
	 */
	@SuppressWarnings("unchecked")
	public void createSecondaryKeys(SecondaryDatabase db, DatabaseEntry key, DatabaseEntry data, Set secondaryKeys) throws DatabaseException {
		User u = (User)ub.entryToObject(data);
		for (Iterator i=banners.iterator(); i.hasNext(); ) {
			Banner banner=(Banner)i.next();
			if (banner.validUser(u)) {
				ib.objectToEntry(new Integer(banner.getID()), secondaryKey);
				secondaryKeys.add(secondaryKey);
			}
		}
	}
}
