package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			//process the input string here
		}
		input.close();
		client.close();
	}
 
	
	private static final int SERVER_PORT = 5556;
	private static final int SOCKET_TIMEOUT = 500; //ms
	private static boolean shutdown = false;
	
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
