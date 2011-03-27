package net.gamerservices.npcx;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.gamerservices.npclibfork.BasicHumanNpc;
import net.gamerservices.npclibfork.NpcEntityTargetEvent;
import net.gamerservices.npclibfork.NpcSpawner;
import net.gamerservices.npclibfork.NpcEntityTargetEvent.NpcTargetReason;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Zombie;


public class npcxEListener extends EntityListener 
{
	
	private final npcx parent;
	
	public npcxEListener(npcx parent) 
	{
        this.parent = parent;
    }
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		super.onEntityDamage(event);
		
		if (event instanceof EntityDamageByEntityEvent )
		{
			if (event.getEntity() instanceof Monster)
			{
				EntityDamageByEntityEvent edee1 = (EntityDamageByEntityEvent)event;
				if (edee1.getDamager() instanceof HumanEntity) 
				{
					
					//System.out.println("npcx : monster go ow");
				}
					
				
		    }
		}
		
		if (event.getEntity() instanceof HumanEntity) 
		{
			BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
			if (event instanceof EntityDamageByEntityEvent)
		    {
				EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) event;

				
		        if (npc != null && edee.getDamager() instanceof LivingEntity) 
		        {
	
		        	Entity p = edee.getDamager();
		        	if (npc.parent != null)
		        	{
		        		for (myPlayer player : parent.universe.players.values())
		        		{
		        			if (player.player == edee.getDamager())
		        			{
		        				if (npc.aggro == null)
		        				{
		        					// First time sent an event
			        				npc.parent.onPlayerAggroChange(player);
		        					
		        				} 
		        			}
		        		}
		        	}
		            npc.follow = (LivingEntity)p;
		            npc.aggro = (LivingEntity)p;
		            
		            try
		            {
		            	npc.hp = npc.hp - 10;
		            	if (npc.hp < 1)
		            	{
		            		
		            		
		            		parent.onNPCDeath(npc);
		            		
		            	}
		            } 
		            catch (Exception e)
		            {
		            	npc.follow = null;
						npc.aggro = null;
						//System.out.println("npcx : forgot about target");
		            	// do not modify mobs health
		            }
		            
		            event.setCancelled(true);
	
		        }
		    }
		}
	}
	@Override
	public void onEntityDeath	(	EntityDeathEvent 	event	)
	{
		
		
		if (event.getEntity() instanceof Monster)
		{
			
			//System.out.println("npcx : deregistered monster");
			this.parent.universe.monsters.remove((Monster)event.getEntity());	

		}
	}
	
	@Override
	public void onEntityExplode	(	EntityExplodeEvent 	event	)
	{
		
		
		if (event.getEntity() instanceof Monster)
		{
			
			//System.out.println("npcx : deregistered monster");
			this.parent.universe.monsters.remove((Monster)event.getEntity());	

		}
	}
	
	@Override
	public void onCreatureSpawn	( CreatureSpawnEvent event)	
	{
		
		if (event.getEntity() instanceof Monster)
		{
			//System.out.println("npcx : registered monster");
			this.parent.universe.monsters.add((Monster)event.getEntity());
		}
	}

	
	
	
	@Override
	
    public void onEntityTarget(EntityTargetEvent event) {

		
		//System.out.println("npcx : target onentityevent");
		
        if (event instanceof NpcEntityTargetEvent) {
            NpcEntityTargetEvent nevent = (NpcEntityTargetEvent)event;


            BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
            
            
            // Targets player
    		if (npc == null)
    		{
    			
    			event.setCancelled(true);
    		}
    		
            if (npc != null && event.getTarget() instanceof Player) 
            {
        		

            	if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
            		
                    event.setCancelled(true);

                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
            		//System.out.println("npcx : reason of event was rightclicked");

                	Player p = (Player) event.getTarget();

                    
                    for (myPlayer player : parent.universe.players.values()){
                    	
                    	
            			if (player.player == p)
            			{
            				if (player.target != null)
            				{
                                p.sendMessage("* Target cleared!");
                                player.target = null;
            					
            				} else {
            					player.target = npc;
            					
            					int tNPCID = 0;
            					int tGPID = 0;
            					int tFID = 0;
            					int tPGID = 0;
            					int tLTID = 0;
            					
            					if (npc.parent != null)
            					{
            						tNPCID = Integer.parseInt(npc.parent.id);
            						
                					if (npc.parent.spawngroup != null)
                						tGPID = npc.parent.spawngroup.id;
                					if (npc.parent.faction != null)
                						tFID = npc.parent.faction.id;
                					if (npc.parent.pathgroup != null)
                						tPGID = npc.parent.pathgroup.id;
                					if (npc.parent.loottable != null)
                						tLTID = npc.parent.loottable.id;
            					}
            					
                                p.sendMessage("NPCID ("+tNPCID+"):SG ("+tGPID+"):F ("+tFID+"):PG ("+tPGID+"):L ("+tLTID+")");
                                p.sendMessage("* Active chat target set as: " + npc.getName() + ". Click again to cancel.");
                                p.sendMessage("* Anything you now type will be redirected to: " + npc.getName());
                                p.sendMessage("* Words in [brackets] are commands. Type 'hello' to begin.");
                                
            				}
            				
            			} else {
            				if (player.name == p.getName())
            				{
            					p.sendMessage("Your name is right but your player is wrong");
            					
            				}
            				
            			}
            		}
                    
                    
                    event.setCancelled(true);
                    
                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_BOUNCED) {
                    //Player p = (Player) event.getTarget();
                    // do something here
                    //p.sendMessage("<" + npc.getName() + "> Stop bouncing on me!");
                    event.setCancelled(true);
                }
            } 
        }

    }
}
