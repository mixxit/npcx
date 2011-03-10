package net.gamerservices.npcx;
import java.util.logging.Logger;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.*;
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
    public void onEntityTarget(EntityTargetEvent event) {

        if (event instanceof NpcEntityTargetEvent) {
            NpcEntityTargetEvent nevent = (NpcEntityTargetEvent)event;

            BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());

            if (npc != null && event.getTarget() instanceof Player) {
                if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
                    //Player p = (Player) event.getTarget();
                    // player is near the npc
                    // do something here
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
