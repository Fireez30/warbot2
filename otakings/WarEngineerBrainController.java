package otakings;

import java.util.ArrayList;

import edu.warbot.agents.agents.WarBase;
import edu.warbot.agents.agents.WarEngineer;
import edu.warbot.agents.agents.WarKamikaze;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarEngineerBrain;

public abstract class WarEngineerBrainController extends WarEngineerBrain {

	private boolean _alreadyCreated;
	
    public WarEngineerBrainController() {
        super();
        ctask = seplacer;
        _alreadyCreated= false;
    }
	WTask ctask;
    int avancer = 0;
    
    static WTask seplacer = new WTask() { 
		String exec(WarBrain bc){
			WarEngineerBrainController me = (WarEngineerBrainController) bc;
			me.setDebugString("Engineer se place ");
			if (me.avancer < 4) {return WarEngineer.ACTION_MOVE;}
			else {
				me.ctask=idle;return WarEngineer.ACTION_IDLE;
			}
		}
	};
	
	static WTask idle = new WTask() {
		String exec(WarBrain bc){
			WarEngineerBrainController me = (WarEngineerBrainController) bc;
			me.setDebugString("Engineer idle ");
			if (me.getNbElementsInBag() >= 0 && me.getHealth() <= 0.7 * me.getMaxHealth())
				return WarEngineer.ACTION_EAT;

			if (me.getMaxHealth()*0.8 <= me.getHealth()) {
				me._alreadyCreated = false;
			}
			
			if (!me._alreadyCreated) {
				
				me._alreadyCreated=true;
				return WarEngineer.ACTION_BUILD;
			}
			
			return WarEngineer.ACTION_IDLE;
		}
	};
	
	public String action() {
		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			if (isBlocked())
				setRandomHeading();
			return WarEngineer.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}
}
