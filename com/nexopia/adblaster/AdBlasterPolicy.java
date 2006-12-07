package com.nexopia.adblaster;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.Arrays;

import com.nexopia.adblaster.db.BannerDatabase;
import com.nexopia.adblaster.struct.Banner;
import com.nexopia.adblaster.struct.BannerView;
import com.nexopia.adblaster.struct.I_Policy;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.util.Integer;

public class AdBlasterPolicy implements I_Policy {
	private HashMap<Banner, Float> coefficients;
	Collection<Banner> banners;
	public AdBlasterPolicy(Collection<Banner> bannerList) {
		banners = new Vector<Banner>();
		coefficients = new HashMap<Banner, Float>();
		for (Banner b : bannerList){
			coefficients.put(b, new Float(1.0/*Math.random()*/));
		}
	}

	public static AdBlasterPolicy randomPolicy(Collection<Banner> bannerList) {
		return new AdBlasterPolicy(bannerList);
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


	static int getRandomBanner(Vector banners, HashMap coefficients) {
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

	public Banner getBestBanner(AbstractAdBlasterInstance instance, BannerView bv) {
		synchronized(banners){
			if (banners.isEmpty()){
				banners = orderBannersByScore(instance);
			}
		}

		Vector<Banner> all = new Vector<Banner>();
		all.addAll(banners);
		while (!all.isEmpty()){
			int banner = getRandomBanner(all, getCoefficients());
			
			if (banner == 0){
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

	Vector<Banner> orderBannersByScore(AbstractAdBlasterInstance instance) {
		Vector<Banner> vec = new Vector<Banner>();
		//int bestMatch = -1;
		//float bestScore = Float.NEGATIVE_INFINITY;
		for (int j = 0; j < instance.universe.getBannerCount(); j++){
			Banner b = instance.universe.getBannerByIndex(j);
			float score = ((Float)getCoefficient(b)).floatValue();
			int i = -1;
			while (true){
				i++;
				if (i >= vec.size()){
					break;
				}
				ServablePropertyHolder b2 = vec.get(i);
				if (getCoefficient(b2).floatValue() < score)
					break;
			}
			vec.insertElementAt(b, i);
			
		}
		System.out.println(vec.get(0));
		return vec;
	}

	public HashMap<Banner, Float> getCoefficients() {
		return coefficients;
	}

	public double getPriority(Banner b, int uid, int time, BannerServer server) {
		return coefficients.get(b).doubleValue();
	}

}
