package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.MissingResourceException;

import com.sleepycat.je.DatabaseException;

public class InsertServer implements Runnable {

	private ServerSocket socket;
	private Socket client;
	private PrintWriter out;
	private BufferedReader in;
	
	private static class ThreadedDatabases{
		private BannerViewDatabase bannerViewDb;
		private UserDatabase userDb;
		private PageDatabase pageDb;
		private int day;
		private static final long DAY_MS = 86400000;
		
		public ThreadedDatabases(){
			day = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
			try {
				bannerViewDb = new BannerViewDatabase(""+day);
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
				bannerViewDb = new BannerViewDatabase(""+day);
				userDb = new UserDatabase(""+day);
				pageDb = new PageDatabase(""+day);
			} catch (DatabaseException dbe) {
				System.err.println("Unable to open databases: " + dbe);
				dbe.printStackTrace();
				System.exit(-1);
			}
		}
		
	}
	
	public InsertServer() {
		try {
			socket = new ServerSocket(SERVER_PORT);
			socket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (IOException e) {
			System.err.println("Failed to open socket on port: "+SERVER_PORT);
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	
	public InsertServer(Socket client, PrintWriter out, BufferedReader in) {
		this.client = client;
		this.out = out;
		this.in = in;
		this.socket = null;
	}


	public boolean accept() throws SocketTimeoutException {
		if (socket == null) {
			throw new MissingResourceException("ServerSocket socket is null, this is likely caused by trying to listen with a non-master thread.", "InsertServer", "null socket");
		}
		try {
			client = socket.accept();
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (SocketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			System.err.println("Failed to initialize client connection.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public synchronized InsertServer spawn() {
		InsertServer child = new InsertServer(client, out, in);
		client = null;
		out = null;
		in = null;
		return child;
	}
	
	public void run() {
		try {
			processInput();
		} catch (IOException e) {
			System.err.println("Error occurred while processing input.");
			e.printStackTrace();
		}
	}
	
	private void processInput() throws IOException {
		String input;
		while ((input=in.readLine()) != null && !shutdown) {
			//System.out.println("Received command " + input);
			String[] words = input.split("\\s");
			if (words.length == 0) continue;
			String command = words[0];
			//get usertime size userid age sex location interests page passback => bannerid
			if (command.equals("get") && words.length == 13) {
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
			} else if (command.equals("quit")) {
				out.println("Closing connection...");
				break;
			} else if (command.equals("shutdown")) {
				out.println("Shutting down...");
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
				break;
			} else {
				out.print("Invalid command \"" + command + "\" passed with arguments:");
				for (int i=1;i<words.length;i++) {
					if (words[i].length() > 0) out.print(" " + words[i]);
				}
				out.print("\n");
			}
			out.flush();
			//if (!client.isConnected()){
			//	System.out.println("Disconnected");
			//	break;
			//}
		}
		in.close();
		out.close();
		client.close();
	}
 
	
	private static final int SERVER_PORT = 5556;
	private static final int SOCKET_TIMEOUT = 500; //ms
	private static boolean shutdown = false;
	private static User user; //We just keep reusing this object when inserting into the user db
	private static ThreadedDatabases tdb;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		tdb = new ThreadedDatabases();
		user = new User();
		
		InsertServer masterServer = new InsertServer();
		System.out.println("Listening for connections on port " + SERVER_PORT + ".");
		while (!shutdown) {
			if (tdb.isOld()){
				tdb.renew();
			}
			try {
				if (masterServer.accept()) {
					Thread t = new Thread(masterServer.spawn(), "WorkerInsertServer");
					t.start();
				}
			} catch (SocketTimeoutException e) {
				//intentionally nothing done here, this just gives us a chance to
				//stop the program if we've been given a shutdown command
			}
		}
	}
}
