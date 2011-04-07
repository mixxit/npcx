package net.gamerservices.npcx;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.gamerservices.npclibfork.BasicHumanNpc;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;


public class myPlayer {

	public npcx parent;
	public Player player;
	public BasicHumanNpc target;
	public boolean dead = false;
	public String name;
	public int zomgcount = 0;
	public myZone lastmyzone;
	public int lastchunkx;
	public int lastchunkz;
	public String lastchunkname;
	public boolean toggle = true;
	
	
	myPlayer(npcx parent,Player player, String name)
	{
		this.parent = parent;
		this.player = player;
		this.name = name;
		
	}
	

	public int getPlayerBalance(Player player) {
		// TODO Auto-generated method stub
		try
		{
			Account account = iConomy.getBank().getAccount(player.getName());
			return (int)account.getBalance();
		} catch (NoClassDefFoundError e)
		{
			
			//e.printStackTrace();
			// We don't have iConomy
			return this.getNPCXBalance();
		}catch (NullPointerException e)
		{
			this.getNPCXBalance();
			// We don't have iConomy
			return this.getNPCXBalance();
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
			
			this.setNPCXBalance(this.getNPCXBalance()-totalcost);
			// We don't have iConomy
			
			
		}catch (NullPointerException e)
		{
			
			this.setNPCXBalance(this.getNPCXBalance()-totalcost);
			
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
			this.setNPCXBalance(this.getNPCXBalance()+totalcost);
			
		}catch (NullPointerException e)
		{
			// We don't have iConomy
			this.setNPCXBalance(this.getNPCXBalance()+totalcost);
			
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
			
			if (this.getNPCXBalance() >= totalcost)
			{
				return true;
			} else {
				return false;
			}
		}
	}
	
	public int getNPCXBalance()
	{
		try {
			PreparedStatement stmtNPC = this.parent.universe.conn.prepareStatement("SELECT coin FROM player WHERE name = ? LIMIT 1;");
			stmtNPC.setString(1,this.player.getName());
			stmtNPC.executeQuery();
			ResultSet rsNPC = stmtNPC.getResultSet ();
			while (rsNPC.next ())
			{
			      return rsNPC.getInt ("coin");
			       
			}
			rsNPC.close();
			stmtNPC.close();
			
			return 0;
		} catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		
	}
	
	public boolean setNPCXBalance(int amount)
	{
		try
		{
		PreparedStatement stmt = this.parent.universe.conn.prepareStatement("INSERT INTO player (coin,name) VALUES (?,?) ON DUPLICATE KEY UPDATE coin=VALUES(coin) ",Statement.RETURN_GENERATED_KEYS);
		stmt.setInt(1,amount);
		stmt.setString(2,this.player.getName());
		
		stmt.executeUpdate();
		ResultSet keyset = stmt.getGeneratedKeys();
		int key = 0;
		if ( keyset.next() ) {
		    // Retrieve the auto generated key(s).
		    key = keyset.getInt(1);
		    
		}
		stmt.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return false;
	}

	public boolean updateFactionNegative(myFaction faction) {
		// TODO Auto-generated method stub
		
		try
		{
			// Can do this to the cache now instead
			
			/*
			PreparedStatement stmt = this.parent.universe.conn.prepareStatement("INSERT INTO player_faction (player_name,faction_id,amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount) ",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1,this.player.getName());
			stmt.setInt(2,faction.id);
			stmt.setInt(3,-1);
			stmt.executeUpdate();
			ResultSet keyset = stmt.getGeneratedKeys();
			int key = 0;
			if ( keyset.next() ) {
			    // Retrieve the auto generated key(s).
			    key = keyset.getInt(1);
			    
			}
			*/
			for (myPlayer_factionentry f : this.parent.universe.playerfactions.values())
			{
				if (f.factionid == faction.id && f.playername.equals(this.player.getName()))
				{
					f.amount = f.amount - 1;
					return true;
				}
			}
			
			// Doesn't exist so lets make a new one
			myPlayer_factionentry fe = createFactionEntry(faction.id,faction.name,this.player.getName(),-1);
			
			if (fe != null)
			{
				this.parent.universe.playerfactions.put(Integer.toString(fe.id),fe);
				return true;
			} else {
				// didn't find an entry
				return false;
			
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	
	
	private myPlayer_factionentry createFactionEntry(int factionid, String factionname,
			String playername, int amount) {
		// TODO Auto-generated method stub
		try {
			
			PreparedStatement stmt = this.parent.universe.conn.prepareStatement("INSERT INTO player_faction (player_name,faction_id,amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount=amount+VALUES(amount) ",Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1,this.player.getName());
			stmt.setInt(2,factionid);
			stmt.setInt(3,amount);
			stmt.executeUpdate();
			ResultSet keyset = stmt.getGeneratedKeys();
			int key = 0;
			if ( keyset.next() ) {
			    // Retrieve the auto generated key(s).
			    key = keyset.getInt(1);
			    
			}
			myPlayer_factionentry m = new myPlayer_factionentry();
			m.id = key;
			m.playername = playername;
			m.factionid = factionid;
			m.amount = amount;
			return m;
			
		} catch (Exception e)
		{
		
			return null;
		}
	}
	
	

	public int getPlayerFactionStanding(myFaction faction) {
		// TODO Auto-generated method stub
		try {
			for (myPlayer_factionentry e : this.parent.universe.playerfactions.values())
			{
				if (e.playername.equals(this.player.getName()) && e.factionid == faction.id)
		        {
					if (e.factionid == faction.id )
					{
						return e.amount;
					}
		        }
			}
			
			return 0;

		} catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
		
		
	}


	public void updateFactionPositive(myPlayer player, myNPC npc) {
		// TODO Auto-generated method stub
		for (myFactionEntry n : this.parent.universe.factionentries.values())
		{
			if (n.targetfactionid == npc.faction.id)
			{
				if (n.amount < 0)
				{
					// found a faction that hates this npcs faction, lets give the player positive faction with them 
					myFaction faction = this.parent.getFactionByID(n.factionid);
					
					
					
					// TODO Auto-generated method stub
					
					try
					{
						int count = 0;
						for (myPlayer_factionentry f : this.parent.universe.playerfactions.values())
						{
							if (f.factionid == n.factionid && f.playername.equals(this.player.getName()))
							{
								f.amount = f.amount + 1;
								player.player.sendMessage(ChatColor.YELLOW + "* Your standing with " + faction.name + " has gotten better!");
								count++;
							}
						}
						
						if (count == 0)
						{
							// Doesn't exist so lets make a new one
							myPlayer_factionentry fe = createFactionEntry(n.factionid,faction.name,this.player.getName(),1);
							
							if (fe != null)
							{
								// created entry
								count++;
								this.parent.universe.playerfactions.put(Integer.toString(fe.id), fe);
								player.player.sendMessage(ChatColor.YELLOW + "* Your standing with " + faction.name + " has gotten better!");
							} else {
								// didn't find an entry
								
							}
						}
						
					} catch (Exception e)
					{
						e.printStackTrace();
						
					}
				}
			}
			
		}

		
	}
	
	
}
