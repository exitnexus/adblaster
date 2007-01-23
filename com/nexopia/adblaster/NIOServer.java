package com.nexopia.adblaster;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.struct.Campaign.CampaignDB;
import com.nexopia.adblaster.util.StringArrayPageValidator;
import com.nexopia.adblaster.util.FlatFilePageValidator;
import com.nexopia.adblaster.util.PageValidatorFactory;

//Listen on a port for connections and write back the current time.
public class NIOServer {
	static int BUFFER_SIZE = 1024;
	static ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

	static Charset charset=Charset.forName("ISO-8859-1");
	static HashMap <SocketChannel, BufferedSocketChannel>socketMap;
	public static final long SELECTOR_TIMEOUT = 500; //ms
	
	static class BufferedSocketChannel{
		String previous_str = "";
		SocketChannel sc;
		BufferedSocketChannel(SocketChannel chan){
			this.sc = chan;
		}
		public void configureBlocking(boolean b) throws IOException{
			this.sc.configureBlocking(b);
		}
		public void register(Selector selector, int i) throws ClosedChannelException {
			this.sc.register(selector, i);
		}
		public void close() throws IOException {
			this.sc.close();
		}
		public int write(ByteBuffer output) throws IOException {
			return this.sc.write(output);
			
		}
		
		/**
		 * 
		 * @param strbuf
		 * @return 0 if success, -1 if fail.
		 * @throws IOException
		 */
		public int read(StringBuffer strbuf) throws IOException {
			buffer.clear();
			int i = this.sc.read(buffer);
			
			if (i == -1){
				strbuf.append(previous_str);
				previous_str = "";
				return -1;
			}

			buffer.flip();
			CharsetDecoder decoder = charset.newDecoder();
			CharBuffer charBuffer = null;
			try {
				charBuffer = decoder.decode(buffer);
			} catch (CharacterCodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			strbuf.append(previous_str);
			strbuf.append(charBuffer.toString());
			previous_str = "";
			return 0;

		}
		public void putBack(String s) {
			previous_str += s;
		}
	}
	
	private static final int HOURLY_SECONDS_OFFSET = 20; //seconds
	private static final int DAILY_SECONDS_OFFSET = 40; //seconds
	private static final int DAILY_HOURS_OFFSET = 6; //hours
	
	public static void main (String args[]) throws IOException {
		socketMap = new HashMap<SocketChannel, BufferedSocketChannel>();
		Object args1[] = {};

		PageValidatorFactory factory = 
			new PageValidatorFactory(StringArrayPageValidator.class,args1);
		
		CampaignDB cdb = new CampaignDB(factory);
		BannerDatabase bdb = new BannerDatabase(cdb, factory);
		BannerServer banners = new BannerServer(bdb, cdb, 1);
		
		//Create the server socket channel
		ServerSocketChannel server = null;
		Selector selector = null;
		

		try {
			server = ServerSocketChannel.open();
			
			//nonblocking I/O
			server.configureBlocking(false);
			
			//host-port 8000
			server.socket().bind(new java.net.InetSocketAddress(8000));
			
			BannerServer.bannerDebug("Server listening on port 8000");
			//Create the selector
			selector = Selector.open();
			
			//Recording server to selector (type OP_ACCEPT)
			server.register(selector,SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//ByteBuffer lastbuffer = null;
		
//		Infinite server loop
		long time = System.currentTimeMillis();
		int lastMinute = Calendar.getInstance().get(Calendar.MINUTE);
		int lastHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int lastDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if (lastHour < DAILY_HOURS_OFFSET) {
			lastDay--;
		}
		
		while (true) {
			if (System.currentTimeMillis()-time > 1000) {
				if (BannerServer.debug.get("tick").booleanValue()) {
					BannerServer.bannerDebug("Tick");
				}
				Calendar now = Calendar.getInstance();
				time = System.currentTimeMillis();
				banners.secondly();
				if (lastMinute != now.get(Calendar.MINUTE)) {
					lastMinute = now.get(Calendar.MINUTE);
					banners.minutely(BannerServer.debug.get("timeupdates").booleanValue());
				}
				if (lastHour != now.get(Calendar.HOUR_OF_DAY) && now.get(Calendar.SECOND) > HOURLY_SECONDS_OFFSET) {
					lastHour = now.get(Calendar.HOUR_OF_DAY);
					banners.hourly(BannerServer.debug.get("timeupdates").booleanValue());
				}
				if (lastDay != now.get(Calendar.DAY_OF_YEAR) &&
						now.get(Calendar.HOUR_OF_DAY) > DAILY_HOURS_OFFSET &&
						now.get(Calendar.SECOND) > DAILY_SECONDS_OFFSET) {
					lastDay = now.get(Calendar.DAY_OF_YEAR);
					banners.daily(BannerServer.debug.get("timeupdates").booleanValue());
					if (BannerServer.debug.get("dailyrestart").booleanValue()) {
						System.exit(0);
					}
				}
			}
			
			selector.select(SELECTOR_TIMEOUT);
				
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
					BufferedSocketChannel client;
					try {
						//System.out.println("Accepting connection.");
						SocketChannel original = server.accept();
						client = new BufferedSocketChannel(original);
						if (BannerServer.debug.get("tick").booleanValue()) {
							BannerServer.bannerDebug("[connection]: " + original.socket().getInetAddress());
						}
						socketMap.put(original, client);
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
					//System.out.println("Close.");
					BannerServer.bannerDebug("Client closed");
					BufferedSocketChannel client = socketMap.get( key.channel() );
					client.close();
					continue;
				}
				
				// if isReadable = true
				// then the server is ready to read 
				if (key.isReadable() && key.isWritable()) {
					//System.out.println("In the block.");
					BufferedSocketChannel client = socketMap.get( key.channel() );
					client.sc = (SocketChannel)key.channel();
					
					StringBuffer strbuf = new StringBuffer("");
					int len = 0;
					try {
						while ((len = getString(client, strbuf)) == 1) {
							String result = null;
							try {
								if (BannerServer.debug.get("development").booleanValue()) {
									BannerServer.bannerDebug(strbuf.toString());
								}
								if (strbuf.toString().equals("reset")) {
									System.out.println("Resetting...");
									cdb = new CampaignDB(factory);
									bdb = new BannerDatabase(cdb, new PageValidatorFactory(StringArrayPageValidator.class, args1));
									banners = new BannerServer(bdb, cdb, 1);
									if (BannerServer.debug.get("development").booleanValue()) {
										BannerServer.bannerDebug("Reinitialized the banner server.");
									}
								} else {
									//The banner server deals with any commands except a server reset.
									result = banners.receive(strbuf.toString());
								}
							} catch (Exception e) {
								BannerServer.bannerDebug("Unexpected exception when attempting to handle input '" + strbuf.toString() + "'");
								e.printStackTrace();
							}
							try {
								ByteBuffer output = charset.encode(result+'\n');
								//System.out.println(output.toString());
								client.write(output);
							} catch (Exception e) {
								//This happens often, it's not a problem condition.  It just means that the client
								//didn't care about the result, for example they triggered a command for which there
								//is no result.
							}
							strbuf.setLength(0);
						}
						if (len == -1){
							//nothing readable
							if (strbuf.length() > 0)
								BannerServer.bannerDebug("Error! " + strbuf.toString());
							client.close();
							continue;
						}
					} catch (IOException e){
						System.out.println("The following error was detected but the server will continue:");
						System.out.println(e);
						e.printStackTrace();
						client.close();
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param client
	 * @param s (output)
	 * @return -1 if stream closed, 0 if not ready, 1 if ready
	 * @throws IOException
	 */
	public static int getString(BufferedSocketChannel client, StringBuffer s) throws IOException{
		// Read byte coming from the client
		int i = client.read(s);
		//System.out.println(s);
		if (i == -1 && s.length() == 0){
			return -1;
		}
		
		int index = s.indexOf("\n");
		if (index == -1){
			client.putBack(s.toString());
			return 0;
		} else {
			client.putBack(s.substring(index+1));
			s.delete(index, s.length());
			if (s.toString().trim().length() > 0)
				return 1;
			else
				return 0;
		}
	}
}
