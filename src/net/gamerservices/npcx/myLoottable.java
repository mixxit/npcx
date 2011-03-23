package net.gamerservices.npcx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.entity.Monster;

public class myLoottable {

	public int id;
	public String name;
	public List< myLoottable_entry > loottable_entries = new CopyOnWriteArrayList< myLoottable_entry >();
	
	myLoottable(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
}
