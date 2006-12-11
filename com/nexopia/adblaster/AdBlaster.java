package com.nexopia.adblaster;

import java.io.File;
import java.io.IOException;

import com.nexopia.adblaster.db.PageFlatFileDatabase;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.util.PageValidator2;
import com.nexopia.adblaster.util.PageValidatorFactory;
import com.nexopia.adblaster.util.Utilities;

public class AdBlaster {

	private static final int THREAD_COUNT = 1;
	static int num_serves = 1;
	static AdBlasterDbUniverse ac;
	//static AdBlasterDbInstance instanc;
	
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

		PageFlatFileDatabase pageDb = null;
		try {
			pageDb = new PageFlatFileDatabase(page_dir, true);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		Object args2[] = {pageDb};
		PageValidatorFactory factory = 
			new PageValidatorFactory(PageValidator2.class,args2);

		long start_time = System.currentTimeMillis();

		ac = new AdBlasterDbUniverse(factory);

/*		instanc = new AdBlasterDbInstance(ac);
		//instanceBinding = new BannerViewBinding(ac, instanc);

		

		if (dataFile != null){
			((AdBlasterDbInstance)instanc).loadNoCount(bv_dir, user_dir, dataFile);
		} else {
			((AdBlasterDbInstance)instanc).load(bv_dir, user_dir);
		}
		System.out.println("Total original profit: " + instanc.totalProfit());
*/		

		AdBlasterPolicy pol = AdBlasterPolicy.randomPolicy(ac.getBanners());

		GlobalData gd = new GlobalData(pol, ac, bv_dir);
		AdBlasterThreadedInstance[] chunk = getChunk(gd, THREAD_COUNT);
		Runnable[] r = new Runnable[THREAD_COUNT];
		Thread[] t = new Thread[THREAD_COUNT];
		
		
		for (int i = 0; i < num_serves; i++){
			
			for (int j=0; j<THREAD_COUNT; j++) {
				r[j] = new AdBlasterThreadedOperation(gd, chunk[j]);
				t[j] = new Thread(r[j], "operateOnChunk");
				
				t[j].start();
			}
			for (int j=0; j<THREAD_COUNT; j++) {
				synchronized (r[j]){
					AdBlasterThreadedOperation op = (AdBlasterThreadedOperation)r[j];
					if (!op.isFinished()) {
						try {
							r[j].wait();
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

		System.out.println("Total time:" + (System.currentTimeMillis()- start_time));
		// TODO Auto-generated method stub

	}

	/*
	 * Get "num" chunks of average size "size".
	 */
	private static AdBlasterThreadedInstance[] getChunk(GlobalData gd, int num) {
		AdBlasterThreadedInstance r[] = new AdBlasterThreadedInstance[num];
		//int modCount = gd.instance.getUserCount() / size;
		for (int i = 0; i < num; i++){
			r[i] = new AdBlasterThreadedInstance(gd, i);
		}
		return r;
	}								
					

	static String outputTime(int i) {
		int hour = i / (60*60); 
		int min = (i / 60) % 60;
		int sec = i % 60;
		return "" + (hour%12) + ":" + min + ":" + sec + (hour > 12?"pm":"am");
	}

}
