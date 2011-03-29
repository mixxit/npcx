package net.gamerservices.npcx;

import net.gamerservices.npclibfork.BasicHumanNpc;

import org.bukkit.entity.Player;


public class myPlayer {

	public Player player;
	public BasicHumanNpc target;
	public boolean dead = false;
	public String name;
	public int zomgcount = 0;
	myPlayer(Player player, String name)
	{
		this.player = player;
		this.name = name;
		
	}
	
}
