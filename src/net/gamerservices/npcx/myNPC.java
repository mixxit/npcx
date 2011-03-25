package net.gamerservices.npcx;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.CreatureType;
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
	public double coin = 250000;
	public HashMap<String, myTriggerword> triggerwords = new HashMap<String, myTriggerword>();
	public int chest = 0;
	public int legs = 0;
	public int helmet = 0;
	public int weapon = 0;
	public int boots = 0;
	public List< myHint > hints = new CopyOnWriteArrayList< myHint >();
	public myPathgroup pathgroup;
	public int currentpathspot = 0;
	public int movecountdown = 0;
			
	myNPC(npcx parent, HashMap<String, myTriggerword> triggerwords)
	{
		this.parent = parent;
		this.triggerwords = triggerwords;
	}
	
	public void onPlayerAggroChange(myPlayer myplayer)
	{
		int count = 0;
		
		// do i already ahve aggro?
		for (myTriggerword tw : triggerwords.values())
			{
				
				if (tw.word.toLowerCase().contains("attack"))
				{
					String send = variablise(tw.response,myplayer.player);
					myplayer.player.sendMessage(npc.getName() + " says to you, '" + send + "'");
					count++;
					return;
				}
				
				
		
		}
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
						String npcattack = "NPCATTACK";
						String summonplayer = "NPCSUMMONPLAYER";
						String npcsummonzombie = "NPCSUMMONZOMBIE";
						
						
						// NPCATTACK variable
						if (tw.response.toLowerCase().contains(npcattack.toLowerCase()))
						{
								myplayer.player.sendMessage(npc.getName() + " says to you, 'You will regret that!'");
								npc.aggro = myplayer.player;
								npc.follow = myplayer.player;
								return;

						}

						// NPCSUMMONPLAYER
						if (tw.response.toLowerCase().contains(summonplayer.toLowerCase()))
						{
								double x = npc.getBukkitEntity().getLocation().getX();
								double y = npc.getBukkitEntity().getLocation().getY();
								double z = npc.getBukkitEntity().getLocation().getZ();
								Location location = new Location(npc.getBukkitEntity().getLocation().getWorld(), x, y, z);
								
								myplayer.player.teleportTo(location);
								return;

						}

						// NPCSUMMONMOB
						if (tw.response.toLowerCase().contains(npcsummonzombie.toLowerCase()))
						{
							double x = myplayer.player.getLocation().getX();
							double y = myplayer.player.getLocation().getY();
							double z = myplayer.player.getLocation().getZ();
							Location location = new Location(npc.getBukkitEntity().getLocation().getWorld(), x, y, z);
							
							
							npc.getBukkitEntity().getWorld().spawnCreature(location,CreatureType.ZOMBIE);
							
							myplayer.player.sendMessage(npc.getName() + " says to you, 'Look out!'");
							return;

						}

						
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
			newresponse = response.replaceAll("bankbalance", Float.toString((float)account.getBalance()));
		}
		
		if (response.contains("playername"))
		{
			//System.out.println("Replacing bankbalance variable");
			Account account = iConomy.getBank().getAccount(player.getName());
			newresponse = response.replaceAll("playername", player.getName());
		}

		
		return newresponse;
	}
	
	public double checkHints(int id)
	{
		for (myHint hint : hints)
		{
			if (hint.id == id)
			{
				hint.age++;
				return (float)hint.price;
				
			}
		}
		// return basic price
		return 1;
		
	}



	public void parseShop(myPlayer player, String message) 
	{
		// TODO Auto-generated method stub
		//myplayer.player.sendMessage("Parsing:" + message + ":" + Integer.toString(this.triggerwords.size()));
		String message2=message+" ";
		String[] aMsg = message.split(" ");
		int size = aMsg.length;
		//player.player.sendMessage("DEBUG: " + size);
		if (aMsg[0].toLowerCase().matches("help"))
		{
						
			player.player.sendMessage(npc.getName() + " says to you, 'What do you need? [list], [sell] or [buy]'");
			return;
		}
		
		if (aMsg[0].toLowerCase().matches("list"))
		{
			boolean match = false;
			
			
			if (size == 2)
 		    {
				match = true;
 		    }
				
			int count = 0;
			for (myShopItem item : shop)
			{
				count++;
				if (match == true)
				{
					if (item.item.getType().name().contains(aMsg[1]))
					{
						player.player.sendMessage(npc.getName() + " says to you, "+ item.item.getType().name() + " x " + item.item.getAmount() + " selling at " + (float)checkHints(item.item.getTypeId()) + " before commision");
					}
				} else {
					player.player.sendMessage(npc.getName() + " says to you, "+ item.item.getType().name() + " x " + item.item.getAmount() + " selling at " + (float)checkHints(item.item.getTypeId()) + " before commision");
				}
			}
			player.player.sendMessage(npc.getName() + " says to you, '" + count + " items in the shop.'");
			
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
				
				
				try 
				{
				item.setTypeId(Material.matchMaterial(aMsg[1]).getId());
				} catch (NullPointerException e)
				{
					player.player.sendMessage(npc.getName() + " says to you, 'Hmm try another item similar named to "+aMsg[1]+" and i might be interested'");
					e.printStackTrace();
					return;
				
				} catch (Exception e)
				{
					player.player.sendMessage(npc.getName() + " says to you, 'Hmm try another item similar named to "+aMsg[1]+" and i might be interested'");
					e.printStackTrace();
					return;
					
				}
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
					totalcoins = (float)(item.getAmount() * checkHints(shopitem.item.getTypeId()) * 0.80);
					
					if (this.coin >= totalcoins)
					{
						player.player.getInventory().removeItem(item);
						shop.add(shopitem);
						player.player.sendMessage(npc.getName() + " says to you, 'Thanks! Heres your " + (float)totalcoins + " coins.'");
						Account account = iConomy.getBank().getAccount(player.name);
						this.coin = (float)this.coin - (float)totalcoins;
						account.add(totalcoins);
					} else {
						player.player.sendMessage(npc.getName() + " says to you, 'Sorry, I only have: "+(float)this.coin+" and thats worth "+(float)totalcoins+"!'");
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
					int originalamount = Integer.parseInt(aMsg[2]);
					int found = 0;
					double totalcost = 0;
					List< myShopItem > basket = new CopyOnWriteArrayList< myShopItem >();
					if (shop.size() > 0)
					{
						
						for (myShopItem item : shop)
						{
							try 
							{
							
								if (item.item.getTypeId() == Material.matchMaterial(aMsg[1]).getId())
								{
									
									//player.player.sendMessage(npc.getName() + " says to you, 'Hmm: " + item.item.getTypeId() + " is worth "+ (item.price+item.price*0.10) +" coin each'");
									
										found++;
										
										
										double cost = ((checkHints(item.item.getTypeId()) * 1.10) * item.item.getAmount());
										if (cost > 0)
										{
											if (amount != 0)
											{
											
												if (item.item.getAmount() <= amount)
												{
													this.coin = (float)this.coin + (float)cost;
													totalcost = (float)totalcost + (float)cost;
													amount = amount - item.item.getAmount();
													
													shop.remove(item);
													basket.add(item);
													
													player.player.sendMessage(npc.getName() + " says to you, '" + (float)cost + " coins for this stack.'");
												} else {
													this.coin = (float)this.coin + (float)cost;
													totalcost = (float)totalcost + (((float)cost/item.item.getAmount())*amount);
													
													myShopItem i = new myShopItem();
													i.price = (((float)cost/item.item.getAmount())*amount);
													ItemStack is = new ItemStack(item.item.getType());
													i.item = is;
													is.setAmount(amount);
													is.setTypeId(item.item.getTypeId());
													
													amount = 0;
													basket.add(i);
													item.item.setAmount(item.item.getAmount()-i.item.getAmount());
													player.player.sendMessage(npc.getName() + " says to you, '" + (float)cost + " coins for this stack.'");
													
													
												}
											}
										}
										
									
								} else {
									// ignore this type
									
								}
							
							} catch (NullPointerException e)
							{
								player.player.sendMessage(npc.getName() + " says to you, 'Hmm try another item similar named to "+aMsg[1]+" and i might be interested'");
								return;
							}
						}
						
						if (found < 1)
						{
							// nothing was ever placed in a basketm, can return
							player.player.sendMessage(npc.getName() + " says to you, 'Sorry, out of stock in that item.");
							return;
						}
						
						if (totalcost > 0)
						{
							Account account = iConomy.getBank().getAccount(player.name);
							
							if (account.hasEnough((float)totalcost))
							{
								for (myShopItem i : basket)
								{
									player.player.getInventory().addItem(i.item);
									basket.remove(i);
								}
								
								player.player.sendMessage(npc.getName() + " says to you, 'Thanks, " + (float)totalcost + " coins.'");
								
								account.subtract((float)totalcost);
								double each = totalcost / originalamount;
															
								// update hints
								updateHints(Material.matchMaterial(aMsg[1]).getId(),each);
								return;
							} else {
								for (myShopItem i : basket)
								{
									shop.add(i);
									basket.remove(i);
								}
								player.player.sendMessage(npc.getName() + " says to you, 'Sorry, you don't have enough (" + (float)totalcost + " coins).'");
								return;

							}
						}
						
					} else {
						player.player.sendMessage(npc.getName() + " says to you, 'Sorry, totally out of stock!'");
						return;
					}
				
				}
			}
		}
		
		// Unknown command
		parseChat(player,message);
		player.player.sendMessage(npc.getName() + " says to you, 'Sorry, can i [help] you?'");
		
		return;
			
	}



	private void updateHints(int parseInt, double each) {
		// TODO Auto-generated method stub
		
		// is it old?
		int age = 0;
		
		for (myHint hint : this.hints)
		{
			if (hint.id == parseInt)
			{
				age = hint.age;
				hints.remove(hint);
			}
		}
		
			myHint h = new myHint();
			h.id = parseInt;
			h.price = (float)each;
			h.age = 0;
			this.hints.add(h);
	}
}
