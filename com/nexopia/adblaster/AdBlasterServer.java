package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.MissingResourceException;

public class AdBlasterServer implements Runnable {

	private ServerSocket socket;
	private Socket client;
	private BufferedReader input;
	
	public AdBlasterServer() {
		try {
			socket = new ServerSocket(SERVER_PORT);
			socket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (IOException e) {
			System.err.println("Failed to open socket on port: "+SERVER_PORT);
			e.printStackTrace();
			System.exit(-1);
		}

	}
	
	
	public AdBlasterServer(Socket client, BufferedReader in) {
		this.client = client;
		this.input = in;
		this.socket = null;
	}


	public boolean accept() throws SocketTimeoutException {
		if (socket == null) {
			throw new MissingResourceException("ServerSocket socket is null, this is likely caused by trying to listen with a non-master thread.", "AdBlasterServer", "null socket");
		}
		try {
			client = socket.accept();
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (SocketTimeoutException e) {
			throw e;
		} catch (IOException e) {
			System.err.println("Failed to initialize client connection.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public synchronized AdBlasterServer spawn() {
		AdBlasterServer child = new AdBlasterServer(client, input);
		client = null;
		input = null;
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
		String inputString;
		while ((inputString=input.readLine()) != null && !shutdown) {
			String words[] = inputString.split(" ");
			String command = words[0];
			if (command.toUpperCase().equals(START_ADBLASTER)) {
				if (words.length < START_ADBLASTER_MIN_PARAM_COUNT + 1) {
					reportParamError(START_ADBLASTER);
				}
				
				AdBlaster.main(new String[0]);
				
				//All parameters after the command will be bannerserver ips/ports in the form 0.0.0.0:1234
				//We will tell all of these to reload their coefficients once the adblaster run has completed.
				for (int i=1; i<words.length; i++) {
					String ip = words[i].split(":")[0];
					int port = Integer.parseInt(words[i].split(":")[1]);
					Socket bannerServer = new Socket(ip, port);
					PrintWriter bannerServerWriter = new PrintWriter(bannerServer.getOutputStream());
					bannerServerWriter.write(BannerServer.RELOAD_COEFFICIENTS + "\n");
				}
			}
			
		}
		input.close();
		client.close();
	}
	
	private void reportParamError(String command) {
		System.err.println("Received command " + command + " with insufficient arguments.");
	}
 
	
	private static final int SERVER_PORT = 8971;
	private static final int SOCKET_TIMEOUT = 500; //ms
	private static boolean shutdown = false;

	//DEFINED COMMANDS
	private static final String START_ADBLASTER = "START_ADBLASTER";
	private static final int START_ADBLASTER_MIN_PARAM_COUNT = 1;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		AdBlasterServer masterServer = new AdBlasterServer();
		System.out.println("Listening for connections on port " + SERVER_PORT + ".");
		while (!shutdown) {
			try {
				if (masterServer.accept()) {
					Thread t = new Thread(masterServer.spawn(), "WorkerAdBlasterServer");
					t.start();
				}
			} catch (SocketTimeoutException e) {
				//intentionally nothing done here, this just gives us a chance to
				//stop the program if we've been given a shutdown command
			}
		}
	}
}
