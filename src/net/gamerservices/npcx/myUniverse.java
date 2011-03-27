package net.gamerservices.npcx;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.PluginDescriptionFile;

public class myUniverse {
	npcx parent;
	
	// config variable names
	public final String FILE_PROPERTIES = "npcx.properties";
	public final String PROP_DBHOST = "db-host";
	public final String PROP_DBUSER = "db-user";
	public final String PROP_DBPASS = "db-pass";
	public final String PROP_DBNAME = "db-name";
	public final String PROP_DBPORT = "db-port";
	public final String PROP_WORLD = "world";
	public final String PROP_UPDATE = "update";
	
	// db
	public String dsn;
	public Connection conn = null;
	public String dbhost;
	public String update;
	public String dbuser;
	public String dbpass;
	public String dbname;
	public String dbport;
	
	public Properties prop;
	public File propfile;
	public File propfolder;
	
	// default world name
	public String defaultworld;

	// Lists of objects that are universal
	public List< myFaction > factions = new CopyOnWriteArrayList< myFaction >();
	public List< myLoottable > loottables = new CopyOnWriteArrayList< myLoottable >();
	public List< myPathgroup > pathgroups = new CopyOnWriteArrayList< myPathgroup >();
	public HashMap<String, mySpawngroup> spawngroups = new HashMap<String, mySpawngroup>();
	public HashMap<String, myPlayer> players = new HashMap<String, myPlayer>();
	public HashMap<String, myNPC> npcs = new HashMap<String, myNPC>();
	public List< Monster > monsters = new CopyOnWriteArrayList< Monster >();
	
	
	public myUniverse(npcx parent)
	{
		this.parent = parent;
	}
	
	public void openDB()
	{
		if (dbhost == null)
	 	{
		 	this.dbhost = "localhost";
			this.dbuser = "npcx";
			this.dbname = "npcx";
			this.dbpass = "p4ssw0rd!";
			this.dbport = "3306";
			this.update = "true";
			dsn = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
	 	}
	 
        try 
        {
        
		 	System.out.println("npcx : initialising database connection");
		 	
		 	try 
		 	{
			 	Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			 		
		 	} catch (ClassNotFoundException e)
		 	{
		 		System.out.println("*****************************************");
		 		System.out.println("npcx : ERROR - Cannot find MySQL Library!");
		 		System.out.println("*****************************************");
		 		return;
		 	}
		 	
		 	try
		 	{
		 		conn = DriverManager.getConnection (dsn, dbuser, dbpass);
		 	} catch (SQLException e)
		 	{
		 		System.out.println("*****************************************");
		 		System.out.println("npcx : ERROR - Error during MySQL login ");
		 		System.out.println("*****************************************");
		 		e.printStackTrace();
		 		return;
		 	}
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
	}

	
	public boolean loadSetup()
	{
		// Loads configuration settings from the properties files
		PluginDescriptionFile pdfFile = this.parent.getDescription();
		System.out.println("npcx : load settings ("+pdfFile.getVersion()+") begun");
		
		Properties config = new Properties();
		BufferedInputStream stream;
		// Access the defined properties file
		try {
			stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
			
			try {
				
				// Load the configuration
				config.load(stream);
				dbhost = config.getProperty("db-host");
				dbuser = config.getProperty("db-user");
				dbpass = config.getProperty("db-pass");
				dbname = config.getProperty("db-name");
				dbport = config.getProperty("db-port");
				this.defaultworld = config.getProperty("world");
				
				update = config.getProperty("update");
				
				dsn = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
				System.out.println(dsn);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO i need to handle this instead
		for (World w : parent.getServer().getWorlds())
		{
			if (w.getName().matches(this.defaultworld))
			{
				System.out.println("npcx : loadsettings() ended");
				return true;
			}
		}
		
		System.out.println("**********************************************");
		System.out.println("* Load settings failed to find default world *");
		System.out.println("*    Please change it in ncpx.properties     *");
		System.out.println("**********************************************");
		return false;
	}
	
	public void checkSetup() {
		// TODO Auto-generated method stub
		propfolder = parent.getDataFolder();
		if (!propfolder.exists())
		{
			try
			{
				propfolder.mkdir();
				System.out.println("npcx : config folder generation ended");
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		if (!propfile.exists())
		{
			try
			{
				propfile.createNewFile();
				prop = new Properties();
				prop.setProperty(PROP_DBHOST, "localhost");
				prop.setProperty(PROP_DBUSER, "npcx");
				prop.setProperty(PROP_DBPASS, "p4ssw0rd!");
				prop.setProperty(PROP_DBNAME, "npcx");
				prop.setProperty(PROP_DBPORT, "3306");
				prop.setProperty(PROP_UPDATE, "true");
				this.dbhost = "localhost";
				this.dbuser = "npcx";
				this.dbname = "npcx";
				this.dbpass = "p4ssw0rd!";
				this.dbport = "3306";
				this.update = "true";
				
				
				
				prop.setProperty(PROP_WORLD, "world");
				BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
				prop.store(stream, "Default generated settings, please ensure mysqld matches");
				System.out.println("npcx : properties file generation ended");
				
			} catch(IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("npcx : initial setup ended");
		
		}
	}
	
	

	public void checkDbSetup() {
		// TODO Auto-generated method stub
		openDB();
		
		// Check the config file
		try {
			
			Properties config = new Properties();
			BufferedInputStream stream;
			stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
			config.load(stream);
			update = config.getProperty("update");
			
			if (update.matches("true"))
            {
            	System.out.println("npcx : DB WIPE");
            	
	            /*
	             * One time Database creation / TODO: Auto Upgrades
	             * 
	             * */
            	
            	if (update.matches("true"))
	            {
	            	System.out.println("npcx : DB WIPE");
	            	
		            /*
		             * One time Database creation / TODO: Auto Upgrades
		             * 
		             * */
	            	
	            	
	            	Statement factionlist = conn.createStatement ();
		            String dropfactionlist = "DROP TABLE IF EXISTS faction_list; ";
		            String fationlistsql = "CREATE TABLE faction_list ( id int(11) NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, base int(11) DEFAULT NULL, PRIMARY KEY (id))";
		            factionlist.executeUpdate(dropfactionlist);
		            factionlist.executeUpdate(fationlistsql);
		            factionlist.close();
		            
		            Statement player_faction = conn.createStatement ();
		            String dropplayer_faction = "DROP TABLE IF EXISTS player_faction; ";
		            String player_factionsql = "CREATE TABLE player_faction (  id int(11) NOT NULL AUTO_INCREMENT,  player_name varchar(45) DEFAULT NULL,  faction_id int(11) DEFAULT NULL,  amount int(11) DEFAULT NULL,  PRIMARY KEY (id))";
		            player_faction.executeUpdate(dropplayer_faction);
		            player_faction.executeUpdate(player_factionsql);
		            player_faction.close();
		            
		            Statement loottable_entries = conn.createStatement ();
		            String droploottable_entries = "DROP TABLE IF EXISTS loottable_entries; ";
		            String loottable_entriessql = "CREATE TABLE loottable_entries (id int(11) NOT NULL AUTO_INCREMENT,  loottable_id int(11) DEFAULT NULL,  item_id int(11) DEFAULT NULL,  amount int(11) DEFAULT NULL,  PRIMARY KEY (id))";
		            loottable_entries.executeUpdate(droploottable_entries);
		            loottable_entries.executeUpdate(loottable_entriessql);
		            loottable_entries.close();
		            
		            Statement loottable = conn.createStatement ();
		            String droploottable = "DROP TABLE IF EXISTS loottables; ";
		            String loottablesql = "CREATE TABLE loottables (  id int(11) NOT NULL AUTO_INCREMENT,  name varchar(50) DEFAULT NULL,  PRIMARY KEY (id))";
		            loottable.executeUpdate(droploottable);
		            loottable.executeUpdate(loottablesql);
		            loottable.close();
	            	
		            Statement npc_faction = conn.createStatement ();
		            String dropnpc_faction = "DROP TABLE IF EXISTS npc_faction; ";
		            String npc_factionsql = "CREATE TABLE npc_faction (id int(11) NOT NULL AUTO_INCREMENT, npc_id int(11) DEFAULT NULL, faction_id int(11) DEFAULT NULL, amount int(11) DEFAULT NULL,PRIMARY KEY (id))";
		            npc_faction.executeUpdate(dropnpc_faction);
		            npc_faction.executeUpdate(npc_factionsql);
		            npc_faction.close();
		            
		            
		            Statement s2 = conn.createStatement ();
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

		            dbhost = config.getProperty("db-host");
		            dbuser = config.getProperty("db-user");
		            dbpass = config.getProperty("db-pass");
		            dbname = config.getProperty("db-name");
		            dbport = config.getProperty("db-port");
		            dsn = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
		            defaultworld = config.getProperty("world");
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
					
	            }
            	
            	
            }
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}

	public void loadData() {
		// TODO Auto-generated method stub
		loadFactions();
		loadPathgroups();
		loadLoottables();
		loadSpawngroups();
	}

	public HashMap<String, myTriggerword> fetchTriggerWords(int npcid) throws SQLException
	{
		HashMap<String, myTriggerword> triggerwords = new HashMap<String, myTriggerword>();

		Statement s = conn.createStatement ();
		s.executeQuery ("SELECT id, npcid, triggerword, reply, category FROM npc_triggerwords WHERE npcid =" + npcid );
		
		ResultSet rs = s.getResultSet ();
			int count = 0;
		   while (rs.next ())
		   {
			   count++;
			   myTriggerword tw = new myTriggerword();
			   tw.response = rs.getString ("reply");
			   
			   tw.word = rs.getString ("triggerword");
		       tw.id = rs.getInt ("id");
		       triggerwords.put(Integer.toString(tw.id), tw);
		   }
		   //System.out.println("npcx : fetched "+count+" triggerwords");
		   rs.close ();
		   s.close ();
		   
		return triggerwords;		
	}
	
	
	private myPathgroup dbGetSpawngrouppg(int id) 
	{
			try
			{
				Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				conn = DriverManager.getConnection (dsn, dbuser, dbpass);
		        PreparedStatement s11 = conn.prepareStatement("SELECT pathgroupid FROM spawngroup WHERE id = ?",Statement.RETURN_GENERATED_KEYS);
		        s11.setInt(1, id);
		        s11.executeQuery();
		        ResultSet rs11 = s11.getResultSet ();
		        
		        while (rs11.next ())
		        {
		        	int pathgroupid = rs11.getInt ("pathgroupid");
		        	for (myPathgroup f : pathgroups)
		        	{
		        		if (f.id == pathgroupid)
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
	
	private void loadSpawngroups() {
		// TODO Auto-generated method stub
		// Load Spawngroups
		
		
		try
		{
        Statement s1 = conn.createStatement ();
        s1.executeQuery ("SELECT id, name, category,x,y,z,world,yaw,pitch,pathgroupid FROM spawngroup");
        ResultSet rs1 = s1.getResultSet ();
        int count1 = 0;
        System.out.println("npcx : loading spawngroups");
        while (rs1.next ())
        {
        	
        	// load spawngroup into cache
            int idVal = rs1.getInt ("id");
            String nameVal = rs1.getString ("name");
            String catVal = rs1.getString ("category");
            
            //BasicHumanNpc hnpc = NpcSpawner.SpawnBasicHumanNpc(args[2], args[2], player.getWorld(), l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
            
            // Create a new spawngroup
            mySpawngroup spawngroup = new mySpawngroup(this.parent);
            spawngroup.name = nameVal;
            //System.out.println("npcx : + " + nameVal);
            spawngroup.id = idVal;
            spawngroup.x = Double.parseDouble(rs1.getString ("x"));
            spawngroup.y = Double.parseDouble(rs1.getString ("y"));
            spawngroup.z = Double.parseDouble(rs1.getString ("z"));
            spawngroup.yaw = Double.parseDouble(rs1.getString ("yaw"));
            spawngroup.pitch = Double.parseDouble(rs1.getString ("pitch"));
            
            Location loc = new Location(parent.getServer().getWorld(this.defaultworld),spawngroup.x,spawngroup.y,spawngroup.z);
            
            spawngroup.pathgroup = dbGetSpawngrouppg(spawngroup.id);
            
            
            // Add to our spawngroup hashmap
            this.spawngroups.put(Integer.toString(idVal), spawngroup);
            
            // Load npcs into spawngroups
            Statement s11 = conn.createStatement ();
            s11.executeQuery ("SELECT npc.weapon As weapon, npc.helmet As helmet,npc.chest As chest,npc.legs As legs,npc.boots As boots,spawngroup_entries.spawngroupid As spawngroupid,spawngroup_entries.npcid As npcid, npc.name As name, npc.category As category, npc.loottable_id As loottable_id, npc.faction_id As faction_id FROM spawngroup_entries,npc WHERE npc.id = spawngroup_entries.npcid AND spawngroup_entries.spawngroupid ="+idVal);
            ResultSet rs11 = s11.getResultSet ();
            
            while (rs11.next ())
            {
            	try
            	{
	            	// Load NPCs
	            	
	            	myNPC npc = new myNPC(this.parent,fetchTriggerWords(rs11.getInt ("npcid")),loc,rs11.getString ("name"));
	            	npc.spawngroup = spawngroup;
	            	npc.id = rs11.getString ("npcid");
	            	npc.name = rs11.getString ("name");
	            	npc.category = rs11.getString("category");
	            	npc.weapon = rs11.getInt("weapon");
	            	npc.helmet = rs11.getInt("helmet");
	            	npc.chest = rs11.getInt("chest");
	            	npc.legs = rs11.getInt("legs");
	            	npc.boots = rs11.getInt("boots");
	            	
	            	if (npc.weapon == 0)
	            	{
	            		npc.weapon = 267;
	            	}
	            	
	            	if (npc.helmet == 0)
	            	{
	            		// no helmet >_<
	            		npc.helmet = 0;
	            	}
	            	
	            	if (npc.chest == 0)
	            	{
	            		npc.chest = 307;
	            	}
	            	
	            	if (npc.legs == 0)
	            	{
	            		npc.legs = 308;
	            	}
	            	if (npc.boots == 0)
	            	{
	            		npc.boots = 309;
	            	}
	            	
	            	
	            	
	            	for (myFaction faction : factions)
	            	{
	            		if (rs11.getInt("faction_id") == faction.id)
	            			npc.faction = faction;
	            	}
	            	
	            	npc.pathgroup = spawngroup.pathgroup;
	            	
	            	for (myLoottable loottable : loottables)
	            	{
	            		if (rs11.getInt("loottable_id") == loottable.id)
	            			npc.loottable = loottable;
	            	}
	            	spawngroup.npcs.put(spawngroup.id+"-"+rs11.getString ("npcid"), npc);
            	} catch(SQLException e)
            	{
            		e.printStackTrace();
            	}
            	
            }
            ++count1;
        }
        rs1.close ();
        s1.close ();
        System.out.println (count1 + " spawngroups loaded");
		} catch (SQLException e)
		{
			System.out.println("npcx : ERROR - spawngroup loading cancelled!");
			e.printStackTrace();
		} catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - spawngroup loading cancelled!");
	 		
        }
	}

	private void loadLoottables() {
		// TODO Auto-generated method stub
		try 
        {
            // Load loot tables
            Statement s1 = conn.createStatement ();
            s1.executeQuery ("SELECT * FROM loottables");
            ResultSet rs1 = s1.getResultSet ();
            int countloottables = 0;
            System.out.println("npcx : loading loottables");
            while (rs1.next ())
            {
            	myLoottable loottable = new myLoottable(rs1.getInt ("id"),rs1.getString ("name"));
            	loottable.id = rs1.getInt ("id");
            	loottable.name = rs1.getString ("name");
            	
            	Statement sFindEntries = conn.createStatement();
            	sFindEntries.executeQuery("SELECT * FROM loottable_entries WHERE loottable_id = " + loottable.id);
            	ResultSet rsEntries = sFindEntries.getResultSet ();
            	int countentries = 0;
            	while (rsEntries.next ())
	            {
            		
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
            rs1.close();
            s1.close();
            System.out.println("npcx : Loaded " + countloottables + " loottables.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - loottable loading cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

	private void loadPathgroups() {
		// TODO Auto-generated method stub
		try 
        {
            // Load pathgroups
            Statement spg = this.conn.createStatement ();
            spg.executeQuery ("SELECT * FROM pathgroup");
            ResultSet rspg = spg.getResultSet ();
            int countpg = 0;
            System.out.println("npcx : loading pathgroups");
            while (rspg.next ())
            {
            	myPathgroup pathgroup = new myPathgroup();
            	pathgroup.id = rspg.getInt ("id");
            	pathgroup.name = rspg.getString ("name");
            	pathgroup.category = rspg.getInt ("category");

            	Statement sFindEntries = conn.createStatement();
            	sFindEntries.executeQuery("SELECT * FROM pathgroup_entries WHERE pathgroup = " + pathgroup.id);
            	ResultSet rsEntries = sFindEntries.getResultSet ();
            	int countentries = 0;
            	while (rsEntries.next ())
	            {
            		
            		Location pgloc = new Location(parent.getServer().getWorld(this.defaultworld),rsEntries.getInt("x"),rsEntries.getInt("y"),rsEntries.getInt("z"),rsEntries.getFloat("yaw"),rsEntries.getFloat("pitch"));
            		
            		myPathgroup_entry entry = new myPathgroup_entry(pgloc,rsEntries.getInt("id"),pathgroup,rsEntries.getInt("s"));
            		
            		entry.id = rsEntries.getInt("id");
            		entry.name = rsEntries.getString("name");
            		entry.pathgroupid = rsEntries.getInt("pathgroup");
            		
            		countentries++;
            		pathgroup.pathgroupentries.add(entry);
	            }
            	rsEntries.close();
            	sFindEntries.close();
            	
            	countpg++;
            	pathgroups.add(pathgroup);
            	
            	
            }
            rspg.close();
            spg.close();
            System.out.println("npcx : Loaded " + countpg + " pathgroup.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - faction loading cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadFactions() {
		// TODO Auto-generated method stub
		try 
        {
            // Load faction_list
            Statement s1 = conn.createStatement ();
            s1.executeQuery ("SELECT * FROM faction_list");
            ResultSet rs1 = s1.getResultSet ();
            int countfaction = 0;
            System.out.println("npcx : loading factions");
            while (rs1.next ())
            {
            	myFaction faction = new myFaction();
            	faction.id = rs1.getInt ("id");
            	faction.name = rs1.getString ("name");
            	faction.base = rs1.getInt ("base");
            	countfaction++;
            	factions.add(faction);
            	
            	
            }
            rs1.close();
            s1.close();
            System.out.println("npcx : Loaded " + countfaction + " factions.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - faction loading cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
