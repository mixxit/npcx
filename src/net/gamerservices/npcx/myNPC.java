package net.gamerservices.npcx;
import java.awt.PageAttributes.ColorType;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.gamerservices.npclibfork.BasicHumanNpc;
import net.gamerservices.npclibfork.CHumanNpc;
import net.gamerservices.npclibfork.NpcSpawner;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class myNPC {
	public npcx parent;
	public String name = "dummy";
	public String id = "0";
	String category = "container";
	public BasicHumanNpc npc;
	public myFaction faction;
	public mySpawngroup spawngroup;
	public myLoottable loottable;
	public myMerchant merchant;
	public int coin = 250000;
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
	public boolean moveforward = true;
			
	myNPC(npcx parent, HashMap<String, myTriggerword> triggerwords, Location location, String name)
	{
		this.name = name;
		
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
					say(myplayer, send);
					count++;
					return;
				}
				
				
		
		}
	}
	
	
	
	private void say(myPlayer myplayer, String string) {
		// TODO Auto-generated method stub
		if (npc != null)
		{
			if (myplayer.player != null)
			{
				myplayer.player.sendMessage(npc.getName()+" says to you, '"+string+"'");
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
			// this needs to be removed			
			if(count == 0)
			{
				if (triggerwords != null)
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
									say(myplayer,"You will regret that");
									npc.setAggro(myplayer.player);
									npc.setFollow(myplayer.player);
									return;
	
							}
	
							// NPCSUMMONPLAYER
							if (tw.response.toLowerCase().contains(summonplayer.toLowerCase()))
							{
									say(myplayer,"Come here");
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
								
								say(myplayer,"Look out");
								return;
							}
	
							
							String send = variablise(tw.response,myplayer.player);
							say(myplayer,send);
							count++;
							return;
		
						}
						
						
						size++;
					}
				
				}
			}
		}
		if (count == 0 && size == 0)
		{
			// too spammy
			//say(myplayer,"I'm sorry. I'm rather busy right now.");
			
			
			
		} else {
				int count2 = 0;
				for (myTriggerword tw : triggerwords.values())
				{
					//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
					if (tw.word.toLowerCase().contains("default") && size > 0 && count == 0)
					{
						String send = variablise(tw.response,myplayer.player);
						
						say(myplayer,send);
						count2++;
						return;
					}
					
					
				}
				if (count2 == 0)
				{
					// too spammy
					//say(myplayer,"I'm sorry. I'm rather busy right now.");
					
				}
				
		}
	}

	private void parseChatGlobalCommands(myPlayer myplayer, String message) {
		// TODO Auto-generated method stub
		// Checks a message for any global commands
		// These are commands all npcs can support
		
		//myplayer.player.sendMessage("Parsing:" + message + ":" + Integer.toString(this.triggerwords.size()));
		String message2=message+" ";
		String arg1 = "";
		String arg2 = "";
		String arg3 = "";
		
		int ocount = 0;
		String words2 = "";
		for (String word : message2.split(" "))
		{
			if (ocount == 0)
			{
				arg1 = word;
				words2+="["+word+"]";
			}
			if (ocount == 1)
			{
				arg2 = word;
				words2+="["+word+"]";

			}
			
			if (ocount == 2)
			{
				arg3 = word;
				words2+="["+word+"]";

			}
			ocount++;
		}
		myplayer.player.sendMessage(words2);
		
		
		
		//
		// Give command
		// 
		if (arg1.matches("give"))
		{
			
			if (arg2.matches("") || arg3.matches(""))
			{
				say(myplayer,"give [itemid] [amount]");
				return;	
			}
			
			

			int amount = 0;
			try
			{
				 amount = Integer.parseInt(arg3);
				
			} catch (NumberFormatException e)
			{
				say(myplayer,"That is not a valid amount.");
				return;
			}
			
			if (amount < 1)
			{
				say(myplayer,"Hmm that's not enough.");
				//e.printStackTrace();
				return;
			}	
			
			
			ItemStack item = new ItemStack(0);
			try 
			{
				item.setTypeId(Material.matchMaterial(arg2).getId());
			} catch (NullPointerException e)
			{
				say(myplayer,"Hmm try another item similar named to "+arg2+" and i might be interested.");
				//e.printStackTrace();
				return;
			
			} catch (Exception e)
			{
				say(myplayer,"Hmm try another item similar named to "+arg2+" and i might be interested.");
				//e.printStackTrace();
				return;
			}
			
			try
			{
				item.setAmount(Integer.parseInt(arg3));
				
			} catch (NumberFormatException e)
			{
				say(myplayer,"That is not a valid amount.");
				return;
			}
			
			int count = 0;
			for (ItemStack curitem : myplayer.player.getInventory().getContents())
			{
				if (curitem.getTypeId() == item.getTypeId())
				{
					count = count + curitem.getAmount();
					//player.player.sendMessage(npc.getName() + " says to you, '"+ curitem.getTypeId() +"/"+curitem.getAmount() +"'");
				}
				
				
			}
					
			if (count >= item.getAmount())
			{		
				say(myplayer,"Hmm! "+ item.getAmount() +" " + item.getType().name() + "! Thanks!");	
				myplayer.player.getInventory().removeItem(item);
				this.onReceiveItem(myplayer,item);
				// Fire event
				
				return;
			} else {
				say(myplayer,"Sorry, you only have: "+count+" !");
				return;
			}
			
		} else {
				// global help?
		}
		
		//
		// End Give command
		// 
		
	}

	private void onReceiveItem(myPlayer p, ItemStack item) {
		// TODO Auto-generated method stub
		
		if (triggerwords != null)
		{
			int count2 = 0;
			for (myTriggerword tw : triggerwords.values())
			{
				if (tw.word.toLowerCase().matches("event_receive"+item.getType().getId()))
				{
					String send = variablise(tw.response,p.player);
					
					say(p,send);
					
					count2++;
					
				}
			}
			if (count2 == 0)
			{
				// If i dont have a triggerword tell them thanks
				p.player.sendMessage(npc.getName() + " says to you, 'Thanks! I'll find some use for that.'");
			}
		} else {
			// Either a standard spawn or has no triggers!
		}
		
	}

	public void onPlayerChat(myPlayer myplayer, String message, String category)
	{
		parseChatGlobalCommands(myplayer, message);
		if (category.matches("merchant"))
		{
			parseMerchant(myplayer, message);
			parseChat(myplayer,message);
		
		} else {
			parseChat(myplayer,message);
		}
		
		
			
				
	}



	public void parseMerchant(myPlayer player, String message) 
	{
		// TODO Auto-generated method stub
		//myplayer.player.sendMessage("Parsing:" + message + ":" + Integer.toString(this.triggerwords.size()));
		String message2=message+" ";
		String[] aMsg = message.split(" ");
		int size = aMsg.length;
		
		// Help Command
		
		//player.player.sendMessage("DEBUG: " + size);
		if (aMsg[0].toLowerCase().matches("help"))
		{
						
			say(player,"What do you need? "+ChatColor.LIGHT_PURPLE +"[list], [sell]"+ChatColor.WHITE+" or "+ChatColor.LIGHT_PURPLE+"[buy]");
			return;
		}
		
		// List Command
		
		if (aMsg[0].toLowerCase().matches("list"))
		{
			if (this.merchant != null)
			{
				int mysize = this.merchant.merchantentries.size();
				say(player,"Checking my list of: " + ChatColor.LIGHT_PURPLE + mysize);
				boolean match = false;
				
	
				
				if (size == 2)
	 		    {
					match = true;
	 		    }
					
				int count = 0;
				if (this.merchant != null)
				{
					if (this.merchant.merchantentries != null)
					{
						for (myMerchant_entry item : this.merchant.merchantentries)
						{
							count++;
							if (match == true)
							{
								try
								{
									if ( Material.matchMaterial(aMsg[1]).getId() == item.itemid)
									{
										if (!this.merchant.category.equals("nolimit"))
										{
											player.player.sendMessage(ChatColor.LIGHT_PURPLE + ""+ Material.matchMaterial(aMsg[1]) + ChatColor.WHITE + " x " + item.amount + " S: " + ChatColor.YELLOW + "$"+ item.pricesell + ChatColor.WHITE + " B: $" + ChatColor.YELLOW+ "$"+ item.pricebuy);
										} else {
											player.player.sendMessage(ChatColor.LIGHT_PURPLE + ""+ Material.matchMaterial(aMsg[1]) + ChatColor.WHITE + " S:" + ChatColor.YELLOW + "$" + item.pricesell + ChatColor.WHITE + " B:" + ChatColor.YELLOW + "$" + item.pricebuy);
											
										}
									}
								} catch (NullPointerException e)
								{
									say(player,"Couldn't find any items matching the name you requested");
								}
							} else {
								if (this.merchant != null)
								{
									if (this.merchant.category != null)
									{
										if (!this.merchant.category.equals("nolimit"))
										{
											player.player.sendMessage(item.itemid + "("+ChatColor.LIGHT_PURPLE + ""+ Material.matchMaterial(Integer.toString(item.itemid)).toString()+ ChatColor.WHITE +") x " + item.amount + " S: " + ChatColor.YELLOW + "$" + item.pricesell + ChatColor.WHITE + " B: " + ChatColor.YELLOW+"$" + item.pricebuy);
										} else {
											player.player.sendMessage(item.itemid + "("+ChatColor.LIGHT_PURPLE + ""+ Material.matchMaterial(Integer.toString(item.itemid)).toString()+ ChatColor.WHITE +") S:" + ChatColor.YELLOW +"$" + item.pricesell + ChatColor.WHITE + " B:" + ChatColor.YELLOW +"$" + item.pricebuy);
										}
									} else {
										player.player.sendMessage(item.itemid + "("+ChatColor.LIGHT_PURPLE + ""+Material.matchMaterial(Integer.toString(item.itemid)).toString()+ ChatColor.WHITE+") x " + item.amount + " S:" + ChatColor.YELLOW + "$" +item.pricesell + ChatColor.WHITE + " B:" + ChatColor.YELLOW + "$" +item.pricebuy);
									}
								}
							}
						}
						
					}
				}
				player.player.sendMessage(ChatColor.LIGHT_PURPLE + "" + count + ChatColor.WHITE + " items in the Merchant.");
				
				return;
			}

			
		}
		
		// Sell Command
		
		if (aMsg[0].toLowerCase().matches("sell"))
		{
			if (aMsg.length < 3)
			{		
				say(player,"sell " + ChatColor.LIGHT_PURPLE + "[itemid] [amount]");
				return;
			} else {
				
				myMerchantItem Merchantitem = new myMerchantItem();
				ItemStack item = new ItemStack(0);
				Merchantitem.item = item;
				// todo price
				int amount = 0;
				try
				{
					 amount = Integer.parseInt(aMsg[2]);
					
				} catch (NumberFormatException e)
				{
					say(player,"That is not a valid amount.");
					return;
				}
				if (amount < 1)
				{
					say(player,"Hmm that's not enough.");
					//e.printStackTrace();
					return;
				}			
					
				try 
				{
					item.setTypeId(Material.matchMaterial(aMsg[1]).getId());
				} catch (NullPointerException e)
				{
					// lol
					say(player,"Hmm try another item similar named to "+ChatColor.LIGHT_PURPLE + aMsg[1]+ChatColor.WHITE+" and i might be interested.");

					say(player,"Hmm try another item similar named to "+ChatColor.LIGHT_PURPLE + aMsg[1]+ChatColor.WHITE+" and i might be interested.");
					//e.printStackTrace();
					return;
				
				} catch (Exception e)
				{
					this.parent.sendPlayerItemList(player.player);
					say(player,"Hmm try another item similar named to "+ChatColor.LIGHT_PURPLE + aMsg[1]+ChatColor.WHITE+" and i might be interested.");
					//e.printStackTrace();
					return;
					
				}
				
				try
				{
					item.setAmount(Integer.parseInt(aMsg[2]));
					
				} catch (NumberFormatException e)
				{
					say(player,"That is not a valid amount.");
					return;
				}
				
				
				
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
					
					say(player,"Ok thats "+ ChatColor.YELLOW+item.getAmount() + ChatColor.WHITE + " out of your "+ ChatColor.YELLOW + count + ChatColor.WHITE +".");
					int totalcoins = 0;
					int buysat = getMerchantPriceBuyAt(Merchantitem.item.getTypeId());
					if (buysat == 0)
					{
						say(player,"Sorry, I am just not interested in that!");
						return;
					}
					totalcoins = (item.getAmount() * buysat);
					if (this.merchant.category != null && !this.merchant.category.matches("nolimit"))
					{
					
						if (this.coin >= totalcoins)
						{
							
							for (myMerchant_entry entry : merchant.merchantentries)
							{
								if (entry.itemid == Material.matchMaterial(aMsg[1]).getId())
								{
									player.player.getInventory().removeItem(item);
									entry.amount = entry.amount + item.getAmount();
									say(player,"Thanks! Heres your " + ChatColor.YELLOW+ totalcoins + ChatColor.WHITE+ " coins.");
									
									this.coin = this.coin - totalcoins;
									player.addPlayerBalance(player.player,totalcoins);
									return;
								}
							}
							
							say(player,"Sorry looks like I no longer need that item.");
							return;
	
							
						} else {
							say(player,"Sorry, I only have: "+ChatColor.YELLOW+this.coin+ChatColor.WHITE+" and thats worth "+ChatColor.YELLOW+totalcoins+ChatColor.WHITE+"!");
							return;
						}
					} else {
						
						// unlimited coin and amounts on this merchant
						for (myMerchant_entry entry : merchant.merchantentries)
						{
							if (entry.itemid == Material.matchMaterial(aMsg[1]).getId())
							{
								player.player.getInventory().removeItem(item);
								entry.amount = entry.amount + item.getAmount();
								say(player,"Thanks! Heres your " + ChatColor.YELLOW+ totalcoins + ChatColor.WHITE+ " coins.");
								
								this.coin = this.coin - totalcoins;
								player.addPlayerBalance(player.player,totalcoins);
								return;
							}
						}
						
						say(player,"Sorry looks like I no longer need that item.");
						return;
						
					}
					
				} else {
					
					say(player,"Sorry, you only have: "+ChatColor.YELLOW+count+ChatColor.WHITE+" !");
					return;
				}
			}
			
		}
		
		// Buy Command
		
		if (aMsg[0].toLowerCase().matches("buy"))
		{
			if (aMsg.length < 3)
			{
				say(player,"buy " + ChatColor.LIGHT_PURPLE+ "[itemid] [amount]");
				return;
			} else {
				
				try
				{
				int a = Integer.parseInt(aMsg[2]);
				} catch (NumberFormatException e)
				{
					say(player,"That is not a valid amount.");
					return;
				}
				
				
				if (Integer.parseInt(aMsg[2]) > 0)
				{
					int amount = Integer.parseInt(aMsg[2]);
					int itemid = Material.matchMaterial(aMsg[1]).getId();
					
					if (this.merchant.category != null)
					{
						
					}
					
					if (this.merchant.category == null)
					{
						if (getMerchantAmount(itemid) >= Integer.parseInt(aMsg[2]))
						{
							for (myMerchant_entry entry : merchant.merchantentries)
							{
								if (entry.itemid == itemid)
								{
									ItemStack i = sellMerchantItem(entry.itemid,amount);
										
									if (i != null)
									{
										
										int cost = entry.pricesell * amount;
										if (cost <= player.getPlayerBalance(player.player))
										{
											player.player.getInventory().addItem(i);
											say(player,"Thanks! That's " + ChatColor.YELLOW+ cost +ChatColor.WHITE+ " total coins!");
											player.subtractPlayerBalance(player.player,cost);		
											return;
										} else {
											say(player,"You don't have enough!!");
											
											return;
										}
										
										
									} else {
										// hmm, didnt get an item back
										say(player,"Sorry, looks like that item just sold!");
										return;
									}
								}
							}
							
							
							
						} else {
							say(player,"Sorry, out of stock in that item. Have you tried our "+ChatColor.LIGHT_PURPLE+"[list]"+ChatColor.WHITE+"?");
							return;
						}
					} else {
						
						if (this.merchant.category.equals("nolimit"))
						{
						
							for (myMerchant_entry entry : merchant.merchantentries)
							{
								if (entry.itemid == itemid)
								{
									ItemStack i = sellMerchantItem(entry.itemid,amount);
										
									if (i != null)
									{
										
										int cost = entry.pricesell * amount;
										if (cost <= player.getPlayerBalance(player.player))
										{
											player.player.getInventory().addItem(i);
											say(player,"Thanks! That's " + ChatColor.YELLOW+ cost + ChatColor.WHITE+" total coins!");
											player.subtractPlayerBalance(player.player,cost);		
											return;
										} else {
											say(player,"You don't have enough!!");
											
											return;
										}
										
										
									} else {
										// hmm, didnt get an item back
										say(player,"Sorry, looks like that item just sold!");
										return;
									}
								}
							}
						} else {
							// Do above
							
							//TODO This needs to move into a seperate function
							if (getMerchantAmount(itemid) >= Integer.parseInt(aMsg[2]))
							{
								for (myMerchant_entry entry : merchant.merchantentries)
								{
									if (entry.itemid == itemid)
									{
										ItemStack i = sellMerchantItem(entry.itemid,amount);
											
										if (i != null)
										{
											
											int cost = entry.pricesell * amount;
											if (cost <= player.getPlayerBalance(player.player))
											{
												player.player.getInventory().addItem(i);
												say(player,"Thanks! That's " + ChatColor.YELLOW+cost + ChatColor.WHITE+" total coins!");
												player.subtractPlayerBalance(player.player,cost);		
												return;
											} else {
												say(player,"You don't have enough!!");
												
												return;
											}
											
											
										} else {
											// hmm, didnt get an item back
											say(player,"Sorry, looks like that item just sold!");
											return;
										}
									}
								}
								
								
								
							} else {
								say(player,"Sorry, out of stock in that item. Have you tried our "+ChatColor.LIGHT_PURPLE+"[list]"+ChatColor.WHITE+"?");
								return;
							}
							
						}
					}
				} else {
					say(player,"Sorry that's not enough.");
				}
			}
		}
		
		// Unknown command
		say(player,"Sorry, can i "+ChatColor.LIGHT_PURPLE+"[help]"+ChatColor.WHITE+" you?");
		
		return;
			
	}

	private ItemStack sellNolimitMerchantItem(int itemid,int amount) {
		// TODO Auto-generated method stub
		// find item
		if (merchant != null)
		{
			if  (merchant.merchantentries != null)
			{
				//System.out.println("Found entries!");
				for (myMerchant_entry entry : merchant.merchantentries)
				{
					if (entry.itemid == itemid)
					{
						//System.out.println("Item matched!");
						
							ItemStack i = new ItemStack(itemid);
							i.setAmount(amount);
							// Update cache
							entry.amount = entry.amount;
							//System.out.println("About to sell: " + i.getType().name() + ":"+ entry.amount);
							return i;
						
					}
				}
			} else {
				//System.out.println("This merchant has no entries");
			
			}
			
			
			
		} else {
			//System.out.println("This isnt a mechant");
		}
		
		return null;
	}

	private ItemStack sellMerchantItem(int itemid,int amount) {
		// TODO Auto-generated method stub
		// find item
		if (merchant != null)
		{
			if  (merchant.merchantentries != null)
			{
				//System.out.println("Found entries!");
				for (myMerchant_entry entry : merchant.merchantentries)
				{
					if (entry.itemid == itemid)
					{
						//System.out.println("Item matched!");
						if (merchant.category == null)
						{
							if (entry.amount >= amount)
							{
								ItemStack i = new ItemStack(itemid);
								i.setAmount(amount);
								// Update cache
								
								entry.amount = entry.amount - amount;
								//System.out.println("About to sell: " + i.getType().name() + ":"+ entry.amount);
								return i;
							} else {
								//System.out.println("Not enough ["+entry.amount+"] compared to your ["+amount+"]!");
								return null;				
							}
						} else {
							
							if (!merchant.category.equals("nolimit"))
							{
								// TODO - NEEDS TO MOVE
								if (entry.amount >= amount)
								{
									ItemStack i = new ItemStack(itemid);
									i.setAmount(amount);
									// Update cache
									
									entry.amount = entry.amount - amount;
									//System.out.println("About to sell: " + i.getType().name() + ":"+ entry.amount);
									return i;
								} else {
									//System.out.println("Not enough ["+entry.amount+"] compared to your ["+amount+"]!");
									return null;				
								}
							} else {
							
							// UNLIMITED 
							ItemStack i = new ItemStack(itemid);
							i.setAmount(amount);
							// Update cache
							
							
							//System.out.println("About to sell: " + i.getType().name() + ":"+ amount);
							return i;
							}
						}
					}
				}
			} else {
				//System.out.println("This merchant has no entries");
			
			}
			
			
			
		} else {
			//System.out.println("This isnt a mechant");
		}
		
		return null;
	}

	private int getMerchantPriceBuyAt(int typeId) {
		// TODO Auto-generated method stub
		int amount = 0;
		
		if (merchant != null)
		{
			if  (merchant.merchantentries != null)
			{
				for (myMerchant_entry entry : merchant.merchantentries)
				{
					if (entry.itemid == typeId)
					{
						return entry.pricebuy;
					}
				}
			}
		}
		return amount;
	}

	private int getMerchantPriceSellAt(int typeId) {
		// TODO Auto-generated method stub
		int amount = 0;
		
		if (merchant != null)
		{
			if  (merchant.merchantentries != null)
			{
				for (myMerchant_entry entry : merchant.merchantentries)
				{
					if (entry.itemid == typeId)
					{
						return entry.pricesell;
					}
				}
			}
		}
		return amount;
	}

	private int getMerchantAmount(int itemid) {
		// TODO Auto-generated method stub
		int amount = 0;
		
		if (merchant != null)
		{
			if  (merchant.merchantentries != null)
			{
				for (myMerchant_entry entry : merchant.merchantentries)
				{
					if (entry.itemid == itemid)
					{
						amount =+ entry.amount;
					}
				}
			}
		}
		return amount;
	}

	private String variablise(String response, Player player) {
		// TODO Auto-generated method stub
		
		myPlayer mp = this.parent.universe.getmyPlayer(player);
		
		int balance = 0;
		if (mp != null)
		{
			balance = mp.getPlayerBalance(player);
		} else {
			System.out.println("npcx : Can't find "+player.getName()+" in the directory");

		}
		
		
		String newresponse = response;
		if (response.contains("bankbalance"))
		{
			//System.out.println("Replacing bankbalance variable");
			
			newresponse = response.replaceAll("bankbalance", ChatColor.YELLOW+Integer.toString(balance)+ChatColor.WHITE);
		}
		
		if (response.contains("playerbalance"))
		{
			//System.out.println("Replacing bankbalance variable");
			
			newresponse = response.replaceAll("playerbalance", ChatColor.YELLOW+Integer.toString(balance)+ChatColor.WHITE);
		}
		
		if (response.contains("playerhealth"))
		{
			//System.out.println("Replacing bankbalance variable");
			newresponse = response.replaceAll("playerhealth", Integer.toString(player.getHealth()));
		}
		
		if (response.contains("playername"))
		{
			//System.out.println("Replacing bankbalance variable");
			
			newresponse = response.replaceAll("playername", player.getName());
		}

		
		return newresponse;
	}
	
	public int checkHints(int id)
	{
		for (myHint hint : hints)
		{
			if (hint.id == id)
			{
				hint.age++;
				return hint.price;
				
			}
		}
		// return basic price
		return 1;
		
	}

	private void updateHints(int parseInt, int each) {
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
			h.price = each;
			h.age = 0;
			this.hints.add(h);
	}

	public BasicHumanNpc Spawn(String id2, String name2,
			World world, double x, double y, double z, Double yaw, Double pitch) {
		// TODO Auto-generated method stub
		BasicHumanNpc hnpc = NpcSpawner.SpawnBasicHumanNpc(this,id2, name2, world, x, y, z,yaw , pitch);
		this.npc = hnpc;
		this.npc.getBukkitEntity().setHealth(this.npc.hp);
        
		return hnpc;
	}

	public void Delete() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	

	public void onRightClick(Player p) {
		// TODO Auto-generated method stub
		for (myPlayer player : parent.universe.players.values()){
			if (player.player == p)
			{

	    		double x1 = p.getLocation().getX();
	    		double y1 = p.getLocation().getY();
	    		double z1 = p.getLocation().getZ();
	    		
	    		double x2 = this.npc.getBukkitEntity().getLocation().getX();
	    		double y2 = this.npc.getBukkitEntity().getLocation().getY();
	    		double z2 = this.npc.getBukkitEntity().getLocation().getZ();
	    		int xdist = (int) (x1 - x2);
	    		int ydist = (int) (y1 - y2);
	    		int zdist = (int) (z1 - z2);
	    		
	    		if ((xdist < -3 || xdist > 2) || (ydist < -3 || ydist > 3) || (zdist < -3 || zdist > 3))
	    		{
	    			// out of range to do this
	    			
	    			 p.sendMessage("You are too far away from this npc");
	                 player.target = null;
	                 return;
	    		}
				
				
				
				if (player.target != null)
				{
                    p.sendMessage("* Target cleared!");
                    player.target = null;
					
				} else {
					player.target = this.npc;
					
					int tNPCID = 0;
					int tGPID = 0;
					int tFID = 0;
					int tPGID = 0;
					int tLTID = 0;
					int tMID = 0;
					if (this.parent != null)
					{
						tNPCID = Integer.parseInt(this.id);
						
    					if (this.spawngroup != null)
    						tGPID = this.spawngroup.id;
    					if (this.faction != null)
    						tFID = this.faction.id;
    					if (this.pathgroup != null)
    						tPGID = this.pathgroup.id;
    					if (this.loottable != null)
    						tLTID = this.loottable.id;
    					if (this.merchant != null)
    						tMID = this.merchant.id;
    					
					}
					
					if (p.isOp())
					{
						p.sendMessage("NPCID ("+tNPCID+"):SG ("+tGPID+"):F ("+tFID+"):PG ("+tPGID+"):L ("+tLTID+"):M ("+tMID+")");
					}
                    p.sendMessage("* You are now chatting to: " + ChatColor.YELLOW + name + ChatColor.WHITE + ". Right Click to cancel.");
                    p.sendMessage("* Words in "+ChatColor.LIGHT_PURPLE+"[brackets]"+ChatColor.WHITE+" you should type! "+ChatColor.YELLOW+"Type a word to begin"+ChatColor.WHITE);
                    if (player.target != null && player.target.parent != null && player.target.parent.category != null)
                    {
                    	// check what type (category) of npc this is
                    	
                    	if (player.target.parent.category.matches("merchant"))
    					{
                    		// merchant
                            onPlayerChat(player, "Hello!","merchant");
    					} else {
    						// normal
    						onPlayerChat(player, "Hello!","");
    					}
                    	
                    } else {
                    	onPlayerChat(player, "Hello!","");
                    }

                    
				}
				
			} else {
				if (player.name == p.getName())
				{
					p.sendMessage("Error, your player is out of sync");
					
				}
				
			}
		}
		this.npc.forceMove(this.npc.getFaceLocationFromMe(p.getLocation(),true));

	}

	public void onClosestPlayer(Player p) {
		// TODO Auto-generated method stub
		
		if (this.parent.universe.isLivingEntityAnNPC(p))
		{
			// ignroe other npcs
			return;
		}
		
		if (triggerwords != null)
		{
			int count2 = 0;
			for (myTriggerword tw : triggerwords.values())
			{
				//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
				if (tw.word.toLowerCase().contains("event_close"))
				{
					String send = variablise(tw.response,p);
					
					for (myPlayer player : this.parent.universe.players.values())
					{
						if (p == player.player)
						{
							say(player,send + "'");
						}
					}
					
					count2++;
					
				}
			}
			if (count2 == 0)
			{
				// If i dont have a triggerword, dont respond
				//p.sendMessage(npc.getName() + " says to you, 'Watch your back.'");
			}
		
		// face target
		//this.npc.forceMove(this.npc.getFaceLocationFromMe(p.getLocation(),true));
		
		} else {
			// Either a standard spawn or has no triggers!
		}
	}

	public void onBounce(Player p) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		
		if (this.parent.universe.isLivingEntityAnNPC(p))
		{
			// ignroe other npcs
			return;
		}
		
		int count2 = 0;
		for (myTriggerword tw : triggerwords.values())
		{
			//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
			if (tw.word.toLowerCase().contains("event_bounce"))
			{
				String send = variablise(tw.response,p);
				
				for (myPlayer player : this.parent.universe.players.values())
				{
					if (p == player.player)
					{
						say(player,send + "'");
					}
				}
				
				count2++;
				
			}
		}
		if (count2 == 0)
		{
			// If i dont have a triggerword, dont respond
			p.sendMessage(npc.getName() + " says to you, 'Hey! Watch where you are going!'");
		}
		
		this.npc.forceMove(this.npc.getFaceLocationFromMe(p.getLocation(),true));
	}

	
	
	public void onDeathFromPlayer(LivingEntity p) {
		// TODO Auto-generated method stub
	
		if (this.parent.universe.isLivingEntityAnNPC(p))
		{
			// Killed by an npc
			return;
		}

		// above is to 
		// make sure this is just for a HUMAN player
		if (p instanceof Player)
		{
			int count2 = 0;
			for (myTriggerword tw : triggerwords.values())
			{
				//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
				if (tw.word.toLowerCase().contains("event_death"))
				{
					String send = variablise(tw.response,(Player)p);
					
					for (myPlayer player : this.parent.universe.players.values())
					{
						if (p == player.player)
						{
							say(player,send + "'");
						}
					}
					count2++;
				}
			}
			if (count2 == 0)
			{
				// If i dont have a triggerword, respond with
				((Player) p).sendMessage(npc.getName() + " says to you, 'I will be avenged for this!'");
			}
			
			((Player) p).sendMessage("You have slain " + this.name + "!");
			/*
			for (myPlayer pl : this.parent.universe.players.values())
			{
				if (pl.player == (Player)p)
				{
					
					pl.zomgcount++;
					if (pl.zomgcount == 10)
					{
						((Player) p).getServer().broadcastMessage("&3"+((Player) p).getName() + " has become a legend spoken of by people all over the world! (Slayed 10)");
						
					}
					
					if (pl.zomgcount == 500)
					{
						((Player) p).getServer().broadcastMessage("&3"+((Player) p).getName() + " is an unstoppable juggernaught (Slayed 50)");
						
					}
					
					if (pl.zomgcount == 1000)
					{
						((Player) p).getServer().broadcastMessage("&3"+"The divine being " + ((Player) p).getName() + " walks among us (Slayed 100)");
					}
					
				}
				
			}
			*/
			if (this.faction != null)
			{
				try
				{
					myPlayer player = this.parent.universe.findmyPlayerByPlayer((Player)p);
					player.updateFactionNegative(this.faction);
					((Player) p).sendMessage(ChatColor.YELLOW + "* Your standing with " + this.faction.name + " has gotten worse!");
				} catch (NullPointerException e)
				{
				}
			}
			
		}

	}

	public void onKilled(LivingEntity ent) {
		// TODO Auto-generated method stub
		
		if (this.parent.universe.isLivingEntityAnNPC(ent))
		{
			// dont want to generate stuff for player npcs atm
			return;
		}
		
		// make sure above is returned since a chumannpc can be a player
		
		if (ent instanceof Player)
		{
			int count2 = 0;
			for (myTriggerword tw : triggerwords.values())
			{
				//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
				if (tw.word.toLowerCase().contains("event_killed"))
				{
					String send = variablise(tw.response,(Player)ent);
					
					for (myPlayer player : this.parent.universe.players.values())
					{
						if (ent == player.player)
						{
							say(player,send + "'");
						}
					}
					count2++;
				}
			}
			if (count2 == 0)
			{
				// If i dont have a triggerword, respond with
				((Player) ent).sendMessage(npc.getName() + " says to you, 'Not as strong as I thought'");
			}
			
			
		}
	}

	public BasicHumanNpc Spawn(String id, String name, Location loc) {
		// TODO Auto-generated method stub
		World world = loc.getWorld();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		double yaw = loc.getYaw();
		double pitch = loc.getPitch();
		
		BasicHumanNpc hnpc = Spawn(id,name,world,x,y,z,yaw,pitch);
		return hnpc;
	}

	public int getDamageDone(BasicHumanNpc npc, Player player) {
		// TODO Auto-generated method stub
		// get weapon
		int damage = 1;
		if (player instanceof Player)
		{
			int itemid = ((Player) player).getInventory().getItemInHand().getTypeId();
			
			
			// SWORDS!
			if (itemid == 268)
			{
				damage=damage+ 5;
			}
			
			if (itemid == 272)
			{
				damage=damage+ 8;
			}
			
			if (itemid == 267)
			{
				damage=damage+ 12;
			}
			
			if (itemid == 276)
			{
				damage=damage+ 20;
			}
			
			if (itemid == 283)
			{
				damage=damage+ 30;
			}
			
			Random rn = new Random();
			int n = damage - (damage/2) + 1;
			int i = rn.nextInt() % n;
			int randomNum =  (damage/2) + i;

			Random rn2 = new Random();
			int n2 = 1000 - 1 + 1;
			int i2 = rn.nextInt() % n;
			int randomNum2 =  1 + i2;
			
			if (randomNum2 > 950)
			{
				player.getServer().broadcastMessage(ChatColor.YELLOW + player.getName() + " did critical damage against "+npc.getName()+"! For "+ (randomNum2 + randomNum) +"!");
				return randomNum2+randomNum;
			}
			
			
			return randomNum;
			
		}
		return damage;
		
	}

	public void onDeath() {
		// TODO Auto-generated method stub
		
	}
}
