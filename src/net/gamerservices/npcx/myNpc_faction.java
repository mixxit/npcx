package net.gamerservices.npcx;

public class myNpc_faction {
	npcx parent;
	int id;
	int npcid;
	int factionid;
	int amount;
	public myNpc_faction(int id, int npcid, int factionid, int amount)
	{
		this.id = id;
		this.npcid = npcid;
		this.factionid = factionid;
		this.amount = amount;
	}
}
