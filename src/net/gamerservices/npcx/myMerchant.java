package net.gamerservices.npcx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class myMerchant {
    public int id;
    public String name;
    npcx parent;
    public List<myMerchant_entry> merchantentries = new ArrayList<myMerchant_entry>();
    public String category = "default";

    public myMerchant(npcx parent, int id, String name) {
        this.parent = parent;
        this.id = id;
        this.name = name;
    }
}
