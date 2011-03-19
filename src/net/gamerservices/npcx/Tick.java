package net.gamerservices.npcx;
import java.util.TimerTask;

public class Tick extends TimerTask {

	private npcx parent;
	
	public Tick(npcx owner)
	{
		parent = owner;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		parent.think();
		
		cancel();
	}
	
}
