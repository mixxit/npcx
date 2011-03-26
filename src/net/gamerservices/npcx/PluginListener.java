package net.gamerservices.npcx;

import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;
import org.bukkit.plugin.Plugin;

public class PluginListener extends ServerListener {
    public npcx parent;

    public PluginListener(npcx parent) {
        this.parent = parent;
    }

    @Override
    public void onPluginEnabled(PluginEvent event) {
        if (parent.getiConomy() == null) {
            Plugin iConomy = parent.getBukkitServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled()) {
                    parent.setiConomy((iConomy) iConomy);
                    System.out.println("[(Plugin)] Successfully linked with iConomy.");
                }
            }
        }
    }
}
