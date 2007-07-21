/**
 * 
 */
package com.nexopia.adblaster.struct;

public class BannerStat{
	//daily == so far today
	private int dailyviews; 
	private int dailyclicks;
	
	//so far this minute
	private int passbacks;
	private int current_views;
	private int current_clicks;
	
	public BannerStat(){
	}
	
	public boolean hasChanged() {
		return (passbacks > 0 || current_views > 0 || current_clicks > 0);
	}
	
	public void view() {
		dailyviews++;
		current_views++;
	}
	
	public void click() {
		dailyclicks++;
		current_clicks++;
	}
	
	public void passback() {
		passbacks++;
	}

	public int getDailyClicks() {
		return this.dailyclicks;
	}

	public int getCurrentClicks() {
		return current_clicks;
	}

	
	public int getCurrentViews() {
		return this.current_views;
	}
	
	public int getDailyViews() {
		return this.dailyviews;
	}
	
	public int getPassbacks() {
		return this.passbacks;
	}
	
	public void clearCurrent() {
		this.current_clicks = 0;
		this.current_views = 0;
		this.passbacks = 0;
	}
	
	public void clearDaily() {
		this.dailyclicks = 0;
		this.dailyviews = 0;
	}
}