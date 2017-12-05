package FSM2;


import java.util.ArrayList;
import java.util.List;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.agents.WarLight;
import edu.warbot.agents.agents.WarRocketLauncher;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarLightBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarLightBrainController extends  WarLightBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 		
		String exec(WarBrain bc){
			WarLightBrainController me = (WarLightBrainController) bc;

			//getMessageEnnemyBase "B"

			List<WarMessage> msgE = me.getMessages();

			for(int i=0;i<msgE.size();i++) {
				if (msgE.get(i).getSenderType() == WarAgentType.WarExplorer) {
					if (msgE.get(i).getMessage().equals("B")) {
						me.setDebugString("Message base recu ! ");
						me.sendMessage(msgE.get(i).getSenderID(), "A", "");
						me.ctask = waitAnswer;
						return null;
					}
				}
			}
			me.ctask = randomMove;
			return null;

		}
	};

	public int timeOut = 0;

	static WTask waitAnswer = new WTask() {
		String exec(WarBrain bc) {
			WarLightBrainController me = (WarLightBrainController) bc;			
			List<WarMessage> msg = me.getMessages();
			me.timeOut++;
			me.setDebugString("Waiting for Answer");
			if (me.timeOut <= 200) {
				if (msg == null) {
					return WarRocketLauncher.ACTION_MOVE;
				}
				for (int i=0;i<msg.size();i++) {
					if (msg.get(i).getSenderType() == WarAgentType.WarExplorer) {
						if (msg.get(i).getMessage().equals("!A")) {
							me.setDebugString("Mission Accepted");
							me.setHeading(msg.get(i).getAngle());
							me.ctask = goenemybase;
							return null;
						}

						else if (msg.get(i).getMessage().equals("|A")) {
							me.setDebugString("Mission Denied");
							me.ctask = randomMove;
							return null;
						}
					}
				}
			}


			else {
				me.timeOut = 0;
				me.ctask = randomMove;
			}
			return null;

		}
	};

	static WTask attackennemy = new WTask() {
		String exec(WarBrain bc){
			WarLightBrainController me = (WarLightBrainController) bc;
			ArrayList<WarAgentPercept> Percepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemies();

			for (int i=0;i<Percepts.size();i++) {
				if(!(Percepts.get(0).getType() == WarAgentType.WarExplorer) && !(Percepts.get(0).getType() == WarAgentType.WarFood)){
					me.setHeading(Percepts.get(0).getAngle());
					me.setDebugString("A l'attaque");
					if (me.isReloaded())
						return ACTION_FIRE;
					else if (me.isReloading())
						return ACTION_IDLE;
					else
						return ACTION_RELOAD;
				}
			}
			
			me.ctask=randomMove;
			return null;
		}
	};

	static WTask randomMove = new WTask() { 
		String exec(WarBrain bc){
			WarLightBrainController me = (WarLightBrainController) bc;

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
			
			ArrayList<WarAgentPercept> Percepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemies();

			for (int i=0;i<Percepts.size();i++) {
				if(!(Percepts.get(0).getType() == WarAgentType.WarExplorer)){
					me.setHeading(Percepts.get(0).getAngle());
					me.ctask=attackennemy;
					return null;
				}		
			}
			me.ctask=handleMsgs;
			return null;
		}
	};

	public WarLightBrainController() {
		super();
		ctask= randomMove;
	}


	static WTask goenemybase = new WTask() { 
		String exec(WarBrain bc){

			WarLightBrainController me = (WarLightBrainController) bc;
			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);
			me.setDebugString("go base ennemy");
			//Si je vois la base
			if(basePercepts != null | basePercepts.size() != 0){
				me.ctask = attackbase;
				return null;
			}

			return WarLight.ACTION_MOVE;
		}
	};

	static WTask attackbase = new WTask() { 
		String exec(WarBrain bc){
			WarLightBrainController me = (WarLightBrainController) bc;
			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);


			if (basePercepts.size() == 0) {
				me.ctask=randomMove;
				return ACTION_MOVE;
			}
			else {
				if (me.isReloaded())
					return ACTION_FIRE;
				else if (me.isReloading())
					return ACTION_IDLE;
				else
					return ACTION_RELOAD;
			}
		}
	};

	@Override
	public String action() {

		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			if (isBlocked())
				setRandomHeading();
			return WarLight.ACTION_MOVE;
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

}