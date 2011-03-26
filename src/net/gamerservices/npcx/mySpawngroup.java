package net.gamerservices.npcx;

import java.util.HashMap;

import org.bukkit.World;

public class mySpawngroup {
    public int id;
    public String name;
    public String category;
    public double x;
    public double y;
    public double z;
    public boolean active = false;
    public World world;
    public HashMap<String, myNPC> npcs = new HashMap<String, myNPC>();
    int activecountdown = 0;
    public double pitch;
    public double yaw;
    public myPathgroup pathgroup;
}
