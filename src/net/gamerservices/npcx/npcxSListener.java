package net.gamerservices.npcx;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.iConomy.iConomy;

public class npcxSListener extends ServerListener {
    private npcx parent;

    public npcxSListener(npcx parent) {
        this.parent = parent;
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        if (parent.iConomy == null && parent.useiConomy) {
            Plugin iConomy = parent.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if (iConomy.isEnabled() && iConomy.getClass().getName().equals("com.iConomy.iConomy")) {
                    parent.iConomy = (iConomy) iConomy;
                    System.out.println("[npcx] hooked into iConomy.");
                }
            }
        }
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        if (parent.iConomy != null && parent.useiConomy) {
            if (event.getPlugin().getDescription().getName().equals("iConomy")) {
                parent.iConomy = null;
                System.out.println("[npcx] un-hooked from iConomy.");
            }
        }
    }
}
