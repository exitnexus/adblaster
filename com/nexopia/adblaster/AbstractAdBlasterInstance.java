package com.nexopia.adblaster;

import java.util.Vector;

import com.sleepycat.je.Environment;

public class AbstractAdBlasterInstance implements I_AdBlasterInstance {

	Vector views;
	Environment dbEnv;
	BannerViewDatabase db;

	public void fillInstance(AdBlasterPolicy pol) {
	}

	public boolean isValidBannerForUser(User u, Banner b) {
		return false;
	}

	public BannerView randomView(AdBlasterUniverse campaign) {
		return null;
	}

	public Vector getUnserved() {
		return null;
	}

	public float totalProfit() {
		return 0;
	}

	public AdBlasterInstance copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
