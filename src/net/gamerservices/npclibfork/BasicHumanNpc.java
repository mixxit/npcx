package net.gamerservices.npclibfork;
import net.gamerservices.npcx.*;


import java.io.Console;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import net.minecraft.server.EntityLiving;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class BasicHumanNpc extends BasicNpc {

	private LivingEntity mFollow;
	public LivingEntity mAggro;
	public int hp = 200;
	public int dmg = 3;
	public double spawnx;
	public double spawny;
	public double spawnz;
	public float spawnyaw;
	public float spawnpitch;
	
	public double seekx;
	public double seeky;
	public double seekz;
	public double seekyaw;
	public double seekpitch;
	public Block lastloc;
	public int stuck;
    public CHumanNpc mcEntity;
    private static final Logger logger = Logger.getLogger("Minecraft");
    public myNPC parent;
	private boolean dead = false;
    public BasicHumanNpc(myNPC parent,CHumanNpc entity, String uniqueId, String name, double spawnx, double spawny, double spawnz,float spawnyaw, float spawnpitch) {
    	super(uniqueId, name);
    	this.parent = parent;
    	this.spawnx = spawnx;
    	this.spawny = spawny;
    	this.spawnz = spawnz;
    	this.spawnyaw = spawnyaw;
    	this.spawnpitch = spawnpitch;
        this.mcEntity = entity;
    }

    
    public HumanEntity getBukkitEntity() {
        return (HumanEntity) this.mcEntity.getBukkitEntity();
    }
    
    public LivingEntity getAggro()
    {
    	return this.mAggro;
    	
    }
    
    public LivingEntity getFollow()
    {
    	return this.mFollow;
    	
    }
    
    public void setAggro(LivingEntity aggro)
    {
    	if (this.stuck == 0)
    	{
    		// Start my stuck check timer
    		this.stuck = 1;
    	}
    	
    	this.mAggro = aggro;
    	
    }
    
    public void setFollow(LivingEntity aggro)
    {
    	if (this.stuck == 0)
    	{
    		// Start my stuck check timer
    		this.stuck = 1;
    	}
    	
    	this.mFollow = aggro;
    	
    }

    protected CHumanNpc getMCEntity() {
        return this.mcEntity;
    }
    
    public void checkNotStuck()
    {
    	if (stuck > 350)
    	{
    		this.setFollow(null);
    		this.setAggro(null);
    		this.stuck = 0;
    		Location loc = new Location (this.getBukkitEntity().getWorld(),spawnx,spawny,spawnz,spawnyaw,spawnpitch);
    		this.forceMove(loc);
    		
    	} else {
    		if (stuck == 0)
    		{
    			// do nothing im fine
    		}else {
    			// am i away from spawn?
    			double x = this.getBukkitEntity().getLocation().getX();
    			double y = this.getBukkitEntity().getLocation().getY();
    			double z = this.getBukkitEntity().getLocation().getZ();
    			
    			if (x != spawnx || y != spawny || z != spawnz)
    			{
    				stuck++;
    			}
    			
    		}
    		
    	}
    }
    
    public void doThinkGreater()
    {
    	if (this.hp > 0)
		{
	    	//System.out.println("npcx : think");
	    	if (this.parent != null)
	    	{
	    		//
	    		// PLAYER TARGET DISTANCE
	    		//
		    	for (myPlayer p : this.parent.parent.universe.players.values())
		    	{
		    		if (p.target == this)
		    		{
			    		double x1 = p.player.getLocation().getX();
			    		double y1 = p.player.getLocation().getY();
			    		double z1 = p.player.getLocation().getZ();
			    		
			    		double x2 = this.getBukkitEntity().getLocation().getX();
			    		double y2 = this.getBukkitEntity().getLocation().getY();
			    		double z2 = this.getBukkitEntity().getLocation().getZ();
			    		int xdist = (int) (x1 - x2);
			    		int ydist = (int) (y1 - y2);
			    		int zdist = (int) (z1 - z2);
			    		
			    		if ((xdist < -3 || xdist > 3) || (ydist < -3 || ydist > 3) || (zdist < -3 || zdist > 3))
			    		{
			    			Debug(1,"player out of range, removing target");
				    		p.target = null;
				    		p.player.sendMessage("You have lost your target");
			    		}
				    	// remove target
		    		}
		    	}
		    	
		    	// END PLAYER TARGET DISTANCE
	    	}
	    	// NPC RETURN TO SPAWN IDLE
	    	if (this.parent.pathgroup != null)
			{
	    		// let them carry on for guard sequences
	    		
	    		//return;
			} else {
	    		
		    	if (getFollow() == null && getAggro() == null)
		    	{
		    		//System.out.println("npcx : moving  ["+ spawnx + "] ["+ spawny + "] ["+ spawnz + "]");
		    		// Requip my armour
		    		this.parent.requipArmour();
		    		
		    		// not aggrod or following, time to go home :)
		    		double x2 = this.getBukkitEntity().getLocation().getX();
		    		double y2 = this.getBukkitEntity().getLocation().getY();
		    		double z2 = this.getBukkitEntity().getLocation().getZ();
		    		
		    		if (!(x2 == spawnx && y2 == spawny && z2 == spawnz))
		    		{
		    			
		    		//	System.out.println("Going home");
		    			
		    			Double yaw2 = new Double(spawnyaw);
		                Double pitch2 = new Double(spawnpitch);
		                
		                Location loc = new Location(this.getBukkitEntity().getWorld(),spawnx,spawny,spawnz,yaw2.floatValue(),pitch2.floatValue());
		                
		                moveCloserToLocation(loc);
		                
		                
		    		}
		    	}
			}

	    	//
	    	// NPC FOLLOW
	    	//
	    	
			if (getFollow() instanceof LivingEntity)
			{
				if (this.getFollow() != null)
				{
					Debug(1,this.getName() + ":follow:" + getFollow().toString()+":"+getFollow().getHealth());
					// lets follow this entity
					if (this.hp == 0 || this.getFollow().getHealth() == 0)
					{
						
						// they're dead, stop following
						this.setFollow(null);
						this.setAggro(null);
						
						
					} else {
			    		Location locforface = this.getFaceLocationFromMe(getFollow().getLocation());
			    		Location modifiedloc = new Location(getBukkitEntity().getWorld(),getBukkitEntity().getLocation().getX(),getBukkitEntity().getLocation().getY(),getBukkitEntity().getLocation().getZ(),locforface.getYaw(),locforface.getPitch());
			    		forceMove(modifiedloc);
						
						Debug(1,this.getName() + ":follow:" + getFollow().toString()+":"+getFollow().getHealth());
						
						// they're alive lets check distance
						double x1 = this.getFollow().getLocation().getX();
				    	double y1 = this.getFollow().getLocation().getY();
				    	double z1 = this.getFollow().getLocation().getZ();
				    	
				    	double x2 = this.getBukkitEntity().getLocation().getX();
				    	double y2 = this.getBukkitEntity().getLocation().getY();
				    	double z2 = this.getBukkitEntity().getLocation().getZ();
				    	int xdist = (int) (x1 - x2);
				    	int ydist = (int) (y1 - y2);
				    	int zdist = (int) (z1 - z2);
				    	
				    	if (xdist > -30 && xdist < 30 && ydist > -30 && ydist < 30 && zdist > -30 && zdist < 30)
				    	{
				    		// face target
				    		//this.getFaceLocationFromMe(this.getFollow().getLocation());
				    		
				    		if (isLineOfSight(this.getFollow()))
				    		{
				    			Debug(1,this.getName() + ":Following Entity near to me");
				    			this.moveCloserToLocation(this.getFollow().getLocation());
					   		
					   		} else {
					   			Debug(1,this.getName() + ":Stopped following near to me (no line of sight)");
					    		this.setFollow(null);
								this.setAggro(null);
					   		}
				    	} else {
				    	// too far for me
				    		Debug(1,this.getName() + ":Stopped following near to me (too far)");
				    		this.setFollow(null);
							this.setAggro(null);
				    	}
					}
				}
			}
			
			//
			// NPC ATTACK
			//
			
			if (getAggro() instanceof LivingEntity)
			{
				// lets follow this entity
				if (this.getAggro() != null)
				{
					if (this.hp == 0)
					{
						this.setFollow(null);
						this.setAggro(null);
					} else {
						attackLivingEntity(getAggro());
					}
				}
			}
		}
    }
    
    public boolean isLineOfSight(LivingEntity follow) {
		// TODO Auto-generated method stub
    	// Are they in a line of sight?
    	
    	if (this.getBukkitEntity() != null)
    	{
			Location higherloc = this.getFollow().getLocation();
			higherloc.setY(higherloc.getY()+1);
			Block h = this.getBukkitEntity().getWorld().getBlockAt(higherloc);
			
			try 
			{
				
				
				List < Block > list = this.getBukkitEntity().getLineOfSight(null,8);
				if (list != null)
				{
			    	for (Block b : list)
			    	{
			    		if (b == h)
			    		{
			    			return true;
			    		} 
			    	}
				} 
			} catch (IllegalStateException e)
			{
				return false;
			} catch (NullPointerException e)
			{
				return false;
			}
    	}
    	
		return false;
	
	}
    
    

	public Location getFaceLocationFromMe(Location location) {
    	try
    	{
			// citizens - https://github.com/fullwall/Citizens
			Location loc = this.getBukkitEntity().getLocation();
			double xDiff = location.getX() - loc.getX();
			double yDiff = location.getY() - loc.getY();
			double zDiff = location.getZ() - loc.getZ();
	
			double DistanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
			double DistanceY = Math.sqrt(DistanceXZ * DistanceXZ + yDiff * yDiff);
			double yaw = (Math.acos(xDiff / DistanceXZ) * 180 / Math.PI);
			double pitch = (Math.acos(yDiff / DistanceY) * 180 / Math.PI) - 90;
			if (zDiff < 0.0) {
				yaw = yaw + (Math.abs(180 - yaw) * 2);
			}
			Location finalloc = new Location(loc.getWorld(),loc.getX(),loc.getY(),loc.getZ(),(float)yaw-90,(float)pitch);
			return finalloc;
			
    	} catch (Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
	}
    
    public void doThinkLesser()
    {
    	
    	if (this.parent != null)
    	{
    		Debug(1,"doThinkLesser:Checking if pathgroup is not null");
    		
	    	if (this.parent.pathgroup != null)
			{
	    		Debug(1,"doThinkLesser:Path group was NOT null");
				doPathgroups();
			}
    	}
    	
    	// How long have i been away from spawn?
    	checkNotStuck();
    }
    
    
    //
    // PATHGROUPS
    //
    
    private void doPathgroups() {
		// TODO Auto-generated method stub
    	
		
    	Debug(1,"doPathgroups:Begun:Countdown: "+ this.parent.movecountdown);
    	if (this.parent.movecountdown == 0)
		{
    		// It's time to execute
    		
    		// Are they on a pathspot?
    		if (this.parent.currentpathspot < 1)
			{
    			// In a pathgroup but not in a pathspot! Fix it
    			this.parent.currentpathspot = 1;
			}
    		Debug(1,"doPathgroups:Checking entries");
    		
    		// Lets look at all the pathgroup entries and get the one we're on
    		for (myPathgroup_entry entry : this.parent.pathgroup.pathgroupentries)
			{
        		Debug(1,"doPathgroups:Cycling entries");

    			// This is the entry the npc is on
    			if (entry.spot == this.parent.currentpathspot)
				{
    				Debug(1,"Does "+entry.location+" match " +this.getBukkitEntity().getLocation() + " : "+ this.parent.currentpathspot);
    				// Ignore yaw and pitch
    				Location loca = new Location(entry.location.getWorld(),entry.location.getX(),entry.location.getY(),entry.location.getZ(),0,0);
    				Location locb = new Location(this.getBukkitEntity().getWorld(),this.getBukkitEntity().getLocation().getX(),this.getBukkitEntity().getLocation().getY(),this.getBukkitEntity().getLocation().getZ(),0,0);
    				
    				if (loca.equals(locb))
    				{
    					Panic(1,"Already reached: "+this.getBukkitEntity().getLocation()+" spot: "+ this.parent.currentpathspot);
	    				
    					// Move to net location
    					this.parent.movecountdown = 10;
    					iteratePathspot();
    					
    				} else {
    					// Lets convert it into a Location  
	    				Location loc = new Location(this.getBukkitEntity().getWorld(),entry.location.getX(),entry.location.getY(),entry.location.getZ(),entry.location.getYaw(),entry.location.getPitch());
	    				// We should try to move closer to it
						this.moveCloserToLocation(loc);	
    				}
					
					
				}
			}
    		
		} else {
			if (this.parent.movecountdown < 0)
			{
				// why is the countdown less than 0, reset it (executes)
				this.parent.movecountdown = 0;
			} else {
				// move the countdown closer to execution
				this.parent.movecountdown--;
			}
		}
	}

	private void Debug(int i, String string) {
		// TODO Auto-generated method stub
		if (i > 1)
		{
			Debug(string);
		}
	}

	private void Panic(int i, String string) {
		// TODO Auto-generated method stub
		if (i > 1)
		{
			Panic(string);
		}
	}

	private void iteratePathspot() {
		// TODO Auto-generated method stub
		
		if (this.parent.moveforward)
		{
			if(this.parent.currentpathspot < this.parent.pathgroup.pathgroupentries.size())
			{
				
				this.parent.currentpathspot++;
				Debug(1,"Iterated Pathspot Forwards ("+this.parent.currentpathspot+")");
			} else {
				Debug(1,"CHANGEDIRECTION Moving Backward");
				this.parent.moveforward = false;
			}
			
		} else {
			
			if(this.parent.currentpathspot > 1)
			{
				
				this.parent.currentpathspot--;
				Debug(1,"Iterated Pathspot Backwards ("+this.parent.currentpathspot+")");
			} else {
				Debug(1,"CHANGEDIRECTION Moving Forward");
				this.parent.moveforward = true;
			}
		}
		
		
	}

	private void moveCloserToLocation(Location loc) {
		// TODO Auto-generated method stub
		Location newloc = getTransformCloserToLocation(this.getBukkitEntity().getLocation(),loc);
		if (this.parent != null)
		{
			Debug(1,"moveCloserToLocation Moving towards: "+this.parent.currentpathspot+newloc);
		}
		
		Location locforface = getFaceLocationFromMe(loc);
		Location modifiedloc = new Location(newloc.getWorld(),newloc.getX(),newloc.getY(),newloc.getZ(),locforface.getYaw(),locforface.getPitch());
		
		forceMove(modifiedloc);
		
	}
	
	public Location getTransformCloserToLocation(Location from, Location to)
	{
		// TODO Auto-generated method stub
		Debug(1,"transformCloserToLocation:Called!!");
		Debug(1,"transformCloserToLocation:TO: ("+to.getX()+","+to.getY()+","+to.getZ()+")");
		Debug(1,"transformCloserToLocation:FROM:"+from.getX()+","+from.getY()+","+from.getZ()+")");
		double diffx = to.getX() - from.getX();
		double diffy = to.getY() - from.getY();
		double diffz = to.getZ() - from.getZ();
		Debug(1,"transformCloserToLocation:DIFF: ("+diffx+","+diffy+","+diffz+")");
		if (diffx <= 1 && diffx >= -1 && diffy <= 1 && diffy >= -1 && diffz <= 1 && diffz >= -1)
		{
			// Close enough, return it
			Debug(1,"transformCloserToLocation:Close (within 1), i'm gonna go with it");
			return to;
		}
		
		Location loc = new Location(this.getBukkitEntity().getWorld(),from.getX(),from.getY(),from.getZ(),from.getYaw(),from.getPitch());
		if (diffx > 1)
		{
			// move on pos-x axis
			loc.setX(loc.getX()+1);
		}
		if (diffy > 1)
		{
			// move on pos-x axis
			loc.setY(loc.getY()+1);
		}
		if (diffz > 1)
		{
			// move on pos-x axis
			loc.setZ(loc.getZ()+1);
		}
		//
		if (diffx < -1)
		{
			// move on pos-x axis
			loc.setX(loc.getX()-1);
		}
		if (diffy < -1)
		{
			// move on pos-x axis
			loc.setY(loc.getY()-1);
		}
		if (diffz < -1)
		{
			// move on pos-x axis
			loc.setZ(loc.getZ()-1);
		}
		
		
		Block block = this.getBukkitEntity().getWorld().getBlockAt(loc);
		if (block.getType() == Material.AIR && !isBlockOccupied(block) && !isBlockFloatingAir(block) && !isBlockWall(block) && !isLastBlock(block))
		{
			Debug(1,"transformCloserToLocation:Initial location was not a wall or last block");
			return loc;
		}

		// Go round but dont go to our previous location
		Location location = getNextBestLocation(this.getBukkitEntity().getLocation(),loc);
		
		if (location != null)
		{
			return location;
		}
		
		Debug(1,"transformCloserToLocation:failed to find a location - going home");
		return from;
		
	}
	
	private boolean isBlockOccupied(Block block) {
		// TODO Auto-generated method stub
		int count = 0;
		for (Entity e : this.getBukkitEntity().getWorld().getEntities())
		{
			if (e.getLocation() == block.getLocation())
			{
				count++;
			}
		}
		if (count == 0)
		{
			return false;
		} else {
			return true;
		}
	}

	private boolean isLastBlock(Block block) {
		if (this.lastloc != null)
		{
			if (block == this.lastloc)
			{
				Debug(1,"isLastBlock=true");
				return true;
			}
		} 
		return false;
	}

	private boolean isBlockWall(Block block)
	{
		Location above = new Location(block.getWorld(),block.getLocation().getX(), block.getLocation().getY()+1, block.getLocation().getZ(),block.getLocation().getYaw(),block.getLocation().getPitch());
		Block babove = block.getWorld().getBlockAt(above);
		if(babove.getType() == Material.AIR)
		{
			return false;
		}
		// assume the worse
		Debug(1,"isBlockWall=true");

		return true;
	}
	
	private Location getNextBestLocation(Location from, Location loc)
	{
		// Using our from location, find the next best block to move to from loc
		
		int minx = -1;
		int miny = -1;
		int minz = -1;
		double x = minx;
		double y = miny;
		double z = minz;
		int maxx = 1;
		int maxy = 1;
		int maxz = 1;
		
		
		double myx = this.getBukkitEntity().getLocation().getX();
		double myy = this.getBukkitEntity().getLocation().getY();
		double myz = this.getBukkitEntity().getLocation().getZ();
		
		double tx = loc.getX();
		double ty = loc.getY();
		double tz = loc.getZ();

		double diffx = myx - tx;
		double diffy = myy - ty;
		double diffz = myz - tz;
		
		if (diffx <= -1)
		{
			minx = -1;
			x = minx;
			maxx = 0;
		}
		
		if (diffy <= -1)
		{
			miny = -1;
			y = miny;
			maxy = 0;
		}
		
		if (diffz <= -1)
		{
			minz = -1;
			z = minz;
			maxz = 0;
		}
		
		if (diffx >= 1)
		{
			minx = 0;
			x = minx;
			maxx = 1;
		}
		
		if (diffy >= 1)
		{
			miny = 0;
			y = miny;
			maxy = 1;
		}
		
		if (diffz >= 1)
		{
			minz = 0;
			z = minz;
			maxz = 1;
		}
		
		while (x <= maxx)
		{
			while (y <= maxy)
			{
				while (z <= maxz)
				{
					Panic(1,"getNextBestLocation:"+x+","+y+","+z);
					Location targetloc = new Location(this.getBukkitEntity().getWorld(),from.getX()-x,from.getY()-y,from.getZ()-z,loc.getYaw(),loc.getPitch());
					Block block = this.getBukkitEntity().getWorld().getBlockAt(targetloc);
					
					if (x == 0 && y == 0 && z == 0)
					{
						// Skip here, we're already there!
					} else {
						if (block.getType() == Material.AIR && !isBlockOccupied(block) && !isBlockFloatingAir(block) && !isBlockWall(block) && !isLastBlock(block))
						{
							Debug(1,"getNextBestLocation:GOINGTOMOVE:"+x+","+y+","+z);
							return targetloc;
						}
					}
					z++;
				}
				y++;
				z=minz;

			}
			x++;
			y=miny;
			z=minz;
		}
		
		Panic(1,"getNextBestLocation:Failed to find a better target");
		return null;
	}

	private boolean isBlockFloatingAir(Block block) {
		// TODO Auto-generated method stub
		Location locbelow = new Location(block.getLocation().getWorld(),block.getLocation().getX(),block.getLocation().getY()-1,block.getLocation().getZ(),block.getLocation().getYaw(),block.getLocation().getPitch());
		Block blockbelow = block.getWorld().getBlockAt(locbelow);
		
		if (block.getType() == Material.AIR && blockbelow.getType() != Material.AIR)
			return false;
		// assume worse
		return true;
	}

	private Location findPlatformDown(Location belowloc) {
		// TODO Auto-generated method stub
		int max = 200;
		int current = 1;
		while (current < max)
		{
			Location newloc = new Location(belowloc.getWorld(),belowloc.getX(), belowloc.getY()-current, belowloc.getZ(),belowloc.getYaw(),belowloc.getPitch());
			if (this.getBukkitEntity().getWorld().getBlockAt(newloc).getType() != Material.AIR)
			{
				return newloc;
				
			}
			current++;
		}
		Panic("findPlatformDown - Failed to find a better platform below");
		return null;
	}

	private void Debug(String string) {
		// TODO Auto-generated method stub
		
		if (this.parent.parent != null)
		{
			this.parent.parent.dbg("BasicHumanNpc:"+string);
		}
	}

	public void forceMove(Location loc)
	{
		
		Debug(1,"forceMove Called ("+loc.getX()+","+loc.getY()+","+loc.getZ()+") from ("+this.mcEntity.getBukkitEntity().getLocation().getX()+","+this.mcEntity.getBukkitEntity().getLocation().getY()+","+this.mcEntity.getBukkitEntity().getLocation().getZ()+")");
		
		
		double myx = this.getBukkitEntity().getLocation().getX();
		double myy = this.getBukkitEntity().getLocation().getY();
		double myz = this.getBukkitEntity().getLocation().getZ();
		
		double tx = loc.getX();
		double ty = loc.getY();
		double tz = loc.getZ();

		double diffx = myx - tx;
		double diffy = myy - ty;
		double diffz = myz - tz;
		Debug(1,"forceMove:GOINGTOMOVE:"+diffx+","+diffy+","+diffz);
		
		// Save last co-ordinate
		this.lastloc = this.getBukkitEntity().getLocation().getBlock();
		
		
		this.mcEntity.c(loc.getX(), loc.getY(), loc.getZ(),loc.getYaw(), loc.getPitch());
	}
	
	public void Panic(String type)
	{
		Debug("npcx : ***"+ this.getBukkitEntity().getName() + " paniced! : "+ type + " ***");
	}

	public void attackLivingEntity(LivingEntity ent) {
        try {
        	// check the ent has some health at least
        	
            if (ent.getHealth() > 0)
            {
	            if ((ent.getHealth() - dmg) <= 0)
	            {
	            	ent.damage(200);
	            	setFollow(null);
	            	setAggro(null);
	            	if (ent instanceof Player)
	            	{
	            		((Player) ent).getServer().broadcastMessage(((Player) ent).getName() + " was slaughtered by " + getName() + ".");
	            		((Player) ent).sendMessage("You have been slaughtered by " + getName());
	            		this.onKilled(ent);
	            		
	            		
	            		// mark player dead
	            		for (myPlayer player : this.parent.parent.universe.players.values())
	        			{
	        					// deal with player death changes
	        					if (player.player == ent)
	        					{
	        						//System.out.println("npcx : player about to respawn, assigning them to the dead list");
	        						player.dead = true;
	        						player.respawned = false;
	        						((Player) ent).sendMessage("You have been slaughtered by " + getName());
	        						
	        					}
	        				
	        			}
	            		
	            	}
	            } else {
	            	if (ent instanceof Monster)
	            	{
	            		
	            		double myx = this.getBukkitEntity().getLocation().getX();
	            		double myy = this.getBukkitEntity().getLocation().getY();
	            		double myz = this.getBukkitEntity().getLocation().getZ();
	            		
	            		double tx = ent.getLocation().getX();
	            		double ty = ent.getLocation().getY();
	            		double tz = ent.getLocation().getZ();
	
	            		double diffx = myx - tx;
	            		double diffy = myy - ty;
	            		double diffz = myz - tz;
	            		
	            		
	            		if (diffx < 2 && diffx > -2 && diffy < 2 && diffy > -2 && diffz < 2 && diffz > -2)
	            		{
	            		
			            	//System.out.println("Gahh! ");
			            	this.mcEntity.animateArmSwing();
			            	ent.damage(dmg);
			            
	            		}
		            	
		            	
		            } else {
		            	if (ent instanceof Player)
		            	{
	
		            		double myx = this.getBukkitEntity().getLocation().getX();
		            		double myy = this.getBukkitEntity().getLocation().getY();
		            		double myz = this.getBukkitEntity().getLocation().getZ();
		            		
		            		double tx = ent.getLocation().getX();
		            		double ty = ent.getLocation().getY();
		            		double tz = ent.getLocation().getZ();
	
		            		double diffx = myx - tx;
		            		double diffy = myy - ty;
		            		double diffz = myz - tz;
		            		
		            		if (diffx < 2 && diffx > -2 && diffy < 2 && diffy > -2 && diffz < 2 && diffz > -2)
		            		{
		            			//System.out.println("Processed this as a player "+ent.getClass().toString());
				            	this.mcEntity.animateArmSwing();
				            	ent.damage(dmg);
				            	
		            		}
			            }
		            }
	            }
        	} else {
        		// no health, not worth the effort
        		setFollow(null);
            	setAggro(null);
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onKilled(LivingEntity ent) {
		// TODO Auto-generated method stub
		this.parent.onKilled(ent);
	}

	public void animateArmSwing()
    {
        this.mcEntity.animateArmSwing();
    }

	public void onBounce(Player p) {
		// TODO Auto-generated method stub
		this.parent.onBounce(p);
	}

	public void onRightClick(Player p) {
		// TODO Auto-generated method stub
        this.parent.onRightClick(p);
	}

	public void onClosestPlayer(Player p) {
		// TODO Auto-generated method stub
		this.parent.onClosestPlayer(p);
	}

	public void onDeathFromPlayer(LivingEntity p) {
		if (this.dead != true)
		{
			// TODO Auto-generated method stub
			this.dead = true;
			this.parent.onDeathFromPlayer(p);
		}
		
	}

	public void chunkactive(Location location) {
		// TODO Auto-generated method stub
		if (this.parent.spawngroup != null)
		{
			//System.out.println("ncpx : Chunkactive for spawngroup:"+this.parent.spawngroup.name);
			this.parent.spawngroup.chunkactive = true;
		}
		
	}

	public void chunkinactive(Location location) {
		// TODO Auto-generated method stub
		if (this.parent.spawngroup != null)
		{
			//System.out.println("npcx : DBG : ChunkInactive for spawngroup:"+this.parent.spawngroup.name);
			this.parent.spawngroup.chunkactive = false;
		}
		
		
	}
	
	public void onNpcVsNpcDamage(myNPC anpc) {
		
    	this.setFollow(anpc.npc.getBukkitEntity());
    	this.setAggro(anpc.npc.getBukkitEntity());
        
        try
        {
        	// default npcvsnpc dmg
        	int dmgdone = 30;
        	this.hp = this.hp - dmgdone;
        	this.parent.requipArmour();
        	
        	if (this.hp < 1)
        	{
        		anpc.npc.setFollow(null);
        		anpc.npc.setAggro(null);
        		
        		//System.out.println("I just died!!");		            		
        		this.onDeathFromNPC(anpc.npc.getBukkitEntity());
        		this.parent.parent.onNPCDeath(this);
        		
        	}
        } 
        catch (Exception e)
        {
        	this.setFollow(null);
        	this.setAggro(null);
        	e.printStackTrace();
        }
	}

	private void onDeathFromNPC(HumanEntity bukkitEntity) {
		if (this.dead != true)
		{
			// TODO Auto-generated method stub
			this.dead = true;
			this.parent.onDeath();
		}
	}

	public void onDamage(EntityDamageEvent event) {
		// TODO Auto-generated method stub
			//System.out.println("onDamage called for entity"+event.getEntity().getEntityId());
			
			if (event instanceof EntityDamageByEntityEvent)
		    {
				EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) event;
				
					
			        if (this != null && edee.getDamager() instanceof LivingEntity) 
			        {
		
			        	Entity p = edee.getDamager();
			        	if (this.parent != null)
			        	{
			        		for (myPlayer player : this.parent.parent.universe.players.values())
			        		{
			        			if (player.player == edee.getDamager())
			        			{
			        				if (this.getAggro() == null)
			        				{
			        					// First time sent an event
			        					this.parent.onPlayerAggroChange(player);
			        					
			        				} 
			        			}
			        		}
			        	}
			        	this.setFollow((LivingEntity)p);
			        	this.setAggro((LivingEntity)p);
			            
			            try
			            {
			            	
			            	int dmgdone = this.parent.getDamageDone(this,(Player)p);
			            	this.hp = this.hp - dmgdone;
			            	
			            	this.parent.requipArmour();
		
			            	if (this.hp < 1)
			            	{
			            		
			            		//System.out.println("I just died!!");		            		
			            		this.onDeathFromPlayer((LivingEntity)p);
			            		this.parent.parent.onNPCDeath(this);
			            		
			            	}
			            } 
			            catch (Exception e)
			            {
			            	this.setFollow(null);
			            	this.setAggro(null);
							
			            }
			        }
				
		    }
	}

}
