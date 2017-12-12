package otakings;

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
import edu.warbot.brains.capacities.Agressive;
import edu.warbot.communications.WarMessage;

public abstract class WarRocketLauncherBrainController extends WarExplorerBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 		
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;

			//getMessageEnnemyBase "B"
			
			WarMessage msgE = me.getMessageFromExplorer();

			if (msgE != null && msgE.getMessage().equals("B")) {
				me.sendMessage(msgE.getSenderID(), "A", "");
				me.ctask = waitAnswer;
				return null;
			}

			me.ctask = randomMove;
			return null;
		}
	};

	public int timeOut = 0;

	static WTask waitAnswer = new WTask() {
		String exec(WarBrain bc) {
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;			
			WarMessage msg = me.getMessageFromExplorer();
			me.timeOut++;
			me.setDebugString("Waiting for Answer");
			if (me.timeOut <= 200) {
				if (msg == null) {
					return WarRocketLauncher.ACTION_MOVE;
				}

				if (msg.getMessage().equals("!A")) {
					me.setDebugString("Mission Accepted");
					double angle = Double.parseDouble(msg.getContent()[0]);
					me.setDebugString("Base at : " + angle);
					me.setHeading(angle);
					me.ctask = goenemybase;
					return null;
				}

				else if (msg.getMessage().equals("|A")) {
					me.setDebugString("Mission Denied");
					me.ctask = randomMove;
					return null;
				}
			}

			me.timeOut = 0;
			me.ctask = randomMove;
			return null;
		}
	};

	static WTask randomMove = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;

			me.setDebugString("IM FREE");
			
			if(me.isBlocked())
				me.setRandomHeading();

			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);

			//Si je  vois une de base
			if(basePercepts != null && basePercepts.size() > 0){
				WarAgentPercept base = basePercepts.get(0);
				me.setHeading(base.getAngle());
				//envois msg "B"
				me.ctask = attackbase;
				return null;
			}

			me.ctask = handleMsgs;
			return null;
		}
	};

	public boolean reloaded = true;
	public double base = 0;

	static WTask attackbase = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);
			if (me.reloaded) {
				me.reloaded = false;
				return WarRocketLauncher.ACTION_FIRE;
			}
			else {
				me.reloaded = true;
				return WarRocketLauncher.ACTION_RELOAD;
			}

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
		ctask = handleMsgs; // initialisation de la FSM
	}

	@Override
	public String action() {


		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			if (isBlocked())
				setRandomHeading();
			return WarRocketLauncher.ACTION_MOVE;
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


