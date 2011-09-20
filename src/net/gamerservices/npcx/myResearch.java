package net.gamerservices.npcx;

public class myResearch {

    int id;
    String name;
    String prereq;
    String time;
    public int cost;

    public myResearch(int id, String name, String prereq, String time, String cost) {
        // TODO Auto-generated constructor stub
        this.id = id;
        this.name = name;
        this.prereq = prereq;
        this.time = time;
        this.cost = Integer.parseInt(cost);
    }

}
