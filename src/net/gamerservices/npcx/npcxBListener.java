package net.gamerservices.npcx;

import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;


public class npcxBListener extends BlockListener {
    private npcx parent;

    public npcxBListener(npcx parent) {
        this.parent = parent;
    }
    public void onBlockIgnite(BlockIgniteEvent event) {
    	String cause = event.getCause().toString();
    	if(cause.equals("SPREAD"))
    	{
    		event.setCancelled(true);
    	}
    	if(cause.equals("FLINT_AND_STEEL"))
    	{
    		event.setCancelled(true);
    	}
    }
}
