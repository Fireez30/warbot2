package FSM;

import java.util.ArrayList;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarAgent;
import edu.warbot.agents.WarResource;
import edu.warbot.agents.actions.constants.ControllableActions;
import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.agents.percepts.WarPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarExplorerBrain;
import edu.warbot.communications.WarMessage;
import test.WarExplorerBrainController;

public abstract class WarRocketLauncherBrainController extends WarExplorerBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 
		String exec(WarBrain bc){return "";}
	};

	static WTask randomMove = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			if(me.isBlocked())
				me.setRandomHeading();

			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);

			//Si je  vois une de base
			if(basePercepts != null | basePercepts.size() != 0){
				WarAgentPercept base = basePercepts.get(0);
				if(base.getDistance() > ControllableActions.MAX_DISTANCE_GIVE){
					me.setHeading(base.getAngle());
					me.ctask=goenemybase;
					return null;
				}else{
					me.setHeading(base.getAngle());
					me.ctask=attackbase;
					return null;
				}

			}
			return null;
		}
	};

	static WTask attackbase = new WTask() { 
		String exec(WarBrain bc){
			
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);

			//Si je  vois une de base
			if(basePercepts != null | basePercepts.size() != 0){
				WarAgentPercept base = basePercepts.get(0);
			}
			return null;
		}
	};

	static WTask goenemybase = new WTask() { 
		String exec(WarBrain bc){
			return null;
		}
	};






	public WarRocketLauncherBrainController() {
		super();
		ctask = randomMove; // initialisation de la FSM
	}

	@Override
	public String action() {


		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			if (isBlocked())
				setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}



	private WarMessage getMessageFromBase() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				return m;
		}

		broadcastMessageToAgentType(WarAgentType.WarBase, "Where are you?", "");
		return null;
	}


}


