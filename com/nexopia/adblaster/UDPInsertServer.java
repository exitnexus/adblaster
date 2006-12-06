package com.nexopia.adblaster;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;

import com.nexopia.adblaster.db.BannerViewFlatFileWriter;
import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.db.UserFlatFileWriter;
import com.nexopia.adblaster.struct.User;
import com.nexopia.adblaster.util.Integer;

public class UDPInsertServer {
	private static class ThreadedDatabases{
		private UserFlatFileWriter userWriter;
		private BannerViewFlatFileWriter bannerViewWriter;
		private PageFlatFileDatabase pageWriter;
		private int day;
		
		public ThreadedDatabases() throws IOException{
			this.init();
		}
		
		public boolean isOld() {
			//System.out.println(System.currentTimeMillis() - start_time);
			if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) != day){
				System.out.println("Too old!");
				return true;
			}
			return false;
		}
		
		private void init() throws IOException {
			day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
			userWriter = new UserFlatFileWriter("DB_" + day, true);
			bannerViewWriter = new BannerViewFlatFileWriter("DB_" + day, true);
			pageWriter = new PageFlatFileDatabase("DB_" + day, true);
		}
		
		synchronized public void renew() {
			try {
				userWriter.sync();
				pageWriter.sync();
				bannerViewWriter.sync();
			} catch (IOException e) {
				System.err.println("Unable to sync file writers at days end: " + e);
				e.printStackTrace();
				System.exit(-1);
			}
			
			try {
				this.init();
			} catch (IOException e) {
				System.err.println("Unable to initialize writing for a new day: " + e);
				e.printStackTrace();
				System.exit(-1);
			}
		}

		public void close() throws IOException {
			userWriter.close();
			pageWriter.close();
			bannerViewWriter.close();
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
		//System.out.println("Received: " + input);
		String[] words = input.split("\\s");
		if (words.length == 0) return;
		String command = words[0];
		//get usertime size userid age sex location interests page passback => bannerid
		if (command.indexOf("get") == 0 && words.length == 14) {
			//System.out.println("Actual request");
			int time = Integer.parseInt(words[1]);
			byte size = Byte.parseByte(words[2]);
			int userid = Integer.parseInt(words[3]);
			int age = Integer.parseInt(words[4]);
			int sex = Integer.parseInt(words[5]);
			int location = Integer.parseInt(words[6]);
			String interests = words[7];
			String page = words[8];
			int bannerid = Integer.parseInt(words[12]);
			
			//we don't create a new user object here to save on object creation overhead, just reuse one user repeatedly
			synchronized (tdb){
				user.fill(userid, age, sex, location, interests);
				try {
					tdb.userWriter.write(user);
					int pageIndex = tdb.pageWriter.write(page);
					tdb.bannerViewWriter.write(userid, bannerid, time, size, pageIndex);
				} catch (IOException e) {
					System.err.println("Error handling input: " + input);
					e.printStackTrace();
				}
			}
		} else if (command.indexOf("shutdown") == 0) {
			shutdown = true;
			try {
				tdb.close();
			} catch (IOException e) {
				System.err.println("Files not all closed properly at shutdown.");
				e.printStackTrace();
			}
		}
	}
	
	
	private static final int SERVER_PORT = 5556;
	private static boolean shutdown = false;
	private static User user; //We just keep reusing this object when inserting into the user db
	private static ThreadedDatabases tdb;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		tdb = new ThreadedDatabases();
		user = new User();
		
		UDPInsertServer server = new UDPInsertServer();
		System.out.println("Listening for packets on port " + SERVER_PORT + ".");
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
