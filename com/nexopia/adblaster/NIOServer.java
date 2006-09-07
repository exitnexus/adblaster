package com.nexopia.adblaster;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.nio.channels.spi.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

import com.nexopia.adblaster.Campaign.CampaignDB;

//Listen on a port for connections and write back the current time.
public class NIOServer {
	public  static void main (String args[]) throws IOException {
		Object args1[] = {};
		CampaignDB cdb = new CampaignDB();
		BannerDatabase bdb = new BannerDatabase(cdb, new PageValidatorFactory(Utilities.PageValidator1.class, args1));
		BannerServer banners = new BannerServer(bdb, cdb, 1);
		
		//Create the server socket channel
		ServerSocketChannel server = null;
		Selector selector = null;
		
		int BUFFER_SIZE = 1024;
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

		try {
			server = ServerSocketChannel.open();
			
			//nonblocking I/O
			server.configureBlocking(false);
			
			//host-port 8000
			server.socket().bind(new java.net.InetSocketAddress("localhost",8000));
			
			System.out.println("Server listening on port 8000");
			//Create the selector
			selector = Selector.open();
			
			//Recording server to selector (type OP_ACCEPT)
			server.register(selector,SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//ByteBuffer lastbuffer = null;
		
//		Infinite server loop
		for(int index=0; index > -1; ) {
			selector.select();
		
			// Get keys
			Set keys = selector.selectedKeys();
			Iterator i = keys.iterator();
			
			// For each keys...
			while(i.hasNext()) {
				SelectionKey key = (SelectionKey) i.next();
				
				// Remove the current key
				i.remove();
				
				// if isAccetable = true
				// then a client required a connection
				if (key.isAcceptable()) {
					// get client socket channel
					SocketChannel client;
					try {
						client = server.accept();
						// Non Blocking I/O
						client.configureBlocking(false);
						//recording to the selector (reading)
						client.register(selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE|SelectionKey.OP_CONNECT);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				
				if (key.isConnectable()) {
					System.err.println("Client closed");
					SocketChannel client = (SocketChannel) key.channel();
					client.close();
					continue;
				}
				
				// if isReadable = true
				// then the server is ready to read 
				if (key.isReadable() && key.isWritable()) {
					
					SocketChannel client = (SocketChannel) key.channel();
					
					// Read byte coming from the client
					buffer.clear();
					try {
						if (client.read(buffer) == -1) {
							System.out.println("Read closed");
						}
					}
					catch (Exception e) {
						// client is no longer active
						e.printStackTrace();
						continue;
					}
					
					// Show bytes on the console
					buffer.flip();
					Charset charset=Charset.forName("ISO-8859-1");
					CharsetDecoder decoder = charset.newDecoder();
					CharBuffer charBuffer = null;
					try {
						charBuffer = decoder.decode(buffer);
					} catch (CharacterCodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String result = null;
					try {
						result = banners.receive(charBuffer.toString());
					} catch (Exception e) {
						System.err.println("Something bad happened.");
						//set some error indication value in result
						System.err.println(e);
						e.printStackTrace();
					}
					try {
						ByteBuffer output = charset.encode(result+"-"+index+'\n');
						//System.out.println(output.toString());
						client.write(output);
						index++;
					} catch (Exception e) {
						//set some error indication value in result
						System.err.println("Error writing banner result value");
						e.printStackTrace();
					}
					
					continue;
				}
			}
		}
	}
}
