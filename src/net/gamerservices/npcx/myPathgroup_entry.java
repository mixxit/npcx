package net.gamerservices.npcx;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class myPathgroup_entry {
	public int id;
	public String name;
	public Location location;
	public int pathgroupid;
	public int spot;
	public myPathgroup parent;
	
	myPathgroup_entry(Location location, int pathgroupid, myPathgroup pathgroup, int spot)
	{
		this.name = name;
		this.location = location;
		this.pathgroupid = pathgroupid;
		this.spot = spot;
		this.parent = pathgroup;
	}
	
	
	
}
