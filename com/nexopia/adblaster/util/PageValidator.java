/**
 * 
 */
package com.nexopia.adblaster.util;

import com.nexopia.adblaster.struct.BannerView;

public abstract interface PageValidator{
	public boolean validate(String page);
	public PageValidator clone();
	public void make(String string);
	public boolean validate(BannerView bv);
}