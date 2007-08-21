package com.nexopia.adblaster;
import java.io.File;
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
import com.nexopia.adblaster.db.CampaignDB;
import com.nexopia.adblaster.db.JDBCConfig;
import com.nexopia.adblaster.struct.ConfigFile;
import com.nexopia.adblaster.util.StringArrayPageValidator;
import com.nexopia.adblaster.util.FlatFilePageValidator;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.Integer;
import com.vladium.utils.ObjectProfiler;

//Listen on a port for connections and write back the current time.
public class NIOServer {
	private static final int BUFFER_SIZE = 1024;
	ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

	Charset charset=Charset.forName("ISO-8859-1");
	HashMap <SocketChannel, BufferedSocketChannel> socketMap;
	private static ConfigFile config;
	public static final long SELECTOR_TIMEOUT = 5; //ms
	
	class BufferedSocketChannel{
		String previous_str = "";
		SocketChannel sc;
		BufferedSocketChannel(SocketChannel chan){
			this.sc = chan;
		}
		public void configureBlocking(boolean b) throws IOException{
			this.sc.configureBlocking(b);
		}
		public void register(Selector selector, int ops) throws ClosedChannelException {
			this.sc.register(selector, ops);
		}
		public void close() throws IOException {
			socketMap.remove(sc);
			this.sc.close();
		}
		public int write(ByteBuffer output) throws IOException {
			int length = output.array().length;
			while (output.hasRemaining()) {
				this.sc.write(output);
			}
			return length;
		}
		
		/**
		 * 
		 * @param strbuf
		 * @return 0 if success, -1 if fail.
		 * @throws IOException
		 */
		public int read(StringBuffer strbuf) throws IOException {
			buffer.clear();
			int i = 0;
			try {
				i = this.sc.read(buffer);
			} catch (IOException ioe) {
				return -1;
			}
			
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
	private static final int DAILY_MINUTES_OFFSET = 0; //hours
	private static final int DAILY_HOURS_OFFSET = 6; //hours
	private static int NUM_SERVERS=1;
	
	private BannerServer banners;
	private Selector accepter;
	private Selector readerWriter;
	private ServerSocketChannel server;
	
	public NIOServer(String args[]) {
		if (args.length > 0){
			config = new ConfigFile(new File(args[0]));
		} else {
			config = new ConfigFile(new File("banner.config"));
		}
		
		NUM_SERVERS = config.getInt("numservers", NUM_SERVERS);
		
		JDBCConfig.initDBConnection(config);
		
		socketMap = new HashMap<SocketChannel, BufferedSocketChannel>();
		
		this.banners = new BannerServer(NUM_SERVERS, config);
		
		//Create the server socket channel
		server = null;
		accepter = null;
		readerWriter = null;
		
		int banner_server_port = config.getInt("port");
		
		try {
			server = ServerSocketChannel.open();
			
			//nonblocking I/O
			server.configureBlocking(false);
			
			//host-port 8000
			server.socket().bind(new java.net.InetSocketAddress(banner_server_port));
			
			BannerServer.bannerDebug("Server listening on port " + banner_server_port);
			//Create the selector
			accepter = Selector.open();
			readerWriter = Selector.open();
			
			//Recording server to selector (type OP_ACCEPT)
			server.register(accepter,SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void run() throws IOException {
//		Infinite server loop
		long time = System.currentTimeMillis();
		int lastMinute = Calendar.getInstance().get(Calendar.MINUTE);
		int lastHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		int lastDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		if (lastHour <= DAILY_HOURS_OFFSET) {
			lastDay--;
		} 
		while (true) {
			if (System.currentTimeMillis()-time > 1000) {
				if (banners.debug.get("tick").booleanValue()) {
					BannerServer.bannerDebug("Tick");
				}
				Calendar now = Calendar.getInstance();
				time = System.currentTimeMillis();
				banners.secondly();
				if (lastMinute != now.get(Calendar.MINUTE)) {
					lastMinute = now.get(Calendar.MINUTE);
					banners.minutely(banners.debug.get("timeupdates").booleanValue());
				}
				if (lastHour != now.get(Calendar.HOUR_OF_DAY) && now.get(Calendar.SECOND) > HOURLY_SECONDS_OFFSET) {
					lastHour = now.get(Calendar.HOUR_OF_DAY);
					banners.hourly(banners.debug.get("timeupdates").booleanValue());
				}
				if (lastDay != now.get(Calendar.DAY_OF_YEAR) &&
						now.get(Calendar.MINUTE) == DAILY_MINUTES_OFFSET &&
						now.get(Calendar.HOUR_OF_DAY) == DAILY_HOURS_OFFSET &&
						now.get(Calendar.SECOND) == DAILY_SECONDS_OFFSET) {
					lastDay = now.get(Calendar.DAY_OF_YEAR);
					banners.daily(banners.debug.get("timeupdates").booleanValue());
					if (banners.debug.get("dailyrestart").booleanValue()) {
						System.exit(0);
					}
				}
			}
			
			accepter.select(SELECTOR_TIMEOUT);
				
			// Get keys
			Set keys = accepter.selectedKeys();
			Iterator i = keys.iterator();
			
			// For each keys...
			while(i.hasNext()) {
				SelectionKey key = (SelectionKey) i.next();
				
				// Remove the current key
				i.remove();
				
				// if isAcceptable = true
				// then a client required a connection
				if (key.isAcceptable()) {
					// get client socket channel
					BufferedSocketChannel client;
					try {
						//System.out.println("Accepting connection.");
						SocketChannel original = server.accept();
						client = new BufferedSocketChannel(original);
						if (banners.debug.get("connect").booleanValue()) {
							BannerServer.bannerDebug("[connection]: " + original.socket().getInetAddress());
						}
						banners.connection();
						socketMap.put(original, client);
						// Non Blocking I/O
						client.configureBlocking(false);
						//recording to the selector (reading)
						client.register(readerWriter, SelectionKey.OP_READ|SelectionKey.OP_WRITE|SelectionKey.OP_CONNECT);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
			}



			readerWriter.select(SELECTOR_TIMEOUT);

			// Get keys
			keys = readerWriter.selectedKeys();
			i = keys.iterator();

			// For each keys...
			while(i.hasNext()) {
				SelectionKey key = (SelectionKey) i.next();
				
				// Remove the current key
				i.remove();
				if (key.isConnectable()) {
					handleConnectable(key);
				} else if (key.isReadable()) {
					handleReadableWritable(key);
				}
			}
		}
	}
	
	public static void main (String args[]) throws IOException {
		NIOServer nio = new NIOServer(args);
		nio.run();
	}

	private void handleConnectable(SelectionKey key) throws IOException {
		//System.out.println("Close.");
		BannerServer.bannerDebug("Client closed");
		banners.connectionClosed();
		BufferedSocketChannel client = socketMap.get( key.channel() );
		client.close();
	}
	
	private void handleReadableWritable(SelectionKey key) throws IOException {
		BufferedSocketChannel client = socketMap.get( key.channel() );
		client.sc = (SocketChannel)key.channel();
		
		StringBuffer strbuf = new StringBuffer("");
		int len = 0;
		try {
			while ((len = getString(client, strbuf)) == 1) {
				String result = null;
				try {
					if (banners.debug.get("development").booleanValue()) {
						BannerServer.bannerDebug("Received: " + strbuf.toString());
					}
					if (strbuf.toString().toUpperCase().startsWith("QUIT")) {
						client.sc.close();
						client.close();
						banners.connectionClosed();
						return;
					} else if (strbuf.toString().toUpperCase().startsWith(BannerServer.MEMORY_STATS)){
						result = "NIOServer size: " + ObjectProfiler.sizeof(this) + " bytes\n";
						result += "Integer Pool size: " + ObjectProfiler.sizeof(Integer.poolSize()) + " bytes\n";
						result += "BannerServer size: " + ObjectProfiler.sizeof(banners) + " bytes\n";
						result += "socketMap size: " + (ObjectProfiler.sizeof(socketMap)-
														ObjectProfiler.sizeof(banners)-
														ObjectProfiler.sizeof(buffer)-
														ObjectProfiler.sizeof(accepter)-
														ObjectProfiler.sizeof(readerWriter)-
														ObjectProfiler.sizeof(server)) 
														+ " bytes\n";
						result += banners.receive(strbuf.toString());
					} else if (strbuf.toString().toUpperCase().startsWith("RESET")) {
						this.banners = new BannerServer(NUM_SERVERS, config);
						result = "Reinitialized the banner server.";
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
				banners.connectionClosed();
			}
		} catch (IOException e){
			System.out.println("The following error was detected but the server will continue:");
			System.out.println(e);
			e.printStackTrace();
			client.close();
			banners.connectionClosed();
		}
	}
	
	/**
	 * 
	 * @param client
	 * @param s (output)
	 * @return -1 if stream closed, 0 if not ready, 1 if ready
	 * @throws IOException
	 */
	public int getString(BufferedSocketChannel client, StringBuffer s) throws IOException{
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
