/**
 * 
 */
package com.nexopia.adblaster.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;

import com.nexopia.adblaster.struct.Campaign;
import com.nexopia.adblaster.struct.ServablePropertyHolder;
import com.nexopia.adblaster.util.Integer;
import com.nexopia.adblaster.util.PageValidatorFactory;

public class CampaignDB{
	private HashMap<Integer, Campaign> campaigns;
	public PageValidatorFactory pvf;
	
	public CampaignDB(PageValidatorFactory pvf) {
		this.pvf = pvf;
		System.out.println("Initing campaigns.");
		campaigns = new HashMap<Integer, Campaign>();
		//Database connection stuff here.
		try {
			String sql = "SELECT * FROM " + JDBCConfig.CAMPAIGN_TABLE;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			int i = 0;
			while (rs.next()) {
				int id = rs.getInt("ID");
				campaigns.put(Integer.valueOf(id), new Campaign(rs, pvf));
				i++;
			}
			System.out.println("Campaigns Total: " + i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	public ServablePropertyHolder add(int campaignID) {
		try {
			String sql = "SELECT * FROM " + JDBCConfig.CAMPAIGN_TABLE + " WHERE id = " + campaignID;
			Statement stmt = JDBCConfig.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			if (rs.next()) {
				int id = rs.getInt("ID");
				Campaign c = new Campaign(rs, pvf);
				campaigns.put(Integer.valueOf(id), c);
				return c;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ServablePropertyHolder update(int campaignID) {
		Integer id = Integer.valueOf(campaignID);
		ServablePropertyHolder c = campaigns.get(id);
		id.free();
		id = null;
		if (c != null) {
			try {
				String sql = "SELECT * FROM " + JDBCConfig.CAMPAIGN_TABLE + " WHERE id = " + campaignID;
				Statement stmt = JDBCConfig.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					c.update(rs, pvf);
					return c;
				} else {
					id = Integer.valueOf(campaignID);
					campaigns.remove(id);
					id.free();
					id = null;
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return add(campaignID);
		}
	}
	public Collection<Campaign> getCampaigns() {
		return campaigns.values();
	}
	public Campaign get(int campaignID) {
		Integer I = Integer.valueOf(campaignID);
		Campaign c = campaigns.get(I);
		I.free();
		return c;
	}
	
	public Campaign getByIndex(int index) {
		Campaign c = get(((Integer)campaigns.keySet().toArray()[index]).intValue());
		return c;
	}

	public int getCampaignCount() {
		return campaigns.size();
	}

	public void delete(int campaignID) {
		Integer id = Integer.valueOf(campaignID);
		campaigns.remove(id);
		id.free();
	}

}