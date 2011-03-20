package redecouverte.npcspawner;
import net.gamerservices.npcx.*;


import java.io.Console;
import java.lang.reflect.Field;
import java.util.logging.Logger;
import net.minecraft.server.EntityLiving;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BasicHumanNpc extends BasicNpc {

	public LivingEntity follow;
	public LivingEntity aggro;
	public int hp = 100;
	public int dmg = 3;
	public double spawnx;
	public double spawny;
	public double spawnz;
	public double seekx;
	public double seeky;
	public double seekz;
	
    private CHumanNpc mcEntity;
    private static final Logger logger = Logger.getLogger("Minecraft");
    public myNPC parent;
    public BasicHumanNpc(CHumanNpc entity, String uniqueId, String name, double spawnx, double spawny, double spawnz) {
        super(uniqueId, name);
    	this.spawnx = spawnx;
    	this.spawny = spawny;
    	this.spawnz = spawnz;
    	

        this.mcEntity = entity;
    }

    public HumanEntity getBukkitEntity() {
        return (HumanEntity) this.mcEntity.getBukkitEntity();
    }

    protected CHumanNpc getMCEntity() {
        return this.mcEntity;
    }

    public void moveto(LivingEntity target)
    {
    	double x = target.getLocation().getX();
		double y = target.getLocation().getY();
		double z = target.getLocation().getZ();
		double x2 = this.getBukkitEntity().getLocation().getX();
		double y2 = this.getBukkitEntity().getLocation().getY();
		double z2 = this.getBukkitEntity().getLocation().getZ();
		
		
		int xdist = (int) (x - x2);
		int ydist = (int) (y - y2);
		int zdist = (int) (z - z2);
		if ((x - x2) <= 10 && (x - x2) >= -10 && xdist != 0)
		{
			this.movecloser(target);
		}
		
		if ((y - y2) <= 10 && (y - y2) >= -10 && ydist != 0)
		{
			this.movecloser(target);
		}
		
		if ((z - z2) <= 10 && (z - z2) >= -10 && zdist != 0)
		{
			this.movecloser(target);
		}
    }
    
    public void think()
    {
    	//System.out.println("npcx : think");
    			
    	if (follow == null && aggro == null)
    	{
    		
    		double x2 = this.getBukkitEntity().getLocation().getX();
    		double y2 = this.getBukkitEntity().getLocation().getY();
    		double z2 = this.getBukkitEntity().getLocation().getZ();
    		
    		if (!(x2 == spawnx && y2 == spawny && z2 == spawnz))
    		{
    		
	    		System.out.println("npcx : moving  ["+ spawnx + "] ["+ spawny + "] ["+ spawnz + "]");
	    		moveTo(spawnx,spawny,spawnz,0,0);
    		}
    	}
    	
		if (follow instanceof Player)
		{
				moveto(follow);
		}
		
		if (aggro instanceof Player)
		{
			if (!(this.hp == 0))
			{
				attackLivingEntity(aggro);
			
			}
			
		}
		
		
				
		
    }
    
    public void movecloser(LivingEntity e)
    {
    	try
    	{
	    	if (e instanceof Player)
	    	{
	    		if (e instanceof BasicHumanNpc)
		    	{
	    			return;	    			
		    	}
		    	double x = e.getLocation().getX();
		    	double y = e.getLocation().getY();
		    	double z = e.getLocation().getZ();
		    	double mx = this.getBukkitEntity().getLocation().getX();
		    	double my = this.getBukkitEntity().getLocation().getY();
		    	double mz = this.getBukkitEntity().getLocation().getZ();
		    	double newx = mx;
		    	double newy = my;
		    	double newz = mz;
		    	
		    	if (mx != x)
		    	{
		    		//System.out.println("npcx : moving x ["+ mx+ "] -> ["+ x + "]("+ (mx - x) + ")");
			    	
		    		if ((mx - x) > 0.5)
		    		{
		    			newx = mx-2;		    		
		    		}
		    		if ((mx - x) < -0.5)
		    		{
		    			newx = mx+2;
		    		}
		    	}
		    	
		    	if (my != y)
		    	{
		    		//System.out.println("npcx : moving y ["+ my+ "] -> ["+ y + "]("+ (my - y) + ")");
			    	
		    		if ((my - y) > 2)
		    		{
		    			newy = my-0.5;
		    		}
		    		if ((my - y) < -2)
		    		{
		    			newy = my+0.5;
		    		}
		    	}
	    		
	    		if (mz != z)
		    	{
	    			//System.out.println("npcz : moving z ["+ mz+ "] -> ["+ z + "]("+ (mz - z) + ")");
			    	
		    		if ((mz - z) > 2)
		    		{
		    			newz = mz-0.5;
		    		}
		    		if ((mz - z) < -2)
		    		{
		    			newz = mz+0.5;
		    			
		    		}
		    	}    
	    		this.moveTo(newx, newy, newz,e.getLocation().getYaw()+180,e.getLocation().getPitch());
                
	    	}
    	} catch (Exception x)
    	{
    		 x.printStackTrace();
    	}
    	
    }
    
    public void moveTo(double x, double y, double z, float yaw, float pitch) {
        this.mcEntity.c(x, y, z, yaw, pitch);
    }

    public void attackLivingEntity(LivingEntity ent) {
        try {
            this.mcEntity.animateArmSwing();
            if ((ent.getHealth() - dmg) < 0)
            {
            	ent.setHealth(0);
            	follow = null;
            	aggro = null;
            	if (ent instanceof Player)
            	{
            		
            		((Player) ent).sendMessage("You have been slaughtered by " + getName());
            		
            	}
            } else {
            	ent.damage(dmg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void animateArmSwing()
    {
        this.mcEntity.animateArmSwing();
    }


}
