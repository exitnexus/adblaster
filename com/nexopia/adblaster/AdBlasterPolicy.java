package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;

import javax.swing.JOptionPane;

import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.ProgressIndicator;

public class AdBlasterPolicy implements I_Policy {
	private HashMap<Banner, Float> coefficients;
	AbstractAdBlasterUniverse universe;
	Vector<Banner> banners = null;
	
	public AdBlasterPolicy(AbstractAdBlasterUniverse ac) {
		banners = new Vector<Banner>();
		coefficients = new HashMap<Banner, Float>();
		universe = ac;
		for (int i = 0; i < ac.getBannerCount(); i++){
			coefficients.put(ac.getBannerByIndex(i), new Float(1.0/*Math.random()*/));
		}
	}

	public static AdBlasterPolicy randomPolicy(AbstractAdBlasterUniverse ac) {
		return new AdBlasterPolicy(ac);
	}

	public void increment(Banner b, float d) {
		coefficients.put(b, new Float(((Float)coefficients.get(b)).floatValue() + d));
	}

	public void incrementMultiply(Banner b, float d) {
		float f = ((Float)coefficients.get(b)).floatValue() * d;
		f = Math.min(f, 100000000.0f);
		f = Math.max(f, 0.00000001f);
		Float new_coef = new Float(f);
		coefficients.put(b, new_coef);
	}

	public void upgradePolicy(AbstractAdBlasterInstance chunk, AdBlasterThreadedOperation op) {
		System.out.println("Upgrading.");
		int sbefore[] = new int[universe.getBannerCount()];
		for (int i = 0; i < universe.getBannerCount(); i++){
			ServablePropertyHolder b = universe.getBannerByIndex(i);
			sbefore[i] = chunk.bannerCount(b);
		}
		

		float count = -1;
		float newcount = 0;
		int iterations = 0;
		while((newcount = chunk.totalProfit()) != count && iterations < 50){
			System.out.println("An iteration...");
			iterations++;
			count = newcount;
			op.iterativeImprove(chunk);
		}
		ProgressIndicator.setTitle("Calculating Banner Coefficients...");
		for (int i = 0; i < universe.getBannerCount(); i++){
			Banner b = universe.getBannerByIndex(i);
			ProgressIndicator.show(i, universe.getBannerCount());
			int after = chunk.bannerCount(b);
			int before = sbefore[i];
			float f = ((float)((1.0f + after) / (1.0f + before)));
			this.incrementMultiply(b, (float)Math.pow(f, 50.0f)); 
		}
		
		synchronized(banners) {
			banners = orderBannersByScore(chunk);
		}

	}

	public Banner getBestBanner(AbstractAdBlasterInstance instance, BannerView bv) {
		synchronized(banners){
			if (banners.isEmpty()){
				banners = orderBannersByScore(instance);
			}
		}

		Vector<Banner> all = new Vector<Banner>();
		all.addAll(banners);
		while (!all.isEmpty()){
			int banner = getRandomBanner(all, coefficients);
			
			if (banner == -1){
				continue;
			}

			Banner b = (Banner) all.get(banner);
			
			if ( instance.bannerCount(b) < b.getIntegerMaxViewsPerDay() && 
					instance.campaignCount(b) < b.getCampaign().getIntegerMaxViewsPerDay()){
				if (instance.isValidBannerForView(bv, b)){
					return b;
				}
			}
			
			all.remove(b);
			
		}
		return null;
	}

	private static int getRandomBanner(Vector banners, HashMap coefficients) {
		double total = 0;
		for (int i = 0; i < banners.size(); i++){
			ServablePropertyHolder b = (ServablePropertyHolder) banners.get(i);
			total += ((Float)coefficients.get(b)).floatValue();
		}

		double r = Math.random() * total;
		double density = 0;
		int banner = -1;
		for (int i = 0; i < banners.size(); i++){
			ServablePropertyHolder b = (ServablePropertyHolder) banners.get(i);
			density += ((Float)coefficients.get(b)).floatValue();
			if (density > r){
				banner = i;
				break;
			}
		}
		return banner;
	}
	

	private Vector<Banner> orderBannersByScore(AbstractAdBlasterInstance instance) {
		Vector<Banner> vec = new Vector<Banner>();
		//int bestMatch = -1;
		//float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < instance.universe.getBannerCount(); j++){
			Banner b = instance.universe.getBannerByIndex(j);
			float score = ((Float)coefficients.get(b)).floatValue();
			int i = -1;
			while (true){
				i++;
				if (i >= vec.size()){
					break;
				}
				ServablePropertyHolder b2 = vec.get(i);
				if (((Float)coefficients.get(b2)).floatValue() < score)
					break;
			}
			vec.insertElementAt(b, i);
			
		}
		System.out.println(vec.get(0));
		return vec;
	}

	public Float getCoefficient(ServablePropertyHolder bannerByIndex) {
		return (Float)coefficients.get(bannerByIndex);
	}

	public static void main(String args[]){
		float coefficients[] = new float[10];
		for (int i = 0; i < 10; i++){
			coefficients[i] = (float) (Math.random() * 10);
		}
		Vector<Integer> vec = new Vector<Integer>();
		//int bestMatch = -1;
		//float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < 10; j++){
			float score = ((Float)coefficients[j]).floatValue();
			int i = -1;
			float score2 = Float.POSITIVE_INFINITY;
			while (score < score2){
				i++;
				if (i >= vec.size()){
					break;
				}
				if (((Float)coefficients[((Integer)vec.get(i)).intValue()]).floatValue() < score)
					break;
			}
			vec.insertElementAt(new Integer(j), i);
			System.out.println(Arrays.toString(vec.toArray()));
			
		}
		System.out.println(Arrays.toString(coefficients));
		System.out.println(Arrays.toString(vec.toArray()));
	}

	public HashMap<Banner, Float> getCoefficients() {
		return coefficients;
	}

	public double getPriority(Banner b, int uid, int time, BannerServer server) {
		// TODO Auto-generated method stub
		return 0;
	}

}
