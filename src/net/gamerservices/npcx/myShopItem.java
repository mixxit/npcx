package net.gamerservices.npcx;

import org.bukkit.inventory.ItemStack;

public class myShopItem {
	public ItemStack item = new ItemStack(0);
	
	public int price = 0;
	
	
	myShopItem()
	{
		item.setAmount(1);
		item.setTypeId(1);
		
	}
}
