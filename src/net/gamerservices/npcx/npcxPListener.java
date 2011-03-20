package net.gamerservices.npcx;
import java.util.logging.Logger;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcEntityTargetEvent;
import redecouverte.npcspawner.NpcEntityTargetEvent.NpcTargetReason;
import redecouverte.npcspawner.NpcSpawner;

public class npcxPListener extends PlayerListener {
	
	private final npcx parent;
	
	public npcxPListener(npcx parent) {
        this.parent = parent;
    }
	public void onPlayerJoin(PlayerEvent event) 
    {
		myPlayer player = new myPlayer(event.getPlayer());
		System.out.println("npcx : added player ("+ event.getPlayer().getName()+")");
		parent.players.put(player.player.getName(), player);
		
    }
	public void onPlayerChat(PlayerChatEvent event)
    {
		for (myPlayer player : parent.players.values()){
			if (player.player == event.getPlayer())
			{
				if (player.target != null)
				{
					System.out.println("npcx : player chat event ("+ player.player.getName()+")");
					player.player.sendMessage("You say to " + player.target.getName() +", '" + event.getMessage() + "'");

					
					player.target.parent.parseChat(player, event.getMessage());
					event.setCancelled(true);
				}
			}
		}
    }
	
	public void onPlayerQuit(PlayerEvent event)
    {
		for (myPlayer player : parent.players.values()){
			if (player.player == event.getPlayer())
			{
				System.out.println("npcx : removed player ("+ player.player.getName()+")");
				parent.players.remove(player);
			}
		}
		
    
    }
}
