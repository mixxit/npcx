package net.gamerservices.npcx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.World;

public class myPathgroup {
    public int id;
    String name;
    public List<myPathgroup_entry> pathgroupentries = new CopyOnWriteArrayList<myPathgroup_entry>();
    public int category;
    public World world;
}
