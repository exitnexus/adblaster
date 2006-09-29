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

import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.ProgressIndicator;
import com.nexopia.adblaster.util.Utilities;

public class AdBlaster {

	private static final int THREAD_COUNT = 2;
	static int num_serves = 4;
	static AdBlasterDbUniverse ac;
	static AdBlasterDbInstance instanc;
	
	private static File user_dir = null;
	private static File page_dir = null;
	private static File bv_dir = null;

	
	public static void main(String args[]){
		File dataFile = null;
		if (args.length >= 3){
			System.out.println("Running with selected directories.");
			bv_dir = new File(args[0]);
			user_dir = new File(args[1]);
			page_dir = new File(args[2]);
		} else {
			bv_dir = Utilities.getDir("BannerView");
			user_dir = Utilities.getDir("User");
			page_dir = Utilities.getDir("Page");
		}
		
		if (args.length == 4){
			dataFile = new File(args[3]);
		}

		ac = new AdBlasterDbUniverse(user_dir, page_dir);
		instanc = new AdBlasterDbInstance(ac);
		//instanceBinding = new BannerViewBinding(ac, instanc);

		long start_time = System.currentTimeMillis();
		
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);

		if (dataFile != null){
			((AdBlasterDbInstance)instanc).loadNoCount(bv_dir, dataFile);
		} else {
			((AdBlasterDbInstance)instanc).load(bv_dir);
		}
		System.out.println("Total original profit: " + instanc.totalProfit());
		
		System.out.println("Chunking.");
		GlobalData gd = new GlobalData(instanc, pol);
		AdBlasterThreadedInstance[] chunk = getChunk(gd, THREAD_COUNT, 1000);
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
		System.out.println("Saving Coefficients...");
		ac.saveCoefficients(pol.getCoefficients());

		System.out.println("Making final results...");
		panel.add(tab, BorderLayout.CENTER);
		JPanel resultPanel = new JPanel(new BorderLayout());
		
		{
			//Actual final results.
			//instanc.fillInstance(gd.pol);
			resultPanel = new JPanel(new BorderLayout());
			JScrollPane scroll = new JScrollPane(resultPanel);
			tab.addTab("Final", scroll);
			DefaultTableModel model = new DefaultTableModel(Math.min(instanc.getViewCount(),1000),3);
			JTable table = new JTable(model);
			System.out.println("Filling table...");
			for (int j = 0; j < Math.min(instanc.getViewCount(),1000); j++){
				model.setValueAt(instanc.getView(j).getUser(), j,0);
				model.setValueAt(instanc.getView(j).getBanner(), j,1);
				model.setValueAt(AdBlaster.outputTime(instanc.getView(j).getTime()), j,2);
			}
			resultPanel.add(table, BorderLayout.CENTER);
			JPanel statsPanel = new JPanel(new FlowLayout());
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
			model.setValueAt(""+ac.getBannerByIndex(i).getID(), i,0);
			model.setValueAt(""+ac.getBannerByIndex(i).getRealPayrate(), i,1);
			model.setValueAt(""+ac.getBannerByIndex(i).getViewsPerDay(), i,2);
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
