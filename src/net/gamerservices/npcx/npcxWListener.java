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
		//System.out.println("debug : closing chunk " + e.getChunk());
		for (myNPC npc : plugin.universe.npcs.values())
		{
			if (plugin.universe.npcs != null)
			{
				if (npc.npc != null)
				{
					if (e.getChunk().getWorld()
							.getChunkAt(npc.npc.getBukkitEntity().getLocation())
							.equals(e.getChunk())) {
						npc.npc.chunkinactive(npc.npc.getBukkitEntity().getLocation());
					}
				}
			}
		}
	}
	
	@Override
	public void onChunkLoad(ChunkLoadEvent e) {
		//System.out.println("debug : loading chunk " + e.getChunk());
		for (myNPC npc : plugin.universe.npcs.values())
		{
			if (plugin.universe.npcs != null)
			{
				if (npc.npc != null)
				{
					if (e.getChunk().getWorld()
							.getChunkAt(npc.npc.getBukkitEntity().getLocation())
							.equals(e.getChunk())) {
						npc.npc.chunkactive(npc.npc.getBukkitEntity().getLocation());
					}
				}
			}
		}
	}
}
