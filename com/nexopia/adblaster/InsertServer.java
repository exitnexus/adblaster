package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class InsertServer implements Runnable {

	private ServerSocket socket;
	private Socket client;
	private PrintWriter out;
	private BufferedReader in;
	
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
			return false;
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
			System.out.println("Received: "+input);
			if (input.equals("Quit")) {
				break;
			} else if (input.equals("Shutdown")) {
				shutdown = true;
				break;
			}
		}
		in.close();
		out.close();
		client.close();
	}
 
	
	private static final int SERVER_PORT = 5555;
	private static final int SOCKET_TIMEOUT = 500; //ms
	private static boolean shutdown = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InsertServer masterServer = new InsertServer();
		while (!shutdown) {
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
