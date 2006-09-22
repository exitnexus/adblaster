package com.nexopia.adblaster;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;

import com.nexopia.adblaster.db.BannerViewBinding;
import com.nexopia.adblaster.db.PageDatabase;
import com.nexopia.adblaster.db.UserDatabase;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.Integer;
import com.sleepycat.je.DatabaseException;

public class UDPInsertServer {
	private static class ThreadedDatabases{
		static BannerViewBinding bvb = new BannerViewBinding(null,null);
		private BannerViewDatabase bannerViewDb;
		private UserDatabase userDb;
		private PageDatabase pageDb;
		private int day;
		
		public ThreadedDatabases(){
			day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
			try {
				bannerViewDb = new BannerViewDatabase(""+day, bvb);
				userDb = new UserDatabase(""+day);
				pageDb = new PageDatabase(""+day);
			} catch (DatabaseException dbe) {
				System.err.println("Unable to open databases: " + dbe);
				dbe.printStackTrace();
				System.exit(-1);
			}
		}
		
		public boolean isOld() {
			//System.out.println(System.currentTimeMillis() - start_time);
			if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != day){
				System.out.println("Too old!");
				return true;
			}
			return false;
		}
		
		synchronized public void renew() {
			try {
				bannerViewDb.close();
				userDb.close();
				pageDb.close();
			} catch (DatabaseException dbe) {
				System.err.println("Unable to close databases: " + dbe);
				dbe.printStackTrace();
				System.exit(-1);
			}
			
			day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
			try {
				bannerViewDb = new BannerViewDatabase(""+day, bvb);
				userDb = new UserDatabase(""+day);
				pageDb = new PageDatabase(""+day);
			} catch (DatabaseException dbe) {
				System.err.println("Unable to open databases: " + dbe);
				dbe.printStackTrace();
				System.exit(-1);
			}
		}
		
	}//end threadeddatabase
	
	private DatagramSocket socket;
	private DatagramPacket packet;
	
	public UDPInsertServer() {
		try {
			socket = new DatagramSocket(SERVER_PORT);
		} catch (SocketException e) {
			System.err.println("Unable to open UDP socket.");
			e.printStackTrace();
		}
		packet = new DatagramPacket(new byte[1024], 1024);
	}
	
	public void handlePacket() throws IOException {
		socket.receive(packet);
		processInput(new String(packet.getData()));
		packet.setData(new byte[1024]);
	}
	
	private void processInput(String input) {
		String[] words = input.split("\\s");
		if (words.length == 0) return;
		String command = words[0];
		//get usertime size userid age sex location interests page passback => bannerid
		if (command.indexOf("get") == 0 && words.length == 14) {
			int time = Integer.parseInt(words[1]);
			byte size = Byte.parseByte(words[2]);
			int userid = Integer.parseInt(words[3]);
			int age = Integer.parseInt(words[4]);
			int sex = Integer.parseInt(words[5]);
			int location = Integer.parseInt(words[6]);
			String interests = words[7];
			String page = words[8];
			int bannerid = Integer.parseInt(words[12]);
			
			try {
				//we don't create a new user object here to save on object creation overhead, just reuse one user repeatedly
				synchronized (tdb){
					user.fill(userid, age, sex, location, interests);
					tdb.userDb.insert(user);
					int pageIndex = tdb.pageDb.insert(page);
					tdb.bannerViewDb.insert(userid, bannerid, time, size, pageIndex);
					if (tdb.bannerViewDb.getBannerViewCount()%1000 == 0) {
						System.out.println("Banner Count: " + tdb.bannerViewDb.getBannerViewCount());
					}
				}
			} catch (DatabaseException e) {
				System.err.println("Failed to insert into bannerview database: "+Integer.parseInt(words[1])+" "+Integer.parseInt(words[2])+" "+Integer.parseInt(words[3]));
				e.printStackTrace();
				System.exit(0);
			}
		} else if (command.indexOf("shutdown") == 0) {
			shutdown = true;
			try {
				tdb.pageDb.dump();
				tdb.userDb.close();
				tdb.bannerViewDb.close();
				tdb.pageDb.close();
			} catch (DatabaseException dbe) {
				System.err.println("Databases not closed properly at shutdown.");
				dbe.printStackTrace();
			}
		}
	}
	
	
	private static final int SERVER_PORT = 5556;
	private static boolean shutdown = false;
	private static User user; //We just keep reusing this object when inserting into the user db
	private static ThreadedDatabases tdb;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		tdb = new ThreadedDatabases();
		user = new User();
		
		UDPInsertServer server = new UDPInsertServer();
		System.out.println("Listening for connections on port " + SERVER_PORT + ".");
		while (!shutdown) {
			if (tdb.isOld()){
				tdb.renew();
			}
			try {
				server.handlePacket();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
}
