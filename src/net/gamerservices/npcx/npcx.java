package net.gamerservices.npcx;

import com.nijiko.coelho.iConomy.iConomy;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Event.Priority;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ConcurrentModificationException;

import redecouverte.npcspawner.BasicHumanNpc;
import redecouverte.npcspawner.BasicHumanNpcList;
import redecouverte.npcspawner.NpcSpawner;

import net.gamerservices.npcx.data.Constants;

public class npcx extends JavaPlugin {

    private static final Logger logger = Logger.getLogger("Minecraft");

    // Properties
    private final String FILE_PROPERTIES = "npcx.properties";

    // Database
    private Connection conn = null;
    private String dsn;

    // Listeners
    private npcxEListener mEntityListener;
    private npcxPListener mPlayerListener;

    // NPC Data
    public HashMap<String, myPlayer> players = new HashMap<String, myPlayer>();
    public HashMap<String, myNPC> npcs = new HashMap<String, myNPC>();
    public HashMap<String, mySpawngroup> spawngroups = new HashMap<String, mySpawngroup>();
    public List<Monster> monsters = new CopyOnWriteArrayList<Monster>();
    public List<myFaction> factions = new CopyOnWriteArrayList<myFaction>();
    public List<myLoottable> loottables = new CopyOnWriteArrayList<myLoottable>();
    public List<myPathgroup> pathgroups = new CopyOnWriteArrayList<myPathgroup>();
    public BasicHumanNpcList npclist = new BasicHumanNpcList();

    // iConomy
    private static PluginListener PluginListener = null;
    private static iConomy iConomy = null;
    private static Server Server = null;

    // Other
    public String world;

    // Timers
    private Timer tick = new Timer();

    public HashMap<String, myTriggerword> fetchTriggerWords(int npcid) throws SQLException {
        //CREATE TABLE npc_triggerwords ( id INT UNSIGNED NOT NULL AUTO_INCREMENT, PRIMARY KEY (id),npcid int,triggerword CHAR(40),reply VARCHAR(256),category CHAR(40))
        HashMap<String, myTriggerword> triggerwords = new HashMap<String, myTriggerword>();
        Statement s = conn.createStatement();
        s.executeQuery("SELECT id, npcid, triggerword, reply, category FROM npc_triggerwords WHERE npcid =" + npcid);
        ResultSet rs = s.getResultSet();
        int count = 0;

        while (rs.next()) {
            count++;
            myTriggerword tw = new myTriggerword();
            tw.response = rs.getString("reply");

            tw.word = rs.getString("triggerword");
            tw.id = rs.getInt("id");
            triggerwords.put(Integer.toString(tw.id), tw);
        }

        //System.out.println("npcx : fetched "+count+" triggerwords");
        rs.close();
        s.close();

        return triggerwords;
    }

    public void onNPCDeath(BasicHumanNpc npc) {
        for (myPlayer player : players.values()) {
            if (player.target == npc) {
                player.target = null;

            }
        }

        for (myLoottable lt : loottables) {
            if (npc.parent != null) {
                if (npc.parent.loottable == lt) {
                    for (myLoottable_entry lte : lt.loottable_entries) {
                        npc.getBukkitEntity().getWorld().dropItem(
                                new Location(
                                npc.getBukkitEntity().getWorld(),
                                npc.getBukkitEntity().getLocation().getX(),
                                npc.getBukkitEntity().getLocation().getY(),
                                npc.getBukkitEntity().getLocation().getZ()),
                                new ItemStack(lte.itemid));
                    }
                }
            }
        }

        npclist.remove(npc);
        npcs.remove(npc);
        NpcSpawner.RemoveBasicHumanNpc(npc);

        if (npc.parent != null) {
            npc.parent.spawngroup.activecountdown = 100;
        }
    }

    public double getDistance(double d, double e) {
        return d - e;
    }

    public void think() {
        tick.schedule(new Tick(this), 1 * 400);
        fixDead();

        // check npc logic
        for (myNPC npc : npcs.values()) {
            npc.npc.think();

            //System.out.println("npcx : " + event.getEntity().getClass().toString());
            //System.out.println("npcx : " + event.getTarget().getClass().toString());

            if (this.players.size() > 0) {
                try {
                    for (myPlayer player : this.players.values()) {
                        if (player.player != null) {
                            if (player.player.getHealth() > 0) {
                                double distancex = getDistance(npc.npc.getBukkitEntity().getLocation().getX(), player.player.getLocation().getX());
                                double distancey = getDistance(npc.npc.getBukkitEntity().getLocation().getY(), player.player.getLocation().getY());
                                double distancez = getDistance(npc.npc.getBukkitEntity().getLocation().getZ(), player.player.getLocation().getZ());

                                if (distancex > -5 && distancey > -5 && distancez > -5 && distancex < 5 && distancey < 5 && distancez < 5) {
                                    if (npc.parent != null) {
                                        if (npc.npc.parent.faction != null) {
                                            if (npc.npc.parent.faction.base < 1000) {
                                                npc.npc.attacking = player.player;
                                                npc.npc.following = player.player;
                                            }
                                        } else {
                                            //System.out.println("npcx : i have no faction so ill be be neutral");
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Concurrent modification occured
                    e.printStackTrace();
                }

            }

            if (this.monsters.size() > 0) {
                try {
                    for (LivingEntity e : this.monsters) {
                        if (e.getHealth() > 0) {
                            double distancex = getDistance(npc.npc.getBukkitEntity().getLocation().getX(), e.getLocation().getX());
                            double distancey = getDistance(npc.npc.getBukkitEntity().getLocation().getY(), e.getLocation().getY());
                            double distancez = getDistance(npc.npc.getBukkitEntity().getLocation().getZ(), e.getLocation().getZ());

                            if (e instanceof Monster) {
                                if (distancex > -5 && distancey > -5 && distancez > -5 && distancex < 5 && distancey < 5 && distancez < 5) {
                                    //System.out.println("npcx : inmysights !");
                                    npc.npc.attacking = e;
                                    npc.npc.following = e;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Concurrent modification occured
                    e.printStackTrace();
                }
            }
        }

        // check spawngroups
        for (mySpawngroup spawngroup : spawngroups.values()) {
            if (spawngroup.activecountdown > 0) {
                spawngroup.activecountdown--;
                if (spawngroup.activecountdown == 1) {
                    spawngroup.active = false;
                }

            }

            if (!spawngroup.active) {
                //System.out.println("npcx : found inactive spawngroup ("+ spawngroup.id +") with :[" + spawngroup.npcs.size() + "]");
                int count = 0;
                Random generator = new Random();
                Object[] values = spawngroup.npcs.values().toArray();

                if (values.length > 0) {
                    myNPC npc = (myNPC) values[generator.nextInt(values.length)];

                    try {
                        // is there at least one player in game?
                        if (this.getServer().getOnlinePlayers().length > 0) {
                            if (!spawngroup.active) {
                                npc.spawngroup = spawngroup;

                                //System.out.println("npcx : made spawngroup active");
                                Double pitch = new Double(spawngroup.pitch);
                                Double yaw = new Double(spawngroup.yaw);
                                BasicHumanNpc hnpc = NpcSpawner.SpawnBasicHumanNpc(npc.id, npc.name, this.getServer().getWorld(this.world), spawngroup.x, spawngroup.y, spawngroup.z, yaw, pitch);

                                npc.npc = hnpc;

                                ItemStack iprimary = new ItemStack(npc.weapon);
                                ItemStack ihelmet = new ItemStack(npc.helmet);
                                ItemStack ichest = new ItemStack(npc.chest);
                                ItemStack ilegs = new ItemStack(npc.legs);
                                ItemStack iboots = new ItemStack(npc.boots);

                                npc.npc.getBukkitEntity().getInventory().setItemInHand(iprimary);
                                npc.npc.getBukkitEntity().getInventory().setHelmet(ihelmet);
                                npc.npc.getBukkitEntity().getInventory().setChestplate(ichest);
                                npc.npc.getBukkitEntity().getInventory().setLeggings(ilegs);
                                npc.npc.getBukkitEntity().getInventory().setBoots(iboots);
                                hnpc.parent = npc;

                                this.npclist.put(spawngroup.id + "" + npc.id, hnpc);
                                this.npcs.put(npc.id, npc);
                                spawngroup.active = true;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // TODO Auto-generated method stub
        try {
            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " disabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "npcx : error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }
    }

    public Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        return DriverManager.getConnection(dsn, Constants.SQL_Username, Constants.SQL_Password);
    }

    public String dbGetNPCname(String string) {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.executeQuery("SELECT name FROM npc WHERE id =" + string);
            rs = stmt.getResultSet();

            while (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) { } finally {
            if(rs != null)
                try { rs.close(); } catch (SQLException ex) { }

            if(stmt != null)
                try { stmt.close(); } catch (SQLException ex) { }

            if(conn != null)
                try { conn.close(); } catch (SQLException ex) { }
        }

        return "dummy";
    }

    public myFaction getFactionByID(int id) {
        for (myFaction f : factions) {
            if (f.id == id) {
                return f;
            }
        }

        return null;
    }

    public myFaction dbGetNPCfaction(String string) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT faction_id FROM npc WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, Integer.parseInt(string));
            stmt.executeQuery();
            rs = stmt.getResultSet();

            while (rs.next()) {
                int factionid = rs.getInt("faction_id");
                for (myFaction f : factions) {
                    if (f.id == factionid) {
                        return f;
                    }
                }
            }
        } catch (Exception e) { } finally {
            if(rs != null)
                try { rs.close(); } catch (SQLException ex) { }
            
            if(stmt != null)
                try { stmt.close(); } catch (SQLException ex) { }
            
            if(conn != null)
                try { conn.close(); } catch (SQLException ex) { }
        }

        return null;
    }

    public myLoottable dbGetNPCloottable(String string) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement("SELECT loottable_id FROM npc WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, Integer.parseInt(string));
            stmt.executeQuery();
            rs = stmt.getResultSet();

            while (rs.next()) {
                int lootttableid = rs.getInt("loottable_id");
                for (myLoottable f : loottables) {
                    if (f.id == lootttableid) {
                        return f;
                    }
                }

            }
        } catch (Exception e) { } finally {
            if(rs != null)
                try { rs.close(); } catch (SQLException ex) { }

            if(stmt != null)
                try { stmt.close(); } catch (SQLException ex) { }

            if(conn != null)
                try { conn.close(); } catch (SQLException ex) { }
        }

        return null;
    }

    public void fixDead() {
        int count = 0;
        for (myPlayer player : players.values()) {
            if (player.dead == true) {
                try {
                    for (Player p : getServer().getWorld(this.world).getPlayers()) {
                        if (player.name == p.getName()) {
                            player.player = p;
                            player.dead = false;
                            count++;
                        }
                    }
                } catch (ConcurrentModificationException e) {
                    System.out.println("npcx : FAILED establishing dead player");
                }
            }
        }

        if (count > 0) {
            System.out.println("npcx : re-established " + count + " dead players.");
        }
    }

    public static Server getBukkitServer() {
        return Server;
    }

    public static iConomy getiConomy() {
        return iConomy;
    }

    public static boolean setiConomy(iConomy plugin) {
        if (iConomy == null) {
            iConomy = plugin;
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onEnable() {
        // TODO Auto-generated method stub
        getDataFolder().mkdir();

        System.out.println("npcx : loading properties...");
        Constants.Plugin_Directory = getDataFolder().getPath();
        Constants.info = getDescription();
        Constants.load(FILE_PROPERTIES);
        System.out.println("npcx : finished loading properties.");

        // TODO Auto-generated method stub
        this.Server = getServer();
        this.PluginListener = new PluginListener(this);

        // Event Registration
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, PluginListener, Priority.Monitor, this);

        try {
            if (dsn == null) {
                dsn = "jdbc:mysql://" + Constants.SQL_Hostname + ":" + Constants.SQL_Port + "/" + Constants.SQL_Database;
            }

            try {
                System.out.println("npcx : initialising database connection");

                try {
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                } catch (ClassNotFoundException e) {
                    System.out.println("*****************************************");
                    System.out.println(" npcx : ERROR - Cannot find MySQL Library!");
                    System.out.println("*****************************************");
                    return;
                }

                try {
                    conn = DriverManager.getConnection(dsn, Constants.SQL_Username, Constants.SQL_Password);
                } catch (SQLException e) {
                    System.out.println("*****************************************");
                    System.out.println(" npcx : ERROR - Error during MySQL login ");
                    System.out.println("*****************************************");
                    e.printStackTrace();
                    return;
                }

                System.out.println("npcx : registering monitored events");

                PluginManager pm = getServer().getPluginManager();
                mEntityListener = new npcxEListener(this);
                mPlayerListener = new npcxPListener(this);

                pm.registerEvent(Type.ENTITY_TARGET, mEntityListener, Priority.Normal, this);
                pm.registerEvent(Type.ENTITY_DAMAGED, mEntityListener, Priority.Normal, this);
                pm.registerEvent(Type.ENTITY_DEATH, mEntityListener, Priority.Normal, this);
                pm.registerEvent(Type.CREATURE_SPAWN, mEntityListener, Priority.Normal, this);
                pm.registerEvent(Type.PLAYER_RESPAWN, mPlayerListener, Priority.Normal, this);
                pm.registerEvent(Type.PLAYER_JOIN, mPlayerListener, Priority.Normal, this);
                pm.registerEvent(Type.PLAYER_QUIT, mPlayerListener, Priority.Normal, this);
                pm.registerEvent(Type.PLAYER_CHAT, mPlayerListener, Priority.Normal, this);

                if (Constants.UPDATE) {
                    System.out.println("npcx : DB WIPE");

                    /*
                     * One time Database creation / TODO: Auto Upgrades
                     */
                    Statement factionlist = conn.createStatement();
                    String dropfactionlist = "DROP TABLE IF EXISTS faction_list; ";
                    String fationlistsql = "CREATE TABLE faction_list ( id int(11) NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, base int(11) DEFAULT NULL, PRIMARY KEY (id))";
                    factionlist.executeUpdate(dropfactionlist);
                    factionlist.executeUpdate(fationlistsql);
                    factionlist.close();

                    Statement player_faction = conn.createStatement();
                    String dropplayer_faction = "DROP TABLE IF EXISTS player_faction; ";
                    String player_factionsql = "CREATE TABLE player_faction (  id int(11) NOT NULL AUTO_INCREMENT,  player_name varchar(45) DEFAULT NULL,  faction_id int(11) DEFAULT NULL,  amount int(11) DEFAULT NULL,  PRIMARY KEY (id))";
                    player_faction.executeUpdate(dropplayer_faction);
                    player_faction.executeUpdate(player_factionsql);
                    player_faction.close();

                    Statement loottable_entries = conn.createStatement();
                    String droploottable_entries = "DROP TABLE IF EXISTS loottable_entries; ";
                    String loottable_entriessql = "CREATE TABLE loottable_entries (id int(11) NOT NULL AUTO_INCREMENT,  loottable_id int(11) DEFAULT NULL,  item_id int(11) DEFAULT NULL,  amount int(11) DEFAULT NULL,  PRIMARY KEY (id))";
                    loottable_entries.executeUpdate(droploottable_entries);
                    loottable_entries.executeUpdate(loottable_entriessql);
                    loottable_entries.close();

                    Statement loottable = conn.createStatement();
                    String droploottable = "DROP TABLE IF EXISTS loottables; ";
                    String loottablesql = "CREATE TABLE loottables (  id int(11) NOT NULL AUTO_INCREMENT,  name varchar(50) DEFAULT NULL,  PRIMARY KEY (id))";
                    loottable.executeUpdate(droploottable);
                    loottable.executeUpdate(loottablesql);
                    loottable.close();

                    Statement npc_faction = conn.createStatement();
                    String dropnpc_faction = "DROP TABLE IF EXISTS npc_faction; ";
                    String npc_factionsql = "CREATE TABLE npc_faction (id int(11) NOT NULL AUTO_INCREMENT, npc_id int(11) DEFAULT NULL, faction_id int(11) DEFAULT NULL, amount int(11) DEFAULT NULL,PRIMARY KEY (id))";
                    npc_faction.executeUpdate(dropnpc_faction);
                    npc_faction.executeUpdate(npc_factionsql);
                    npc_faction.close();


                    Statement s2 = conn.createStatement();
                    String droptable = "DROP TABLE IF EXISTS npc; ";
                    String npctable = "CREATE TABLE npc (  id int(10) unsigned NOT NULL AUTO_INCREMENT,  name char(40) DEFAULT NULL,  category char(40) DEFAULT NULL,  faction_id int(11) DEFAULT NULL,  loottable_id int(11) DEFAULT NULL,  weapon int(11) DEFAULT NULL,  helmet int(11) DEFAULT NULL,  chest int(11) DEFAULT NULL,  legs int(11) DEFAULT NULL,  boots int(11) DEFAULT NULL,  PRIMARY KEY (id)) ";
                    s2.executeUpdate(droptable);
                    s2.executeUpdate(npctable);


                    String droptable0 = "DROP TABLE IF EXISTS pathgroup; ";
                    String npctable0 = "CREATE TABLE pathgroup ( id INT UNSIGNED NOT NULL AUTO_INCREMENT, PRIMARY KEY (id),name CHAR(40),category CHAR(40))";
                    s2.executeUpdate(droptable0);
                    s2.executeUpdate(npctable0);

                    String droptable1 = "DROP TABLE IF EXISTS pathgroup_entries; ";
                    String npctable1 = "CREATE TABLE pathgroup_entries (  id int(10) unsigned NOT NULL AUTO_INCREMENT,  s int(11) DEFAULT NULL,  pathgroup int(11) DEFAULT NULL,  name char(40) DEFAULT NULL,  x char(40) DEFAULT NULL,  y char(40) DEFAULT NULL,  z char(40) DEFAULT NULL,  yaw char(40) DEFAULT NULL,  pitch char(40) DEFAULT NULL,  PRIMARY KEY (id))";
                    s2.executeUpdate(droptable1);
                    s2.executeUpdate(npctable1);

                    String droptable2 = "DROP TABLE IF EXISTS spawngroup; ";
                    String spawngrouptable = "CREATE TABLE spawngroup (  id int(10) unsigned NOT NULL AUTO_INCREMENT,  name char(40) DEFAULT NULL,  world char(40) DEFAULT NULL,  category char(40) DEFAULT NULL,  x char(40) DEFAULT NULL,  y char(40) DEFAULT NULL,  z char(40) DEFAULT NULL,  yaw char(40) DEFAULT NULL,  pitch char(40) DEFAULT NULL,  pathgroupid int(10) DEFAULT NULL,  PRIMARY KEY (id))";
                    s2.executeUpdate(droptable2);

                    s2.executeUpdate(spawngrouptable);

                    String droptable3 = "DROP TABLE IF EXISTS spawngroup_entries; ";
                    String sgetable = "CREATE TABLE spawngroup_entries ( id INT UNSIGNED NOT NULL AUTO_INCREMENT, PRIMARY KEY (id),spawngroupid int,npcid int)";
                    s2.executeUpdate(droptable3);

                    s2.executeUpdate(sgetable);

                    String droptable4 = "DROP TABLE IF EXISTS npc_triggerwords; ";
                    String spawngrouptable4 = "CREATE TABLE npc_triggerwords ( id INT UNSIGNED NOT NULL AUTO_INCREMENT, PRIMARY KEY (id),npcid int,triggerword CHAR(40),reply VARCHAR(256),category CHAR(40))";
                    s2.executeUpdate(droptable4);
                    s2.executeUpdate(spawngrouptable4);

                    s2.close();

                    System.out.println("npcx : finished table configuration");

                    world = Constants.WORLD;
                    Constants.Configuration.setBoolean("update", false);
                }

                try {
                    // Load faction_list
                    Statement stmt = conn.createStatement();
                    stmt.executeQuery("SELECT * FROM faction_list");
                    ResultSet rs = stmt.getResultSet();
                    int countfaction = 0;
                    System.out.println("npcx : loading factions");

                    while (rs.next()) {
                        myFaction faction = new myFaction();
                        faction.id = rs.getInt("id");
                        faction.name = rs.getString("name");
                        faction.base = rs.getInt("base");
                        countfaction++;
                        factions.add(faction);
                    }

                    rs.close();
                    stmt.close();

                    System.out.println("npcx : Loaded " + countfaction + " factions.");

                } catch (NullPointerException e) {
                    System.out.println("npcx : ERROR - faction loading cancelled!");
                }

                try {
                    // Load faction_list
                    Statement stmt = conn.createStatement();
                    stmt.executeQuery("SELECT * FROM pathgroup");
                    ResultSet rs = stmt.getResultSet();

                    // Currently
                    System.out.println("npcx : loading pathgroups");

                    int countpg = 0;
                    while (rs.next()) {
                        // Pathing Data
                        myPathgroup pathgroup = new myPathgroup();
                        pathgroup.id = rs.getInt("id");
                        pathgroup.name = rs.getString("name");
                        pathgroup.category = rs.getInt("category");

                        // Queries
                        Statement sFindEntries = conn.createStatement();
                        sFindEntries.executeQuery("SELECT * FROM pathgroup_entries WHERE pathgroup = " + pathgroup.id);
                        ResultSet rsEntries = sFindEntries.getResultSet();

                        int countentries = 0;
                        while (rsEntries.next()) {
                            myPathgroup_entry entry = new myPathgroup_entry();
                            entry.id = rsEntries.getInt("id");
                            entry.s = rsEntries.getInt("s");
                            entry.name = rsEntries.getString("name");
                            entry.pathgroupid = rsEntries.getInt("pathgroup");
                            entry.x = rsEntries.getInt("x");
                            entry.y = rsEntries.getInt("y");
                            entry.z = rsEntries.getInt("z");
                            entry.yaw = rsEntries.getFloat("yaw");
                            entry.pitch = rsEntries.getFloat("pitch");
                            entry.parent = pathgroup;
                            countentries++;

                            pathgroup.pathgroupentries.add(entry);
                        }

                        rsEntries.close();
                        sFindEntries.close();
                        countpg++;

                        pathgroups.add(pathgroup);
                    }

                    rs.close();
                    stmt.close();

                    System.out.println("npcx : Loaded " + countpg + " pathgroup.");
                } catch (NullPointerException e) {
                    System.out.println("npcx : ERROR - faction loading cancelled!");
                }

                try {
                    // Load loot tables
                    Statement stmt = conn.createStatement();
                    stmt.executeQuery("SELECT * FROM loottables");
                    ResultSet rs = stmt.getResultSet();

                    // Currently
                    System.out.println("npcx : loading loottables");

                    int countloottables = 0;
                    while (rs.next()) {
                        myLoottable loottable = new myLoottable(rs.getInt("id"), rs.getString("name"));
                        loottable.id = rs.getInt("id");
                        loottable.name = rs.getString("name");

                        Statement sFindEntries = conn.createStatement();
                        sFindEntries.executeQuery("SELECT * FROM loottable_entries WHERE loottable_id = " + loottable.id);
                        ResultSet rsEntries = sFindEntries.getResultSet();

                        int countentries = 0;
                        while (rsEntries.next()) {
                            myLoottable_entry entry = new myLoottable_entry();
                            entry.id = rsEntries.getInt("id");
                            entry.itemid = rsEntries.getInt("item_id");
                            entry.loottable_id = rsEntries.getInt("loottable_id");
                            entry.amount = rsEntries.getInt("amount");
                            entry.parent = loottable;
                            countentries++;

                            loottable.loottable_entries.add(entry);
                        }

                        rsEntries.close();
                        sFindEntries.close();

                        countloottables++;
                        loottables.add(loottable);
                    }

                    rs.close();
                    stmt.close();

                    System.out.println("npcx : Loaded " + countloottables + " loottables.");
                } catch (NullPointerException e) {
                    System.out.println("npcx : ERROR - loottable loading cancelled!");
                }

                try {
                    // Load Spawngroups
                    Statement stmt = conn.createStatement();
                    stmt.executeQuery("SELECT id, name, category,x,y,z,world,yaw,pitch,pathgroupid FROM spawngroup");
                    ResultSet rs = stmt.getResultSet();

                    System.out.println("npcx : loading spawngroups");

                    int countSpawnGroups = 0;
                    while (rs.next()) {
                        // load spawngroup into cache
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");

                        // Create a new spawngroup
                        mySpawngroup spawngroup = new mySpawngroup();
                        spawngroup.name = nameVal;

                        //System.out.println("npcx : + " + nameVal);
                        spawngroup.id = idVal;
                        spawngroup.x = Double.parseDouble(rs.getString("x"));
                        spawngroup.y = Double.parseDouble(rs.getString("y"));
                        spawngroup.z = Double.parseDouble(rs.getString("z"));
                        spawngroup.yaw = Double.parseDouble(rs.getString("yaw"));
                        spawngroup.pitch = Double.parseDouble(rs.getString("pitch"));
                        spawngroup.pathgroup = dbGetSpawngrouppg(spawngroup.id);

                        // Add to our spawngroup hashmap
                        this.spawngroups.put(Integer.toString(idVal), spawngroup);

                        // Load npcs into spawngroups
                        Statement stmtNPC = conn.createStatement();
                        stmtNPC.executeQuery("SELECT npc.weapon As weapon, npc.helmet As helmet,npc.chest As chest,npc.legs As legs,npc.boots As boots,spawngroup_entries.spawngroupid As spawngroupid,spawngroup_entries.npcid As npcid, npc.name As name, npc.category As category, npc.loottable_id As loottable_id, npc.faction_id As faction_id FROM spawngroup_entries,npc WHERE npc.id = spawngroup_entries.npcid AND spawngroup_entries.spawngroupid =" + idVal);
                        ResultSet rsNPC = stmtNPC.getResultSet();

                        while (rsNPC.next()) {
                            myNPC npc = new myNPC(this, fetchTriggerWords(rsNPC.getInt("npcid")));
                            npc.spawngroup = spawngroup;
                            npc.id = rsNPC.getString("npcid");
                            npc.name = rsNPC.getString("name");
                            npc.category = rsNPC.getString("category");
                            npc.weapon = rsNPC.getInt("weapon");
                            npc.helmet = rsNPC.getInt("helmet");
                            npc.chest = rsNPC.getInt("chest");
                            npc.legs = rsNPC.getInt("legs");
                            npc.boots = rsNPC.getInt("boots");

                            for (myFaction faction : factions) {
                                if (rsNPC.getInt("faction_id") == faction.id) {
                                    npc.faction = faction;
                                }
                            }

                            npc.pathgroup = spawngroup.pathgroup;

                            for (myLoottable loottable : loottables) {
                                if (rsNPC.getInt("loottable_id") == loottable.id) {
                                    npc.loottable = loottable;
                                }
                            }

                            //System.out.println("npcx : + npc.name + " + rs11.getString ("npcid"));
                            spawngroup.npcs.put(rsNPC.getString("npcid"), npc);
                        }

                        // System.out.println ("id = " + idVal+ ", name = " + nameVal+ ", category = " + catVal);
                        ++countSpawnGroups;
                    }

                    rs.close();
                    stmt.close();

                    System.out.println(countSpawnGroups + " spawngroups loaded");
                } catch (NullPointerException e) {
                    System.out.println("npcx : ERROR - spawngroup loading cancelled!");
                }

                //this.HumanNPCList = new BasicHumanNpcList();
                //System.out.println("npcx : caching npcs");

            } catch (Exception e) {
                e.printStackTrace();
            }

            PluginDescriptionFile pdfFile = this.getDescription();
            logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");
        } catch (Exception e) {
            logger.log(Level.WARNING, "npcx : error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return;
        }

        think();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        try {
            if (!command.getName().toLowerCase().equals("npcx"))
                return false;

            if (!(sender instanceof Player))
                return false;

            if (sender.isOp() == false)
                return false;

            Player player = (Player) sender;

            if (args.length < 1) {
                player.sendMessage("Insufficient arguments /npcx spawngroup");
                player.sendMessage("Insufficient arguments /npcx faction");
                player.sendMessage("Insufficient arguments /npcx loottable");
                player.sendMessage("Insufficient arguments /npcx npc");
                player.sendMessage("Insufficient arguments /npcx pathgroup");
                return false;
            }

            String subCommand = args[0].toLowerCase();
            //debug: logger.log(Level.WARNING, "npcx : " + command.getName().toLowerCase() + "(" + subCommand + ")");

            Location l = player.getLocation();
            if (subCommand.equals("debug")) { }
            if (subCommand.equals("spawngroup")) {
                // Overview:
                // A spawngroup is like a container. It contains many npcs and any one of them could spawn randomly.
                // If you placed just one npc in the group only one npc would spawn. This allows you to create 'rare' npcs

                // Spawngroups need to be assigned to a location with 'spawngroup place' Once assigned that group
                // will spawn in that location and remain stationary

                // If a path is assigned to the spawn group, the npc will follow the path continuously after spawning
                // at the location of 'spawngroup place'

                // todo: functionality
                // creates a new spawngroup with name
                // adds an npc to a spawngroup with a chance
                // makes the spawngroup spawn at your location
                // assigns a path to the spawngroup
                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx spawngroup create spawngroupname");
                    player.sendMessage("Insufficient arguments /npcx spawngroup add spawngroupid npcid");
                    player.sendMessage("Insufficient arguments /npcx spawngroup pathgroup spawngroupid pathgroupid");
                    player.sendMessage("Insufficient arguments /npcx spawngroup list [name]");
                    return false;
                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup create spawngroupname");

                    } else {
                        player.sendMessage("Created spawngroup: " + args[2]);
                        double x = player.getLocation().getX();
                        double y = player.getLocation().getY();
                        double z = player.getLocation().getZ();
                        double pitch = player.getLocation().getPitch();
                        double yaw = player.getLocation().getYaw();

                        PreparedStatement stmt = conn.prepareStatement("INSERT INTO spawngroup (name,x,y,z,pitch,yaw) VALUES (?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        stmt.setString(1, args[2]);
                        stmt.setString(2, Double.toString(x));
                        stmt.setString(3, Double.toString(y));
                        stmt.setString(4, Double.toString(z));
                        stmt.setString(5, Double.toString(pitch));
                        stmt.setString(6, Double.toString(yaw));
                        stmt.executeUpdate();

                        ResultSet keyset = stmt.getGeneratedKeys();

                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);

                        }

                        stmt.close();
                        player.sendMessage("Spawngroup [" + key + "] now active at your position");

                        mySpawngroup sg = new mySpawngroup();
                        sg.id = key;
                        sg.name = args[2];
                        sg.x = x;
                        sg.y = y;
                        sg.z = z;
                        sg.pitch = pitch;
                        sg.yaw = yaw;
                        sg.world = player.getWorld();
                        this.spawngroups.put(Integer.toString(key), sg);

                        System.out.println("npcx : + cached new spawngroup(" + args[2] + ")");
                    }
                }

                if (args[1].equals("pathgroup")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup pathgroup spawngroupid pathgroupid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE spawngroup SET pathgroupid = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);
                        stmt.executeUpdate();

                        for (mySpawngroup sg : spawngroups.values()) {
                            if (sg.id == Integer.parseInt(args[2])) {
                                if (Integer.parseInt(args[3]) != 0) {
                                    sg.pathgroup = getPathgroupByID(Integer.parseInt(args[3]));
                                    player.sendMessage("npcx : Updated spawngroups cached pathgroup (" + args[3] + "): " + sg.pathgroup.name);
                                } else {
                                    sg.pathgroup = null;
                                    player.sendMessage("npcx : Updated spawngroups cached pathgroup (0)");
                                }
                            }
                        }

                        player.sendMessage("Updated pathgroup ID:" + args[3] + " on spawngroup ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("add")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx spawngroup add spawngroup npcid");
                    } else {
                        player.sendMessage("Added to spawngroup " + args[2] + "<" + args[3] + ".");

                        // add to database
                        PreparedStatement s2 = conn.prepareStatement("INSERT INTO spawngroup_entries (spawngroupid,npcid) VALUES (?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setString(1, args[2]);
                        s2.setString(2, args[3]);
                        s2.executeUpdate();
                        player.sendMessage("NPC [" + args[3] + "] added to group [" + args[2] + "]");

                        // add to cached spawngroup
                        for (mySpawngroup sg : this.spawngroups.values()) {
                            if (sg.id == Integer.parseInt(args[2])) {
                                myNPC npc = new myNPC(this, fetchTriggerWords(Integer.parseInt(args[3])));
                                PreparedStatement stmtNPC = conn.prepareStatement("SELECT * FROM npc WHERE id = ?;");
                                stmtNPC.setString(1, args[3]);
                                stmtNPC.executeQuery();
                                ResultSet rsNPC = stmtNPC.getResultSet();
                                int count = 0;

                                while (rsNPC.next()) {
                                    npc.name = rsNPC.getString("name");
                                    npc.category = rsNPC.getString("category");
                                    npc.faction = dbGetNPCfaction(args[3]);
                                    npc.loottable = dbGetNPCloottable(args[3]);
                                    npc.helmet = rsNPC.getInt("helmet");
                                    npc.pathgroup = sg.pathgroup;
                                    npc.chest = rsNPC.getInt("chest");
                                    npc.legs = rsNPC.getInt("legs");
                                    npc.boots = rsNPC.getInt("boots");
                                    npc.weapon = rsNPC.getInt("weapon");
                                    ++count;
                                }

                                rsNPC.close();
                                stmtNPC.close();
                                npc.spawngroup = sg;
                                npc.id = args[3];
                                System.out.println("npcx : + cached new spawngroup entry(" + args[3] + ")");
                                sg.npcs.put(args[3], npc);
                            }
                        }

                        mySpawngroup sg = new mySpawngroup();

                        // close db
                        s2.close();
                    }
                }

                if (args[1].equals("list")) {
                    player.sendMessage("Spawngroups:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = conn.prepareStatement("SELECT id, name, category FROM spawngroup ORDER BY ID DESC LIMIT 10");
                    } else {

                        sglist = conn.prepareStatement("SELECT id, name, category FROM spawngroup WHERE name LIKE '%" + args[2] + "%'");
                    }

                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();
                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");
                        player.sendMessage(
                                "id = " + idVal
                                + ", name = " + nameVal
                                + ", category = " + catVal);
                        ++count;
                    }

                    rs.close();
                    sglist.close();
                    player.sendMessage(count + " rows were retrieved");
                }
            }

            //
            // START LOOTTABLE
            //
            if (subCommand.equals("loottable")) {
                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx loottable create loottablename");
                    player.sendMessage("Insufficient arguments /npcx loottable list");
                    player.sendMessage("Insufficient arguments /npcx loottable add loottableid itemid amount");
                    return false;
                }

                if (args[1].equals("add")) {
                    if (args.length < 5) {
                        player.sendMessage("Insufficient arguments /npcx loottable add loottableid itemid amount");
                        return false;
                    } else {
                        player.sendMessage("Added to loottable " + args[2] + "<" + args[3] + "x" + args[4] + ".");

                        // add to database
                        PreparedStatement s2 = conn.prepareStatement("INSERT INTO loottable_entries (loottable_id,item_id,amount) VALUES (?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setString(1, args[2]);
                        s2.setString(2, args[3]);
                        s2.setString(3, args[4]);
                        s2.executeUpdate();

                        player.sendMessage("NPC [" + args[3] + "x" + args[4] + "] added to group [" + args[2] + "]");

                        // add to cached loottable
                        for (myLoottable lt : this.loottables) {
                            if (lt.id == Integer.parseInt(args[2])) {
                                myLoottable_entry entry = new myLoottable_entry();
                                entry.id = Integer.parseInt(args[2]);
                                entry.itemid = Integer.parseInt(args[3]);
                                entry.amount = Integer.parseInt(args[4]);
                                lt.loottable_entries.add(entry);

                                System.out.println("npcx : + cached new loottable entry(" + args[3] + ")");
                            }
                        }

                        mySpawngroup sg = new mySpawngroup();

                        // close db
                        s2.close();
                    }
                }

                if (args[1].equals("create")) {
                    if (args.length < 2) {
                        player.sendMessage("Insufficient arguments /npcx loottable create loottablename");
                        return false;
                    } else {
                        try {
                            PreparedStatement stmt = conn.prepareStatement("INSERT INTO loottables (name) VALUES (?);", Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, args[2]);
                            stmt.executeUpdate();
                            ResultSet keyset = stmt.getGeneratedKeys();

                            int key = 0;
                            if (keyset.next()) {
                                // Retrieve the auto generated key(s).
                                key = keyset.getInt(1);
                            }

                            stmt.close();

                            player.sendMessage("Loottable [" + key + "] now active");

                            myLoottable fa = new myLoottable(key, args[2]);
                            fa.id = key;
                            fa.name = args[2];
                            this.loottables.add(fa);

                            System.out.println("npcx : + cached new loottable (" + args[2] + ")");
                        } catch (IndexOutOfBoundsException e) {
                            player.sendMessage("Insufficient arguments");
                        }
                    }
                }

                if (args[1].equals("list")) {
                    player.sendMessage("Loottables:");

                    Statement s = conn.createStatement();
                    s.executeQuery("SELECT id, name FROM loottables");
                    ResultSet rs = s.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal);
                        Statement sFindEntries = conn.createStatement();
                        sFindEntries.executeQuery("SELECT * FROM loottable_entries WHERE loottable_id = " + idVal);
                        ResultSet rsEntries = sFindEntries.getResultSet();

                        int countentries = 0;
                        while (rsEntries.next()) {
                            int id = rsEntries.getInt("id");
                            int itemid = rsEntries.getInt("item_id");
                            int loottableid = rsEntries.getInt("loottable_id");
                            int amount = rsEntries.getInt("amount");

                            player.sendMessage(" + id = " + id + ", loottableid = " + loottableid + ", itemid = " + itemid + ", amount = " + amount);
                            countentries++;
                        }
                        player.sendMessage(countentries + " entries in this set");
                        ++count;
                    }

                    rs.close();
                    s.close();

                    player.sendMessage(count + " loottables were retrieved");
                }
            }
            // END LOOTTABLE            

            //
            // START FACTION
            //
            if (subCommand.equals("faction")) {
                if (args.length < 2) {
                    player.sendMessage("Insufficient arguments /npcx faction create baseamount factionname");
                    player.sendMessage("Insufficient arguments /npcx faction list");
                    return false;
                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx faction create baseamount factionname");
                    } else {
                        try {
                            PreparedStatement stmt = conn.prepareStatement("INSERT INTO faction_list (name,base) VALUES (?,?);", Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, args[3]);
                            stmt.setInt(2, Integer.parseInt(args[2]));
                            stmt.executeUpdate();
                            ResultSet keyset = stmt.getGeneratedKeys();

                            int key = 0;
                            if (keyset.next()) {
                                // Retrieve the auto generated key(s).
                                key = keyset.getInt(1);
                            }

                            stmt.close();
                            player.sendMessage("Faction [" + key + "] now active");
                            myFaction fa = new myFaction();
                            fa.id = key;
                            fa.name = args[3];
                            fa.base = Integer.parseInt(args[2]);
                            this.factions.add(fa);
                            System.out.println("npcx : + cached new faction(" + args[3] + ")");
                        } catch (IndexOutOfBoundsException e) {
                            player.sendMessage("Insufficient arguments");
                        }
                    }
                }

                if (args[1].equals("list")) {
                    player.sendMessage("Factions:");

                    Statement s = conn.createStatement();
                    s.executeQuery("SELECT id, name, base FROM faction_list");
                    ResultSet rs = s.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String baseVal = rs.getString("base");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", base = " + baseVal);
                        ++count;
                    }

                    rs.close();
                    s.close();

                    player.sendMessage(count + " rows were retrieved");
                }
            }
            // END FACTION

            if (subCommand.equals("pathgroup")) {
                if (args.length < 2) {
                    // todo: need to implement npc types here ie: 0 = default 1 = banker 2 = merchant
                    // todo: need to implement '/npcx npc edit' here
                    player.sendMessage("Insufficient arguments /npcx pathgroup create name");

                    // todo needs to force the player to provide a search term to not spam them with lots of results in the event of a huge npc list
                    player.sendMessage("Insufficient arguments /npcx pathgroup list");
                    player.sendMessage("Insufficient arguments /npcx pathgroup add pathgroupid order");
                    return false;
                }

                if (args[1].equals("add")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx pathgroup add pathgroupid order");
                    } else {
                        player.sendMessage("Added to pathgroup " + args[2] + "<" + args[3] + ".");

                        // add to database
                        PreparedStatement s2 = conn.prepareStatement("INSERT INTO pathgroup_entries (pathgroup,s,x,y,z,pitch,yaw) VALUES (?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
                        s2.setString(1, args[2]);
                        s2.setString(2, args[3]);
                        s2.setDouble(3, player.getLocation().getX());
                        s2.setDouble(4, player.getLocation().getY());
                        s2.setDouble(5, player.getLocation().getZ());
                        s2.setFloat(6, player.getLocation().getPitch());
                        s2.setFloat(7, player.getLocation().getYaw());
                        s2.executeUpdate();

                        player.sendMessage("Pathing Position [" + args[3] + "] added to pathggroup [" + args[2] + "]");

                        // add to cached spawngroup
                        for (myPathgroup pg : this.pathgroups) {
                            if (pg.id == Integer.parseInt(args[2])) {
                                myPathgroup_entry pge = new myPathgroup_entry();
                                pge.parent = pg;
                                pge.pathgroupid = Integer.parseInt(args[2]);
                                pge.x = player.getLocation().getX();
                                pge.y = player.getLocation().getY();
                                pge.z = player.getLocation().getZ();
                                pge.pitch = player.getLocation().getPitch();
                                pge.yaw = player.getLocation().getYaw();
                                pge.s = Integer.parseInt(args[3]);
                                System.out.println("npcx : + cached new pathgroup entry(" + args[3] + ")");
                                pg.pathgroupentries.add(pge);
                            }
                        }
                        s2.close();
                    }
                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx pathgroup create name");
                    } else {
                        PreparedStatement statementPCreate = conn.prepareStatement("INSERT INTO pathgroup (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                        statementPCreate.setString(1, args[2]);
                        statementPCreate.executeUpdate();
                        ResultSet keyset = statementPCreate.getGeneratedKeys();

                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);
                        }

                        myPathgroup pathgroup = new myPathgroup();
                        pathgroup.id = key;
                        pathgroup.name = args[2];
                        this.pathgroups.add(pathgroup);
                        statementPCreate.close();
                        player.sendMessage("Created pathgroup [" + key + "]: " + args[2]);
                    }
                }

                if (args[1].equals("list")) {
                    player.sendMessage("Pathgroups:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = conn.prepareStatement("SELECT id, name, category FROM pathgroup ORDER BY ID DESC LIMIT 10");
                    } else {
                        sglist = conn.prepareStatement("SELECT id, name, category FROM pathgroup WHERE name LIKE '%" + args[2] + "%'");
                    }

                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");
                        player.sendMessage("id = " + idVal + ", name = " + nameVal + ", category = " + catVal);
                        ++count;
                    }

                    rs.close();
                    sglist.close();

                    player.sendMessage(count + " rows were retrieved");
                }
            }

            if (subCommand.equals("npc")) {
                // Overview:
                // NPCs are just that, definitions of the mob you want to appear in game. There can be multiple of the same
                // npc in many spawngroups, for example if you wanted a custom npc called 'Thief' to spawn in several locations
                // you would put the npc into many spawn groups
                // In the future these npcs will support npctypes which determines how the npc will respond to right click, attack, etc events
                // ie for: bankers, normal npcs, merchants etc
                // Also loottables will be assignable
                // todo: functionality
                // creates a new npc with name
                if (args.length < 2) {
                    // todo: need to implement npc types here ie: 0 = default 1 = banker 2 = merchant
                    // todo: need to implement '/npcx npc edit' here
                    player.sendMessage("Insufficient arguments /npcx npc create name");
                    // todo needs to force the player to provide a search term to not spam them with lots of results in the event of a huge npc list
                    player.sendMessage("Insufficient arguments /npcx npc list [name]");
                    // spawns the npc temporarily at your current spot for testing
                    player.sendMessage("Insufficient arguments /npcx npc spawn name");
                    player.sendMessage("Insufficient arguments /npcx npc triggerword add npcid triggerword response");
                    player.sendMessage("Insufficient arguments /npcx npc faction npcid factionid");
                    player.sendMessage("Insufficient arguments /npcx npc loottable npcid loottableid");
                    player.sendMessage("Insufficient arguments /npcx npc category npcid category");
                    player.sendMessage("Insufficient arguments /npcx npc primary npcid itemid");
                    player.sendMessage("Insufficient arguments /npcx npc helmet npcid itemid");
                    player.sendMessage("Insufficient arguments /npcx npc chest npcid itemid");
                    player.sendMessage("Insufficient arguments /npcx npc legs npcid itemid");
                    player.sendMessage("Insufficient arguments /npcx npc boots npcid itemid");
                    return false;
                }

                if (args[1].equals("triggerword")) {
                    if (args.length < 6) {
                        player.sendMessage("Insufficient arguments /npcx npc triggerword add npcid triggerword response");
                    } else {
                        String reply = "";

                        int current = 6;
                        while (current <= args.length) {
                            reply = reply + args[current - 1] + " ";
                            current++;
                        }

                        reply = reply.substring(0, reply.length() - 1);

                        PreparedStatement statementTword = conn.prepareStatement("INSERT INTO npc_triggerwords (npcid,triggerword,reply) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                        statementTword.setString(1, args[3]);
                        statementTword.setString(2, args[4]);
                        statementTword.setString(3, reply);
                        statementTword.executeUpdate();
                        ResultSet keyset = statementTword.getGeneratedKeys();

                        int key = 0;
                        if (keyset.next()) {
                            // Retrieve the auto generated key(s).
                            key = keyset.getInt(1);
                        }

                        player.sendMessage("Added (" + npcs.values().size() + ") triggerword [" + key + "] to npc " + args[3]);

                        // add it to any spawned npcs
                        for (myNPC npc : npcs.values()) {
                            if (npc.id.equals(args[3])) {
                                System.out.println("npcx : adding reply because (" + npc.id + ") is (" + args[3] + ")  (" + reply + ") and trigger (" + reply + ") for [" + args[3] + "] npc to npc: " + npc.id);
                                myTriggerword tw = new myTriggerword();
                                tw.word = args[4];
                                tw.id = key;
                                tw.response = reply;
                                player.sendMessage("Added triggerword to Active npc " + args[3]);
                                npc.triggerwords.put(Integer.toString(tw.id), tw);
                            }
                        }
                    }
                }

                if (args[1].equals("chest")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc chest npcid itemid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET chest = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        //TODO not in schema yet
                        //stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.chest = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.chest);
                                i.setTypeId(Integer.parseInt(args[3]));
                                n.npc.getBukkitEntity().getInventory().setChestplate(i);
                                player.sendMessage("npcx : Updated living npc to cached chest (" + args[3] + "): " + n.chest);
                                stmt.executeUpdate();
                            }
                        }

                        player.sendMessage("Updated npc chest: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("helmet")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc helmet npcid itemid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET helmet = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        //TODO not in schema yet
                        //stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.helmet = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.helmet);
                                i.setTypeId(Integer.parseInt(args[3]));
                                n.npc.getBukkitEntity().getInventory().setHelmet(i);
                                player.sendMessage("npcx : Updated living npc to cached helmet (" + args[3] + "): " + n.helmet);
                                stmt.executeUpdate();
                            }
                        }

                        player.sendMessage("Updated npc helmet: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("weapon")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc weapon npcid itemid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET weapon = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        //TODO not in schema yet
                        //stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.weapon = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.weapon);
                                i.setTypeId(Integer.parseInt(args[3]));
                                n.npc.getBukkitEntity().getInventory().setItemInHand(i);
                                player.sendMessage("npcx : Updated living npc to cached weapon (" + args[3] + "): " + n.weapon);
                                stmt.executeUpdate();
                            }
                        }

                        player.sendMessage("Updated npc primary: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("boots")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc boots npcid itemid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET boots = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        //TODO not in schema yet
                        //stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.boots = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.boots);
                                i.setTypeId(Integer.parseInt(args[3]));
                                n.npc.getBukkitEntity().getInventory().setBoots(i);
                                player.sendMessage("npcx : Updated living npc to cached boots (" + args[3] + "): " + n.boots);
                                stmt.executeUpdate();
                            }
                        }

                        player.sendMessage("Updated npc boots: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("legs")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc legs npcid itemid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET legs = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);

                        //TODO not in schema yet
                        //stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.legs = Integer.parseInt(args[3]);
                                ItemStack i = new ItemStack(n.legs);
                                i.setTypeId(Integer.parseInt(args[3]));
                                n.npc.getBukkitEntity().getInventory().setLeggings(i);
                                player.sendMessage("npcx : Updated living npc to cached legs (" + args[3] + "): " + n.legs);
                                stmt.executeUpdate();
                            }
                        }

                        player.sendMessage("Updated npc legs: item ID:" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("faction")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc faction npcid factionid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET faction_id = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);
                        stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {

                                n.faction = getFactionByID(Integer.parseInt(args[3]));
                                player.sendMessage("npcx : Updated living npc to cached faction (" + args[3] + "): " + n.faction.name);
                                // when faction changes reset aggro and follow status
                                n.npc.attacking = null;
                                n.npc.following = null;
                            }
                        }

                        player.sendMessage("Updated npc faction ID:" + args[3] + " on NPC ID:[" + args[2] + "]");

                        stmt.close();
                    }
                }

                if (args[1].equals("category")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc category npcid category");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET category = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);
                        stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.category = args[3];
                                player.sendMessage("npcx : Updated living npc to cached category (" + args[3] + "): " + n.category);
                                // when faction changes reset aggro and follow status

                            }
                        }

                        player.sendMessage("Updated npc category :" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("list")) {
                    player.sendMessage("Npcs:");
                    PreparedStatement sglist;

                    if (args.length < 3) {
                        sglist = conn.prepareStatement("SELECT id, name, category FROM npc ORDER BY ID DESC LIMIT 10");
                    } else {
                        sglist = conn.prepareStatement("SELECT id, name, category FROM npc WHERE name LIKE '%" + args[2] + "%'");
                    }

                    sglist.executeQuery();
                    ResultSet rs = sglist.getResultSet();

                    int count = 0;
                    while (rs.next()) {
                        int idVal = rs.getInt("id");
                        String nameVal = rs.getString("name");
                        String catVal = rs.getString("category");
                        player.sendMessage("id = " + idVal+ ", name = " + nameVal+ ", category = " + catVal);
                        ++count;
                    }

                    rs.close();
                    sglist.close();
                    player.sendMessage(count + " rows were retrieved");
                }

                if (args[1].equals("loottable")) {
                    if (args.length < 4) {
                        player.sendMessage("Insufficient arguments /npcx npc loottable npcid loottableid");
                    } else {
                        PreparedStatement stmt = conn.prepareStatement("UPDATE npc SET loottable_id = ? WHERE id = ?;");
                        stmt.setString(1, args[3]);
                        stmt.setString(2, args[2]);
                        stmt.executeUpdate();

                        for (myNPC n : npcs.values()) {
                            if (n.id.matches(args[2])) {
                                n.loottable = getLoottableByID(Integer.parseInt(args[3]));
                                player.sendMessage("npcx : Updated living npc to cached loottable (" + args[3] + "): " + n.loottable.name);

                            }
                        }

                        player.sendMessage("Updated npc loottable ID:" + args[3] + " on NPC ID:[" + args[2] + "]");
                        stmt.close();
                    }
                }

                if (args[1].equals("create")) {
                    if (args.length < 3) {
                        player.sendMessage("Insufficient arguments /npcx npc create npcname");

                    } else {
                        PreparedStatement stmt = null;
                        ResultSet keyset = null;

                        try {
                            stmt = conn.prepareStatement("INSERT INTO npc (name,weapon,helmet,chest,legs,boots) VALUES (?,'267','0','307','308','309');", Statement.RETURN_GENERATED_KEYS);
                            stmt.setString(1, args[2]);
                            stmt.executeUpdate();
                            keyset = stmt.getGeneratedKeys();

                            int key = 0;
                            if (keyset.next()) {
                                // Retrieve the auto generated key(s).
                                key = keyset.getInt(1);
                            }

                            player.sendMessage("Created npc: " + args[2] + "ID:[" + key + "]");
                        } catch (SQLException E) { } finally {
                            if(stmt != null)
                                stmt.close();

                            if(keyset != null)
                                keyset.close();
                        }
                    }
                }

                if (args[1].equals("spawn")) {
                    player.sendMessage("Spawning new (temporary) NPC: " + args[2]);

                    // Temporary NPC Spawn
                    BasicHumanNpc hnpc = NpcSpawner.SpawnBasicHumanNpc(args[2], args[2], player.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
                    this.npclist.put(args[2], hnpc);

                    try {
                        if (args.length < 4) {
                            if (args[3].equals("1")) {

                                ItemStack is = new ItemStack(Material.IRON_SWORD);
                                is.setAmount(1);
                                hnpc.getBukkitEntity().setItemInHand(is);

                                /*
                                ItemStack ic = new ItemStack(Material.IRON_CHESTPLATE);
                                ic.setAmount(1);
                                hnpc.getBukkitEntity().getInventory().setChestplate(ic);

                                ItemStack ih = new ItemStack(Material.IRON_HELMET);
                                ih.setAmount(1);
                                hnpc.getBukkitEntity().getInventory().setHelmet(ih);

                                ItemStack il = new ItemStack(Material.IRON_LEGGINGS);
                                il.setAmount(1);
                                hnpc.getBukkitEntity().getInventory().setLeggings(il);

                                ItemStack ib = new ItemStack(Material.IRON_BOOTS);
                                ib.setAmount(1);
                                hnpc.getBukkitEntity().getInventory().setBoots(ib);

                                 */

                            }

                        }
                    } catch (Exception e) { }

                    return true;

                }

            }

            /*
             * 
             * For reference: NPC Spawner Code (thanks to the npc lib)

            // create npc-id npc-name
            if (subCommand.equals("create")) {
            if (args.length < 3) {
            player.sendMessage("Insufficient arguments /npcx create id name ");
            return false;
            }

            if (this.npclist.get(args[1]) != null) {
            player.sendMessage("This npc-id is already in use.");
            return true;
            }

            player.sendMessage("Creating npc....");

            BasicHumanNpc hnpc = NpcSpawner.SpawnBasicHumanNpc(args[1], args[2], player.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            this.npclist.put(args[1], hnpc);

            ItemStack is = new ItemStack(Material.BOOKSHELF);
            is.setAmount(1);
            hnpc.getBukkitEntity().setItemInHand(is);


            // attackme npc-id
            } else if (subCommand.equals("attackme")) {

            if (args.length < 2) {
            return false;
            }

            BasicHumanNpc npc = this.npclist.get(args[1]);
            if (npc != null) {
            npc.attackLivingEntity(player);
            return true;
            }

            // move npc-id
            } else if (subCommand.equals("move")) {
            if (args.length < 2) {
            return false;
            }

            BasicHumanNpc npc = this.npclist.get(args[1]);
            if (npc != null) {
            npc.moveTo(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            return true;
            }

            // spawnpig
            } else if (subCommand.equals("spawnpig")) {
            NpcSpawner.SpawnMob(MobType.PIG, player.getWorld(), l.getX(), l.getY(), l.getZ());
            }
            
             */
        } catch (Exception e) {
            sender.sendMessage("An error occured.");
            logger.log(Level.WARNING, "npcx: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return true;
        }

        return true;
    }

    private myPathgroup dbGetSpawngrouppg(int id) {
        try {
            conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT pathgroupid FROM spawngroup WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, id);
            stmt.executeQuery();
            ResultSet rs = stmt.getResultSet();

            while (rs.next()) {
                int pathgroupid = rs.getInt("pathgroupid");
                for (myPathgroup f : pathgroups) {
                    if (f.id == pathgroupid) {
                        return f;
                    }
                }
            }
        } catch (Exception e) { }

        return null;
    }

    private myPathgroup getPathgroupByID(int parseInt) {
        // TODO Auto-generated method stub
        for (myPathgroup g : pathgroups) {
            if (g.id == parseInt) {
                return g;
            }
        }
        return null;
    }

    private myLoottable getLoottableByID(int parseInt) {
        for (myLoottable f : loottables) {
            if (f.id == parseInt) {
                return f;
            }
        }

        return null;
    }
}
