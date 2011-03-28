package net.gamerservices.npcx;
import java.util.logging.Logger;

import net.gamerservices.npclibfork.BasicHumanNpc;
import net.gamerservices.npclibfork.NpcEntityTargetEvent;
import net.gamerservices.npclibfork.NpcSpawner;
import net.gamerservices.npclibfork.NpcEntityTargetEvent.NpcTargetReason;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;

public class npcxPListener extends PlayerListener {
	
	private final npcx parent;
	
	public npcxPListener(npcx parent) {
        this.parent = parent;
    }
	public void onPlayerJoin(PlayerEvent event) 
    {
		myPlayer player = new myPlayer(event.getPlayer(),event.getPlayer().getName());
		//System.out.println("npcx : added player ("+ event.getPlayer().getName()+")");
		parent.universe.players.put(player.player.getName(), player);
		
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
							if (player.target.parent.category.matches("shop"))
							{
								// shop
								player.target.parent.onPlayerChat(player, event.getMessage(),"shop");
								
							} else {
								if (player.target.parent.category.matches("merchant"))
								{
									// merchant
									player.target.parent.onPlayerChat(player, event.getMessage(),"merchant");
									
								} else {
									// normal chat event / unknown category
									player.target.parent.onPlayerChat(player, event.getMessage(),"");
								}
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
	
	public void onPlayerQuit(PlayerEvent event)
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
