package net.gamerservices.npcx;
import java.util.logging.Logger;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;
import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcEntityTargetEvent;
import redecouverte.npcspawner.NpcEntityTargetEvent.NpcTargetReason;
import redecouverte.npcspawner.NpcSpawner;

public class npcxEListener extends EntityListener {
	
	private final npcx parent;
	
	public npcxEListener(npcx parent) {
        this.parent = parent;
    }
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		super.onEntityDamage(event);
		
		if (event.getEntity() instanceof HumanEntity) {
			BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
			if (event instanceof EntityDamageByEntityEvent)
		    {
				

				EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) event;

		        if (npc != null && edee.getDamager() instanceof Player) {
	
		        	Player p = (Player) edee.getDamager();
		        	
		            npc.follow = p;
		            npc.aggro = p;
		            
		            try
		            {
		            	npc.hp = npc.hp - 1;
		            	if (npc.hp < 1)
		            	{
		            		p.sendMessage(npc.getName() + " has been slain!");

		            		NpcSpawner.RemoveBasicHumanNpc(npc);
		            	}
	            
		          
		            
		            } 
		            catch (Exception e)
		            {
		            	// do not modify mobs health
		            }
		            event.setCancelled(true);
	
		        }
		    }

	    }
	}
	
	@Override
    public void onEntityTarget(EntityTargetEvent event) {

        if (event instanceof NpcEntityTargetEvent) {
            NpcEntityTargetEvent nevent = (NpcEntityTargetEvent)event;

            BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());

            if (npc != null && event.getTarget() instanceof Player) {
                if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
                    //Player p = (Player) event.getTarget();
                    // player is near the npc
                    // do something here
                	 
                     if (npc != null) {
                         npc.moveTo(event.getTarget().getLocation().getX(), event.getTarget().getLocation().getY(), event.getTarget().getLocation().getZ(), event.getTarget().getLocation().getYaw(), event.getTarget().getLocation().getPitch());
                         
                     }
                    event.setCancelled(true);

                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
                    Player p = (Player) event.getTarget();
                    p.sendMessage("* You say, 'Hail, " + npc.getName() + "!'");
                    p.sendMessage("* " + npc.getName() + " says, 'Hello " + p.getName() + "!");
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
