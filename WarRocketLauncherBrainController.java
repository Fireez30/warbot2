package FSM;

import java.util.ArrayList;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarAgent;
import edu.warbot.agents.WarResource;
import edu.warbot.agents.actions.constants.ControllableActions;
import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.agents.WarRocketLauncher;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.agents.percepts.WarPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarExplorerBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarRocketLauncherBrainController extends WarExplorerBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 		
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;

			WarMessage msgE = me.getMessageFromExplorer();

			if (msgE.getMessage().equals("B")) {
				me.sendMessage(msgE.getSenderID(), "A", "");
				me.ctask = waitAnswer;
				return MovableWarAgent.ACTION_MOVE;
			}

			return null;
		}
	};

	static WTask waitAnswer = new WTask() {
		String exec(WarBrain bc) {
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;			
			WarMessage msg = me.getMessageFromExplorer();

			if (msg.getMessage().equals("!A")) {

				double angle = Double.parseDouble(msg.getContent()[0]);

				me.setHeading(angle);

				me.ctask = goenemybase;
				return null;
			}
			else if (msg.getMessage().equals("|A")) {
				me.ctask = randomMove;
				return null;
			}

			return ACTION_MOVE;
		}
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
			me.ctask = handleMsgs;
			return WarRocketLauncher.ACTION_MOVE;
		}
	};

	static WTask attackbase = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);
			
			me.setHeading(basePercepts.get(0).getAngle());
			
			return WarRocketLauncher.ACTION_FIRE;			
		}
	};

	static WTask goenemybase = new WTask() { 
		String exec(WarBrain bc){
			
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);

			//Si je vois la base
			if(basePercepts != null | basePercepts.size() != 0){
				me.ctask = attackbase;
				return null;
			}
			
			return MovableWarAgent.ACTION_MOVE;
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

	private WarMessage getMessageFromExplorer() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarExplorer))
				return m;
		}
		return null;	
	}

	private WarMessage getMessageFromBase() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				return m;
		}

		broadcastMessageToAgentType(WarAgentType.WarBase, "?H", "");
		return null;
	}


}


