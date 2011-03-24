package net.gamerservices.npcx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class myPathgroup {
	int id;
	String name;
	public List< myPathgroup_entry > pathgroupentries = new CopyOnWriteArrayList< myPathgroup_entry >();
	public int category;
}
