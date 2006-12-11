package com.nexopia.adblaster;

import java.io.File;

public class GlobalData {

	public AdBlasterPolicy pol;
	public AbstractAdBlasterUniverse universe;
	public File bannerViewDirectory;
	
	public GlobalData(AdBlasterPolicy pol, AbstractAdBlasterUniverse universe, File bvdir){
		this.pol = pol;
		this.universe = universe;
		this.bannerViewDirectory = bvdir;
		
	}

}
