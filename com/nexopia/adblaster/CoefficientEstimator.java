package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Set;

import Jama.Matrix;


public class CoefficientEstimator {
	//the goal is to solve Ax=b for x
	private static final int n = 13;	//the number of columns we're considering (interests, size, pay, etc)
	private Matrix A; //BannerCount x n matrix
	private Matrix x; //1xn matrix to solve for of weights to the various banner columns
	private Matrix b; //1xBannerCount matrix of coefficients
	
	public CoefficientEstimator(HashMap<Banner, Float> coefficients) {
		double[][] AArray = new double[coefficients.size()][];
		double[][] bArray = new double[coefficients.size()][1];
		
		int i=0;
		for (Banner banner : coefficients.keySet()) {
			bArray[i][0] = coefficients.get(banner).doubleValue();
			AArray[i] = getRow(banner);
			i++;
		}
		
		this.A = new Matrix(AArray);
		this.b = new Matrix(bArray);
		this.x = this.A.solve(this.b);
	}
	
	private double[] getRow(Banner banner) {
		double[] row = new double[n];
		row[0] = banner.getPayrate();
		row[1] = banner.getLimitbyperiod();
		row[2] = banner.getViewsperuser();
		for (byte i=0; i<10;i++) {
			row[i+3] = banner.validAge(i)?1:0;
		}
		return row;
	}

	private double estimateCoefficient(Banner b) {
		double[][] row = x.getArray();
		return row[0][0]*b.getPayrate() +
			   row[1][0]*b.getLimitbyperiod() +
			   row[2][0]*b.getViewsperuser();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BannerDatabase bdb = new BannerDatabase();
		HashMap<Banner,Float> coefficients = bdb.getCoefficientMap();
		CoefficientEstimator ce = new CoefficientEstimator(coefficients);
		for (Banner b : coefficients.keySet()) {
			System.out.println("Original Coefficient: " + coefficients.get(b) + " Estimated Coefficient: " + ce.estimateCoefficient(b));
		}
	}

}
