package com.nexopia.adblaster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPClient {

	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;
			DatagramSocket socket = new DatagramSocket();
			while ((input = br.readLine()) != null) {
				System.out.println(input);
				byte[] buf = input.getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), 5556);
				socket.send(packet);
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
