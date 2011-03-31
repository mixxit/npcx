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

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class myUniverse {
	npcx parent;
	
	// config variable names
	public final String FILE_PROPERTIES = "npcx.properties";
	public final String PROP_NOWILD = "nowild";
	public final String PROP_NOCREEPER = "nocreeper";
	public final String PROP_NOSPREAD = "nospread";
	public final String PROP_DBHOST = "db-host";
	public final String PROP_DBUSER = "db-user";
	public final String PROP_DBPASS = "db-pass";
	public final String PROP_DBNAME = "db-name";
	public final String PROP_DBPORT = "db-port";
	public final String PROP_WORLD = "world";
	public final String PROP_UPDATE = "update";
	public final String PROP_DBVERSION = "db-version";
	public final String PROP_NATIONS = "nations";
	
	// db
	public String dsn;
	public Connection conn = null;
	public String nowild;
	public String nospread;	
	public String nocreeper;
	public String nations;
	public String dbhost;
	public String update;
	public String dbuser;
	public String dbpass;
	public String dbname;
	public String dbport;
	public String dbversion;
	
	
	public Properties prop;
	public File propfile;
	public File propfolder;
	
	// default world name
	public String defaultworld;

	// Lists of objects that are universal
	public List< myFaction > factions = new CopyOnWriteArrayList< myFaction >();
	public List< myLoottable > loottables = new CopyOnWriteArrayList< myLoottable >();
	public List< myPathgroup > pathgroups = new CopyOnWriteArrayList< myPathgroup >();
	public List< myMerchant > merchants = new CopyOnWriteArrayList< myMerchant >();
	public List< myZone > zones = new CopyOnWriteArrayList< myZone >();
	
	public HashMap<String, mySpawngroup> spawngroups = new HashMap<String, mySpawngroup>();
	public HashMap<String, myPlayer> players = new HashMap<String, myPlayer>();
	public HashMap<String, myNPC> npcs = new HashMap<String, myNPC>();
	public List< Monster > monsters = new CopyOnWriteArrayList< Monster >();
	public HashMap<String, myPlayer_factionentry> playerfactions = new HashMap<String, myPlayer_factionentry>();
	
	public myUniverse(npcx parent)
	{
		this.parent = parent;
	}
	
	public boolean openDB()
	{
		if (dbhost == null)
	 	{
		 	this.nowild = "false";
			this.nospread = "false";
		 	this.nocreeper = "false";
		 	this.dbhost = "localhost";
			this.dbuser = "npcx";
			this.dbname = "npcx";
			this.dbpass = "p4ssw0rd!";
			this.dbport = "3306";
			this.update = "true";
			this.dbversion = "1";
			this.nations = "false";
			
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
		 		return false;
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
		 		return false;
		 	}
		 	
		 	return true;
        } catch (Exception e)
        {
        	e.printStackTrace();
        	return false;
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
				nowild = config.getProperty("nowild");
				nospread = config.getProperty("nospread");
				nocreeper = config.getProperty("nocreeper");
				dbhost = config.getProperty("db-host");
				dbuser = config.getProperty("db-user");
				dbpass = config.getProperty("db-pass");
				dbname = config.getProperty("db-name");
				dbport = config.getProperty("db-port");
				dbversion = config.getProperty("db-version");
				nations = config.getProperty("nations");
				
				this.defaultworld = config.getProperty("world");
				
				update = config.getProperty("update");
				
				dsn = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
				System.out.println(dsn);
				
				if (nospread == null || nocreeper == null || nowild == null || nations == null)
				{
					if (nowild == null)
					{
						config.setProperty(PROP_NOWILD,"false");
						nowild = "false";

					}
					
					if (nocreeper == null)
					{
						config.setProperty(PROP_NOCREEPER,"false");
						nocreeper = "false";

					}
					
					if (nations == null)
					{
						config.setProperty(PROP_NATIONS,"false");
						nations = "false";
					}
					
					if (nospread == null)
					{
						config.setProperty(PROP_NOSPREAD,"false");
						nospread = "false";
					}
					
					
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
		            config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,update);
		            
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
					
				}
				
				
				
				if (dbversion == null)
				{
					System.out.println("************************************************");
					System.out.println("* Load settings failed to load your DB setup   *");
					System.out.println("*    YOU ARE USING AN OLD RELEASE OF NPCX      *");
					System.out.println("*  AND MUST NOW WIPE YOUR DB BY REMOVING YOUR  *");
					System.out.println("* PLUGINS/NPCX FOLDER AND RESTARTING BUKKIT    *");
					System.out.println("* PLEASE RECONFIGURE ANY SETTINGS AS NECESSARY *");
					System.out.println("************************************************");
					return false;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (openDB() != true)
		{
			System.out.println("**********************************************");
			System.out.println("* Load settings failed to load your DB setup *");
			System.out.println("*                 openDB()                   *");
			System.out.println("**********************************************");
			return false;
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
	
	private boolean updateDB() {
		// TODO Auto-generated method stub
		String targetdbversion = "1.06";
		System.out.println("npcx : Checking for DB Updates from DBVersion:"+this.dbversion);
		if(this.dbversion.matches(targetdbversion))
		{
			return true;
		}
		
		if (this.dbversion.matches("1"))
		{
			// Fix autonumber on npcx.merchant_entries
			Statement sqlCreatestmt;
			try {
				sqlCreatestmt = conn.createStatement();
				
				String sqlcreate = "ALTER TABLE merchant_entries CHANGE COLUMN id id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT";
	            sqlCreatestmt.executeUpdate(sqlcreate);
	            sqlCreatestmt.close();
	            Properties config = new Properties();
				BufferedInputStream stream;
				try
				{
					stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
					config.load(stream);
			
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_NOSPREAD, nospread);
					config.setProperty(PROP_NOCREEPER,nocreeper);
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            config.setProperty(PROP_DBVERSION, "1.01");
		            
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
		            
				} catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("**********************************************");
					System.out.println("*   Problem during update to version 1.01    *");
					System.out.println("*     Can you access your config file?       *");
					System.out.println("**********************************************");
					return false;
				}
				
				this.dbversion = "1.01";
				System.out.println("**********************************************");
				System.out.println("* Congratulations! Your NPCX database is now *");
				System.out.println("*       updated to version 1.01              *");
				System.out.println("**********************************************");
				// continue on till we get the right version 
				//return true;
			} catch (SQLException e) {
				System.out.println("**********************************************");
				System.out.println("*   Problem during update to version 1.01    *");
				System.out.println("*  Do you have entries in merchant_entries?  *");
				System.out.println("**********************************************");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		if (this.dbversion.matches("1.01"))
		{
			// Create Player table
			// Update size of triggerword response
			Statement sqlCreatestmt;
			try {
				sqlCreatestmt = conn.createStatement();
				
			     
				String sqldrop = "DROP TABLE IF EXISTS player";
	            sqlCreatestmt.executeUpdate(sqldrop);
				String sqlcreate = "CREATE TABLE player (  id int(10) unsigned NOT NULL AUTO_INCREMENT,  name varchar(45) DEFAULT NULL,  coin int(10) unsigned DEFAULT NULL,  flags int(10) unsigned DEFAULT NULL,  petid int(11) DEFAULT NULL,  PRIMARY KEY (id),  UNIQUE KEY name_UNIQUE (name)) ";
				sqlCreatestmt.executeUpdate(sqlcreate);
				String sqlalter = "ALTER TABLE npc_triggerwords CHANGE COLUMN reply reply VARCHAR(512) NULL DEFAULT NULL;";
				sqlCreatestmt.executeUpdate(sqlalter);
				sqlCreatestmt.close();
	            Properties config = new Properties();
				BufferedInputStream stream;
				try
				{
					stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
					config.load(stream);
			
					config.setProperty(PROP_NOCREEPER,nocreeper);
					config.setProperty(PROP_NOSPREAD, nospread);
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            config.setProperty(PROP_DBVERSION, "1.02");
		            this.dbversion = "1.02";
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
		            // continue on
				} catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("**********************************************");
					System.out.println("*   Problem during update to version 1.02    *");
					System.out.println("*     Can you access your config file?       *");
					System.out.println("**********************************************");
					return false;
				}
				
				
				System.out.println("**********************************************");
				System.out.println("* Congratulations! Your NPCX database is now *");
				System.out.println("*       updated to version 1.02              *");
				System.out.println("**********************************************");
				// continue on
			} catch (SQLException e) {
				System.out.println("**********************************************");
				System.out.println("*   Problem during update to version 1.02    *");
				System.out.println("* Please provide stacktrace below to devs    *");
				System.out.println("**********************************************");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		if (this.dbversion.matches("1.02"))
		{
			// Create Player table
			// Update size of triggerword response
			Statement sqlCreatestmt;
			try {
				sqlCreatestmt = conn.createStatement();
				
			     
				String sqldrop = "DROP TABLE IF EXISTS player_quests";
	            sqlCreatestmt.executeUpdate(sqldrop);
	            String sqlcreate = "CREATE  TABLE player_quests (  idnew_table INT NOT NULL ,  questid INT(11) UNSIGNED NULL ,  playername VARCHAR(45) NULL ,  status INT UNSIGNED NULL DEFAULT 1 ,  step INT UNSIGNED NULL DEFAULT 1 ,  INDEX fk_questid (questid ASC) ,  PRIMARY KEY (idnew_table) ,  CONSTRAINT fk_questid    FOREIGN KEY (questid )REFERENCES quests (id )    ON DELETE NO ACTION    ON UPDATE NO ACTION);";
				sqlCreatestmt.executeUpdate(sqlcreate);

				sqldrop = "DROP TABLE IF EXISTS quests";
	            sqlCreatestmt.executeUpdate(sqldrop);
				sqlcreate = "CREATE TABLE quests (  id int(11) unsigned NOT NULL AUTO_INCREMENT,  name varchar(45) DEFAULT 'DefaultQuest',  reward int(10) unsigned DEFAULT '0',  rewardcoin int(10) unsigned DEFAULT '0',  rewardtext varchar(512) DEFAULT 'Well done!',  step int(11) DEFAULT '1',  category varchar(45) DEFAULT 'default',  PRIMARY KEY (id)) ";
				sqlCreatestmt.executeUpdate(sqlcreate);

				sqlCreatestmt.close();
	            Properties config = new Properties();
				BufferedInputStream stream;
				try
				{
					stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
					config.load(stream);
			
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_NOSPREAD, nospread);
					config.setProperty(PROP_NOCREEPER,nocreeper);
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            config.setProperty(PROP_DBVERSION, "1.03");
		            this.dbversion = "1.03";
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
		            
				} catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("**********************************************");
					System.out.println("*   Problem during update to version 1.03    *");
					System.out.println("*     Can you access your config file?       *");
					System.out.println("**********************************************");
					return false;
				}
				
				
				System.out.println("**********************************************");
				System.out.println("* Congratulations! Your NPCX database is now *");
				System.out.println("*       updated to version 1.03              *");
				System.out.println("**********************************************");
				return true;
			} catch (SQLException e) {
				System.out.println("**********************************************");
				System.out.println("*   Problem during update to version 1.03    *");
				System.out.println("*  Please provide stacktrace below to devs   *");
				System.out.println("**********************************************");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		if (this.dbversion.matches("1.03"))
		{
			// Create Player table
			// Update size of triggerword response
			Statement sqlCreatestmt;
			try {
				sqlCreatestmt = conn.createStatement();
				String sqldrop = "DROP TABLE IF EXISTS zone_members";
	            sqlCreatestmt.executeUpdate(sqldrop);
				sqldrop = "DROP TABLE IF EXISTS zone";
	            sqlCreatestmt.executeUpdate(sqldrop);
				String sqlcreate = "CREATE TABLE zone (  id int(10) unsigned NOT NULL AUTO_INCREMENT,  name varchar(45) DEFAULT 'The Wild',  x int(11) DEFAULT NULL,  z int(11) DEFAULT NULL,  ownername varchar(45) DEFAULT '',  PRIMARY KEY (id),  UNIQUE KEY loc (x,z))";
				sqlCreatestmt.executeUpdate(sqlcreate);
				
				sqlcreate = "CREATE  TABLE zone_members (  id INT UNSIGNED NOT NULL AUTO_INCREMENT ,  playername VARCHAR(45) NULL ,  zoneid INT UNSIGNED NULL ,  PRIMARY KEY (id) ,  INDEX fk_zoneid (id ASC) ,CONSTRAINT fk_zoneid    FOREIGN KEY (id )    REFERENCES zone (id )    ON DELETE NO ACTION    ON UPDATE NO ACTION);";
				sqlCreatestmt.executeUpdate(sqlcreate);

				sqlCreatestmt.close();
	            Properties config = new Properties();
				BufferedInputStream stream;
				try
				{
					stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
					config.load(stream);
			
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_NOSPREAD,nospread);
					config.setProperty(PROP_NOCREEPER,nocreeper);
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            config.setProperty(PROP_DBVERSION, "1.04");
		            this.dbversion = "1.04";
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
		            
				} catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("**********************************************");
					System.out.println("*   Problem during update to version 1.04    *");
					System.out.println("*     Can you access your config file?       *");
					System.out.println("**********************************************");
					return false;
				}
				
				
				System.out.println("**********************************************");
				System.out.println("* Congratulations! Your NPCX database is now *");
				System.out.println("*       updated to version 1.04              *");
				System.out.println("**********************************************");
				return true;
			} catch (SQLException e) {
				System.out.println("**********************************************");
				System.out.println("*   Problem during update to version 1.04    *");
				System.out.println("*  Please provide stacktrace below to devs   *");
				System.out.println("**********************************************");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		if (this.dbversion.matches("1.04"))
		{
			// Create Player table
			// Update size of triggerword response
			Statement sqlCreatestmt;
			try {
				
				sqlCreatestmt = conn.createStatement();
				String sqlcreate = "ALTER TABLE player_faction ADD UNIQUE INDEX playerfactionzz (player_name ASC, faction_id ASC) ;";
				sqlCreatestmt.executeUpdate(sqlcreate);

				sqlCreatestmt.close();
	            Properties config = new Properties();
				BufferedInputStream stream;
				try
				{
					stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
					config.load(stream);
			
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_NOSPREAD,nospread);
					config.setProperty(PROP_NOCREEPER,nocreeper);
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            config.setProperty(PROP_DBVERSION, "1.05");
		            this.dbversion = "1.05";
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
		            
				} catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("**********************************************");
					System.out.println("*   Problem during update to version 1.05    *");
					System.out.println("*     Can you access your config file?       *");
					System.out.println("**********************************************");
					return false;
				}
				
				
				System.out.println("**********************************************");
				System.out.println("* Congratulations! Your NPCX database is now *");
				System.out.println("*       updated to version 1.05              *");
				System.out.println("**********************************************");
				// continue on
			} catch (SQLException e) {
				System.out.println("**********************************************");
				System.out.println("*   Problem during update to version 1.05    *");
				System.out.println("*  Please provide stacktrace below to devs   *");
				System.out.println("**********************************************");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
		if (this.dbversion.matches("1.05"))
		{
			// Create Player table
			// Update size of triggerword response
			Statement sqlCreatestmt;
			try {
				
				sqlCreatestmt = conn.createStatement();
				String sqlcreate = "ALTER TABLE merchant ADD COLUMN category VARCHAR(45) NULL  AFTER name ;";
				sqlCreatestmt.executeUpdate(sqlcreate);

				sqlCreatestmt.close();
	            Properties config = new Properties();
				BufferedInputStream stream;
				try
				{
					stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
					config.load(stream);
			
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_NOSPREAD,nospread);
					config.setProperty(PROP_NOCREEPER,nocreeper);
					config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
					config.setProperty(PROP_WORLD,defaultworld);
		            config.setProperty(PROP_UPDATE,"false");
		            config.setProperty(PROP_DBVERSION, "1.06");
		            this.dbversion = "1.06";
		            File propfolder = parent.getDataFolder();
		            File propfile = new File(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES);
		            propfile.createNewFile();
		            
		            BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(propfile.getAbsolutePath()));
					config.store(stream1, "Default generated settings, please ensure mysqld matches");
		            
				} catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("**********************************************");
					System.out.println("*   Problem during update to version 1.06    *");
					System.out.println("*     Can you access your config file?       *");
					System.out.println("**********************************************");
					return false;
				}
				
				
				System.out.println("**********************************************");
				System.out.println("* Congratulations! Your NPCX database is now *");
				System.out.println("*       updated to version 1.06              *");
				System.out.println("**********************************************");
				return true;
			} catch (SQLException e) {
				System.out.println("**********************************************");
				System.out.println("*   Problem during update to version 1.06    *");
				System.out.println("*  Please provide stacktrace below to devs   *");
				System.out.println("**********************************************");
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		
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
				prop.setProperty(PROP_NOWILD, "false");
				prop.setProperty(PROP_NOSPREAD, "false");
				prop.setProperty(PROP_NOCREEPER, "false");
				prop.setProperty(PROP_DBHOST, "localhost");
				prop.setProperty(PROP_DBUSER, "npcx");
				prop.setProperty(PROP_DBPASS, "p4ssw0rd!");
				prop.setProperty(PROP_DBNAME, "npcx");
				prop.setProperty(PROP_DBPORT, "3306");
				prop.setProperty(PROP_NATIONS, "false");
				prop.setProperty(PROP_DBVERSION, "1");
				prop.setProperty(PROP_UPDATE, "true");
				this.nospread = "false";
				this.nocreeper = "false";
				this.nowild = "false";
				this.dbhost = "localhost";
				this.dbuser = "npcx";
				this.dbname = "npcx";
				this.dbpass = "p4ssw0rd!";
				this.dbport = "3306";
				this.nations = "false";
				this.dbversion = "1";
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
	
	
	public boolean checkUpdates()
	{
		if (updateDB() != true)
		{
			System.out.println("**********************************************");
			System.out.println("* Your DB is currently out of sync with your *");
			System.out.println("*           version of NPCX                  *");
			System.out.println("**********************************************");			
			return false;
		} else {
			return true;
		}
		
	}
	

	public void checkDbSetup() {
		// TODO Auto-generated method stub
		
		
		// Check the config file
		try {
			
			Properties config = new Properties();
			BufferedInputStream stream;
			stream = new BufferedInputStream(new FileInputStream(propfolder.getAbsolutePath() + File.separator + FILE_PROPERTIES));
			config.load(stream);
			update = config.getProperty("update");
			
			if (update.matches("true"))
            {
            	
            	
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
	            	
	            	// Purge old tables
	            	
	            	
	            	Statement sqlDropstmt = conn.createStatement();
	            	String sqldrop = "DROP TABLE IF EXISTS npc_triggerwords; ";
		            sqlDropstmt.executeUpdate(sqldrop);

		            sqldrop = "DROP TABLE IF EXISTS spawngroup_entries; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS npc_faction; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS npc; ";
		            sqlDropstmt.executeUpdate(sqldrop);

		            sqldrop = "DROP TABLE IF EXISTS loottables; ";
	            	sqlDropstmt.executeUpdate(sqldrop);
		            
	            	sqldrop = "DROP TABLE IF EXISTS loottable_entries; ";
		            sqlDropstmt.executeUpdate(sqldrop);

		            sqldrop = "DROP TABLE IF EXISTS merchant_entries";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS merchant";
		            sqlDropstmt.executeUpdate(sqldrop);
		       
		            
		            sqldrop = "DROP TABLE IF EXISTS player_faction";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS faction_list; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            
		            
		            sqldrop = "DROP TABLE IF EXISTS spawngroup; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS pathgroup_entries; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS pathgroup; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS player_bank";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS player_flags";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS flags; ";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS settings";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS storage_entries";
		            sqlDropstmt.executeUpdate(sqldrop);
		            
		            sqldrop = "DROP TABLE IF EXISTS storage";
		            sqlDropstmt.executeUpdate(sqldrop);
		            sqlDropstmt.close();
		            
		            // Create Tables
		            
	            	Statement sqlCreatestmt = conn.createStatement();
		        
	            	
	            	String sqlcreate = "CREATE TABLE storage ( id int(10) unsigned NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, PRIMARY KEY (id) ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE storage_entries ( id int(10) unsigned NOT NULL AUTO_INCREMENT, storageid int(10) unsigned DEFAULT NULL, itemid int(10) unsigned DEFAULT '0', amount int(10) unsigned DEFAULT '0', price int(10) unsigned DEFAULT '0', PRIMARY KEY (id), KEY fk_storage_id (storageid), CONSTRAINT fk_storage_id FOREIGN KEY (storageid) REFERENCES storage (id) ON DELETE NO ACTION ON UPDATE NO ACTION ) ";
	            	
		            sqlcreate = "CREATE TABLE flags ( id int(10) unsigned NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, PRIMARY KEY (id) )";
	            	sqlCreatestmt.executeUpdate(sqlcreate);
	            	
		            sqlcreate = "CREATE TABLE faction_list ( id int(11) unsigned NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, base int(11) DEFAULT NULL, PRIMARY KEY (id) )";
	            	sqlCreatestmt.executeUpdate(sqlcreate);
	            	
		            sqlcreate = "CREATE TABLE loottable_entries (id int(11) NOT NULL AUTO_INCREMENT,  loottable_id int(11) DEFAULT NULL,  item_id int(11) DEFAULT NULL,  amount int(11) DEFAULT NULL,  PRIMARY KEY (id))";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE loottables ( id int(11) unsigned NOT NULL AUTO_INCREMENT, name varchar(50) DEFAULT NULL, PRIMARY KEY (id) ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE merchant ( id int(10) unsigned NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, PRIMARY KEY (id) ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE merchant_entries ( id int(10) unsigned NOT NULL, merchantid int(10) unsigned DEFAULT NULL, itemid int(10) unsigned DEFAULT NULL, amount int(10) unsigned DEFAULT NULL, pricebuy int(10) unsigned DEFAULT NULL, pricesell int(10) unsigned DEFAULT NULL, PRIMARY KEY (id), KEY fk_merchantid (merchantid), CONSTRAINT fk_merchantid FOREIGN KEY (merchantid) REFERENCES merchant (id) ON DELETE NO ACTION ON UPDATE NO ACTION ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE npc ( id int(11) unsigned NOT NULL AUTO_INCREMENT, name char(40) DEFAULT 'dummy', category char(40) DEFAULT NULL, faction_id int(11) unsigned DEFAULT NULL, loottable_id int(11) unsigned DEFAULT NULL, weapon int(11) unsigned DEFAULT '0', helmet int(11) unsigned DEFAULT '0', chest int(11) unsigned DEFAULT '0', legs int(11) unsigned DEFAULT '0', boots int(11) unsigned DEFAULT '0', merchantid int(10) unsigned DEFAULT NULL, hp int(11) unsigned DEFAULT '100', damage int(11) unsigned DEFAULT '3', coin int(11) unsigned DEFAULT '100', storageid int(11) unsigned DEFAULT NULL, PRIMARY KEY (id), KEY fk_npc_factionid (faction_id), KEY fk_npc_loottableid (loottable_id), KEY fk_npc_merchantid (merchantid), KEY fk_npc_storageid (storageid), CONSTRAINT fk_npc_storageid FOREIGN KEY (storageid) REFERENCES storage (id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fk_npc_factionid FOREIGN KEY (faction_id) REFERENCES faction_list (id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fk_npc_loottableid FOREIGN KEY (loottable_id) REFERENCES loottables (id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fk_npc_merchantid FOREIGN KEY (merchantid) REFERENCES merchant (id) ON DELETE NO ACTION ON UPDATE NO ACTION )";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE npc_faction ( id int(11) unsigned NOT NULL AUTO_INCREMENT, npc_id int(11) unsigned DEFAULT NULL, faction_id int(11) unsigned DEFAULT NULL, amount int(11) DEFAULT NULL, PRIMARY KEY (id), KEY fk_npcid (npc_id), KEY fk_factionid (faction_id), CONSTRAINT fk_npcid FOREIGN KEY (npc_id) REFERENCES npc (id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fk_factionid FOREIGN KEY (faction_id) REFERENCES faction_list (id) ON DELETE NO ACTION ON UPDATE NO ACTION )";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE pathgroup ( id int(10) unsigned NOT NULL AUTO_INCREMENT, name char(40) DEFAULT NULL, category char(40) DEFAULT NULL, PRIMARY KEY (id) ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE pathgroup_entries ( id int(10) unsigned NOT NULL AUTO_INCREMENT, s int(11) unsigned DEFAULT NULL, pathgroup int(11) unsigned DEFAULT NULL, name char(40) DEFAULT NULL, x char(40) DEFAULT NULL, y char(40) DEFAULT NULL, z char(40) DEFAULT NULL, yaw char(40) DEFAULT NULL, pitch char(40) DEFAULT NULL, PRIMARY KEY (id), KEY fk_pathgroupid (pathgroup), CONSTRAINT fk_pathgroupid FOREIGN KEY (pathgroup) REFERENCES pathgroup (id) ON DELETE NO ACTION ON UPDATE NO ACTION ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE spawngroup ( id int(10) unsigned NOT NULL AUTO_INCREMENT, name char(40) DEFAULT 'defaultspawngroup', world char(40) DEFAULT NULL, category char(40) DEFAULT NULL, x char(40) DEFAULT NULL, y char(40) DEFAULT NULL, z char(40) DEFAULT NULL, yaw char(40) DEFAULT NULL, pitch char(40) DEFAULT NULL, pathgroupid int(10) unsigned DEFAULT '0', PRIMARY KEY (id) )";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE spawngroup_entries ( id int(10) unsigned NOT NULL AUTO_INCREMENT, spawngroupid int(11) unsigned DEFAULT NULL, npcid int(11) unsigned DEFAULT NULL, PRIMARY KEY (id), KEY fk_spawngroupid (spawngroupid), KEY fk_npcidSGE (npcid), CONSTRAINT fk_npcidSGE FOREIGN KEY (npcid) REFERENCES npc (id) ON DELETE NO ACTION ON UPDATE NO ACTION, CONSTRAINT fk_spawngroupid FOREIGN KEY (spawngroupid) REFERENCES spawngroup (id) ON DELETE NO ACTION ON UPDATE NO ACTION )";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE npc_triggerwords ( id int(10) unsigned NOT NULL AUTO_INCREMENT, npcid int(11) unsigned DEFAULT NULL, triggerword char(40) DEFAULT NULL, reply varchar(256) DEFAULT NULL, category char(40) DEFAULT NULL, PRIMARY KEY (id), KEY fk_npct2n (npcid), CONSTRAINT fk_npct2n FOREIGN KEY (npcid) REFERENCES npc (id) ON DELETE NO ACTION ON UPDATE NO ACTION )";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE player_bank ( id int(10) unsigned NOT NULL AUTO_INCREMENT, playername varchar(45) DEFAULT NULL, itemid int(10) unsigned DEFAULT NULL, coin int(10) unsigned DEFAULT NULL, amount int(10) unsigned DEFAULT NULL, PRIMARY KEY (id) ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE player_faction ( id int(11) unsigned NOT NULL AUTO_INCREMENT, player_name varchar(45) DEFAULT NULL, faction_id int(11) unsigned DEFAULT NULL, amount int(11) DEFAULT NULL, PRIMARY KEY (id), KEY fk_faction_id (faction_id), CONSTRAINT fk_faction_id FOREIGN KEY (faction_id) REFERENCES faction_list (id) ON DELETE NO ACTION ON UPDATE NO ACTION ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE player_flags ( id int(10) unsigned NOT NULL AUTO_INCREMENT, flagid int(10) unsigned DEFAULT NULL, playername varchar(45) DEFAULT NULL, value varchar(45) DEFAULT NULL, PRIMARY KEY (id), KEY fk_flagid (flagid), CONSTRAINT fk_flagid FOREIGN KEY (flagid) REFERENCES flags (id) ON DELETE NO ACTION ON UPDATE NO ACTION ) ";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
		            sqlcreate = "CREATE TABLE settings ( id int(10) unsigned NOT NULL AUTO_INCREMENT, name varchar(45) DEFAULT NULL, value varchar(45) DEFAULT NULL, PRIMARY KEY (id) )";
		            sqlCreatestmt.executeUpdate(sqlcreate);
		            
	            	sqlCreatestmt.close();
		            
		            System.out.println("npcx : finished table configuration");
		            nospread = config.getProperty("nospread");
		            nocreeper = config.getProperty("nocreeper");
		            nowild = config.getProperty("nowild");
		            dbhost = config.getProperty("db-host");
		            dbuser = config.getProperty("db-user");
		            dbpass = config.getProperty("db-pass");
		            dbname = config.getProperty("db-name");
		            dbport = config.getProperty("db-port");
		            nations = config.getProperty("nations");
		            dbversion = config.getProperty("db-version");
		            
		            dsn = "jdbc:mysql://" + dbhost + ":" + dbport + "/" + dbname;
		            defaultworld = config.getProperty("world");
		            
					config.setProperty(PROP_NOWILD,nowild);
					config.setProperty(PROP_NOSPREAD,nospread);
					config.setProperty(PROP_NOCREEPER,nocreeper);
		            config.setProperty(PROP_DBHOST,dbhost);
					config.setProperty(PROP_DBUSER,dbuser);
					config.setProperty(PROP_DBPASS,dbpass);
					config.setProperty(PROP_DBNAME,dbname);
					config.setProperty(PROP_DBPORT,dbport);
					config.setProperty(PROP_NATIONS,nations);
		            config.setProperty(PROP_DBVERSION,dbversion);
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
		loadPlayerFactions();
		loadZones();
		loadMerchants();
		loadFactions();
		loadPathgroups();
		loadLoottables();
		loadSpawngroups();
	}
	
	public void commitPlayerFactions() {
		// TODO Auto-generated method stub
		try 
        {
           int countfaction = 0;
			// save factions
			for (myPlayer_factionentry e : playerfactions.values())
			{
				PreparedStatement stmt = this.parent.universe.conn.prepareStatement("INSERT INTO player_faction (player_name,faction_id,amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount=VALUES(amount) ",Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1,e.playername);
				stmt.setInt(2,e.factionid);
				stmt.setInt(3,e.amount);
				
				stmt.executeUpdate();
				ResultSet keyset = stmt.getGeneratedKeys();
				int key = 0;
				if ( keyset.next() ) {
				    // Retrieve the auto generated key(s).
				    key = keyset.getInt(1);
				}
				countfaction++;

				stmt.close();
			}
			
            //System.out.println("npcx : finished " + countfaction + " factions commit.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - player factions commit cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadPlayerFactions() {
		// TODO Auto-generated method stub
		try 
        {
            // Load faction_list
            Statement s1 = conn.createStatement ();
            s1.executeQuery ("SELECT * FROM player_faction");
            ResultSet rs1 = s1.getResultSet ();
            int countfaction = 0;
            System.out.println("npcx : loading player factions");
            while (rs1.next ())
            {
            	myPlayer_factionentry z = new myPlayer_factionentry();
            	z.id = rs1.getInt("id");
            	z.playername = rs1.getString("player_name");
            	z.factionid = rs1.getInt("id");
            	countfaction++;
            	playerfactions.put(Integer.toString(z.id),z);
            }
            rs1.close();
            s1.close();
            System.out.println("npcx : Loaded " + countfaction + " player factions.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - player factions loading cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
            s11.executeQuery ("SELECT npc.weapon As weapon, npc.merchantid As merchantid, npc.helmet As helmet,npc.chest As chest,npc.legs As legs,npc.boots As boots,spawngroup_entries.spawngroupid As spawngroupid,spawngroup_entries.npcid As npcid, npc.name As name, npc.category As category, npc.loottable_id As loottable_id, npc.faction_id As faction_id FROM spawngroup_entries,npc WHERE npc.id = spawngroup_entries.npcid AND spawngroup_entries.spawngroupid ="+idVal);
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
	            	
	            	for (myMerchant merchant : merchants)
	            	{
	            		if (rs11.getInt("merchantid") == merchant.id)
	            			npc.merchant = merchant;
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
	 		System.out.println("npcx : ERROR - pathgroup loading cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void loadMerchants() {
		// TODO Auto-generated method stub
		try 
        {
            // Load Merchants
            Statement spg = this.conn.createStatement ();
            spg.executeQuery ("SELECT * FROM merchant");
            ResultSet rspg = spg.getResultSet ();
            int countpg = 0;
            System.out.println("npcx : loading Merchants");
            int countentries = 0;
            while (rspg.next ())
            {
            	myMerchant merchant = new myMerchant(this.parent,rspg.getInt ("id"),rspg.getString ("name"));
            	merchant.id = rspg.getInt ("id");
            	merchant.name = rspg.getString ("name");
            	merchant.category = rspg.getString ("category");
            	
            	

            	Statement sFindEntries = conn.createStatement();
            	sFindEntries.executeQuery("SELECT * FROM merchant_entries WHERE merchantid = " + merchant.id);
            	ResultSet rsEntries = sFindEntries.getResultSet ();
            	
            	while (rsEntries.next ())
	            {
            		
            		
            		myMerchant_entry entry = new myMerchant_entry(merchant, merchant.id, rsEntries.getInt("itemid"), rsEntries.getInt("amount"), rsEntries.getInt("pricebuy"), rsEntries.getInt("pricesell"));
            		
            		entry.id = rsEntries.getInt("id");
            		entry.itemid = rsEntries.getInt("itemid");
            		entry.amount = rsEntries.getInt("amount");
            		entry.pricebuy = rsEntries.getInt("pricebuy");
            		entry.pricesell = rsEntries.getInt("pricesell");
            		
            		countentries++;
            		merchant.merchantentries.add(entry);
	            }
            	rsEntries.close();
            	sFindEntries.close();
            	
            	countpg++;
            	merchants.add(merchant);
            	
            	
            }
            rspg.close();
            spg.close();
            System.out.println("npcx : Loaded " + countpg + " Merchant with ("+countentries+") entries.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - Merchant loading cancelled!");
        } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void loadZones() {
		// TODO Auto-generated method stub
		try 
        {
            // Load faction_list
            Statement s1 = conn.createStatement ();
            s1.executeQuery ("SELECT * FROM zone");
            ResultSet rs1 = s1.getResultSet ();
            int countzone = 0;
            System.out.println("npcx : loading zones");
            while (rs1.next ())
            {
            	myZone z = new myZone(this,rs1.getInt ("id"),null,rs1.getInt ("x"),rs1.getInt ("z"));
            	z.name = rs1.getString ("name");
            	z.ownername = rs1.getString ("ownername");


            	countzone++;
            	zones.add(z);
            	
            	
            }
            rs1.close();
            s1.close();
            System.out.println("npcx : Loaded " + countzone + " zones.");
            
        } catch (NullPointerException e) { 
	 		System.out.println("npcx : ERROR - zone loading cancelled!");
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

	public int getPlayerBalance(Player player) {
		// TODO Auto-generated method stub
		try
		{
			Account account = iConomy.getBank().getAccount(player.getName());
			return (int)account.getBalance();
		} catch (NoClassDefFoundError e)
		{
			for (myPlayer p : this.players.values())
			{
				if (p.player == player)
					return p.getNPCXBalance();
			}
			e.printStackTrace();
			// We don't have iConomy
			return 0;
		}
	}

	public void subtractPlayerBalance(Player player, int totalcost) {
		// TODO Auto-generated method stub
		try
		{
			Account account = iConomy.getBank().getAccount(player.getName());
			account.subtract(totalcost);
		} catch (NoClassDefFoundError e)
		{
			
			for (myPlayer p : this.players.values())
			{
				if (p.player == player)
					p.setNPCXBalance(p.getNPCXBalance()-totalcost);
			}
			// We don't have iConomy
			
			
		}
	}
	

	public void addPlayerBalance(Player player, int totalcost) {
		// TODO Auto-generated method stub
		try
		{
			Account account = iConomy.getBank().getAccount(player.getName());
			account.add(totalcost);
		} catch (NoClassDefFoundError e)
		{
			// We don't have iConomy
			for (myPlayer p : this.players.values())
			{
				if (p.player == player)
					p.setNPCXBalance(p.getNPCXBalance()+totalcost);
			}
		}
	}

	public boolean hasPlayerEnoughPlayerBalance(Player player, float totalcost) {
		// TODO Auto-generated method stub
		
		try
		{
			Account account = iConomy.getBank().getAccount(player.getName());
			return account.hasEnough(totalcost);
		} catch (NoClassDefFoundError e)
		{
			for (myPlayer p : this.players.values())
			{
				if (p.player == player)
				{
					if (p.getNPCXBalance() >= totalcost)
					{
						
						return true;
					} else {
						return false;
					}
				}
			}
			return false;
			
		}
	}

	public boolean checkChunks() {
		// TODO Auto-generated method stub
		
		try
		{
			int count = 0;
			for (myNPC npc : parent.universe.npcs.values())
			{
				if (parent.universe.npcs != null)
				{
					if (npc.npc != null)
					{
						for (myZone zone : this.zones)
						{
							
							if (zone.chunk.getWorld()
									.getChunkAt(npc.npc.getBukkitEntity().getLocation())
									.equals(zone)) {
								count++;
								npc.npc.chunkinactive(npc.npc.getBukkitEntity().getLocation());
							}
						}
					}
				}
			}
			if (count != 0)
			{
				return true;
			}
			
			return false;
			
		} catch (Exception e)
		{
			// looks like a table was locked, we need to mark this to try again
			return false;
		}
		
	}

	public myPlayer findmyPlayerByPlayer(Player p) {
		// TODO Auto-generated method stub
		for (myPlayer player : this.players.values())
		{
			if (p == player.player)
				return player;
		}
		return null;
	}

	public myZone getmyChunk(Chunk chunk) {
		// TODO Auto-generated method stub
		for (myZone zone : this.zones)
		{
			if (zone.chunk != null)
			{				
				if (chunk == zone.chunk)
				{
					return zone;
				}
			}
		}
		
		return null;
	}

	public void setPlayerLastmyChunk(Player player, myZone myzone) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player.getName() == player.getName())
			{
				p.lastmyzone = myzone;
			}
		}
	}

	public myZone getPlayerLastmyChunk(Player player) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player.getName() == player.getName())
			{
				return p.lastmyzone;
			}
		}
		return null;
	}

	public int getPlayerLastChunkX(Player player) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player.getName() == player.getName())
			{
				return p.lastchunkx;
			}
		}
		System.out.println("Did not find player!");
		return 0;
	}
	
	public int setPlayerLastChunkX(Player player,int x) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player.getName() == player.getName())
			{
				p.lastchunkx = x;
			}
		}
		return 0;
	}
	
	public int getPlayerLastChunkZ(Player player) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player.getName() == player.getName())
			{
				return p.lastchunkz;
			}
		}
		return 0;
	}
	
	public int setPlayerLastChunkZ(Player player,int z) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player.getName() == player.getName())
			{
				p.lastchunkz = z;
			}
		}
		return 0;
	}

	public String getZoneNameByLocation(int x, int z,World world) {
		// TODO Auto-generated method stub
	
		for(myZone zone : zones)
		{
			if (zone.x == x && zone.z == z)
			{
				return zone.name;
			}
		}
		
		// doesnt exist, create it
		myZone c = createBlankZone(x,z,world);
		
		return c.name;
	}
	
	public int getZoneCoord(double x)
	{
		
		return (int) Math.floor(( x / 16 ) + 0.9375 );
	}
	public double getZoneLocPos(int x)
	{
		
		return (x-0.9375)*16;
	}
	
	
	public Location getLocationAtZoneLoc(int x, int z, World world)
	{
		double xloc = getZoneLocPos(x);
		double yloc = 0;
		double zloc = getZoneLocPos(z);
		Location loc = new Location(world, xloc,yloc,zloc);
		return loc;
		
	}

	private myZone createBlankZone(int x, int z, World world) {
		// TODO Auto-generated method stub
		try {
			PreparedStatement stmt = this.parent.universe.conn.prepareStatement("INSERT INTO zone (name,x,z,ownername) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE ownername=VALUES(ownername) ",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1,"The Wild");
			stmt.setInt(2,x);
			stmt.setInt(3,z);
			stmt.setString(4,"");
			
			stmt.executeUpdate();
			ResultSet keyset = stmt.getGeneratedKeys();
			int key = 0;
			if ( keyset.next() ) {
			    // Retrieve the auto generated key(s).
			    key = keyset.getInt(1);
			    
			}
			stmt.close();
			
			try
			{
				myZone c = new myZone(this,key, world.getChunkAt(getLocationAtZoneLoc(x,z,world)),x,z);
				this.zones.add(c);
				return c;
			} catch (Exception e)
			{
				// failed to attach a zone to the zone entry
				return null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}

	public String getZoneOwnerNameFromChunkAndLoc(Chunk chunkAt, int x, int z, World world) {
		// TODO Auto-generated method stub
		for(myZone chunk : zones)
		{
			if (chunk.chunk == chunkAt)
			{
				return chunk.ownername;
			}
		}
		// Does not exist - create it!
		myZone zn = createBlankZone(x,z,world);
		return "";
	}
	
	public myZone getZoneFromChunkAndLoc(int x, int z, World world) {
		// TODO Auto-generated method stub
		for(myZone chunk : zones)
		{
			if (chunk.x == x && chunk.z == z)
			{
				return chunk;
			}
		}
		// Does not exist - create it!
		return createBlankZone(x,z,world);
	}
	
	public myZone getZoneFromChunk(Chunk chunkAt, Location loc) {
		// TODO Auto-generated method stub
		for(myZone chunk : zones)
		{
			if (chunk.chunk == chunkAt)
			{
				return chunk;
			}
		}
		// Does not exist - return null
		double xm = loc.getX();
		double zm = loc.getZ();
		
		int x = this.getZoneCoord(xm);
		int z = this.getZoneCoord(zm);
		return createBlankZone(x,z,loc.getWorld());
	}
	
	public String setZoneOwnerName(Chunk chunkAt, String owner) {
		// TODO Auto-generated method stub
		for(myZone zone : zones)
		{
			if (zone.chunk == chunkAt)
			{
				
				
				try {
					PreparedStatement stmt = this.parent.universe.conn.prepareStatement("UPDATE zone set name=?,ownername=? WHERE id = ?");
					stmt.setString(1,owner+"s land");
					stmt.setString(2,owner);
					stmt.setInt(3,zone.id);
					
					stmt.executeUpdate();
					stmt.close();
					System.out.println("npcx :myUniverse:setZoneOwnerName:"+owner);
					zone.ownername = owner;
					
					
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				
			}
		}
		return "";
	}

	public String getZoneOwnerName(Chunk chunkAt) {
		// TODO Auto-generated method stub
		for(myZone zone : zones)
		{
			if (zone.chunk == chunkAt)
			{
				
				
				return zone.ownername;
				
			}
		}
		return "";
	}

	public myZone getZoneFromLoc(int x, int z, World world) {
		// TODO Auto-generated method stub
		for(myZone zone : zones)
		{
			if (zone.x == x && zone.z == z)
			{
				return zone;
				
			}
		}
		return createBlankZone(x,z,world);
	}

	public void setPlayerLastChunkName(Player player, String name) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player != null)
				p.lastchunkname = name;
		}
	}

	public String getPlayerLastChunkName(Player player) {
		// TODO Auto-generated method stub
		for (myPlayer p : players.values())
		{
			if (p.player != null)
				return p.lastchunkname;
		}
		return "";
	}

	
}
