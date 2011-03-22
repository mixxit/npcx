package net.gamerservices.npcx;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Zombie;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcEntityTargetEvent;
import redecouverte.npcspawner.NpcEntityTargetEvent.NpcTargetReason;
import redecouverte.npcspawner.NpcSpawner;

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

				if (npc != null && npc.aggro != null && edee.getDamager() == npc.aggro) 
				{
					npc.follow = null;
					npc.aggro = null;
					//System.out.println("npcx : forgot about target");
						 
   			    }
		        if (npc != null && edee.getDamager() instanceof LivingEntity) 
		        {
	
		        	Entity p = edee.getDamager();
		        	
		            npc.follow = (LivingEntity)p;
		            npc.aggro = (LivingEntity)p;
		            
		            try
		            {
		            	npc.hp = npc.hp - 10;
		            	if (npc.hp < 1)
		            	{
		            		
		            		//p.sendMessage(npc.getName() + " has been slain!");
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
			this.parent.monsters.remove((Monster)event.getEntity());	

		}
	}
	@Override
	public void onCreatureSpawn	( CreatureSpawnEvent event)	
	{
		
		if (event.getEntity() instanceof Monster)
		{
			//System.out.println("npcx : registered monster");
			this.parent.monsters.put(Integer.toString(event.getEntity().getEntityId()), (Monster)event.getEntity());
		}
	}

	
	
	
	@Override
	
    public void onEntityTarget(EntityTargetEvent event) {

		
		//System.out.println("npcx : target onentityevent");
		
        if (event instanceof NpcEntityTargetEvent) {
            NpcEntityTargetEvent nevent = (NpcEntityTargetEvent)event;

/*            for (myNPC n : parent.npcs.values())
            {
            }*/
            
            BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
    		//System.out.println("npcx : this is an instance of npcentitytargetevent");
            
            
            // Targets player
    		if (npc == null)
    		{
    			
    			System.out.println("npcx : npc is null");
    			/*for (myNPC n : parent.npcs.values())
                {
    				if (nevent.getTarget() == n.npc)
    				{
    					//System.out.println("npcx : npc found!");
    					npc = n.npc;
    				}
                }*/
    			event.setCancelled(true);
    		}
    		
            if (npc != null && event.getTarget() instanceof Player) 
            {
        		//System.out.println("npcx : npc is not null and target is a player");

            	if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
            		//System.out.println("npcx : reason of event was closest player");

            		//Player p = (Player) event.getTarget();
                    // player is near the npc
                    // do something here
                	 /*
                     if (npc != null) {
                         npc.moveTo(event.getTarget().getLocation().getX(), event.getTarget().getLocation().getY(), event.getTarget().getLocation().getZ(), event.getTarget().getLocation().getYaw(), event.getTarget().getLocation().getPitch());
                         
                     }
                     */
                    event.setCancelled(true);

                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
            		//System.out.println("npcx : reason of event was rightclicked");

                	Player p = (Player) event.getTarget();

                    /*
                    p.sendMessage("* You say, 'Hail, " + npc.getName() + "!'");
                    p.sendMessage("* " + npc.getName() + " says, 'Hello " + p.getName() + "!");
                    */
                    
                    for (myPlayer player : parent.players.values()){
                    	
                    	
            			if (player.player == p)
            			{
            				if (player.target != null)
            				{
                                p.sendMessage("* Target cleared!");
                                player.target = null;
            					
            				} else {
            					player.target = npc;
                                p.sendMessage("* Active chat target set as: " + npc.getName());
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
