package net.gamerservices.npcx;

import org.bukkit.inventory.ItemStack;

public class myMerchantItem {
	public ItemStack item = new ItemStack(1,1);
	
	public double price = 0;
	
	
	myMerchantItem()
	{
		item.setAmount(1);
		item.setTypeId(1);
		
	}
}
