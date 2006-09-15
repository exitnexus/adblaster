package com.nexopia.adblaster;

import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

class QueryWrapper {
	String query;
	byte[] a;
	byte[] b;
	
	public QueryWrapper(String s) {
		query = s;
	}
	
	public QueryWrapper(String s, byte[] a, byte[] b) {
		query = s;
		this.a = a;
		this.b = b;
	}
}

public class SQLQueue
{
	private final int nThreads;
	private final SQLWorker[] threads;
	private final LinkedList<QueryWrapper> queue;
	
	public SQLQueue(int nThreads)
	{
		this.nThreads = nThreads;
		queue = new LinkedList<QueryWrapper>();
		threads = new SQLWorker[nThreads];
		
		for (int i=0; i<this.nThreads; i++) {
			threads[i] = new SQLWorker();
			threads[i].start();
		}
	}
	
	public void execute(String query) {
		synchronized(queue) {
			queue.addLast(new QueryWrapper(query));
			queue.notify();
		}
	}
	
	public void execute(String prepare_query, byte[] a, byte[] b) {
		synchronized(queue) {
			queue.addLast(new QueryWrapper(prepare_query, a, b));
			queue.notify();
		}
	}
	
	private class SQLWorker extends Thread {
		public void run() {
			QueryWrapper query;
			
			while (true) {
				synchronized(queue) {
					while (queue.isEmpty()) {
						try
						{
							queue.wait();
						}
						catch (InterruptedException ignored)
						{
						}
					}
					
					query =  queue.removeFirst();
				}
				
				try {
					PreparedStatement st = JDBCConfig.prepareStatement(query.query);
					if (query.a != null && query.b != null) {
						st.setBytes(1,query.a);
						st.setBytes(2,query.b);
					}
					st.execute();
				}
				catch (RuntimeException e) {
					// If we don't catch RuntimeException, 
					// the pool could leak threads
					System.err.println("Runtime exception while executing query '" + query + "'.");
				} catch (SQLException e) {
					System.err.println("Error executing query '" + query + "'.");
					e.printStackTrace();
				}
			}
		}
	}
}