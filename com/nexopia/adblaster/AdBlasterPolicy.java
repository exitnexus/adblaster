package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;

public class AdBlasterPolicy implements I_Policy {
	private HashMap<Banner, Float> coefficients;
	AbstractAdBlasterUniverse universe;
	Vector banners = null;
	
	public AdBlasterPolicy(AbstractAdBlasterUniverse ac) {
		coefficients = new HashMap<Banner, Float>();
		universe = ac;
		for (int i = 0; i < ac.getBannerCount(); i++){
			coefficients.put(ac.getBannerByIndex(i), new Float(1.0/*Math.random()*/));
		}
	}

	public static AdBlasterPolicy randomPolicy(AbstractAdBlasterUniverse ac) {
		return new AdBlasterPolicy(ac);
	}

	public void increment(Banner b, double d) {
		coefficients.put(b, new Float(((Float)coefficients.get(b)).floatValue() + d));
		banners = null;
	}

	public void incrementMultiply(Banner b, double d) {
		coefficients.put(b, new Float(((Float)coefficients.get(b)).floatValue() * d));
		banners = null;
	}

	public void upgradePolicy(AbstractAdBlasterInstance chunk, AdBlasterThreadedOperation op) {

		int sbefore[] = new int[universe.getBannerCount()];
		for (int i = 0; i < universe.getBannerCount(); i++){
			Banner b = universe.getBannerByIndex(i);
			sbefore[i] = chunk.count(b);
		}
		

		float count = -1;
		float newcount = 0;
		while((newcount = chunk.totalProfit()) != count){
			count = newcount;
			op.iterativeImprove(chunk);
		}
		for (int i = 0; i < universe.getBannerCount(); i++){
			Banner b = universe.getBannerByIndex(i);
			System.out.println("Calculating banner " + i + "/" + universe.getBannerCount());
			int after = chunk.count(b);
			int before = sbefore[i];
			System.out.println("" + after + "  :  " + before + "  :  " + (float)after/(float)before);
			float f = ((float)((1.0f + after) / (1.0f + before)));
			this.incrementMultiply(b, f); 
			banners = null;
		}

	}

	public Banner getBestBanner(AbstractAdBlasterInstance instance, BannerView bv) {
		//User u = bv.getUser();
		//int t = bv.getTime();
		if (banners == null){
			banners = orderBannersByScore(instance);
		}
		int bestMatch = -1;
		float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < instance.universe.getBannerCount(); j++){
			Banner b = (Banner) banners.get(j);
			float score = ((Float)coefficients.get(b)).floatValue();
			if (score > bestScore){
				if ( instance.count(b) < b.getMaxHits() ){
					if (instance.isValidBannerForView(bv, b)){
						/* If everything is working properly, this should be fine...
						 * 
						 */
						if (true) return b;
						bestScore = score;
						bestMatch = j;
					}
				}
			}
		}
		
		Banner banner = instance.universe.getBannerByIndex(bestMatch);
		return banner;
	}
	

	private Vector orderBannersByScore(AbstractAdBlasterInstance instance) {
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
				Banner b2 = vec.get(i);
				if (((Float)coefficients.get(b2)).floatValue() < score)
					break;
			}
			vec.insertElementAt(b, i);
			
		}
		System.out.println(vec.get(0));
		return vec;
	}

	public Float getCoefficient(Banner bannerByIndex) {
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

}
