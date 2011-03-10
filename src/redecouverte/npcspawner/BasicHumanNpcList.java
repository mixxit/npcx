package redecouverte.npcspawner;

import java.util.HashMap;
import org.bukkit.entity.Entity;


public class BasicHumanNpcList extends HashMap<String, BasicHumanNpc> {

    public boolean containsBukkitEntity(Entity entity)
    {
        for(BasicHumanNpc bnpc : this.values())
        {
            if(bnpc.getBukkitEntity().getEntityId() == entity.getEntityId())
                return true;
        }

        return false;
    }

    public BasicHumanNpc getBasicHumanNpc(Entity entity)
    {
        for(BasicHumanNpc bnpc : this.values())
        {
            if(bnpc.getBukkitEntity().getEntityId() == entity.getEntityId())
                return bnpc;
        }

        return null;
    }

}
