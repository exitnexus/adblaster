/**
 * 
 */
package com.nexopia.adblaster;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

final class AdBlasterThreadedOperation implements Runnable {
	private final GlobalData gd;
	private final AdBlasterThreadedInstance chunk;
	private final String name;
	private boolean finished;

	public AdBlasterThreadedOperation(GlobalData globalData, AdBlasterThreadedInstance chunk, String name) {
		super();
		this.gd = globalData;
		this.chunk = chunk;
		this.name = name;
		finished = false;
	}

	public void run() {
		finished = false;
		operateOnChunk(chunk);
	}
	
	public synchronized void operateOnChunk(AdBlasterThreadedInstance chunk) {
		JFrame frame = new JFrame("AdBlaster Test " + name);
		JPanel panel = new JPanel(new BorderLayout());
		
		frame.setContentPane(panel);
	
		JTabbedPane tab = new JTabbedPane();
		panel.add(tab, BorderLayout.CENTER);
		JPanel resultPanel = new JPanel(new BorderLayout());
		chunk.fillInstance(gd.pol);
		{	
				resultPanel = new JPanel(new BorderLayout());
				JScrollPane scroll = new JScrollPane(resultPanel);
				tab.addTab("Original", scroll);
				DefaultTableModel model = new DefaultTableModel(chunk.getViewCount(),3);
				JTable table = new JTable(model);
				for (int j = 0; j < chunk.getViewCount(); j++){
					model.setValueAt(chunk.getView(j).getUser(), j,0);
					model.setValueAt(chunk.getView(j).getBanner(), j,1);
					model.setValueAt(AdBlaster.outputTime(chunk.getView(j).getTime()), j,2);
				}
				resultPanel.add(table, BorderLayout.CENTER);
				JPanel statPanel = new JPanel(new FlowLayout());
				statPanel.add(new JTextField(""+chunk.totalProfit()));
				
				
				
				statPanel.add(new JTextField(""+chunk.totalProfit()));
				resultPanel.add(statPanel, BorderLayout.PAGE_START);
	
		}
	
		System.out.println("Upgrading policy.");
		gd.pol.upgradePolicy(chunk, this);
		
		{
			//Perfect results panel
			resultPanel = new JPanel(new BorderLayout());
			JScrollPane scroll = new JScrollPane(resultPanel);
			tab.addTab("Perfect", scroll);
			DefaultTableModel model = new DefaultTableModel(chunk.getViewCount(),3);
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
			DefaultTableModel model = new DefaultTableModel(chunk.getViewCount(),3);
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
		this.finished = true;
		this.notify();
	}
	
	public void iterativeImprove(AbstractAdBlasterInstance instanc) {
		Vector<Tuple<Banner,Integer>> unserved = gd.getUnserved();
		
		for (int i = 0; i < unserved.size(); i++){
			System.out.println("Unserved: " + i +" /" + unserved.size());
			Tuple<Banner, Integer> t = unserved.get(i);
			Banner b = (Banner)t.getFirst(0); 
			//while (((Integer)instance.bannerCountMap.get(b)).intValue() < b.getMaxHits()){
			int depth = 0;
			for (int j = 0; j < instanc.getViewCount() && instanc.count(b) < b.getMaxHits(); j++){
				//System.out.println("Trying bannerview " + j);
				BannerView bv = instanc.getView(j);
				
				if (bv.getBanner() == null || bv.getBanner().getPayrate() < b.getPayrate()){
					if (depth == 0 && instanc.isValidBannerForView(bv,b)){
						//single swapbreak;
						bv.setBanner(b);
					} else if (depth > 0 && false){
						Vector swaps = null;
						int swap_max = 0;
						for (int l = 1; l < swap_max; l+=2){
							Vector path = instanc.depthLimitedDFS(bv, b, l);
							if (path != null){
								swaps = path;
								break;
							}
						}
						if (swaps != null){
							instanc.doSwap(swaps, b);
						}
					}
				}
				//}
				//depth++;
			}
		}
	}

	public synchronized boolean isFinished() {
		return this.finished;
	}

}