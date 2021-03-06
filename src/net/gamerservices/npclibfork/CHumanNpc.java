package net.gamerservices.npclibfork;

import java.util.logging.Logger;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.NetHandler;
import net.minecraft.server.Packet18ArmAnimation;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.entity.EntityTargetEvent;

public class CHumanNpc extends EntityPlayer {

    private static final Logger logger = Logger.getLogger("Minecraft");
    private int lastTargetId;
    private long lastBounceTick;
    private int lastBounceId;

    public CHumanNpc(MinecraftServer minecraftserver, World world, String s, ItemInWorldManager iteminworldmanager) {
        super(minecraftserver, world, s, iteminworldmanager);

        NetworkManager netMgr = new NpcNetworkManager(new NpcSocket(), "npc mgr", new NetHandler() {
            @Override
            public boolean c() {
                return false;
            }
        });
        this.netServerHandler = new NpcNetHandler(minecraftserver, this, netMgr);
        netMgr.a(this.netServerHandler);

        this.lastTargetId = -1;
        this.lastBounceId = -1;
        this.lastBounceTick = 0;
    }

    public void animateArmSwing() {
        this.b.getTracker(this.dimension).a(this, new Packet18ArmAnimation(this, 1));
    }

    public void forceSetName(String n) {
        this.displayName = n;
    }

    @Override
    public boolean b(EntityHuman entity) {

        EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_RIGHTCLICKED);
        CraftServer server = ((WorldServer) this.world).getServer();
        server.getPluginManager().callEvent(event);

        return super.b(entity);
    }

    @Override
    public void a_(EntityHuman entity) {
        if (lastTargetId == -1 || lastTargetId != entity.id) {
            EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.CLOSEST_PLAYER);
            CraftServer server = ((WorldServer) this.world).getServer();
            server.getPluginManager().callEvent(event);
        }
        lastTargetId = entity.id;

        super.a_(entity);
    }

    @Override
    public void c(Entity entity) {
        if (lastBounceId != entity.id || System.currentTimeMillis() - lastBounceTick > 1000) {
            EntityTargetEvent event = new NpcEntityTargetEvent(getBukkitEntity(), entity.getBukkitEntity(), NpcEntityTargetEvent.NpcTargetReason.NPC_BOUNCED);
            CraftServer server = ((WorldServer) this.world).getServer();
            server.getPluginManager().callEvent(event);

            lastBounceTick = System.currentTimeMillis();
        }

        lastBounceId = entity.id;

        super.c(entity);
    }
}