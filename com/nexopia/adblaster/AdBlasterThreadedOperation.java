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
	private float original_profit;
	JTabbedPane tab;
	
	public AdBlasterThreadedOperation(GlobalData globalData, AdBlasterThreadedInstance chunk, String name, JTabbedPane tab) {
		super();
		this.gd = globalData;
		this.chunk = chunk;
		this.tab = tab;
		
		this.name = name;
		finished = false;
	}

	public void run() {
		finished = false;
		operateOnChunk(chunk);
	}
	
	public synchronized void operateOnChunk(AdBlasterThreadedInstance chunk) {
		original_profit = chunk.totalProfit();

		chunk.fillInstance(gd.pol);
		
		if (tab != null){
			createTab(chunk, tab, "Original", original_profit);
		}

	
		System.out.println("Upgrading policy.");
		gd.pol.upgradePolicy(chunk, this);
		
		if (tab != null){
			createTab(chunk, tab, "Perfect", original_profit);
		}

		for (int i = 0; i < chunk.getViewCount(); i++){
			chunk.getView(i).setBanner(null);
		}

		chunk.fillInstance(gd.pol);
		
		if (tab != null){
			createTab(chunk, tab, "Final", original_profit);

			JPanel statPanel = new JPanel();
			statPanel.setLayout(new BoxLayout(statPanel, BoxLayout.PAGE_AXIS));
			statPanel.add(new JScrollPane(AdBlaster.getBannerTable(AdBlaster.ac, gd.pol)));
		}
		this.finished = true;
		this.notify();
	}

	public static void createTab(AdBlasterThreadedInstance chunk, JTabbedPane tab, String title, float original_profit) {
		JPanel resultPanel = new JPanel(new BorderLayout());

		{	
				resultPanel = new JPanel(new BorderLayout());
				JScrollPane scroll = new JScrollPane(resultPanel);
				tab.addTab(title, scroll);
				DefaultTableModel model = new DefaultTableModel(chunk.getViewCount(),3);
				JTable table = new JTable(model);
				for (int j = 0; j < chunk.getViewCount(); j++){
					String s = "";
					s += "User: " + chunk.getView(j).getUserID() + "; ";
					s += "Page: " + chunk.getView(j).getPage() + "; ";
					s += "Size: " + chunk.getView(j).getSize() + "; ";
					s += chunk.getView(j).comment;
					model.setValueAt(s, j,0);
					model.setValueAt(chunk.getView(j).getBanner(), j,1);
					model.setValueAt(AdBlaster.outputTime(chunk.getView(j).getTime()), j,2);
				}
				resultPanel.add(table, BorderLayout.CENTER);
				JPanel statPanel = new JPanel(new FlowLayout());
				statPanel.add(new JTextField(""+original_profit));
				
				
				
				statPanel.add(new JTextField(""+chunk.totalProfit()));
				resultPanel.add(statPanel, BorderLayout.PAGE_START);
	
		}
	}
	
	public void iterativeImprove(AbstractAdBlasterInstance instanc) {
		Vector<Banner> unserved = gd.getUnserved();
		System.out.println("Improving based on unserved banners.");
		ProgressIndicator.setTitle("Using Unserved Banners...");
		for (int i = 0; i < unserved.size(); i++){
			ProgressIndicator.show(i, unserved.size());
			Banner b = (Banner)unserved.get(i);
			
			// First try simple search
			for (int j = 0; j < instanc.getViewCount() && 
				instanc.bannerCount(b) < b.getViewsperday() &&
				instanc.campaignCount(b) < b.getCampaign().getViewsperuser(); j++){
				// System.out.println("Trying bannerview " + j);
				BannerView bv = instanc.getView(j);
				if (bv.getBanner() == null || bv.getBanner().getPayrate(instanc) < b.getPayrate(instanc)){
					if (instanc.isValidBannerForView(bv,b)){
						// single swap
						bv.setBanner(b);
					}
				}
			}
			
			// Then try DFS
			boolean doable = false;
			for (int j = 0; j < instanc.getViewCount() && 
				instanc.bannerCount(b) < b.getViewsperday() &&
				instanc.campaignCount(b) < b.getCampaign().getViewsperuser(); j++){
				BannerView bv = instanc.getView(j);
				if (instanc.isValidBannerForView(bv,b)){
					doable = true;
				}
			}
			if (doable){
				for (int j = 0; j < instanc.getViewCount() && 
					instanc.bannerCount(b) < b.getViewsperday() &&
					instanc.campaignCount(b) < b.getCampaign().getViewsperuser(); j++){
					// System.out.println("Trying bannerview " + j);
					BannerView bv = instanc.getView(j);
					if (bv.getBanner() == null || bv.getBanner().getPayrate(instanc) < b.getPayrate(instanc)){
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
							break;
						}
					}
				}
			}
		}
	}

	public synchronized boolean isFinished() {
		return this.finished;
	}

}