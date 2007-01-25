/**
 * 
 */
package com.nexopia.adblaster.struct;

public class BannerStat{
	public static final int MEMORY_USAGE = 46;
	public int dailyviews;
	public int dailyclicks;
	public int passbacks;
	int current_views;
	int current_clicks;
	
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

	public int getDailyClicks() {
		return this.dailyclicks;
	}

	public int getCurrentClicks() {
		return current_clicks;
	}

	public void setCurrentClicks(int i) {
		current_clicks = i;
	}

	public void SetCurrentViews(int i) {
		current_views = i;
	}

	public int getCurrentViews() {
		return this.current_views;
	}
}