package net.gamerservices.npcx;

import java.util.HashMap;

import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldListener;

// Citizens Mod: http://forums.bukkit.org/threads/7173/

public class npcxWListener extends WorldListener {
	private npcx plugin;

	public npcxWListener(npcx plugin) {
		this.plugin = plugin;
	}

	@Override
	public void onChunkUnload(ChunkUnloadEvent e) {
		try
		{
			for (myNPC npc : plugin.universe.npcs.values())			
			{
				if (e.getChunk().getWorld()
						.getChunkAt(npc.npc.getBukkitEntity().getLocation())
						.equals(e.getChunk())) {
					npc.npc.forceMove(npc.npc.getBukkitEntity().getLocation());

				}
			}
		} catch (Exception e2) {
			
		}
	}

	public void onChunkLoad(ChunkLoadEvent e) {
		try
		{
			for (myNPC npc : plugin.universe.npcs.values())
			{
				if (e.getChunk().getWorld()
						.getChunkAt(npc.npc.getBukkitEntity().getLocation())
						.equals(e.getChunk())) {
					npc.npc.forceMove(npc.npc.getBukkitEntity().getLocation());
				}
			}
		} catch (Exception e2) {
			
		}
	}
}
