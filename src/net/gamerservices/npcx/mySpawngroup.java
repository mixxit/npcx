package net.gamerservices.npcx;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import net.gamerservices.npclibfork.NpcSpawner;

import org.bukkit.World;


public class mySpawngroup {
	public int id;
	public String name;
	public String category;
	public double x;
	public double y;
	public double z;
	public npcx parent;
	public boolean active = false;
	public World world;
	public HashMap<String, myNPC> npcs = new HashMap<String, myNPC>();
	int activecountdown = 0;
	public double pitch;
	public double yaw;
	public myPathgroup pathgroup;
	
	public mySpawngroup(npcx parent)
	{
		this.parent = parent;
	}
	
	public boolean Delete() {
		// Go through my children
		
		try 
		{
			for (myNPC npc : npcs.values())
			{
				// Deleting npc
				sqlDeleteEntry(Integer.parseInt(npc.id));
				// Remove npcs from world
				if(npc.npc != null)
				{
					NpcSpawner.RemoveBasicHumanNpc(npc.npc);
					npc.npc = null;
				}
			}
			
			// Ok removed the instances of those npcs from the game, cleared their SQL entries
			// Lets remove the entire npc cache
			npcs = null;
			sqlDelete();
			
			
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
	}
	
	private void sqlDeleteEntry(int id2) {
		// TODO Auto-generated method stub
		PreparedStatement s2;
		try {
			s2 = this.parent.universe.conn.prepareStatement("DELETE FROM spawngroup_entries WHERE spawngroupid = ? AND id = ?;");
			s2.setInt(1,this.id);
			s2.setInt(2,id2);
		    
			s2.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sqlDelete()
	{
		PreparedStatement s2;
		try {
			s2 = this.parent.universe.conn.prepareStatement("DELETE FROM spawngroup WHERE id = ?;");
			s2.setInt(1,this.id);
		    
			s2.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
