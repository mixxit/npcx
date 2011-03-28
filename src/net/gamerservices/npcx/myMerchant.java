package net.gamerservices.npcx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class myMerchant {
	public int id;
	public String name;
	npcx parent;
	public List< myMerchant_entry > merchantentries = new CopyOnWriteArrayList< myMerchant_entry >();
	
	public myMerchant(npcx parent, int id, String name)
	{
		this.parent = parent;
		this.id = id;
		this.name = name;
	}
}
