package com.nexopia.adblaster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class AdBlaster {

	static int num_serves = 10;
	static AbstractAdBlasterUniverse ac;
	static BannerViewBinding instanceBinding;
	
	public static void main(String args[]){
		long start_time = System.currentTimeMillis();
		
		//ac = AdBlasterUniverse.generateTestData(num_banners, num_users);
		//((AdBlasterUniverse)ac).makeMeADatabase();
		
		ac = new AdBlasterDbUniverse();
		
		
		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac);

		AbstractAdBlasterInstance instanc = new AdBlasterDbInstance(ac);
		instanceBinding = new BannerViewBinding(ac, instanc);
		((AdBlasterDbInstance)instanc).load();
		GlobalData gd = new GlobalData(instanc, pol);
		
		AdBlasterThreadedInstance chunk = new AdBlasterThreadedInstance(gd);
		getChunk(chunk, instanc);
		Runnable r = new AdBlasterThreadedOperation(gd, chunk);
		Thread t = new Thread(r, "operateOnChunk");
		t.start();
		try {
			t.wait();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			System.exit(0);
		}

		JFrame frame = new JFrame("AdBlaster Test Main Window");
		JPanel panel = new JPanel(new BorderLayout());
		
		frame.setContentPane(panel);
	
		JTabbedPane tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		JPanel resultPanel = new JPanel(new BorderLayout());
		
		{
			//Perfect results panel
			resultPanel = new JPanel(new BorderLayout());
			JScrollPane scroll = new JScrollPane(resultPanel);
			tab.addTab("Perfect", scroll);
			DefaultTableModel model = new DefaultTableModel(AdBlaster.num_serves,3);
			JTable table = new JTable(model);
			for (int j = 0; j < chunk.getViewCount(); j++){
				model.setValueAt(chunk.getView(j).getUser(), j,0);
				model.setValueAt(chunk.getView(j).getBanner(), j,1);
				model.setValueAt(AdBlaster.outputTime(chunk.getView(j).getTime()), j,2);
			}
			resultPanel.add(table, BorderLayout.CENTER);
			JPanel statsPanel = new JPanel(new FlowLayout());
			statsPanel.add(new JTextField(""+chunk.totalProfit()));
			statsPanel.add(new JTextField(""+chunk.totalProfit()));
			resultPanel.add(statsPanel, BorderLayout.PAGE_START);
	
		}		
		{
			//Actual final results.
			chunk.fillInstance(gd.pol);
			resultPanel = new JPanel(new BorderLayout());
			JScrollPane scroll = new JScrollPane(resultPanel);
			tab.addTab("Final", scroll);
			DefaultTableModel model = new DefaultTableModel(AdBlaster.num_serves,3);
			JTable table = new JTable(model);
			for (int j = 0; j < AdBlaster.num_serves; j++){
				model.setValueAt(chunk.getView(j).getUser(), j,0);
				model.setValueAt(chunk.getView(j).getBanner(), j,1);
				model.setValueAt(AdBlaster.outputTime(chunk.getView(j).getTime()), j,2);
			}
			resultPanel.add(table, BorderLayout.CENTER);
			JPanel statsPanel = new JPanel(new FlowLayout());
			statsPanel.add(new JTextField(""+chunk.totalProfit()));
			statsPanel.add(new JTextField(""+chunk.totalProfit()));
			resultPanel.add(statsPanel, BorderLayout.PAGE_START);
	
		}
		JPanel statPanel = new JPanel();
		statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.PAGE_AXIS));
		statPanel.add(new JScrollPane(AdBlaster.getBannerTable(AdBlaster.ac, gd.pol)));
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

		
		System.out.println("Total time:" + (System.currentTimeMillis()- start_time));
		// TODO Auto-generated method stub

	}

	private static void getChunk(AdBlasterThreadedInstance chunk, AbstractAdBlasterInstance instance) {
		for (int i = 0; i < instance.getViewCount(); i++){
			BannerView bv = instance.getView(i);
			if (bv.getUser().id % 10 == 0){
				chunk.addView(bv);
			}
			if (i%1000 == 0){
				System.out.println("Loaded bannerview " + i + ": " + bv);
			}
		}
		chunk.bannerCountMap = instance.bannerCountMap;
	}								
					
	static JTable getBannerTable(AbstractAdBlasterUniverse ac2, AdBlasterPolicy pol) {
		// TODO Auto-generated method stub
		DefaultTableModel model = new DefaultTableModel(ac.getBannerCount(),4);
		final JTable table = new JTable(model);
		for (int i = 0; i < ac.getBannerCount(); i++){
			model.setValueAt(""+ac.getBannerByIndex(i).index, i,0);
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

	static String outputTime(int i) {
		int hour = i / (60*60); 
		int min = (i / 60) % 60;
		int sec = i % 60;
		return "" + (hour%12) + ":" + min + ":" + sec + (hour > 12?"pm":"am");
	}

}
