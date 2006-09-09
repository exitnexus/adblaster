package com.nexopia.adblaster;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class SQLQueue
{
	private final int nThreads;
	private final SQLWorker[] threads;
	private final LinkedList<String> queue;
	
	public SQLQueue(int nThreads)
	{
		this.nThreads = nThreads;
		queue = new LinkedList<String>();
		threads = new SQLWorker[nThreads];
		
		for (int i=0; i<this.nThreads; i++) {
			threads[i] = new SQLWorker();
			threads[i].start();
		}
	}
	
	public void execute(String query) {
		synchronized(queue) {
			queue.addLast(query);
			queue.notify();
		}
	}
	
	private class SQLWorker extends Thread {
		public void run() {
			String query;
			
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
					Statement st = JDBCConfig.createStatement();
					st.execute(query);
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