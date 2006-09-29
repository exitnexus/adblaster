/**
 * 
 */
package com.nexopia.adblaster.struct;

public class ServerStat {
	public int starttime;
	public ServerStat() {
		starttime = (int)(System.currentTimeMillis()/1000);
	}
	public int connect = 0; 
	public int get = 0;
	public int getfail = 0; 
	public int click = 0;
}