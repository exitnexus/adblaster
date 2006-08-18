package com.nexopia.adblaster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class AdBlaster {

	private static final int THREAD_COUNT = 1;
	static int num_serves = 4;
	static AdBlasterDbUniverse ac;
	static AdBlasterDbInstance instanc;
	static BannerViewBinding instanceBinding;
	
	static File user_dir = null;
	static File page_dir = null;
	static File bv_dir = null;

	public static File getDir(String name){
		if (bv_dir == null){
			JFileChooser u_jfc = new JFileChooser();
		
			u_jfc.setDialogTitle("Choose the " + name + " directory to load");
			u_jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
		    int returnVal = u_jfc.showOpenDialog(null);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		       System.out.println("You chose to open this file: " +
		            u_jfc.getSelectedFile().getName());
		    } else {
		    	System.exit(0);
		    }
		    bv_dir = u_jfc.getSelectedFile();
		} 
		
		return bv_dir; 
	}
	
	public static void main(String args[]){
		if (args.length == 3){
			System.out.println("Running with selected directories.");
			bv_dir = new File(args[0]);
			user_dir = new File(args[1]);
			page_dir = new File(args[2]);
		} else {
			bv_dir = getDir("BannerView");
			user_dir = getDir("User");
			page_dir = getDir("Page");
		}
		
		ac = new AdBlasterDbUniverse(user_dir);
		instanc = new AdBlasterDbInstance(ac);
		instanceBinding = new BannerViewBinding(ac, instanc);

		long start_time = System.currentTimeMillis();
		
		//ac = AdBlasterUniverse.generateTestData(num_banners, num_users);
		//((AdBlasterUniverse)ac).makeMeADatabase();
		
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);

		((AdBlasterDbInstance)instanc).load(bv_dir);
		System.out.println("Total original profit: " + instanc.totalProfit());
		
		System.out.println("Chunking.");
		GlobalData gd = new GlobalData(instanc, pol);
		AdBlasterThreadedInstance[] chunk = getChunk(gd, THREAD_COUNT, 10);
		Runnable[] r = new Runnable[THREAD_COUNT];
		Thread[] t = new Thread[THREAD_COUNT];
		
		JFrame frame = null;
		JPanel panel = null;
		JTabbedPane tab = null;

		frame = new JFrame("AdBlaster Test");
		panel = new JPanel(new BorderLayout());
		frame.setContentPane(panel);
		tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		
		panel.setPreferredSize(new Dimension(800,600));
		frame.setSize(800,600);
		frame.addWindowListener(new WindowListener(){
			public void windowOpened(WindowEvent e) {			}
	
			public void windowClosing(WindowEvent e) {			
				System.exit(0);
			}
	
			public void windowClosed(WindowEvent e) {		
				System.exit(0);
			}
	
			public void windowIconified(WindowEvent e) {			}
			public void windowDeiconified(WindowEvent e) {			}
			public void windowActivated(WindowEvent e) {			}
			public void windowDeactivated(WindowEvent e) {			}}
		);
		frame.pack();
		frame.setVisible(true);
		
		JTabbedPane tabs[] = new JTabbedPane[THREAD_COUNT];
		for (int j=0; j<THREAD_COUNT; j++) {
			tabs[j] = new JTabbedPane();
			tab.addTab("Thread " + j, tabs[j]);
		}
		
		for (int i = 0; i < num_serves; i++){
			for (int j=0; j<THREAD_COUNT; j++) {
				AdBlasterThreadedOperation.createTab(chunk[j], tabs[j], "Starting", chunk[j].totalProfit());
			}
			
			for (int j=0; j<THREAD_COUNT; j++) {
				for (int k = 0; k < chunk[j].getViewCount(); k++){
					BannerView bv = chunk[j].getView(k);
					bv.setBanner(null);
				}
				if (i == 0 || i == num_serves-1){
					r[j] = new AdBlasterThreadedOperation(gd, chunk[j], "ThreadSet " + j, tabs[j]);
				} else {
					r[j] = new AdBlasterThreadedOperation(gd, chunk[j], "ThreadSet " + j, null);
				}
				t[j] = new Thread(r[j], "operateOnChunk");
				
				t[j].start();
			}
			for (int j=0; j<THREAD_COUNT; j++) {
				synchronized (r[j]){
					AdBlasterThreadedOperation op = (AdBlasterThreadedOperation)r[j];
					if (!op.isFinished()) {
						try {
							r[j].wait();
							frame.pack();
							frame.setVisible(true);
						} catch (Exception e1) {
							e1.printStackTrace();
							System.exit(0);
						}
					}
				}
			}
		}
		ac.saveCoefficients(pol.getCoefficients());
			panel.add(tab, BorderLayout.CENTER);
			JPanel resultPanel = new JPanel(new BorderLayout());
			
			{
				//Actual final results.
				//instanc.fillInstance(gd.pol);
				resultPanel = new JPanel(new BorderLayout());
				JScrollPane scroll = new JScrollPane(resultPanel);
				tab.addTab("Final", scroll);
				DefaultTableModel model = new DefaultTableModel(instanc.getViewCount(),3);
				JTable table = new JTable(model);
				for (int j = 0; j < Math.min(instanc.getViewCount(),1000); j++){
					model.setValueAt(instanc.getView(j).getUser(), j,0);
					model.setValueAt(instanc.getView(j).getBanner(), j,1);
					model.setValueAt(AdBlaster.outputTime(instanc.getView(j).getTime()), j,2);
				}
				resultPanel.add(table, BorderLayout.CENTER);
				JPanel statsPanel = new JPanel(new FlowLayout());
				//statsPanel.add(new JTextField(""+instanc.totalProfit()));
				/*
				for (int j = 0; j < instanc.getViewCount(); j++){
					BannerView bv = instanc.getView(j);
					bv.setBanner(null);
				}
				float pro = 0;
				for (int j = 0; j < 10000; j++){
					System.out.println((float)j/100.0f + "% calculating final profit.");
					AdBlasterThreadedInstance f = new AdBlasterThreadedInstance(gd);
					for (int i = 0; i < ac.getUserCount()-1; i++){
						ProgressIndicator.show(i, ac.getUserCount());
						if (ac.getUserByIndex(i).getID() % 10000 == j){
							Vector <BannerView>vec = instanc.db.bv_db.getByUser(ac.getUserByIndex(i).getID());
							for (BannerView bv : vec){
								f.addView(bv);
							}
						}
					}
					f.fillInstance(pol);
					pro += f.totalProfit();
				}
				statsPanel.add(new JTextField(""+pro));
				*/
				resultPanel.add(statsPanel, BorderLayout.PAGE_START);
		
			}
			JPanel statPanel = new JPanel();
			statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.PAGE_AXIS));
			statPanel.add(new JScrollPane(AdBlaster.getBannerTable(AdBlaster.ac, gd.pol)));
			panel.add(statPanel, BorderLayout.SOUTH);
		
			frame.pack();
			frame.setVisible(true);

		System.out.println("Total time:" + (System.currentTimeMillis()- start_time));
		// TODO Auto-generated method stub

	}

	/*
	 * Get "num" chunks of average size "size".
	 */
	private static AdBlasterThreadedInstance[] getChunk(GlobalData gd, int num, int size) {
		AdBlasterThreadedInstance r[] = new AdBlasterThreadedInstance[num];
		int modCount = ac.getUserCount() / size;
		for (int i = 0; i < num; i++){
			r[i] = new AdBlasterThreadedInstance(gd);
		}
		for (int i = 0; i < ac.getUserCount()-1; i++){
			ProgressIndicator.show(i, ac.getUserCount());
			int hash = Math.abs(ac.getUserByIndex(i).getID() % modCount);
			if (hash < num){
				Vector <BannerView>vec = gd.instance.db.getByUser(ac.getUserByIndex(i).getID());
				for (BannerView bv : vec){
					r[hash].addView(bv);
				}
			}
		}
		return r;
	}								
					
	static JTable getBannerTable(AbstractAdBlasterUniverse ac2, AdBlasterPolicy pol) {
		// TODO Auto-generated method stub
		DefaultTableModel model = new DefaultTableModel(ac.getBannerCount(),4);
		final JTable table = new JTable(model);
		for (int i = 0; i < ac.getBannerCount(); i++){
			model.setValueAt(""+ac.getBannerByIndex(i).index, i,0);
			model.setValueAt(""+ac.getBannerByIndex(i).getRealPayrate(), i,1);
			model.setValueAt(""+ac.getBannerByIndex(i).getViewsperday(), i,2);
			model.setValueAt(pol.getCoefficient(ac.getBannerByIndex(i)), i,3);
		}
		// TODO Auto-generated method stub
		

		table.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				System.out.println(ac.getBannerByIndex(table.getSelectedRow()));
			}

			public void mousePressed(MouseEvent e) {			}

			public void mouseReleased(MouseEvent e) {			}

			public void mouseEntered(MouseEvent e) {			}

			public void mouseExited(MouseEvent e) {			}
			});
		return table;
	}

	static String outputTime(int i) {
		int hour = i / (60*60); 
		int min = (i / 60) % 60;
		int sec = i % 60;
		return "" + (hour%12) + ":" + min + ":" + sec + (hour > 12?"pm":"am");
	}

}
