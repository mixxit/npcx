package net.gamerservices.npcx;

import java.util.TimerTask;

public class LongTick extends TimerTask {

    private npcx parent;

    public LongTick(npcx owner) {
        parent = owner;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        parent.longCheck();

        cancel();
    }

}
