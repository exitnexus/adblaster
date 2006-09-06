package com.nexopia.adblaster;

import java.util.HashMap;
import java.util.Vector;

import com.nexopia.adblaster.Campaign.CampaignDB;
import com.sleepycat.je.DatabaseException;

public class BannerServer {
		public static final Integer BANNER_BANNER = new Integer(1);
		public static final Integer BANNER_LEADERBOARD = new Integer(2);
		public static final Integer BANNER_BIGBOX = new Integer(3);
		public static final Integer BANNER_SKY120 = new Integer(4);
		public static final Integer BANNER_SKY160 = new Integer(5);
		public static final Integer BANNER_BUTTON60 = new Integer(6);
		public static final Integer BANNER_VULCAN = new Integer(7);
		public static final Integer BANNER_LINK = new Integer(8);
	
		public BannerDatabase db;
		public CampaignDB cdb;
		public HashMap<String,Integer> sizes;

		private static int numservers;

		//public $banners;
		//public $bannerids;
		//public $bannersizes;
		//public HashMap<Integer, Campaign> bannercampaigns;
		//public HashMap<Integer, Integer> campaignids; // array( bannerid => campaignid );
		public Object[] dailyviews;
		public Object[] dailyclicks;

		public int time;

		public BannerServer(BannerDatabase db, CampaignDB cdb, int numservers) {
			this.db = db;
			this.cdb = cdb;
			BannerServer.numservers = numservers;
			this.sizes = new HashMap<String, Integer>();
			this.sizes.put("468x60", BANNER_BANNER);
			this.sizes.put("728x90", BANNER_LEADERBOARD);
			this.sizes.put("300x250", BANNER_BIGBOX);
			this.sizes.put("120x600", BANNER_SKY120);
			this.sizes.put("160x600", BANNER_SKY160);
			this.sizes.put("120x60", BANNER_BUTTON60);
			this.sizes.put("Voken", BANNER_VULCAN);
			this.sizes.put("Link", BANNER_LINK);
			
			this.dailyviews = new Object[this.sizes.size()];
			this.dailyclicks= new Object[this.sizes.size()];

			for(Integer size : this.sizes.values()) {
				this.dailyviews[size.intValue()] = new Object();
				this.dailyclicks[size.intValue()]= new Object();
			}

			this.time = (int) (System.currentTimeMillis()/1000);
		}
		
		public boolean addCampaign(int id){
			Campaign c = cdb.add(id);
			if (c != null) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean updateCampaign(int id) {
			return cdb.update(id) != null;
		}
		
		public void deleteCampaign(int id) {
			cdb.get(id).minutely();
			cdb.delete(id);
			this.db.deleteCampaign(id);
		}
		public boolean addBanner(int id) {
			return this.db.add(id) != null;
		}
		
		public boolean updateBanner(int id) {
			return this.db.update(id) != null;
		}
		
		public void deleteBanner(int id) {
			this.db.delete(id);
		}
		
		public int getBanner(int usertime, int size, int userid, byte age, byte sex, short location, Interests interests, int page, boolean debug){
			//String debugLog = "";
			//if (debug) debugLog += usertime+", "+size+", "+userid+", "+age+", "+sex+", "+loc+", "+page+", "+debug;
			
			Vector<Banner> valid = new Vector<Banner>();
			for (Campaign campaign : cdb.getCampaigns()){
				valid.addAll(campaign.getBanners(usertime, size, userid, age, sex, location, interests, page, debug));
			}
			if (!valid.isEmpty()) {
				Banner b = Utilities.priorityChoose(valid);
				b.hit(userid, usertime);
				return b.getID();
			} else {
				return 0;
			}
		}
		
		/*
		function addBanner($id){
			$res = $this->db->prepare_query("SELECT * FROM banners WHERE id = #", $id);
			$banner = $res->fetchrow();
			unset($res);
			
			if($banner){
				$newBanner = new banner($banner, $this->numservers, $this->bannercampaigns[$banner['campaignid']]);
				$this->bannercampaigns[$banner['campaignid']]->addBanner($newBanner);
				$this->campaignids[$banner['id']] = $banner['campaignid'];
				return true;
			}else
				return false;
		}


		function updateBanner($id){
			$res = $this->db->prepare_query("SELECT * FROM banners WHERE id = #", $id);
			$banner = $res->fetchrow();
			unset($res);
			
			if($banner){
				if ($banner['campaignid'] == $this->campaignids[$id]) { //same campaign just update the banner in it
					$this->bannercampaigns[$this->campaignids[$id]]->getBannerID($id)->update($banner, $this->numservers, $this->bannercampaigns[$this->campaignids[$id]]);
				} else { //moved to a new campaign
					$this->bannercampaigns[$this->campaignids[$id]]->deleteBanner($id);
					$newBanner = new banner($banner, $this->numservers, $this->bannercampaigns[$banner['campaignid']]);
					$this->bannercampaigns[$banner['campaignid']]->addBanner($newBanner);
					$this->campaignids[$banner['id']] = $banner['campaignid'];
				}
				return true;
				if(isset($this->banners[$banner['bannersize']][$banner['id']])){ //exists where expected
			}else
				return false;
		}

		function deleteBanner($id){
			$banner = $this->bannercampaigns[$this->campaignids[$id]]->getBannerID($id);
			$banner->minutely($this->db, $this->time, false); //update stats
			$this->bannercampaigns[$this->campaignids[$id]]->deleteBanner($id);
			unset($this->campaignids[$id]);
		}

		function deleteCampaign($id) {
			if (isset($this->bannercampaigns[$id])) {
				$this->bannercampaigns[$id]->minutely($this->db, $this->time, false);
				foreach($this->bannercampaigns[$id]->banners as $banner) {
					unset($this->campaignids[$banner->id]);
				}
				unset($this->bannercampaigns[$id]);
			}
		}

		function passbackBanner($id, $userid){
			if(isset($this->campaignids[$id])) {
					$this->bannercampaigns[$this->campaignids[$id]]->getBannerID($id)->passback($userid, $this->time);
			}
		}

		function getBanner($usertime, $size, $userid, $age, $sex, $loc, $interests, $page, $debug = 0, $id = null){
			$debugLog = "";
			if ($debug) $debugLog .= "$usertime, $size, $userid, $age, $sex, $loc, $page, $debug";
			if($id === null){
				$valid = array();
				foreach($this->bannercampaigns as &$campaign){
					//check campaign size and campaign enabled
					if ($debug) $debugLog .= "\nPre-checking campaign $campaign->id:"; 
					if(isset($campaign->sizes[$size]) && $campaign->enabled) {
						//check valid start/end date and valid sex
						if ($debug) $debugLog .= " 1"; 
						if(!($campaign->startdate >= $this->time || ($campaign->enddate && $campaign->enddate <= $this->time)) && $campaign->sex[$sex]) {
							if ($debug) $debugLog .= " 2"; 
							//bannerDebug("Passed prescreening for $campaign->id");
							if($validBanners = $campaign->valid($userid, $age, $sex, $loc, $interests, $page, $this->time, $size, $usertime, $debug)){
								$valid[$campaign->id] = $campaign->priority($userid, $validBanners, $this->time);
								$banners[$campaign->id] = $validBanners;
								foreach ($validBanners as $bannerid) {
									$this->bannercampaigns[$campaign->id]->getBannerID($bannerid)->potentialHit();
								}
							}
						} else {
							//bannerDebug("Failed date check for campaign:$campaign->id");
						}
					} else {
						//bannerDebug("Failed size check for campaign:$campaign->id");
					}
				}
				
				if(count($valid) == 0) {
					if ($debug) bannerDebug($debugLog); 
					return 0;
				}

				
				$campaignID = chooseWeight($valid, false);
				$id = $this->bannercampaigns[$campaignID]->getBanner($userid, $banners[$campaignID], $this->time);

			}


			$this->dailyviews[$size]->hit($age, $sex, $loc, $interests, $page, $usertime);

			$this->bannercampaigns[$campaignID]->hit($id, $userid, $this->time);

			if ($debug) $debugLog .= "\nChose banner $id";
			if ($debug) bannerDebug($debugLog); 

			return $id;
		}

		function clickBanner($id, $age, $sex, $loc, $interests, $page, $time){
//			echo "id: $id, age: $age, sex: $sex, loc: $loc, page: $page\n";

			if(!isset($this->campaignids[$id])){
				echo "banner doesn't exist!\n";
				return;
			}

			$this->bannercampaigns[$this->campaignids[$id]]->getBannerID($id)->click();
			$this->dailyclicks[$this->bannercampaigns[$this->campaignids[$id]]->getBannerID($id)->size]->hit($age, $sex, $loc, $interests, $page, $time);
		}

		function minutely($debug){
			foreach($this->bannercampaigns as $campaign) {
				$campaign->minutely($this->db, $this->time, $debug);
			}

			return 1;
		}

		function hourly($debug){
			foreach($this->bannercampaigns as $campaign) {
				$campaign->hourly($this->db, $this->time, $debug);
			}
			return 2;
		}

		function daily($debug){
			if($debug)
			bannerDebug("daily");

			foreach($this->bannercampaigns as $campaign){
				$campaign->daily($debug);
			}
			foreach($this->sizes as $size) {
				$viewsdump = gzcompress(serialize($this->dailyviews[$size]));
				$clicksdump= gzcompress(serialize($this->dailyclicks[$size]));

				$this->db->prepare_query("INSERT INTO bannertypestats SET size = #, time = #, views = #, clicks = #, viewsdump = ?, clicksdump = ?", $size, $this->time, $this->dailyviews[$size]->total, $this->dailyclicks[$size]->total,  $viewsdump, $clicksdump);

				unset($viewsdump, $clicksdump);

				$this->dailyviews[$size] = new bannerstats();
				$this->dailyclicks[$size]= new bannerstats();
			}

			return 4;
		}

		function settime($time, $debug){
			$this->time = $time;

			$ret = 0;

			if($time % 60 == 0)
				$ret |= $this->minutely($debug);

			if($time % 3600 == 0)
				$ret |= $this->hourly($debug);

			if($time % 86400 == 3600*BANNER_DAILY_HOUR)
				$ret |= $this->daily($debug);

			return $ret;
		}

		function getBanners(){
			/*$res = $this->db->prepare_query("SELECT * FROM banners WHERE moded = 'y'");
			$result = $this->db->prepare_query('SELECT * FROM bannercampaigns');
			while ($line = $result->fetchrow()) {
				$this->bannercampaigns[$line['id']] = new bannercampaign($line, $this->numservers);
				foreach($this->bannercampaigns as $campaign) {
					foreach($campaign->banners as $banner) {
						$this->campaignids[$banner->id] = $campaign->id;
					}
				}
			}
			unset($result);
		}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			BannerDatabase bdb = new BannerDatabase(new CampaignDB(new PageDatabase("")));
			bdb.loadCoefficients(new HashMap<Banner, Float>());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
