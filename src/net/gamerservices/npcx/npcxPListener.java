package net.gamerservices.npcx;
import java.util.logging.Logger;

import net.gamerservices.npclibfork.BasicHumanNpc;
import net.gamerservices.npclibfork.NpcEntityTargetEvent;
import net.gamerservices.npclibfork.NpcSpawner;
import net.gamerservices.npclibfork.NpcEntityTargetEvent.NpcTargetReason;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;

public class npcxPListener extends PlayerListener {
	
	private final npcx parent;
	
	public npcxPListener(npcx parent) {
        this.parent = parent;
    }
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if (this.parent.universe.nations.matches("true"))
		{
			
			// natiosn chunk checking
			// Area Coordinate = round down ( ( position / areasize ) + 0.9375 )
			int xchunkloc = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getX());
			int zchunkloc = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getZ());
			//event.getPlayer().sendMessage(xchunkloc+":"+zchunkloc);
			
			int lastx = this.parent.universe.getPlayerLastChunkX(event.getPlayer());
			int lastz = this.parent.universe.getPlayerLastChunkZ(event.getPlayer());
			String lastname = this.parent.universe.getPlayerLastChunkName(event.getPlayer());
			//event.getPlayer().sendMessage("Zone: "+xchunkloc+":"+zchunkloc+" - from:"+ lastx + ":" + lastz);
				if (lastx != xchunkloc ||  lastz != zchunkloc) 
				{
					// new position!
					int x = xchunkloc;
					int z = zchunkloc;
					myZone zone = this.parent.universe.getZoneFromLoc(x,z,event.getPlayer().getWorld());
					if (zone != null)
					{
						if (lastname != null)
						{
							if (!zone.name.equals(lastname))
							{
								if (this.parent.universe.getPlayerToggle(event.getPlayer()))
								{
								
									if (zone.ownername.equals(""))
									{
										event.getPlayer().sendMessage("Zone: ["+ChatColor.LIGHT_PURPLE+""+xchunkloc+":"+zchunkloc+""+ChatColor.WHITE+"] - "+ChatColor.YELLOW+"for sale");
										
									} else {
										event.getPlayer().sendMessage("["+ChatColor.LIGHT_PURPLE+""+xchunkloc+":"+zchunkloc+""+ChatColor.WHITE+"] "+ChatColor.LIGHT_PURPLE +""+zone.name + ""+ChatColor.WHITE+" Owner: "+ChatColor.YELLOW+""+zone.ownername);
									}
								
								}
								this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
								this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
								this.parent.universe.setPlayerLastChunkName(event.getPlayer(),zone.name);
							
							} else {
								// skip we've been here recently
								this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
								this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
								this.parent.universe.setPlayerLastChunkName(event.getPlayer(),zone.name);
							}
						} else {
							// dont provide them info, just update them
							this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
							this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
							this.parent.universe.setPlayerLastChunkName(event.getPlayer(),zone.name);
						}
					}
				} else {
					// Already this value
				}
		}
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		if (this.parent.universe.nations.matches("true"))
		{
			
			// natiosn chunk checking
			// Area Coordinate = round down ( ( position / areasize ) + 0.9375 )
			int xchunkloc = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getX());
			int zchunkloc = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getZ());
			//event.getPlayer().sendMessage(xchunkloc+":"+zchunkloc);
			
			int lastx = this.parent.universe.getPlayerLastChunkX(event.getPlayer());
			int lastz = this.parent.universe.getPlayerLastChunkZ(event.getPlayer());
			String lastname = this.parent.universe.getPlayerLastChunkName(event.getPlayer());
			//event.getPlayer().sendMessage("Zone: "+xchunkloc+":"+zchunkloc+" - from:"+ lastx + ":" + lastz);
				if (lastx != xchunkloc ||  lastz != zchunkloc) 
				{
					// new position!
					int x = xchunkloc;
					int z = zchunkloc;
					myZone zone = this.parent.universe.getZoneFromLoc(x,z,event.getPlayer().getWorld());
					if (zone != null)
					{
						if (lastname != null)
						{
							if (!zone.name.matches(lastname))
							{
								if (zone.ownername.matches(""))
								{
									event.getPlayer().sendMessage("Zone: ["+ChatColor.LIGHT_PURPLE+""+xchunkloc+":"+zchunkloc+""+ChatColor.WHITE+"] - "+ChatColor.YELLOW+"for sale");
									
								} else {
									event.getPlayer().sendMessage("["+ChatColor.LIGHT_PURPLE+""+xchunkloc+":"+zchunkloc+""+ChatColor.WHITE+"] "+ChatColor.LIGHT_PURPLE +""+zone.name + ""+ChatColor.WHITE+" Owner: "+ChatColor.YELLOW+""+zone.ownername);
								}
								
								this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
								this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
								this.parent.universe.setPlayerLastChunkName(event.getPlayer(),zone.name);
							
							} else {
								// skip we've been here recently
							}
						} else {
							// dont provide them info, just update them
							this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
							this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
							this.parent.universe.setPlayerLastChunkName(event.getPlayer(),zone.name);
						}
					}
				} else {
					
				}
		}
	}
	 
	
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (!event.getPlayer().isOp())
		{
			if (this.parent.universe.nowild != null)
			{
				if (this.parent.universe.nowild.matches("true"))
				{
					try
					{
						for (myPlayer player : parent.universe.players.values())
						{
							if (player.player == event.getPlayer())
							{
								
								
								Location loc = event.getClickedBlock().getLocation();
								Chunk chunk = loc.getWorld().getChunkAt(loc);
								int x = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getX());
								int z = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getZ());
								String owner = "";
								int zoneid = 0;
								for (myZone zone : this.parent.universe.zones)
								{
									if (zone.x == x && zone.z == z)
									{
										owner = zone.ownername;
										zoneid = zone.id;
									}
								}
								
								if (player.player.getName().matches(owner))
								{
									return;
								} else {
									// Are they a member?
									if (zoneid != 0)
									{
										if (this.parent.universe.isZoneMember(zoneid, event.getPlayer().getName()))
										{
											return;
										}
									}
									event.getPlayer().sendMessage(ChatColor.RED+"You are not in the wild or in an area you own ("+x+":"+z+")!");
									event.setCancelled(true);
								}
							}
						}
					} catch (Exception e)
					{
						// locked table
						event.setCancelled(true);
					}
				
				} else {
					// False -  we dont want to our wild areas or are using some other system
					
					try
					{
						for (myPlayer player : parent.universe.players.values())
						{
							if (player.player == event.getPlayer())
							{
								
								
								Location loc = event.getClickedBlock().getLocation();
								Chunk chunk = loc.getWorld().getChunkAt(loc);
								int x = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getX());
								int zoneid = 0;
								
								int z = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getZ());
								String owner = "";
								for (myZone zone : this.parent.universe.zones)
								{
									if (zone.x == x && zone.z == z)
									{
										owner = zone.ownername;
										zoneid = zone.id;
									}
								}
								
								if (player.player.getName().matches(owner))
								{
									return;
								} else {
									if (owner.matches(""))
									{
										// wild is ok
										//event.getPlayer().sendMessage(ChatColor.RED+"You are not in the wild or in an area you own ("+x+":"+z+")!");
										return;
									} else {
										// Are they a member?
										if (zoneid != 0)
										{
											if (this.parent.universe.isZoneMember(zoneid, event.getPlayer().getName()))
											{
												return;
											}
										}
										event.getPlayer().sendMessage(ChatColor.RED+"You are not in the wild or in an area you own ("+x+":"+z+")!");
										event.setCancelled(true);
									}
								}
							}
						}
					} catch (Exception e)
					{
						// locked table
						event.setCancelled(true);
					}
				}
				
				
			} else {
				// No setting, assume we dont want to protect or are using some other system
				try
				{
					for (myPlayer player : parent.universe.players.values())
					{
						if (player.player == event.getPlayer())
						{
							
							
							Location loc = event.getClickedBlock().getLocation();
							Chunk chunk = loc.getWorld().getChunkAt(loc);
							int x = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getX());
							int z = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getZ());
							String owner = "";
							int zoneid = 0;
							for (myZone zone : this.parent.universe.zones)
							{
								if (zone.x == x && zone.z == z)
								{
									owner = zone.ownername;
									zoneid = zone.id;
								}
							}
							
							if (player.player.getName().matches(owner))
							{
								return;
							} else {
								if (owner.matches(""))
								{
									// wild is ok
									return;
								} else {
									// Are they a member?
									if (zoneid != 0)
									{
										if (this.parent.universe.isZoneMember(zoneid, event.getPlayer().getName()))
										{
											return;
										}
									}
									
									event.getPlayer().sendMessage(ChatColor.RED+"You are not in the wild or in an area you own ("+x+":"+z+")!");
									event.setCancelled(true);
								}
							}
						}
					}
				} catch (Exception e)
				{
					// locked table
					event.setCancelled(true);
				}
				return;
			}
		} else {
			// Is an Operator
			return;
		}
		// is not running Nations
		//event.setCancelled(true);
		
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) 
    {
		myPlayer player = new myPlayer(this.parent,event.getPlayer(),event.getPlayer().getName());

		
		
		if (this.parent.universe.nations.matches("true"))
		{
			// Area Coordinate = round down ( ( position / areasize ) + 0.9375 )
			int xchunkloc = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getX());
			int zchunkloc = this.parent.universe.getZoneCoord(event.getPlayer().getLocation().getZ());
			//event.getPlayer().sendMessage(xchunkloc+":"+zchunkloc);
			
			// new position!
			int x = xchunkloc;
			int z = zchunkloc;
			
			myZone zone = this.parent.universe.getZoneFromLoc(x,z,event.getPlayer().getWorld());
			if (zone != null)
			{
										
				this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
				this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
				this.parent.universe.setPlayerLastChunkName(event.getPlayer(),zone.name);
					
				
			} else {
				// dont provide them info, just update them
				this.parent.universe.setPlayerLastChunkX(event.getPlayer(),xchunkloc);
				this.parent.universe.setPlayerLastChunkZ(event.getPlayer(),zchunkloc);
				this.parent.universe.setPlayerLastChunkName(event.getPlayer(),"Unknown Zone");
				
			}
			
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"This server runs "+ChatColor.YELLOW+"NPCX"+ChatColor.LIGHT_PURPLE+" with Civilizations enabled!");
			event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE+"To claim your own piece of paradise use /civ buy!");
		}
		int count = 0;
		for (myPlayer p : this.parent.universe.players.values())
		{
			if (p.name.equals(event.getPlayer().getName()))
			{
				// attach them
				p.player = event.getPlayer();
				count++;
			}
		}
		if (count == 0)
		{
			//System.out.println("npcx : added new player ("+ event.getPlayer().getName()+")");
			parent.universe.players.put(player.player.getName(), player);
		}
		
    }
	

	
	
	
	public void onPlayerChat(PlayerChatEvent event)
    {
		for (myPlayer player : parent.universe.players.values()){
			if (player.player == event.getPlayer())
			{
				if (player.target != null)
				{
					
					//System.out.println("npcx : player chat event ("+ player.player.getName()+")");
					player.player.sendMessage("You say to " + player.target.getName() +", '" + event.getMessage() + "'");

					if (player.target.parent != null)
					{
						// this is not a temporary spawn
						
						// does it have a category set?
						if (player.target.parent.category != null)
						{
							
							// check what type of category it is
							if (player.target.parent.category.matches("merchant"))
							{
								// merchant
								player.target.parent.onPlayerChat(player, event.getMessage(),"merchant");
								
							} else {
								// normal chat event / unknown category
								player.target.parent.onPlayerChat(player, event.getMessage(),"");
							}
						} else {
							// normal chat event
							player.target.parent.onPlayerChat(player, event.getMessage(),"");
						}
					} else {
						player.player.sendMessage("You cannot talk to temporary spawns");
					}
					event.setCancelled(true);
				}
			}
		}
    }
	
	
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		for (myPlayer player : this.parent.universe.players.values())
		{
				// deal with player death changes
				if (player.player == event.getPlayer())
				{
					//System.out.println("npcx : player about to respawn, assigning them to the dead list");
					player.dead = true;
					
				}
			
		}
		
	}
	
	public void onPlayerQuit(PlayerQuitEvent event)
    {
		
		for (myPlayer player : parent.universe.players.values()){
			if (player.player == event.getPlayer())
			{
				
				player.dead = true;
				this.parent.informNpcDeadPlayer(event.getPlayer());
				
				//System.out.println("npcx : removed player ("+ player.player.getName()+")");
				parent.universe.players.remove(player);
			}
		}
		
    
    }
}
