package net.gamerservices.npcx;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import net.gamerservices.npclibfork.BasicHumanNpc;

import org.bukkit.Location;
import org.bukkit.entity.Player;


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
	
	
	myPlayer(npcx parent,Player player, String name)
	{
		this.parent = parent;
		this.player = player;
		this.name = name;
		
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
			return true;
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public int getPlayerFactionStanding(myFaction faction) {
		// TODO Auto-generated method stub
		try {
			PreparedStatement stmtNPC = this.parent.universe.conn.prepareStatement("SELECT amount FROM player_faction WHERE name = ? AND faction_id = ? LIMIT 1;");
			stmtNPC.setString(1,this.player.getName());
			stmtNPC.executeQuery();
			ResultSet rsNPC = stmtNPC.getResultSet ();
			while (rsNPC.next ())
			{
			      return rsNPC.getInt ("amount");
			       
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
	
	
}
