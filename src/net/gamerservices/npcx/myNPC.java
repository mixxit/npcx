package net.gamerservices.npcx;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

import redecouverte.npcspawner.BasicHumanNpc;
public class myNPC {
	public npcx parent;
	public String name = "dummy";
	public String id = "0";
	String category = "container";
	public BasicHumanNpc npc;
	public myFaction faction;
	public mySpawngroup spawngroup;
	public myLoottable loottable;
	public List< myShopItem > shop = new CopyOnWriteArrayList< myShopItem >();
	public double coin = 100;
	public HashMap<String, myTriggerword> triggerwords = new HashMap<String, myTriggerword>();
	
	myNPC(npcx parent, HashMap<String, myTriggerword> triggerwords)
	{
		this.parent = parent;
		this.triggerwords = triggerwords;
	}
	
	
	
	public void parseChat(myPlayer myplayer, String message)
	{
		int count = 0;
		int size = 0;
		//myplayer.player.sendMessage("Parsing:" + message + ":" + Integer.toString(this.triggerwords.size()));
		String message2=message+" ";
		for (String word : message2.split(" "))
		{
			if(count == 0)
			{
				for (myTriggerword tw : triggerwords.values())
				{
					//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
					if (word.toLowerCase().contains(tw.word.toLowerCase()))
					{
							String send = variablise(tw.response,myplayer.player);
							myplayer.player.sendMessage(npc.getName() + " says to you, '"+ send +"'");
							count++;
							return;
	
					}
					size++;
				}
			}
		}
		if (count == 0 && size == 0)
		{
			myplayer.player.sendMessage(npc.getName() + " says to you, 'I'm sorry. I'm rather busy right now.'");
		} else {
				int count2 = 0;
				for (myTriggerword tw : triggerwords.values())
				{
					//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
					if (tw.word.toLowerCase().contains("default") && size > 0 && count == 0)
					{
						String send = variablise(tw.response,myplayer.player);
						
						myplayer.player.sendMessage(npc.getName() + " says to you, '" + send + "'");
						count2++;
						return;
					}
					
					
				}
				if (count2 == 0)
				{
					myplayer.player.sendMessage(npc.getName() + " says to you, 'I'm sorry. I'm rather busy right now.'");
				}
				
		}
	}



	private String variablise(String response, Player player) {
		// TODO Auto-generated method stub
		String newresponse = response;
		if (response.contains("bankbalance"))
		{
			//System.out.println("Replacing bankbalance variable");
			Account account = iConomy.getBank().getAccount(player.getName());
			newresponse = response.replaceAll("bankbalance", Double.toString(account.getBalance()));
			
		}
		return newresponse;
	}



	public void parseShop(myPlayer player, String message) {
		// TODO Auto-generated method stub
		//myplayer.player.sendMessage("Parsing:" + message + ":" + Integer.toString(this.triggerwords.size()));
		String message2=message+" ";
		String[] aMsg = message.split(" ");
		
		if (aMsg[0].toLowerCase().matches("help"))
		{
						
			player.player.sendMessage(npc.getName() + " says to you, 'What do you need? [sell] or [buy]'");
			return;
		}
		if (aMsg[0].toLowerCase().matches("sell"))
		{
			if (aMsg.length < 3)
			{		
				player.player.sendMessage(npc.getName() + " says to you, 'sell [itemid] [amount]'");
				return;
			} else {
				
				myShopItem shopitem = new myShopItem();
				ItemStack item = new ItemStack(0);
				shopitem.item = item;
				// todo price
				if (shopitem.price == 0)
				{
					shopitem.price = 1;
				}
				
				item.setTypeId(Integer.parseInt(aMsg[1]));
				item.setAmount(Integer.parseInt(aMsg[2]));
				int count = 0;
				for (ItemStack curitem : player.player.getInventory().getContents())
				{
					if (curitem.getTypeId() == item.getTypeId())
					{
						count = count + curitem.getAmount();
						//player.player.sendMessage(npc.getName() + " says to you, '"+ curitem.getTypeId() +"/"+curitem.getAmount() +"'");
					}
					
					
				}
				
				if (count >= item.getAmount())
				{
					
					player.player.sendMessage(npc.getName() + " says to you, 'Ok thats "+ item.getAmount() +" out of your "+count +".'");
					double totalcoins = 0;
					totalcoins = item.getAmount() * shopitem.price;
					
					if (this.coin >= totalcoins)
					{
						player.player.getInventory().removeItem(item);
						shop.add(shopitem);
						player.player.sendMessage(npc.getName() + " says to you, 'Thanks! Heres your " + totalcoins + "coins.'");
						Account account = iConomy.getBank().getAccount(player.name);
						this.coin = this.coin - totalcoins;
						account.add(totalcoins);
					} else {
						player.player.sendMessage(npc.getName() + " says to you, 'Sorry, I only have: "+this.coin+" and thats worth "+totalcoins+"!'");
					}
				} else {
					
					player.player.sendMessage(npc.getName() + " says to you, 'Sorry, you only have: "+count+" !'");
				}
			}
			return;
		}
		
		
		if (aMsg[0].toLowerCase().matches("buy"))
		{
			if (aMsg.length < 3)
			{
				player.player.sendMessage(npc.getName() + " says to you, 'buy [itemid] [amount]'");
				return;
			} else {
				if (Integer.parseInt(aMsg[2]) > 0)
				{
					int amount = Integer.parseInt(aMsg[2]);
					int found = 0;
					double totalcost = 0;
					if (shop.size() > 0)
					{
						
						for (myShopItem item : shop)
						{
							if (item.item.getTypeId() == Integer.parseInt(aMsg[1]))
							{
								
								//player.player.sendMessage(npc.getName() + " says to you, 'Hmm: " + item.item.getTypeId() + " is worth "+ (item.price+item.price*0.10) +" coin each'");
								
									found++;
									double cost = ((item.price * 1.10) * item.item.getAmount());
									this.coin = this.coin + cost;
									totalcost = totalcost + cost;
									amount = amount - item.item.getAmount();
									player.player.getInventory().addItem(item.item);
									shop.remove(item);
									player.player.sendMessage(npc.getName() + " says to you, '" + cost + " coins for this stack.'");
									
									
								
							} else {
								// ignore this type
								
							}
						}
						if (found < 1)
						{
							player.player.sendMessage(npc.getName() + " says to you, 'Sorry, out of stock in that item.");
						}
						
						if (totalcost > 0)
						{
							player.player.sendMessage(npc.getName() + " says to you, 'Thanks, " + totalcost + " coins.'");
							Account account = iConomy.getBank().getAccount(player.name);
							account.subtract(totalcost);
							
						}
						
						
						
					} else {
						player.player.sendMessage(npc.getName() + " says to you, 'Sorry, totally out of stock!'");
					}
				
				}
			}
			
			return;
			
		}
		
		// Unknown command
		player.player.sendMessage(npc.getName() + " says to you, 'Sorry, can i [help] you?'");


	}
}
