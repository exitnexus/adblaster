package com.nexopia.adblaster;

import java.util.Vector;

public class GlobalData {

	public AdBlasterPolicy pol;
	public AbstractAdBlasterUniverse universe;
	public AbstractAdBlasterInstance instance;

	public GlobalData(AbstractAdBlasterInstance instance, AdBlasterPolicy pol){
		this.pol = pol;
		this.instance = instance;
		this.universe = instance.universe;
		
	}

	public Vector<Banner> getUnserved() {
		return instance.getUnserved();
	}
}
