package com.nexopia.adblaster;

import java.io.File;

public class GlobalData {

	public AdBlasterPolicy pol;
	public AbstractAdBlasterUniverse universe;
	public AdBlasterDbInstance fullDay;
	public File bannerViewDirectory;
	
	public GlobalData(AdBlasterPolicy pol, AdBlasterDbInstance inst, AbstractAdBlasterUniverse universe, File bvdir){
		this.pol = pol;
		fullDay = inst;
		this.universe = universe;
		this.bannerViewDirectory = bvdir;
		
	}

}
