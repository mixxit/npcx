package net.gamerservices.npcx;

import org.bukkit.event.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.HumanEntity;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.NpcEntityTargetEvent;
import redecouverte.npcspawner.NpcEntityTargetEvent.NpcTargetReason;

public class npcxEListener extends EntityListener {

    private final npcx parent;

    public npcxEListener(npcx parent) {
        this.parent = parent;
    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {
        super.onEntityDamage(event);

        if (event instanceof EntityDamageByEntityEvent) {
            if (event.getEntity() instanceof Monster) {
                EntityDamageByEntityEvent edee1 = (EntityDamageByEntityEvent) event;
                if (edee1.getDamager() instanceof HumanEntity) {
                    //System.out.println("npcx : monster go ow");
                }
            }
        }

        if (event.getEntity() instanceof HumanEntity) {
            BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) event;

                /*if (npc != null && npc.aggro != null && edee.getDamager() == npc.aggro)
                {
                npc.follow = null;
                npc.aggro = null;
                //System.out.println("npcx : forgot about target");

                }*/
                if (npc != null && edee.getDamager() instanceof LivingEntity) {
                    Entity p = edee.getDamager();
                    if (npc.parent != null) {
                        for (myPlayer player : parent.players.values()) {
                            if (player.player == edee.getDamager()) {
                                if (npc.attacking == null) {
                                    npc.parent.onPlayerAggroChange(player);
                                } else {
                                    // else dont fire again
                                }
                            }
                        }
                    }

                    npc.following = (LivingEntity) p;
                    npc.attacking = (LivingEntity) p;

                    try {
                        npc.health = npc.health - 10;
                        if (npc.health < 1) {
                            parent.onNPCDeath(npc);
                        }
                    } catch (Exception e) {
                        npc.following = null;
                        npc.attacking = null;
                        //System.out.println("npcx : forgot about target");
                        // do not modify mobs health
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Monster) {
            //System.out.println("npcx : deregistered monster");
            this.parent.monsters.remove((Monster) event.getEntity());
        }
    }

    @Override
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            //System.out.println("npcx : registered monster");
            this.parent.monsters.add((Monster) event.getEntity());
        }
    }

    @Override
    public void onEntityTarget(EntityTargetEvent event) {
        //System.out.println("npcx : target onentityevent");
        if (event instanceof NpcEntityTargetEvent) {
            NpcEntityTargetEvent nevent = (NpcEntityTargetEvent) event;
            BasicHumanNpc npc = parent.npclist.getBasicHumanNpc(event.getEntity());
            //System.out.println("npcx : this is an instance of npcentitytargetevent");

            // Targets player
            if (npc == null) {
                event.setCancelled(true);
            }

            if (npc != null && event.getTarget() instanceof Player) {
                //System.out.println("npcx : npc is not null and target is a player");
                if (nevent.getNpcReason() == NpcTargetReason.CLOSEST_PLAYER) {
                    //System.out.println("npcx : reason of event was closest player");
                    //Player p = (Player) event.getTarget();
                    // player is near the npc
                    // do something here
                     /*
                        if (npc != null) {
                            npc.moveTo(event.getTarget().getLocation().getX(), event.getTarget().getLocation().getY(), event.getTarget().getLocation().getZ(), event.getTarget().getLocation().getYaw(), event.getTarget().getLocation().getPitch());
                        }
                     */
                    event.setCancelled(true);
                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_RIGHTCLICKED) {
                    //System.out.println("npcx : reason of event was rightclicked");

                    Player p = (Player) event.getTarget();

                    for (myPlayer player : parent.players.values()) {
                        if (player.player == p) {
                            if (player.target != null) {
                                p.sendMessage("* Target cleared!");
                                player.target = null;
                            } else {
                                player.target = npc;
                                p.sendMessage("* Active chat target set as: " + npc.getName());
                            }

                        } else {
                            if (player.name == p.getName()) {
                                p.sendMessage("Your name is right but your player is wrong");
                            }
                        }
                    }

                    event.setCancelled(true);
                } else if (nevent.getNpcReason() == NpcTargetReason.NPC_BOUNCED) {
                    //Player p = (Player) event.getTarget();
                    // do something here
                    //p.sendMessage("<" + npc.getName() + "> Stop bouncing on me!");
                    event.setCancelled(true);
                }
            }
        }

    }
}
