package net.gamerservices.npcx;
import java.util.HashMap;

import redecouverte.npcspawner.BasicHumanNpc;
public class myNPC {
	public npcx parent;
	String name = "dummy";
	String id = "0";
	String category = "container";
	BasicHumanNpc npc;
	mySpawngroup spawngroup;

	public HashMap<String, myTriggerword> spawngroups = new HashMap<String, myTriggerword>();
	
	myNPC(npcx parent)
	{
		this.parent = parent;
	}
	
	public void parseChat(myPlayer myplayer, String message)
	{
		myplayer.player.sendMessage(npc.getName() + " says to you, 'I'm sorry. I'm rather busy right now.'");
	}
}
