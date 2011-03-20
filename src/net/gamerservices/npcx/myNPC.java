package net.gamerservices.npcx;
import redecouverte.npcspawner.BasicHumanNpc;
public class myNPC {
	String name = "dummy";
	String id = "0";
	String category = "container";
	BasicHumanNpc npc;
	mySpawngroup spawngroup;

	
	myNPC()
	{
		
	}
	
	public void parseChat(myPlayer myplayer, String message)
	{
		myplayer.player.sendMessage(npc.getName() + " says to you, 'I'm sorry. I'm rather busy right now.'");
	}
}
