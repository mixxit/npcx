package net.gamerservices.npcx;
import java.util.HashMap;

import redecouverte.npcspawner.BasicHumanNpc;
public class myNPC {
	public npcx parent;
	public String name = "dummy";
	public String id = "0";
	String category = "container";
	public BasicHumanNpc npc;
	public mySpawngroup spawngroup;

	public HashMap<String, myTriggerword> triggerwords = new HashMap<String, myTriggerword>();
	
	myNPC(npcx parent, HashMap<String, myTriggerword> triggerwords)
	{
		this.parent = parent;
		this.triggerwords = triggerwords;
	}
	
	
	
	public void parseChat(myPlayer myplayer, String message)
	{
		int count = 0;
		//myplayer.player.sendMessage("Parsing:" + message + ":" + Integer.toString(this.triggerwords.size()));
		String message2=message+" ";
		for (String word : message2.split(" "))
		{
			if(count == 0)
			{
				for (myTriggerword tw : triggerwords.values())
				{
					//myplayer.player.sendMessage("Test:" + word + ":"+ tw.word);
					if (word.toLowerCase().contains(tw.word.toLowerCase()))
					{
						
							myplayer.player.sendMessage(npc.getName() + " says to you, '"+ tw.response +"'");
							count++;
						
	
					}
				}
			}
		}
		if (count == 0)
		{
			myplayer.player.sendMessage(npc.getName() + " says to you, 'I'm sorry. I'm rather busy right now.'");
		}
	}
}
