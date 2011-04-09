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
		if(event.isCancelled())
        {
            return;
        }
		if (event.getEntity() instanceof HumanEntity) 
		{
			
			BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
			
			if (npc != null)
			{
				npc.onDamage(event);
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
		if(event.isCancelled())
        {
            return;
        }
		
		if (event.getEntity() instanceof Monster)
		{
			
			//System.out.println("npcx : deregistered monster");
			
			this.parent.universe.monsters.remove((Monster)event.getEntity());	
			
		}
		if (this.parent.universe.nocreeper != null)
		{
			// creeper protection is on
			if (this.parent.universe.nocreeper.matches("true"))
			{
				// creeper events are bad
				event.setCancelled(true);
			}
		}
		
	}
	
	@Override
	public void onCreatureSpawn	( CreatureSpawnEvent event)	
	{
		if(event.isCancelled())
        {
            return;
        }
		if (event.getEntity() instanceof Monster)
		{
			//System.out.println("npcx : registered monster");
			this.parent.universe.monsters.add((Monster)event.getEntity());
		}
	}

	
	
	
	@Override
	
    public void onEntityTarget(EntityTargetEvent event) {

		if(event.isCancelled())
        {
            return;
        }
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
            		
            		Player p = (Player) event.getTarget();
            		npc.onClosestPlayer(p);
                    event.setCancelled(true);

                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
            		//System.out.println("npcx : reason of event was rightclicked");

                	Player p = (Player) event.getTarget();
                	npc.onRightClick(p);

                    
                    
                    event.setCancelled(true);
                    
                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_BOUNCED) {
                    Player p = (Player) event.getTarget();
                    // do something here
                    //p.sendMessage("<" + npc.getName() + "> Stop bouncing on me!");
                	npc.onBounce(p);
                    event.setCancelled(true);
                }
            } 
        }

    }
}
