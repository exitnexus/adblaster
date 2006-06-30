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

	static int num_serves = 10000;
	static int num_users = 1000;
	static int num_banners = 10;
	static AbstractAdBlasterUniverse ac;
	static BannerViewBinding instanceBinding;
	
	public static void main(String args[]){
				
		JFrame frame = new JFrame("AdBlaster Test");
		JPanel panel = new JPanel(new BorderLayout());
		
		frame.setContentPane(panel);

		JTabbedPane tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		JPanel resultPanel = new JPanel(new BorderLayout());
		
		//ac = AdBlasterUniverse.generateTestData(num_banners, num_users);
		//((AdBlasterUniverse)ac).makeMeADatabase();
		
		ac = new AdBlasterDbUniverse();
		
		
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);


		for (int day = 0; day < 1; day++){
			System.out.println("Day "+ day);
			//AdBlasterInstance instance = AdBlasterInstance.randomInstance(num_serves, ac);
			AbstractAdBlasterInstance instance = new AdBlasterDbInstance(ac);
			instanceBinding = new BannerViewBinding(ac, instance);
			((AdBlasterDbInstance)instance).load();
			System.out.println("Instances generated.");
			long time = System.currentTimeMillis();
			//for (int i = 0; i < 100000; i++){
			//	instance.getView(i);
			//}
			//System.out.println(System.currentTimeMillis() - time);
			//System.exit(0);
			for (int i = 0; i < 1; i++){
				System.out.println("Total profit:" + instance.totalProfit());
				instance.fillInstance(pol);
				System.out.println("Total profit:" + instance.totalProfit());
				//instance.makeMeADatabase();

				resultPanel = new JPanel(new BorderLayout());
				JScrollPane scroll = new JScrollPane(resultPanel);
				tab.addTab("" + day + " : " + i, scroll);
				DefaultTableModel model = new DefaultTableModel(num_serves,3);
				JTable table = new JTable(model);
				for (int j = 0; j < num_serves; j++){
					model.setValueAt(instance.getView(j).getUser(), j,0);
					model.setValueAt(instance.getView(j).getBanner(), j,1);
					model.setValueAt(outputTime(instance.getView(j).getTime()), j,2);
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
	
	public static void iterativeImprove(AbstractAdBlasterInstance instanc) {
		Vector unserved = instanc.getUnserved();
		
		AdBlasterInstance chunk = new AdBlasterInstance(ac);
		getChunk(chunk, instanc);
		
		for (int i = 0; i < unserved.size(); i++){
			System.out.println("Unserved: " + i +" /" + unserved.size());
			Tuple t = (Tuple)unserved.get(i);
			Banner b = (Banner)t.data.get(0); 
			//while (((Integer)instance.bannerCountMap.get(b)).intValue() < b.getMaxHits()){
				int depth = 0;
				for (int j = 0; j < chunk.getViewCount() && ((Integer)instanc.bannerCountMap.get(b)).intValue() < b.getMaxHits(); j++){
					//System.out.println("Trying bannerview " + j);
					BannerView bv = chunk.getView(j);
					if (bv.getBanner().getPayrate() < b.getPayrate()){
						if (depth == 0 && chunk.isValidBannerForView(bv,b)){
							//single swapbreak;
							bv.setBanner(b);
						} else if (depth > 0 && false){
							Vector swaps = null;
							int swap_max = 0;
							for (int l = 1; l < swap_max; l+=2){
									Vector path = chunk.depthLimitedDFS(bv, b, l);
									if (path != null){
										swaps = path;
										break;
									}
							}
							if (swaps != null){
								chunk.doSwap(swaps, b);
							}
						}
					}
				//}
				//depth++;
			}
		}
	}
	private static void getChunk(AdBlasterInstance chunk, AbstractAdBlasterInstance instance) {
		for (int i = 0; i < instance.getViewCount(); i++){
			BannerView bv = instance.getView(i);
			if (bv.getUser().id % 10 == 0){
				chunk.addView(bv);
			}
		}
	}

								
					
	private static JTable getBannerTable(AbstractAdBlasterUniverse ac2, AdBlasterPolicy pol) {
		// TODO Auto-generated method stub
		DefaultTableModel model = new DefaultTableModel(ac.getBannerCount(),4);
		final JTable table = new JTable(model);
		for (int i = 0; i < ac.getBannerCount(); i++){
			model.setValueAt(""+ac.getBannerByIndex(i).getID(), i,0);
			model.setValueAt(""+ac.getBannerByIndex(i).getPayrate(), i,1);
			model.setValueAt(""+ac.getBannerByIndex(i).getMaxHits(), i,2);
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

	private static String outputTime(int i) {
		int hour = i / (60*60); 
		int min = (i / 60) % 60;
		int sec = i % 60;
		return "" + (hour%12) + ":" + min + ":" + sec + (hour > 12?"pm":"am");
	}

}
