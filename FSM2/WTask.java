package FSM2;

import edu.warbot.brains.WarBrain;

public abstract class WTask {
	WarBrain myBrain;
	
	abstract String exec(WarBrain bc);
}
