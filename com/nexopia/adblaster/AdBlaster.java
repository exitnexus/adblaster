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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

public class AdBlaster {

	static int num_serves = 100000;
	static int num_users = 1000;
	static int num_banners = 10;
	static AbstractAdBlasterUniverse ac;
	
	public static void main(String args[]){
				
		JFrame frame = new JFrame("AdBlaster Test");
		JPanel panel = new JPanel(new BorderLayout());
		
		frame.setContentPane(panel);

		JTabbedPane tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		JPanel resultPanel = new JPanel(new BorderLayout());
		
		//ac = AdBlasterUniverse.generateTestData(num_banners, num_users);
		//((AdBlasterUniverse)ac).makeMeADatabase(dbEnv);
		
		ac = new AdBlasterDbUniverse();
		
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);
		
		/*
		//AdBlasterInstance instance1 = AdBlasterInstance.randomInstance(num_serves, ac);
		//instance1.fillInstance(pol);
		//instance1.makeMeADatabase(dbEnv);

		AbstractAdBlasterInstance instance2 = new AdBlasterDbInstance(ac, dbEnv);
		instance2.fillInstance(pol);

		try {
			dbEnv.close();
		} catch (DatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(0);
		*/
		for (int day = 0; day < 1; day++){
			System.out.println("Day "+ day);
			//AdBlasterInstance instance = AdBlasterInstance.randomInstance(num_serves, ac);
			AbstractAdBlasterInstance instance = new AdBlasterDbInstance(ac);
			System.out.println("Instances generated.");
			for (int i = 0; i < 2; i++){
				instance.fillInstance(pol);
				//instance.makeMeADatabase();

				resultPanel = new JPanel(new BorderLayout());
				JScrollPane scroll = new JScrollPane(resultPanel);
				tab.addTab("" + day + " : " + i, scroll);
				DefaultTableModel model = new DefaultTableModel(num_serves,3);
				JTable table = new JTable(model);
				for (int j = 0; j < num_serves; j++){
					model.setValueAt(instance.getUserForView(j), j,0);
					model.setValueAt(instance.getBannerForView(j), j,1);
					model.setValueAt(outputTime(instance.getTimeForView(j)), j,2);
				}
				resultPanel.add(table, BorderLayout.CENTER);
				JPanel statPanel = new JPanel(new FlowLayout());
				statPanel.add(new JTextField(""+instance.totalProfit()));
				System.out.println("Upgrading policy.");
				pol.upgradePolicy(instance);
				statPanel.add(new JTextField(""+instance.totalProfit()));
				resultPanel.add(statPanel, BorderLayout.PAGE_START);

				
			}

		}
		JPanel statPanel = new JPanel();
		statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.PAGE_AXIS));
		statPanel.add(new JScrollPane(getBannerTable(ac, pol)));
		panel.add(statPanel, BorderLayout.SOUTH);

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
		// TODO Auto-generated method stub
		

	}
	
	public static void iterativeImprove(AbstractAdBlasterInstance instance) {
		Vector unserved = instance.getUnserved();
		for (int i = 0; i < unserved.size(); i++){
			System.out.println("Unserved: " + i +" /" + unserved.size());
			Tuple t = (Tuple)unserved.get(i);
			Banner b = (Banner)t.data.get(0); 
			int c = ((Integer)t.data.get(1)).intValue(); 
			for (int j = 0; j < instance.getViewCount() && c > 0; j++){
				//System.out.println("Trying bannerview " + j);
				if (instance.getBannerForView(j).getPayrate() < b.getPayrate()){
					if (instance.isValidBannerForUser(instance.getUserForView(j),b)){
						//single swapbreak;
						c--;
						instance.setBannerView(j, b);
					} else {
						Vector swaps = null;
						int swap_max = 2;
						for (int l = 1; l < swap_max; l+=2){
								Vector path = instance.depthLimitedDFS(j, b, l);
								if (path != null){
									swaps = path;
									break;
								}
						}
						if (swaps != null){
							instance.doSwap(swaps, b);
							c--;
						}
					}
				}
			}
		}
	}
						
					
				
			
	private static float maxProfit(AbstractAdBlasterInstance instance){
		float count = -1;
		AbstractAdBlasterInstance i2 = instance.copy();
		while(i2.totalProfit() != count){
			count = i2.totalProfit();
			iterativeImprove(i2);
		}
		return i2.totalProfit();
	}

	private static JTable getBannerTable(AbstractAdBlasterUniverse ac2, AdBlasterPolicy pol) {
		// TODO Auto-generated method stub
		DefaultTableModel model = new DefaultTableModel(ac.getBannerCount(),4);
		final JTable table = new JTable(model);
		for (int i = 0; i < ac.getBannerCount(); i++){
			model.setValueAt(""+ac.getBanner(i).getID(), i,0);
			model.setValueAt(""+ac.getBanner(i).getPayrate(), i,1);
			model.setValueAt(""+ac.getBanner(i).getMaxHits(), i,2);
			model.setValueAt((Float)pol.coefficients.get(ac.getBanner(i)), i,3);
		}
		// TODO Auto-generated method stub
		

		table.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				System.out.println(ac.getBanner(table.getSelectedRow()).interests.getChecked());
			}

			public void mousePressed(MouseEvent e) {			}

			public void mouseReleased(MouseEvent e) {			}

			public void mouseEntered(MouseEvent e) {			}

			public void mouseExited(MouseEvent e) {			}
			});
		return table;
	}

	private static String outputTime(int i) {
		int hour = i / (60*60); 
		int min = (i / 60) % 60;
		int sec = i % 60;
		return "" + (hour%12) + ":" + min + ":" + sec + (hour > 12?"pm":"am");
	}

}
