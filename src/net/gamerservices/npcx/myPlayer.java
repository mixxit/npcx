package net.gamerservices.npcx;

import org.bukkit.entity.Player;
import redecouverte.npcspawner.BasicHumanNpc;

public class myPlayer {
    public Player player;
    public BasicHumanNpc target;
    public boolean dead = false;
    public String name;

    myPlayer(Player player, String name) {
        this.player = player;
        this.name = name;
    }
}
