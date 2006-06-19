package com.nexopia.adblaster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class AdBlaster {

	public static void main(String args[]){
		
		JFrame frame = new JFrame("ABlaster Test");
		JPanel panel = new JPanel(new BorderLayout());
		
		frame.setContentPane(panel);
		JTabbedPane tab = new JTabbedPane();
		JScrollPane scroll = new JScrollPane(tab);
		panel.add(scroll, BorderLayout.CENTER);
		
		AdCampaign ac = AdCampaign.generateTestData(100,100);
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);
		AdBlasterInstance instance = AdBlasterInstance.randomInstance(100, ac);
		
		JPanel resultPanel = new JPanel(new BorderLayout());
		tab.addTab("Original", resultPanel);
		TableModel model = new DefaultTableModel(100,3);
		JTable table = new JTable(model);
		
		for (int i = 0; i < 100; i++){
			model.setValueAt(((BannerView)instance.views.get(i)).u, i,0);
			model.setValueAt(((BannerView)instance.views.get(i)).b, i,1);
			model.setValueAt(""+((BannerView)instance.views.get(i)).time, i,2);
		}
		resultPanel.add(table, BorderLayout.CENTER);
		
		for (int i = 0; i < 10; i++){
			instance.fillInstance(pol);
			upgradePolicy(instance, pol);
			resultPanel = new JPanel(new BorderLayout());
			tab.addTab("Iteration" + i, resultPanel);
			model = new DefaultTableModel(3,100);
			table = new JTable(model);
			for (int j = 0; i < 100; i++){
				model.setValueAt(((BannerView)instance.views.get(i)).u, j,0);
				model.setValueAt(((BannerView)instance.views.get(i)).b, j,1);
				model.setValueAt(""+((BannerView)instance.views.get(i)).time, j,2);
			}
			resultPanel.add(table, BorderLayout.CENTER);
		}
		panel.setPreferredSize(new Dimension(800,600));
		frame.setSize(800,600);
		frame.pack();
		frame.setVisible(true);
		
	}

	private static void upgradePolicy(AdBlasterInstance instance, AdBlasterPolicy pol) {
		Vector unserved = instance.getUnserved();
		for (int i = 0; i < unserved.size(); i++){
			Tuple t = (Tuple)unserved.get(i);
			Banner b = (Banner)t.data.get(0); 
			int c = ((Integer)t.data.get(1)).intValue(); 
			for (int j = 0; j < instance.views.size() && c > 0; j++){
				BannerView bv = (BannerView) instance.views.get(j);
				if (bv.b.profit < b.profit){
					c--;
					pol.increment(b, 0.1);
					pol.increment(bv.b, -0.1);
					bv.b = b;
				}
			}
		}
	}

}
