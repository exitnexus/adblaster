/**
 * 
 */
package com.nexopia.adblaster.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public final class EasyDatagramSocket extends DatagramSocket {
	public EasyDatagramSocket() throws SocketException {
		super();
	}

	public void send(String s) throws IOException{
		byte[] b = s.getBytes();
		this.send(new DatagramPacket(b,b.length,this.getRemoteSocketAddress()));
	}
}