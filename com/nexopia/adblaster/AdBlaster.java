package com.nexopia.adblaster;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.*;

import com.sleepycat.je.DatabaseException;

public class AdBlaster {

	static int num_serves = 2000;
	static int num_users = 200;
	static int num_banners = 35;
	
	public static void main(String args[]){
		
		JFrame frame = new JFrame("AdBlaster Test");
		JPanel panel = new JPanel(new BorderLayout());
		
		frame.setContentPane(panel);

		JTabbedPane tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		
		AdCampaign ac = AdCampaign.generateTestData(num_banners, num_users);
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);
		
		JPanel resultPanel = new JPanel(new BorderLayout());
		
		for (int day = 0; day < 5; day++){
			System.out.println("Day "+ day);
			AdBlasterInstance instance = AdBlasterInstance.randomInstance(num_serves, ac);
			for (int i = 0; i < 2; i++){
				instance.fillInstance(pol);

				resultPanel = new JPanel(new BorderLayout());
				JScrollPane scroll = new JScrollPane(resultPanel);
				tab.addTab("" + day + " : " + i, scroll);
				DefaultTableModel model = new DefaultTableModel(num_serves,3);
				JTable table = new JTable(model);
				for (int j = 0; j < num_serves; j++){
					model.setValueAt(((BannerView)instance.views.get(j)).u, j,0);
					model.setValueAt(((BannerView)instance.views.get(j)).b, j,1);
					model.setValueAt(outputTime(((BannerView)instance.views.get(j)).time), j,2);
				}
				resultPanel.add(table, BorderLayout.CENTER);
				resultPanel.add(new JTextField(""+instance.totalProfit()+"/"+maxProfit(instance)), BorderLayout.PAGE_END);
				pol.upgradePolicy(instance);

				
			}
			for(int i = 0; i < instance.views.size(); i++){
				try {
					instance.db.insert((BannerView)instance.views.get(i));
				} catch (DatabaseException dbe) {
					System.err.println("DatabaseException: " + dbe);
				}
				
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
	
	private static void iterativeImprove(AdBlasterInstance instance) {
		Vector unserved = instance.getUnserved();
		for (int i = 0; i < unserved.size(); i++){
			Tuple t = (Tuple)unserved.get(i);
			Banner b = (Banner)t.data.get(0); 
			int c = ((Integer)t.data.get(1)).intValue(); 
			for (int j = 0; j < instance.views.size() && c > 0; j++){
				BannerView bv = (BannerView) instance.views.get(j);
				if (bv.b.profit < b.profit && instance.isValidBannerForUser(bv.u,b)){
					c--;
					bv.b = b;
				}
			}
		}
	}
	private static float maxProfit(AdBlasterInstance instance){
		float count = -1;
		AdBlasterInstance i2 = instance.copy();
		while(i2.totalProfit() != count){
			count = i2.totalProfit();
			iterativeImprove(i2);
		}
		return i2.totalProfit();
	}

	private static JTable getBannerTable(final AdCampaign ac, AdBlasterPolicy pol) {
		// TODO Auto-generated method stub
		DefaultTableModel model = new DefaultTableModel(ac.b.length,4);
		final JTable table = new JTable(model);
		for (int i = 0; i < ac.b.length; i++){
			model.setValueAt(""+ac.b[i].getID(), i,0);
			model.setValueAt(""+ac.b[i].profit, i,1);
			model.setValueAt(""+ac.b[i].max_hits, i,2);
			model.setValueAt((Float)pol.coefficients.get(ac.b[i]), i,3);
		}
		// TODO Auto-generated method stub
		

		table.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent e) {
				System.out.println(ac.b[table.getSelectedRow()].interests.checked);
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
