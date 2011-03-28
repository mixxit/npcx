package net.gamerservices.npcx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;

public class myMerchant_entry {

	public int merchantid;
	public int itemid;
	public int amount;
	public int pricebuy;
	public int pricesell;
	public myMerchant parent;
	public int id;
	
	
	public myMerchant_entry(myMerchant parent, int merchantid,int itemid,int amount,int pricebuy,int pricesell) {
		// TODO Auto-generated constructor stub
		
		this.parent = parent;
		this.merchantid = merchantid;
		this.itemid = itemid;
		this.amount = amount;
		this.pricebuy = pricebuy;
		this.pricesell = pricesell;
	}

}
