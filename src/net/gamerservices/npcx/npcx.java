package net.gamerservices.npcx;
import com.nijiko.coelho.iConomy.iConomy;

import net.gamerservices.npclibfork.BasicHumanNpc;
import net.gamerservices.npclibfork.BasicHumanNpcList;
import net.gamerservices.npclibfork.NpcEntityTargetEvent;
import net.gamerservices.npclibfork.NpcSpawner;
import net.gamerservices.npclibfork.NpcEntityTargetEvent.NpcTargetReason;

import org.bukkit.plugin.PluginManager;
import java.util.HashMap;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.CreatureType;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import org.bukkit.event.Event.Type;
import java.util.logging.Logger;
import org.bukkit.event.Event.Priority;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
public class npcx extends JavaPlugin {

	private static final Logger logger = Logger.getLogger("Minecraft");
	
	
	private npcxEListener mEntityListener;
	private npcxPListener mPlayerListener;
	public myUniverse universe;
	
	// iconomy
	private static PluginListener PluginListener = null;
    private static iConomy iConomy = null;
    private static Server Server = null;
    // end iconomy
	public BasicHumanNpcList npclist = new BasicHumanNpcList();
	private Timer tick = new Timer();
	public void onNPCDeath(BasicHumanNpc npc)
	{
		for (myPlayer player : universe.players.values()){
				if (player.target == npc)
				{
					player.target = null;
					
				}
		}
		
		
		for (myLoottable lt : this.universe.loottables)
		{
			if (npc.parent != null)
			{
				if (npc.parent.loottable == lt)
				{
					for (myLoottable_entry lte : lt.loottable_entries)
					{
						npc.getBukkitEntity().getWorld().dropItem(
								new Location (
										npc.getBukkitEntity().getWorld(),
										npc.getBukkitEntity().getLocation().getX(),
										npc.getBukkitEntity().getLocation().getY(),
										npc.getBukkitEntity().getLocation().getZ()
										),
								new ItemStack(lte.itemid));
						
					}
				}
			}
		}
		
		npclist.remove(npc);
		universe.npcs.remove(npc);
		NpcSpawner.RemoveBasicHumanNpc(npc);
		
		if (npc.parent != null)
		{
			npc.parent.spawngroup.activecountdown = 100;
		}
		
		
		
		
		
		
	}

	public double getDistance(double d, double e)
	{
		return d-e;
	}
	
	public void think()
	{
		tick.schedule(new Tick(this), 1 * 300);
		
		fixDead();
		
		// check npc logic
		
		try
		{
			for (myNPC npc : universe.npcs.values())
			{
				if (npc.npc != null)
				{
					npc.npc.doThinkGreater();
					
					//always do pathgroups after think()
					
					npc.npc.doThinkLesser();
					
					if (this.universe.players.size() > 0)
					{
						try
						{
							for (myPlayer player : this.universe.players.values())
							{
								if (player.player != null)
								{
									if (player.player.getHealth() > 0)
							    	{
										double distancex = getDistance(npc.npc.getBukkitEntity().getLocation().getX(), player.player.getLocation().getX());
									    double distancey = getDistance(npc.npc.getBukkitEntity().getLocation().getY(), player.player.getLocation().getY());
									    double distancez = getDistance(npc.npc.getBukkitEntity().getLocation().getZ(), player.player.getLocation().getZ());
									    
									    if (distancex > -5 && distancey > -5 && distancez > -5 && distancex < 5 && distancey < 5 && distancez < 5)
									    {
									    		if (npc.parent != null)
									    		{
									    			if (npc.npc.parent.faction != null)
									    			{
									    				if (npc.npc.parent.faction.base <= -1000)
									    				{
									    					npc.npc.aggro = player.player;
									    					npc.npc.follow = player.player;
									    				}
									    			} else {
									    				//System.out.println("npcx : i have no faction so ill be be neutral");
									    			}
									    		}
									    	
										}
									    
										
							    	}
								}
							}
						}
							catch (Exception e)
						{
							// Concurrent modification occured
							e.printStackTrace();
						}
						
					}
					
					// VS MONSTERS
					
					if (this.universe.monsters.size() > 0)
					{
						try
						{
							
							for (LivingEntity e : this.universe.monsters)
							{
								if (e.getHealth() > 0)
						    	{
									double distancex = getDistance(npc.npc.getBukkitEntity().getLocation().getX(), e.getLocation().getX());
								    double distancey = getDistance(npc.npc.getBukkitEntity().getLocation().getY(), e.getLocation().getY());
								    double distancez = getDistance(npc.npc.getBukkitEntity().getLocation().getZ(), e.getLocation().getZ());
							
								    if (e instanceof Monster)
								    {
								    	// mosnter in range?
									    if (distancex > -5 && distancey > -5 && distancez > -5 && distancex < 5 && distancey < 5 && distancez < 5)
									    {
										    // monster in range but is it worth chasing?
									    	
									    	// face direction
		
									    	// line of site
		
									    	boolean foundresult = false;
									    	for (Block blockinsight : e.getLineOfSight(null, 5))
									    	{
									    		// Entities seem to be Y + 1
									    		Location eloc = e.getLocation();
									    		eloc.setY(eloc.getY()+1);
									    		
									    		if (blockinsight == eloc.getBlock())
									    		{
									    			foundresult = true;
										    		npc.npc.aggro =  e;
										    		npc.npc.follow =   e;
									    		}
									    	}
									    	if (foundresult == false)
									    	{
									    		//System.out.println("I can hear one but can't see it");
									    		npc.npc.faceLocation(e.getLocation());
									    		npc.npc.aggro = null;
									    		npc.npc.follow = null;
									    		
									    	}
		
									    	
										}
								    }
						    	}
							}
						} 
							catch (Exception e)
						{
							// Concurrent modification occured
							e.printStackTrace();
						}
					}
				}
				
			}
			
				
				
			
			// check spawngroups
			
			for (mySpawngroup spawngroup : universe.spawngroups.values())
			{
				
				if (spawngroup.activecountdown > 0)
				{
					spawngroup.activecountdown--;
					
					if (spawngroup.activecountdown == 1)
					{
						spawngroup.active = false;					
					}
					
				}
	
				
				if (!spawngroup.active)
				{
						
					//System.out.println("npcx : found inactive spawngroup ("+ spawngroup.id +") with :[" + spawngroup.npcs.size() + "]");
					int count = 0;
					Random generator = new Random();
					Object[] values = spawngroup.npcs.values().toArray();
					
					if (values.length > 0)
					{
					
					myNPC npc = (myNPC) values[generator.nextInt(values.length)];
					
						try
						{
					
						// is there at least one player in game?
						if (this.getServer().getOnlinePlayers().length > 0)
						{
							if (!spawngroup.active)
							{
								npc.spawngroup = spawngroup;
								
								//System.out.println("npcx : made spawngroup active");
								Double  pitch = new Double(spawngroup.pitch);
								Double yaw = new Double(spawngroup.yaw);
								BasicHumanNpc hnpc = npc.Spawn(npc.id, npc.name, this.getServer().getWorld(this.universe.defaultworld), spawngroup.x, spawngroup.y, spawngroup.z,yaw , pitch);
								
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
				                
								this.npclist.put(spawngroup.id + "-" + npc.id, hnpc);
								this.universe.npcs.put(spawngroup.id+"-"+npc.id,npc);
								spawngroup.active = true;
							}
						}
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
					
				}
			}
		} catch (ConcurrentModificationException e)
		{
			// its locked being written to atm, try again on next loop
			
			
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

	public String dbGetNPCname(String string)
	{
		try
		{
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
	        universe.conn = DriverManager.getConnection (universe.dsn, universe.dbuser, universe.dbpass);
	        Statement s11 = this.universe.conn.createStatement ();
	        s11.executeQuery ("SELECT name FROM npc WHERE id ="+string);
	        ResultSet rs11 = s11.getResultSet ();
	        
	        while (rs11.next ())
	        {
	        	String name = rs11.getString ("name");
	        	return name;
	        	
	        }
		} catch (Exception e)
		{
	        return "dummy";
	
		}
        return "dummy";
	}
	
	public myFaction getFactionByID(int id)
	{
		for (myFaction f : this.universe.factions)
		{
			if (f.id == id)
			{
				return f;
			}
		}
		
		return null;
		
	}
	
	public myFaction dbGetNPCfaction(String string)
	{
		try
		{
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			universe.conn = DriverManager.getConnection (universe.dsn, universe.dbuser, universe.dbpass);
	        PreparedStatement s11 = this.universe.conn.prepareStatement("SELECT faction_id FROM npc WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
	        s11.setInt(1, Integer.parseInt(string));
	        s11.executeQuery();
	        ResultSet rs11 = s11.getResultSet ();
	        
	        while (rs11.next ())
	        {
	        	int factionid = rs11.getInt ("faction_id");
	        	for (myFaction f : this.universe.factions)
	        	{
	        		if (f.id == factionid)
	        		{
	        			return f;
	        		}
	        	}
	        	
	        }
		} catch (Exception e)
		{
	        return null;
	
		}
        return null;
	}
	
	
	public myLoottable dbGetNPCloottable(String string)
	{
		try
		{
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
	        universe.conn = DriverManager.getConnection (universe.dsn, universe.dbuser, universe.dbpass);
	        PreparedStatement s11 = this.universe.conn.prepareStatement("SELECT loottable_id FROM npc WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
	        s11.setInt(1, Integer.parseInt(string));
	        s11.executeQuery();
	        ResultSet rs11 = s11.getResultSet ();
	        
	        while (rs11.next ())
	        {
	        	int lootttableid = rs11.getInt ("loottable_id");
	        	for (myLoottable f : this.universe.loottables)
	        	{
	        		if (f.id == lootttableid)
	        		{
	        			return f;
	        		}
	        	}
	        	
	        }
		} catch (Exception e)
		{
	        return null;
	
		}
        return null;
	}
	
	
	public void fixDead()
	{
		int count = 0;
		for (myPlayer player : universe.players.values())
		{
			if (player.dead == true)
			{
				try
				{
					for (World w : getServer().getWorlds())
					{
						for (Player p : w.getPlayers())
						{
							if (player.name == p.getName())
							{
								player.player = p;
								player.dead = false;
								count++;
							}
						}
					}
					
				} catch (ConcurrentModificationException e)
				{
					System.out.println("npcx : FAILED establishing dead player");
				}
			}
		}
		if (count > 0)
		{
			System.out.println("npcx : reestablished " + count + " dead players.");
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

	 public void EventsSetup()
	 {
		 System.out.println("npcx : registering monitored events");
		 this.Server = getServer();
	     this.PluginListener = new PluginListener(this);
		 getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, PluginListener, Priority.Monitor, this);
		 PluginManager pm = getServer().getPluginManager();

         mEntityListener = new npcxEListener(this);
         mPlayerListener = new npcxPListener(this);
         pm.registerEvent(Type.ENTITY_TARGET, mEntityListener, Priority.Normal, this);
         pm.registerEvent(Type.ENTITY_DAMAGED, mEntityListener, Priority.Normal, this);
         pm.registerEvent(Type.ENTITY_EXPLODE, mEntityListener, Priority.Normal, this);
         
         pm.registerEvent(Type.ENTITY_DEATH, mEntityListener, Priority.Normal, this);
         pm.registerEvent(Type.CREATURE_SPAWN, mEntityListener, Priority.Normal, this);
         
         pm.registerEvent(Type.PLAYER_RESPAWN, mPlayerListener, Priority.Normal, this);
         
         pm.registerEvent(Type.PLAYER_JOIN, mPlayerListener, Priority.Normal, this);
         pm.registerEvent(Type.PLAYER_QUIT, mPlayerListener, Priority.Normal, this);
         pm.registerEvent(Type.PLAYER_CHAT, mPlayerListener, Priority.Normal, this); 
		 
	 }
	
	@Override
	public void onEnable() {
		
		// TODO Auto-generated method stub
		universe = new myUniverse(this);
		universe.checkSetup();
		
		// Check if world exists and settings are loaded
		if (!universe.loadSetup())
			return;
		
		// TODO Auto-generated method stub
        EventsSetup();
        
        universe.checkDbSetup();
        universe.checkUpdates();
        
        
        universe.loadData();
        
        PluginDescriptionFile pdfFile = this.getDescription();
        logger.log(Level.INFO, pdfFile.getName() + " version " + pdfFile.getVersion() + " enabled.");
        
        think();
        
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {

        try {
	           
            if (!command.getName().toLowerCase().equals("npcx")) {
            	
                return false;
            }
            
            
            if (!(sender instanceof Player)) {

                return false;
            }
            
            if (sender.isOp() == false) {

                return false;
            }

            Player player = (Player) sender;
            
            if (args.length < 1) {
            	player.sendMessage("Insufficient arguments /npcx spawngroup");
            	player.sendMessage("Insufficient arguments /npcx faction");
            	player.sendMessage("Insufficient arguments /npcx loottable");
            	player.sendMessage("Insufficient arguments /npcx npc");
            	player.sendMessage("Insufficient arguments /npcx pathgroup");
            	player.sendMessage("Insufficient arguments /npcx merchant");
            	
            	return false;
            }

            String subCommand = args[0].toLowerCase();
        	//debug: logger.log(Level.WARNING, "npcx : " + command.getName().toLowerCase() + "(" + subCommand + ")");

            
            Location l = player.getLocation();
            
            if (subCommand.equals("debug"))
            {
            	
            
            }
            
            if (subCommand.equals("spawngroup"))
            {
            	
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
                	player.sendMessage("Insufficient arguments /npcx spawngroup updatepos spawngroupid");
                	player.sendMessage("Insufficient arguments /npcx spawngroup delete spawngroupid");
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
            			
            			PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO spawngroup (name,x,y,z,pitch,yaw) VALUES (?,?,?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
            			stmt.setString(1,args[2]);
            			stmt.setString(2, Double.toString(x));
            		    stmt.setString(3, Double.toString(y));
            		    stmt.setString(4, Double.toString(z));
            		    stmt.setString(5, Double.toString(pitch));
            		    stmt.setString(6, Double.toString(yaw));
            			stmt.executeUpdate();
            			ResultSet keyset = stmt.getGeneratedKeys();
            			int key = 0;
            			if ( keyset.next() ) {
            			    // Retrieve the auto generated key(s).
            			    key = keyset.getInt(1);
            			    
            			}
            			stmt.close();
            			
        	            player.sendMessage("Spawngroup ["+ key + "] now active at your position");
            			mySpawngroup sg = new mySpawngroup(this);
            			sg.id = key;
            			sg.name = args[2];
            			sg.x = x;
            			sg.y = y;
            			sg.z = z;
            			sg.pitch = pitch;
            			sg.yaw = yaw;
            			sg.world = player.getWorld();
            			
            			this.universe.spawngroups.put(Integer.toString(key),sg);
            			System.out.println("npcx : + cached new spawngroup("+ args[2] + ")");
        	            
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("delete")) 
            	{
            		if (args.length < 3)
         		    {
                    	player.sendMessage("Insufficient arguments /npcx spawngroup delete spawngroupid");
                    	
            			
         		    } else {
         		    	int count = 0;
         		    	for (mySpawngroup spawngroup : this.universe.spawngroups.values())
         		    	{
         		    		if (spawngroup.id == Integer.parseInt(args[2]))
         		    		{
         		    			spawngroup.Delete();
         		    			count++;
         		    		}
         		    	}
         		    	player.sendMessage("Deleted cached "+count+" spawngroups.");
         		    	
         		    	
         		    }
            		
            	}
            	
            	
            	
            	
            	if (args[1].equals("pathgroup")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx spawngroup pathgroup spawngroupid pathgroupid");
            			
            			
            			
            		} else {

        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE spawngroup SET pathgroupid = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            stmt.executeUpdate();
        	            
        	            for(mySpawngroup sg : universe.spawngroups.values())
        	            {
        	            	if (sg.id == Integer.parseInt(args[2]))
        	            	{
        	            		if (Integer.parseInt(args[3]) != 0)
        	            		{
        	            			sg.pathgroup = getPathgroupByID(Integer.parseInt(args[3]));
        	            			for (myNPC n : universe.npcs.values())
        	            			{
        	            				if (n.spawngroup == sg)
        	            				{
        	            					n.pathgroup = sg.pathgroup;
        	            				}
        	            			}
        	            			
        	            			player.sendMessage("npcx : Updated spawngroups cached pathgroup ("+args[3]+"): "+sg.pathgroup.name);
        	            		} else {
        	            			sg.pathgroup = null;
        	            			
        	            			sg.pathgroup = getPathgroupByID(Integer.parseInt(args[3]));
        	            			for (myNPC n : universe.npcs.values())
        	            			{
        	            				if (n.spawngroup == sg)
        	            				{
        	            					n.pathgroup = null;
        	            				}
        	            			}
        	            			
        	            			player.sendMessage("npcx : Updated spawngroups cached pathgroup (0)");

        	            		}
        	            		
        	            	}
        	            }
            			
            			player.sendMessage("Updated pathgroup ID:" + args[3] + " on spawngroup ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	
            	
            	
            	if (args[1].equals("add")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx spawngroup add spawngroup npcid");
                    	
            		} else {
            			player.sendMessage("Added to spawngroup " + args[2] + "<"+ args[3]+ ".");
            			
            			// add to database
            		
            			
            			PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO spawngroup_entries (spawngroupid,npcid) VALUES (?,?);",Statement.RETURN_GENERATED_KEYS);
            			s2.setString(1,args[2]);
            			s2.setString(2,args[3]);
            		    
            			s2.executeUpdate();
        	            player.sendMessage("NPC ["+ args[3] + "] added to group ["+ args[2] + "]");
            			
        	            // add to cached spawngroup
        	            for (mySpawngroup sg : universe.spawngroups.values())
        	            {
        	            	if (sg.id == Integer.parseInt(args[2]))
        	            	{
        	            		
        	            		
        	            		
        	            		
        	            		
        	            		PreparedStatement stmtNPC = this.universe.conn.prepareStatement("SELECT * FROM npc WHERE id = ?;");
        	            		stmtNPC.setString(1,args[3]);
        	            		stmtNPC.executeQuery();
                    			ResultSet rsNPC = stmtNPC.getResultSet ();
                     		    int count = 0;
                     		    while (rsNPC.next ())
                     		    {
                     		       Location loc = new Location(getServer().getWorld(this.universe.defaultworld),0,0,0,0,0);
            	            		
                     		       myNPC npc = new myNPC(this,universe.fetchTriggerWords(Integer.parseInt(args[3])), loc, "dummy");
                     		       npc.name = rsNPC.getString ("name");
                     		       npc.category = rsNPC.getString ("category");
                     		       npc.faction = dbGetNPCfaction(args[3]);
                     		       npc.loottable = dbGetNPCloottable(args[3]);
                     		       npc.helmet = rsNPC.getInt ("helmet");
                     		       npc.pathgroup = sg.pathgroup;
                     		       npc.chest = rsNPC.getInt ("chest");
                     		       npc.legs = rsNPC.getInt ("legs");
                     		       npc.boots = rsNPC.getInt ("boots");
                     		       npc.weapon = rsNPC.getInt ("weapon");
                     		       
                     		       npc.spawngroup = sg;
                     		       npc.id = args[3];
                    		       
                     		      sg.npcs.put(sg.id+"-"+npc.id, npc);
                     		     universe.npcs.put(sg.id+"-"+npc.id, npc);
                     		       
                     		       ++count;
                     		    }
                     		    rsNPC.close();
                     		    stmtNPC.close();
                     		    
                     		    
        	            		        	            		
        	            		
        	            		dbg(1,"npcx : + cached new spawngroup entry("+ args[3] + ")");
        	            		
        	            		
        	            	}
        	            }
        	            
        	            
        	            mySpawngroup sg = new mySpawngroup(this);
            			
            			// close db
        	            s2.close();
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("updatepos")) 
            	{
            		if (args.length < 3)
         		    {
                    	player.sendMessage("Insufficient arguments /npcx spawngroup updatepos spawngroupid");
                    	
            			
         		    } else {
         		    	
            			Location loc = player.getLocation();
            			PreparedStatement s2 = this.universe.conn.prepareStatement("UPDATE spawngroup SET x=?,y=?,z=?,yaw=?,pitch=? WHERE id = ?;");
            			s2.setString(1,Double.toString(loc.getX()));
            			s2.setString(2,Double.toString(loc.getY()));
            			s2.setString(3,Double.toString(loc.getZ()));
            			s2.setString(4,Double.toString(loc.getYaw()));
            			s2.setString(5,Double.toString(loc.getPitch()));
            			s2.setString(6,args[2]);
            			
            			
            			s2.executeUpdate();
            			player.sendMessage("Updated Spawngroup " + args[2] + " to your position");
                		
        	            // Update cached spawngroups
        	            for (mySpawngroup sg : this.universe.spawngroups.values())
        	            {
        	            	if (sg.id == Integer.parseInt(args[2]))
        	            	{
        	            		// update the spawngroup
        	            		sg.x = loc.getX();
        	            		sg.y = loc.getY();
        	            		sg.z = loc.getZ();
        	            		sg.yaw = loc.getYaw();
        	            		sg.pitch = loc.getPitch();
        	            		
        	            		dbg(1,"npcx : + cached updated spawngroup ("+ args[2] + ")");
        	            		
        	            		// Found the spawngroup, lets make sure the NPCs have their spawn values set right
        	            		
        	            		for (myNPC np : sg.npcs.values())
        	            		{
        	            			if (np.npc != null)
        	            			{
        	            			
	        	            			np.npc.spawnx = sg.x;
	        	            			np.npc.spawny = sg.y;
	        	            			np.npc.spawnz = sg.z;
	        	            			np.npc.spawnyaw = sg.yaw;
	        	            			np.npc.spawnpitch = sg.pitch;
	        	            			Location locnpc = new Location(getServer().getWorld(this.universe.defaultworld),loc.getX(),loc.getY(),loc.getZ(),loc.getYaw(),loc.getPitch());
	        	            			np.npc.forceMove(locnpc);
	        	            			
        	            			}
        	            				
        	            		}
        	            		
        	            		
        	            		
        	            	}
        	            }
        	            
        	            
            			
            			// close statement
        	            s2.close();
         		    	
         		    }
            		
            	}
            	
            	if (args[1].equals("list")) {
            		   player.sendMessage("Spawngroups:");
            		   PreparedStatement sglist;
         		       
            		   
            		   if (args.length < 3)
            		   {
            			   sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM spawngroup ORDER BY ID DESC LIMIT 10");
            		   } else {

                		   sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM spawngroup WHERE name LIKE '%"+args[2]+"%'");
            		   }
            		   sglist.executeQuery ();
            		   ResultSet rs = sglist.getResultSet ();
            		   
            		   int count = 0;
            		   while (rs.next ())
            		   {
            		       int idVal = rs.getInt ("id");
            		       String nameVal = rs.getString ("name");
            		       String catVal = rs.getString ("category");
            		       player.sendMessage(
            		               "id = " + idVal
            		               + ", name = " + nameVal
            		               + ", category = " + catVal);
            		       ++count;
            		   }
            		   rs.close ();
            		   sglist.close ();
            		   player.sendMessage (count + " rows were retrieved");
            		
            	
        			
        		}
        		
            }
            

            //
            // START LOOTTABLE
            //
            
            if (subCommand.equals("loottable"))
            {
            
            	
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
            			player.sendMessage("Added to loottable " + args[2] + "<"+ args[3]+ "x"+args[4]+".");
            			
            			// add to database
            		
            			
            			PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO loottable_entries (loottable_id,item_id,amount) VALUES (?,?,?);",Statement.RETURN_GENERATED_KEYS);
            			s2.setString(1,args[2]);
            			s2.setString(2,args[3]);
            			s2.setString(3,args[4]);
            			
            			s2.executeUpdate();
        	            player.sendMessage("NPC ["+ args[3] + "x"+args[4]+"] added to group ["+ args[2] + "]");
            			
        	            // add to cached loottable
        	            for (myLoottable lt : this.universe.loottables)
        	            {
        	            	if (lt.id == Integer.parseInt(args[2]))
        	            	{
        	            		
        	            		
        	            		
        	            		myLoottable_entry entry = new myLoottable_entry();
        	            		entry.id = Integer.parseInt(args[2]);
        	            		entry.itemid = Integer.parseInt(args[3]);
        	            		entry.amount = Integer.parseInt(args[4]);
        	            		
        	            		dbg(1,"npcx : + cached new loottable entry("+ args[3] + ")");
        	            		lt.loottable_entries.add(entry);
        	            		
        	            	}
        	            }
        	            
        	            
        	            mySpawngroup sg = new mySpawngroup(this);
            			
            			// close statement
        	            s2.close();
        	            
            		}
        			
        		}
            	
            	
            	if (args[1].equals("create")) {
            		if (args.length < 2) {
            			player.sendMessage("Insufficient arguments /npcx loottable create loottablename");
                    	return false;
            		} else {
           			
            			try
            			{
	            			PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO loottables (name) VALUES (?);",Statement.RETURN_GENERATED_KEYS);
	            			stmt.setString(1,args[2]);
	            			
	            			stmt.executeUpdate();
	            			ResultSet keyset = stmt.getGeneratedKeys();
	            			int key = 0;
	            			if ( keyset.next() ) {
	            			    // Retrieve the auto generated key(s).
	            			    key = keyset.getInt(1);
	            			    
	            			}
	            			stmt.close();
	            			
	        	            player.sendMessage("Loottable ["+ key + "] now active");
	        	            myLoottable fa = new myLoottable(key,args[2]);
	            			fa.id = key;
	            			fa.name = args[2];
	            			
	            			this.universe.loottables.add(fa);
	            			dbg(1,"npcx : + cached new loottable ("+ args[2] + ")");
	            			
            			} catch (IndexOutOfBoundsException e)
            			{
            				player.sendMessage("Insufficient arguments");
            			}
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("list")) {
            		player.sendMessage("Loottables:");
            		
            		Statement s = this.universe.conn.createStatement ();
            		   s.executeQuery ("SELECT id, name FROM loottables");
            		   ResultSet rs = s.getResultSet ();
            		   int count = 0;
            		   while (rs.next ())
            		   {
            		       int idVal = rs.getInt ("id");
            		       String nameVal = rs.getString ("name");
            		       player.sendMessage(
            		               "id = " + idVal
            		               + ", name = " + nameVal);
            		       
            		        Statement sFindEntries = this.universe.conn.createStatement();
	   		            	sFindEntries.executeQuery("SELECT * FROM loottable_entries WHERE loottable_id = " + idVal);
	   		            	ResultSet rsEntries = sFindEntries.getResultSet ();
	   		            	int countentries = 0;
	   		            	while (rsEntries.next ())
	   			            {
	   		            		
	   		            		int id = rsEntries.getInt("id");
	   		            		int itemid =  rsEntries.getInt("item_id");
	   		            		int loottableid = rsEntries.getInt("loottable_id");
	   		            		int amount = rsEntries.getInt("amount");
	   		            		
	   		            		player.sendMessage(
	             		               " + id = " + id + ", loottableid = " + loottableid + ", itemid = " + itemid + ", amount = " + amount);
	   		            		
	   		            		countentries++;
	   		            		
	   			            }
	   		            	player.sendMessage (countentries + " entries in this set");            		       
            		       ++count;
            		   }
            		   rs.close ();
            		   s.close ();
            		   player.sendMessage (count + " loottables were retrieved");
            		
            	
        			
        		}
        		
            }
            
            // END LOOTTABLE            
            
            
            //
            // START FACTION
            //
            
            if (subCommand.equals("faction"))
            {
            
            	
            	if (args.length < 2) {
            		player.sendMessage("Insufficient arguments /npcx faction create baseamount factionname");
                	player.sendMessage("Insufficient arguments /npcx faction list");
                	return false;
            		
            		
            		
                }
            	
            	if (args[1].equals("create")) {
            		if (args.length < 3) {
            			player.sendMessage("Insufficient arguments /npcx faction create baseamount factionname");
                    	
            		} else {
           			
            			try
            			{
	            			PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO faction_list (name,base) VALUES (?,?);",Statement.RETURN_GENERATED_KEYS);
	            			stmt.setString(1,args[3]);
	            			stmt.setInt(2,Integer.parseInt(args[2]));
	            			
	            			stmt.executeUpdate();
	            			ResultSet keyset = stmt.getGeneratedKeys();
	            			int key = 0;
	            			if ( keyset.next() ) {
	            			    // Retrieve the auto generated key(s).
	            			    key = keyset.getInt(1);
	            			    
	            			}
	            			stmt.close();
	            			
	        	            player.sendMessage("Faction ["+ key + "] now active");
	            			myFaction fa = new myFaction();
	            			fa.id = key;
	            			fa.name = args[3];
	            			fa.base = Integer.parseInt(args[2]);
	            			
	            			this.universe.factions.add(fa);
	            			dbg(1,"npcx : + cached new faction("+ args[3] + ")");
            			} catch (IndexOutOfBoundsException e)
            			{
            				player.sendMessage("Insufficient arguments");
            			}
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("list")) {
            		player.sendMessage("Factions:");
            		
            		Statement s = this.universe.conn.createStatement ();
            		   s.executeQuery ("SELECT id, name, base FROM faction_list");
            		   ResultSet rs = s.getResultSet ();
            		   int count = 0;
            		   while (rs.next ())
            		   {
            		       int idVal = rs.getInt ("id");
            		       String nameVal = rs.getString ("name");
            		       String baseVal = rs.getString ("base");
            		       player.sendMessage(
            		               "id = " + idVal
            		               + ", name = " + nameVal
            		               + ", base = " + baseVal);
            		       ++count;
            		   }
            		   rs.close ();
            		   s.close ();
            		   player.sendMessage (count + " rows were retrieved");
            		
            	
        			
        		}
        		
            }
            
            // END FACTION
            
            
            if (subCommand.equals("pathgroup"))
            {
            	if (args.length < 2) {
            		// todo: need to implement npc types here ie: 0 = default 1 = banker 2 = merchant
            		// todo: need to implement '/npcx npc edit' here
                	player.sendMessage("Insufficient arguments /npcx pathgroup create name");

                	// todo needs to force the player to provide a search term to not spam them with lots of results in the event of a huge npc list
                	player.sendMessage("Insufficient arguments /npcx pathgroup list");
        			player.sendMessage("Insufficient arguments /npcx pathgroup add pathgroupid order");
         			player.sendMessage("Insufficient arguments /npcx pathgroup inspect pathgroupid");

               	
                    return false;
                }
            	
            	if (args[1].equals("inspect")) {
            		
            		player.sendMessage("Pathgroup Entries:");
       		       
          		   
          		   if (args.length >= 3)
          		   {
          			   
          			   PreparedStatement pginspect = this.universe.conn.prepareStatement("SELECT id,s,x,y,z,pathgroup,name FROM pathgroup_entries WHERE pathgroup = ? ORDER BY s ASC");
            		   pginspect.setInt(1, Integer.parseInt(args[2]));
          			   pginspect.executeQuery ();
              		   ResultSet rspginspect = pginspect.getResultSet ();
              		   
              		   int count = 0;
              		   while (rspginspect.next ())
              		   {
              			   	   int idVal = rspginspect.getInt ("id");
              			       String nameVal = rspginspect.getString ("name");
     	       		       
              			   	   int s = rspginspect.getInt ("s");
              				   int pgid = rspginspect.getInt ("pathgroup");
              				   String x = rspginspect.getString ("x");
              				   String y = rspginspect.getString ("y");
              				   String z = rspginspect.getString ("z");
          	       		       
        	       		       player.sendMessage("s: "+s+" pgid: "+pgid+" XYZ: "+x+","+y+","+z);
        	       		       ++count;
              		   }
              		   rspginspect.close ();
              		   pginspect.close ();
              		   player.sendMessage (count + " rows were retrieved");

          		   }  else {
          			 player.sendMessage("Insufficient arguments /npcx pathgroup inspect pathgroupid");
          		   }
          		
            		
        			
        		}
            	
            	if (args[1].equals("add")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx pathgroup add pathgroupid order");
                    	
            		} else {
            			player.sendMessage("Added to pathgroup " + args[2] + "<"+ args[3]+ ".");
            			
            			// add to database
            		
            			
            			PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO pathgroup_entries (pathgroup,s,x,y,z,pitch,yaw) VALUES (?,?,?,?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
            			s2.setString(1,args[2]);
            			s2.setString(2,args[3]);
            			s2.setDouble(3,player.getLocation().getX());
            			s2.setDouble(4,player.getLocation().getY());
            			s2.setDouble(5,player.getLocation().getZ());
            			s2.setFloat(6,player.getLocation().getPitch());
            			s2.setFloat(7,player.getLocation().getYaw());
            			
            		    
            			s2.executeUpdate();
        	            player.sendMessage("Pathing Position ["+ args[3] + "] added to pathggroup ["+ args[2] + "]");
            			
        	            // add to cached spawngroup
        	            for (myPathgroup pg : this.universe.pathgroups)
        	            {
        	            	if (pg.id == Integer.parseInt(args[2]))
        	            	{
        	            		
        	            		int dpathgroupid = Integer.parseInt(args[2]);
        	            		int dspot = Integer.parseInt(args[3]);
        	            		myPathgroup_entry pge = new myPathgroup_entry(player.getLocation(),dpathgroupid,pg,dspot);
        	            		dbg(1,"npcx : + cached new pathgroup entry("+ args[3] + ")");
        	            		
        	            		// add new pathgroup entry object to the pathgroups entry list
        	            		pg.pathgroupentries.add(pge);
        	            		
        	            	}
        	            }
        	            
        	            
        	            
            			
            			// close db
        	            s2.close();
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("create")) {
            		if (args.length < 3) {
            			player.sendMessage("Insufficient arguments /npcx pathgroup create name");
                    	
            		} else {
            			
            			
            			PreparedStatement statementPCreate = this.universe.conn.prepareStatement("INSERT INTO pathgroup (name) VALUES (?)",Statement.RETURN_GENERATED_KEYS);
            			statementPCreate.setString(1, args[2]);
            			statementPCreate.executeUpdate();
        	            
        	            ResultSet keyset = statementPCreate.getGeneratedKeys();
        	            
            			int key = 0;
            			if ( keyset.next() ) {
            			    // Retrieve the auto generated key(s).
	            			key = keyset.getInt(1);
	            			
            			}
            			
            			myPathgroup pathgroup = new myPathgroup();
            			pathgroup.id = key;
            			pathgroup.name = args[2];

            			this.universe.pathgroups.add(pathgroup);
            			
            			
            			statementPCreate.close();
        	            player.sendMessage("Created pathgroup ["+key+"]: " + args[2]);
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("list")) {
            		
            		player.sendMessage("Pathgroups:");
          		   PreparedStatement sglist;
       		       
          		   
          		   if (args.length < 3)
          		   {
          			   sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM pathgroup ORDER BY ID DESC LIMIT 10");
          		   } else {

              		   sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM pathgroup WHERE name LIKE '%"+args[2]+"%'");
          		   }
          		   sglist.executeQuery ();
          		   ResultSet rs = sglist.getResultSet ();
          		   
          		   int count = 0;
          		   while (rs.next ())
          		   {
          			  int idVal = rs.getInt ("id");
 	       		       String nameVal = rs.getString ("name");
 	       		       String catVal = rs.getString ("category");
 	       		       player.sendMessage(
 	       		               "id = " + idVal
 	       		               + ", name = " + nameVal
 	       		               + ", category = " + catVal);
 	       		       ++count;
          		   }
          		   rs.close ();
          		   sglist.close ();
          		   player.sendMessage (count + " rows were retrieved");
          		
            		
        			
        		}
            }
            
            if (subCommand.equals("merchant"))
            {
            	if (args.length < 2) {
            		// todo: need to implement npc types here ie: 0 = default 1 = banker 2 = merchant
            		// todo: need to implement '/npcx npc edit' here
                	player.sendMessage("Insufficient arguments /npcx merchant create name");

                	// todo needs to force the player to provide a search term to not spam them with lots of results in the event of a huge npc list
                	player.sendMessage("Insufficient arguments /npcx merchant list");
        			player.sendMessage("Insufficient arguments /npcx merchant add merchantid item amount pricebuyat pricesellat");
         			player.sendMessage("Insufficient arguments /npcx merchant inspect merchantid");

               	
                    return false;
                }
            	
            	if (args[1].equals("inspect")) {
            		
            		player.sendMessage("Merchant Entries:");
       		       
          		   
          		   if (args.length >= 3)
          		   {
          			   
          			   PreparedStatement pginspect = this.universe.conn.prepareStatement("SELECT id,merchantid,itemid,amount,pricebuy,pricesell FROM merchant_entries WHERE merchantid = ? ORDER BY id ASC");
            		   pginspect.setInt(1, Integer.parseInt(args[2]));
          			   pginspect.executeQuery ();
              		   ResultSet rspginspect = pginspect.getResultSet ();
              		   
              		   int count = 0;
              		   while (rspginspect.next ())
              		   {
              			   	   int idVal = rspginspect.getInt ("id");
              			   	   int merchantid = rspginspect.getInt ("merchantid");
              			   	   int itemid = rspginspect.getInt ("itemid");
           			   	   	   int amount = rspginspect.getInt ("amount");
           			   	   	   int pricebuy = rspginspect.getInt ("pricebuy");
           			   	   	   int pricesell = rspginspect.getInt ("pricesell");
              				   
        	       		       player.sendMessage("EID:"+idVal+":MID:"+merchantid+" Item:"+itemid +" - Amount: "+amount+" Buying: "+pricebuy+"Selling: "+pricesell);
        	       		       ++count;
              		   }
              		   rspginspect.close ();
              		   pginspect.close ();
              		   player.sendMessage (count + " rows were retrieved");

          		   }  else {
          			 player.sendMessage("Insufficient arguments /npcx merchant inspect merchantid");
          		   }
          		
            		
        			
        		}
            	
            	if (args[1].equals("add")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx merchant add merchantid itemid amount pricebuyat pricesellat");
                    	
            		} else {
            			
            			// add to database
            		
            			
            			PreparedStatement s2 = this.universe.conn.prepareStatement("INSERT INTO merchant_entries (merchantid,itemid,amount,pricebuy,pricesell) VALUES (?,?,?,?,?);",Statement.RETURN_GENERATED_KEYS);
            			
            			s2.setInt (1,Integer.parseInt(args[2]));
            			s2.setInt (2,Integer.parseInt(args[3]));
            			s2.setInt (3,Integer.parseInt(args[4]));
            			s2.setInt (4,Integer.parseInt(args[5]));
            			s2.setInt (5,Integer.parseInt(args[6]));
            			s2.executeUpdate();
        	            player.sendMessage("Merchant Item ["+ args[3] + "x" + args[4] + "@"+args[5]+"/"+args[6]+"] added to Merchant: ["+ args[2] + "]");
            			
        	            // add to cached spawngroup
        	            for (myMerchant pg : this.universe.merchants)
        	            {
        	            	if (pg.id == Integer.parseInt(args[2]))
        	            	{
        	            		
        	            		int dmerchantid = Integer.parseInt(args[2]);
        	            		int itemid = Integer.parseInt(args[3]);
        	            		int amount = Integer.parseInt(args[4]);
        	            		int pricebuy = Integer.parseInt(args[5]);
        	            		int pricesell = Integer.parseInt(args[6]);
        	            		
        	            		myMerchant_entry pge = new myMerchant_entry(pg, dmerchantid,itemid,amount,pricebuy,pricesell);
        	            		dbg("npcx : + cached new merchant entry("+ args[3] + ")");
        	            		
        	            		// add new merchant entry object to the merchants entry list
        	            		pg.merchantentries.add(pge);
        	            		player.sendMessage("Added to merchant " + args[2] + "<"+ args[3]+ "x"+args[4]+"@"+args[5]+".");
        	            		
        	            	}
        	            }
        	            
        	            
        	            
            			
            			// close db
        	            s2.close();
        	            
            			
            		}
        			
        		}
            	
            	if (args[1].equals("create")) {
            		if (args.length < 3) {
            			player.sendMessage("Insufficient arguments /npcx merchant create name");
                    	
            		} else {
            			
            			
            			PreparedStatement statementPCreate = this.universe.conn.prepareStatement("INSERT INTO merchant (name) VALUES (?)",Statement.RETURN_GENERATED_KEYS);
            			statementPCreate.setString(1, args[2]);
            			statementPCreate.executeUpdate();
        	            
        	            ResultSet keyset = statementPCreate.getGeneratedKeys();
        	            
            			int key = 0;
            			if ( keyset.next() ) {
            			    // Retrieve the auto generated key(s).
	            			key = keyset.getInt(1);
	            			
            			}
            			
            			myMerchant merchant = new myMerchant(this,key,args[2]);
            			merchant.id = key;
            			merchant.name = args[2];

            			this.universe.merchants.add(merchant);
            			
            			
            			statementPCreate.close();
        	            player.sendMessage("Created merchant ["+key+"]: " + args[2]);
        	            
            		}
        			
        		}
            	
            	if (args[1].equals("list")) {
            		
            		player.sendMessage("merchants:");
          		   PreparedStatement sglist;
       		       
          		   
          		   if (args.length < 3)
          		   {
          			   sglist = this.universe.conn.prepareStatement("SELECT id, name FROM merchant ORDER BY ID DESC LIMIT 10");
          		   } else {

              		   sglist = this.universe.conn.prepareStatement("SELECT id, name FROM merchant WHERE name LIKE '%"+args[2]+"%'");
          		   }
          		   sglist.executeQuery ();
          		   ResultSet rs = sglist.getResultSet ();
          		   
          		   int count = 0;
          		   while (rs.next ())
          		   {
          			  int idVal = rs.getInt ("id");
 	       		       String nameVal = rs.getString ("name");
 	       		       player.sendMessage(
 	       		               "id = " + idVal
 	       		               + ", name = " + nameVal);
 	       		       ++count;
          		   }
          		   rs.close ();
          		   sglist.close ();
          		   player.sendMessage (count + " rows were retrieved");
          		
            		
        			
        		}
            }
            
        
            
            
            
            if (subCommand.equals("npc"))
            {
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
                	//player.sendMessage("Insufficient arguments /npcx npc spawn name");
                	
                	player.sendMessage("Insufficient arguments /npcx npc triggerword add npcid triggerword response");
                	player.sendMessage("Insufficient arguments /npcx npc faction npcid factionid");
                	player.sendMessage("Insufficient arguments /npcx npc loottable npcid loottableid");
                	player.sendMessage("Insufficient arguments /npcx npc category npcid category");
                	player.sendMessage("Insufficient arguments /npcx npc merchant npcid merchantid");
                	
                	
                	player.sendMessage("Insufficient arguments /npcx npc weapon npcid itemid");
                	player.sendMessage("Insufficient arguments /npcx npc helmet npcid itemid");
                	player.sendMessage("Insufficient arguments /npcx npc chest npcid itemid");
                	player.sendMessage("Insufficient arguments /npcx npc legs npcid itemid");
                	player.sendMessage("Insufficient arguments /npcx npc boots npcid itemid");
                    return false;
                }
            	
            	if (args[1].equals("merchant")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc merchant npcid merchantid");
            			return false;
            		} else {
            			if (Integer.parseInt(args[3]) == 0)
            			{
            			
	        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET merchantid = null WHERE id = ?;");
	        	            stmt.setString(1, args[2]);
	        	            stmt.executeUpdate();
	        	            stmt.close();
	        	            
            			} else {
            				PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET merchantid = ? WHERE id = ?;");
            	            stmt.setString(1, args[3]);
            	            stmt.setString(2, args[2]);
            	            stmt.executeUpdate();
            	            stmt.close();
            			}
        	            int count = 0;
        	            for(myNPC sg : universe.npcs.values())
        	            {
        	            	if (sg.id == args[2])
        	            	{
        	            		if (Integer.parseInt(args[3]) != 0)
        	            		{
        	            			sg.merchant = getMerchantByID(Integer.parseInt(args[3]));
        	            			
        	            			player.sendMessage("npcx : Updated NPCs cached merchant ("+args[3]+"): "+sg.merchant.name);
        	            			count++;
        	            		} else {
        	            			sg.merchant = null;
        	            			player.sendMessage("npcx : Updated NPCs cached merchant (0)");
        	            			count++;

        	            		}
        	            		
        	            	}
        	            }
            			
            			player.sendMessage("Updated "+count+" entries.");
        	            
            			
            		}
            	}
            	
            	if (args[1].equals("triggerword")) {
            		if (args.length < 6) {
            			player.sendMessage("Insufficient arguments /npcx npc triggerword add npcid triggerword response");
            			
            		
            		} else {
            			
            			String reply = "";
            			int current = 6;
            			while (current <=  args.length)
            			{
            				reply = reply + args[current-1]+" ";
            				current++;
            			}
            			
            			reply = reply.substring(0,reply.length()-1);
            			
            			
            			PreparedStatement statementTword = this.universe.conn.prepareStatement("INSERT INTO npc_triggerwords (npcid,triggerword,reply) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS);
            			statementTword.setString(1,args[3]);
            			statementTword.setString(2,args[4]);
            			statementTword.setString(3,reply);
            			
            			statementTword.executeUpdate();
            			ResultSet keyset = statementTword.getGeneratedKeys();
            			int key = 0;
            			if ( keyset.next() ) {
            			    // Retrieve the auto generated key(s).
	            			key = keyset.getInt(1);
	            			
            			}
            			player.sendMessage("Added ("+universe.npcs.values().size()+") triggerword ["+key+"] to npc "+args[3]);
            			
            			
            			// add it to any spawned npcs
            			for (myNPC npc : universe.npcs.values())
            			{
            				dbg(1,"my id="+npc.id.toString());
            				if (npc.id.equals(args[3]))
            				{
            					dbg(1,"npcx : adding reply because ("+ npc.id +") is ("+args[3]+")  ("+ reply + ") and trigger ("+ reply +") for [" + args[3] + "] npc to npc: " + npc.id);
                				
            					myTriggerword tw = new myTriggerword();
            					tw.word = args[4];
            					tw.id = key;
            					tw.response = reply;
            					player.sendMessage("Added triggerword to Active npc "+args[3]);
            					npc.triggerwords.put(Integer.toString(tw.id), tw);
            				
            				}
            			}
            			
            		}
            		
            	}
            	
            	
            	
            	if (args[1].equals("chest")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc chest npcid itemid");
            			
            			
            			
            		} else {

           			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET chest = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            //TODO not in schema yet
        	            //stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.chest = Integer.parseInt(args[3]);
        	            		ItemStack i = new ItemStack(n.chest);
        	            		i.setTypeId(Integer.parseInt(args[3]));

        	            		n.npc.getBukkitEntity().getInventory().setChestplate(i);
        	            		player.sendMessage("npcx : Updated living npc to cached chest ("+args[3]+"): "+n.chest);
        	            		stmt.executeUpdate();

        	            	}
        	            }
            			
            			player.sendMessage("Updated npc chest: item ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	if (args[1].equals("helmet")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc helmet npcid itemid");
            			
            			
            			
            		} else {

           			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET helmet = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            //TODO not in schema yet
        	            //stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.helmet = Integer.parseInt(args[3]);
        	            		ItemStack i = new ItemStack(n.helmet);
        	            		i.setTypeId(Integer.parseInt(args[3]));
        	            		n.npc.getBukkitEntity().getInventory().setHelmet(i);
        	            		player.sendMessage("npcx : Updated living npc to cached helmet ("+args[3]+"): "+n.helmet);
        	            		stmt.executeUpdate();
        	            	}
        	            }
            			
            			player.sendMessage("Updated npc helmet: item ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	if (args[1].equals("weapon")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc weapon npcid itemid");
            			
            			
            			
            		} else {

           			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET weapon = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            //TODO not in schema yet
        	            //stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.weapon = Integer.parseInt(args[3]);
        	            		ItemStack i = new ItemStack(n.weapon);
        	            		i.setTypeId(Integer.parseInt(args[3]));

        	            		n.npc.getBukkitEntity().getInventory().setItemInHand(i);
        	            		player.sendMessage("npcx : Updated living npc to cached weapon ("+args[3]+"): "+n.weapon);
        	            		stmt.executeUpdate();

        	            	}
        	            }
            			
            			player.sendMessage("Updated npc weapon: item ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	if (args[1].equals("boots")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc boots npcid itemid");
            			
            			
            			
            		} else {

           			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET boots = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            //TODO not in schema yet
        	            //stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.boots = Integer.parseInt(args[3]);
        	            		ItemStack i = new ItemStack(n.boots);
        	            		i.setTypeId(Integer.parseInt(args[3]));

        	            		n.npc.getBukkitEntity().getInventory().setBoots(i);
        	            		player.sendMessage("npcx : Updated living npc to cached boots ("+args[3]+"): "+n.boots);
        	            	
        	            		stmt.executeUpdate();

        	            	}
        	            }
            			
            			player.sendMessage("Updated npc boots: item ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	if (args[1].equals("legs")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc legs npcid itemid");
            			
            			
            			
            		} else {

           			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET legs = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            //TODO not in schema yet
        	            //stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.legs = Integer.parseInt(args[3]);
        	            		ItemStack i = new ItemStack(n.legs);
        	            		i.setTypeId(Integer.parseInt(args[3]));

        	            		n.npc.getBukkitEntity().getInventory().setLeggings(i);
        	            		player.sendMessage("npcx : Updated living npc to cached legs ("+args[3]+"): "+n.legs);
        	            		
        	            		stmt.executeUpdate();

        	            	}
        	            }
            			
            			player.sendMessage("Updated npc legs: item ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	if (args[1].equals("faction")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc faction npcid factionid");
            			
            			
            			
            		} else {

           			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET faction_id = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.faction = getFactionByID(Integer.parseInt(args[3]));
        	            		player.sendMessage("npcx : Updated living npc to cached faction ("+args[3]+"): "+n.faction.name);
        	            		// when faction changes reset aggro and follow status
        	            		n.npc.aggro = null;
        	            		n.npc.follow = null;
        	            	}
        	            }
            			
            			player.sendMessage("Updated npc faction ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	
            	
            	
            	if (args[1].equals("category")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc category npcid category");
            			
            			
            			
            		} else {

            			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET category = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		n.category = args[3];
        	            		player.sendMessage("npcx : Updated living npc to cached category ("+args[3]+"): "+n.category);
        	            		// when faction changes reset aggro and follow status
        	            		
        	            	}
        	            }
            			
            			player.sendMessage("Updated npc category :" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	
            	
            	if (args[1].equals("list")) {
         		   player.sendMessage("Npcs:");
         		   PreparedStatement sglist;
      		       
         		   
         		   if (args.length < 3)
         		   {
         			   sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM npc ORDER BY ID DESC LIMIT 10");
         		   } else {

             		   sglist = this.universe.conn.prepareStatement("SELECT id, name, category FROM npc WHERE name LIKE '%"+args[2]+"%'");
         		   }
         		   sglist.executeQuery ();
         		   ResultSet rs = sglist.getResultSet ();
         		   
         		   int count = 0;
         		   while (rs.next ())
         		   {
         			  int idVal = rs.getInt ("id");
	       		       String nameVal = rs.getString ("name");
	       		       String catVal = rs.getString ("category");
	       		       player.sendMessage(
	       		               "id = " + idVal
	       		               + ", name = " + nameVal
	       		               + ", category = " + catVal);
	       		       ++count;
         		   }
         		   rs.close ();
         		   sglist.close ();
         		   player.sendMessage (count + " rows were retrieved");
         		
         	
     			
            	}
            	
            	
            	
            	
            	if (args[1].equals("loottable")) {
            		if (args.length < 4) {
            			player.sendMessage("Insufficient arguments /npcx npc loottable npcid loottableid");
            			
            			
            			
            		} else {

        	            PreparedStatement stmt = this.universe.conn.prepareStatement("UPDATE npc SET loottable_id = ? WHERE id = ?;");
        	            stmt.setString(1, args[3]);
        	            stmt.setString(2, args[2]);
        	            
        	            stmt.executeUpdate();
        	            
        	            for(myNPC n : universe.npcs.values())
        	            {
        	            	if (n.id.matches(args[2]))
        	            	{
        	            		
        	            		
        	            		n.loottable = getLoottableByID(Integer.parseInt(args[3]));
        	            		player.sendMessage("npcx : Updated living npc to cached loottable ("+args[3]+"): "+n.loottable.name);
        	            		
        	            	}
        	            }
            			
            			player.sendMessage("Updated npc loottable ID:" + args[3] + " on NPC ID:[" + args[2]  + "]");
        	            
            			stmt.close();
            		}
            	}
            	
            	if (args[1].equals("create")) {
            		if (args.length < 3) {
            			player.sendMessage("Insufficient arguments /npcx npc create npcname");
                    	
            		} else {
                    	
            			Statement s2 = this.universe.conn.createStatement ();
            			
        	            PreparedStatement stmt = this.universe.conn.prepareStatement("INSERT INTO npc (name,weapon,helmet,chest,legs,boots) VALUES (?,'267','0','307','308','309');",Statement.RETURN_GENERATED_KEYS);
        	            stmt.setString(1, args[2]);
        	            stmt.executeUpdate();
            			ResultSet keyset = stmt.getGeneratedKeys();
            			int key = 0;
            			if ( keyset.next() ) {
            			    // Retrieve the auto generated key(s).
            			    key = keyset.getInt(1);
            			    
            			}
            			player.sendMessage("Created npc: " + args[2] + " ID:[" + key  + "]");
        	            
        	            s2.close();
        	            
            		}
        			
        		}
            	
            	
            	
            	
            	/*
            	 * Disabled temporarily
            	 * 
            	if (args[1].equals("spawn")) {
	            		player.sendMessage("Spawning new (temporary) NPC: " + args[2]);
	                    // temporary
	            		Location loc = new Location(player.getWorld(),player.getLocation().getX(),player.getLocation().getY(),player.getLocation().getZ(),player.getLocation().getYaw(),player.getLocation().getPitch());
	            		 myNPC npc = new myNPC(this, null, loc, args[2]);
	            		 npc.Spawn(args[2],loc);
	            		 this.universe.npcs.put("ZZSpawns"+"-"+npc.id,npc);
	            		 this.npclist.put(args[2], npc.npc);
	            		 
	            		return true;
                }
                */
            }
            
        } catch (Exception e) {
            sender.sendMessage("An error occured.");
            logger.log(Level.WARNING, "npcx: error: " + e.getMessage() + e.getStackTrace().toString());
            e.printStackTrace();
            return true;
        }

        return true;
    }

	

	private myMerchant getMerchantByID(int parseInt) {
		// TODO Auto-generated method stub
		for (myMerchant g : this.universe.merchants)
		{
			if (g.id == parseInt)
			{
				System.out.println("Found a merchant");
				return g;
			}
		}
		return null;
	}

	private myPathgroup getPathgroupByID(int parseInt) {
		// TODO Auto-generated method stub
		dbg(1,"getPathgroupByID:called ("+parseInt+")!");
		for (myPathgroup g : this.universe.pathgroups)
		{
			dbg(1,"getPathgroupByID:iterating!");

			if (g.id == parseInt)
			{
				dbg(1,"getPathgroupByID:found!");

				return g;
			}
		}
		return null;
	}

	private myLoottable getLoottableByID(int parseInt) {
		
			for (myLoottable f : this.universe.loottables)
			{
				if (f.id == parseInt)
				{
					return f;
				}
			}
			return null;
	}

	public void dbg(int debug, String string) {
		// TODO Auto-generated method stub
		if (debug >= 1)
		{
			// do stuff with info/warn
			
		} else {
			dbg(string);			
		}
	}
	
	public void dbg(String string) {
		System.out.println("npcx: "+string);			
		
	}


	public void informNpcDeadPlayer(Player player) {
		// TODO Auto-generated method stub
		
		for (myNPC npc : this.universe.npcs.values())
		{
			if (npc.npc != null && npc.npc.aggro != null)
			{
				if (npc.npc.aggro == player)
				{
					dbg(1,"informNpcDeadPlayer:aggro:"+player.getName());
					npc.npc.follow = null;
				}
				
			}
			
		}
		
	}

	public void sendPlayerItemList(Player player) {
		// TODO Auto-generated method stub
		String list = "Examples are: STONE GRASS DIRT IRON_SPADE LOG LEAVES GLASS LAPIS_ORE LAPIS_BLOCK IRON_INGOT";
		player.sendMessage(list);
		
	}
}
