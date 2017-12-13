package otakings;


import java.util.ArrayList;
import java.util.List;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.agents.WarHeavy;
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
			WarMessage m1 = me.getMessageAboutBase();
			if (m1 != null) {
				me.setDebugString("Message base adverse recu ! ");
				me.sendMessage(m1.getSenderID(), "A", "");
				me.setHeading(m1.getAngle());
				me.ctask = goenemybase;
				return null;
			}

			List<WarMessage> msgE = me.getMessages();

			for(int i=0;i<msgE.size();i++) {

				if (msgE.get(i).getMessage().equals("!A")){
					me.setHeading(msgE.get(i).getAngle());
					me.ctask = goenemybase;
					return null;					
				}
			}
			me.ctask = randomMove;
			return null;
		}
	};

	//	public int timeOut = 0;

	/*	static WTask waitAnswer = new WTask() {
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

						if (msg.get(i).getSenderType() == WarAgentType.WarBase) {
							if (msg.get(i).getMessage().equals("!UA")) {
								me.setDebugString("Mission Accepted");
								me.setHeading(msg.get(i).getAngle());
								me.ctask = randomMove;
								return null;
							}

							else if (msg.get(i).getMessage().equals("|UA")) {
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
		};*/

	static WTask attackennemy = new WTask() {
		String exec(WarBrain bc){
			WarLightBrainController me = (WarLightBrainController) bc;
			ArrayList<WarAgentPercept> Percepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemies();

			for (int i=0;i<Percepts.size();i++) {
				if(!(Percepts.get(0).getType() == WarAgentType.WarExplorer || Percepts.get(0).getType() == WarAgentType.WarTurret) && !(Percepts.get(0).getType() == WarAgentType.WarFood)){
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
				if (Percepts.get(0).getType() ==  WarAgentType.WarKamikaze || Percepts.get(0).getType() ==  WarAgentType.WarHeavy) {me.setHeading(Percepts.get(0).getAngle() + 150);me.timerfuite = 5;me.ctask=fuite;return WarLight.ACTION_MOVE;}
				if(!(Percepts.get(0).getType() == WarAgentType.WarExplorer) || !(Percepts.get(0).getType() == WarAgentType.WarTurret)){
					me.setHeading(Percepts.get(0).getAngle());
					me.ctask=attackennemy;
					return null;
				}		
			}
			me.ctask=handleMsgs;
			return null;
		}
	};

	int timerfuite = 0;

	static WTask fuite = new WTask() {
		String exec(WarBrain bc) {
			WarHeavyBrainController me = (WarHeavyBrainController) bc;
			if (me.timerfuite != 0) {
				me.timerfuite--;
				return WarLight.ACTION_MOVE;
			}
			else {
				me.ctask=randomMove;
				return WarLight.ACTION_MOVE;
			}
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
			if(basePercepts != null | basePercepts.size() != 0){
				me.ctask = attackbase;
				return null;
			}

			return WarLight.ACTION_MOVE;
		}
	};
	int timeoutheal;
	static WTask retourbaseheal = new WTask() {
		String exec(WarBrain bc){
			WarLightBrainController me = (WarLightBrainController) bc;
			
			if(me.isBlocked())
				me.setRandomHeading();
			
			if (me.getHealth()/me.getMaxHealth() > 0.9 || me.timeoutheal > 400) {
				me.ctask=randomMove;
				return WarLight.ACTION_MOVE;
			}

			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsAlliesByType(WarAgentType.WarBase);
			if(basePercepts != null | basePercepts.size() != 0){
				if (!me.isBagEmpty()) {
					me.timeoutheal++;
					return WarLight.ACTION_EAT;}
				else {
					me.timeoutheal++;
					return WarLight.ACTION_IDLE;
				}
			}

			WarMessage m = me.getMessageFromBase();
			if (!(m==null) && m.getMessage()=="here") {
				me.setHeading(m.getAngle());
				me.timeoutheal++;
				return WarLight.ACTION_MOVE;
			}
			me.timeoutheal++;
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

	public void reflexes(){
		if (getHealth()/getMaxHealth() < 0.2) {
			broadcastMessageToAgentType(WarAgentType.WarBase, "where", "");
			ctask=retourbaseheal;
		}
	}

	@Override
	public String action() {

		reflexes();
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

	private WarMessage getMessageAboutBase() {
		for (WarMessage m : getMessages()) {
			if(m.getMessage().equals("B"))
				return m;
		}
		return null;
	}

	private WarMessage getMessageFromBase() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarBase))
				return m;
		}
		return null;	
	}

}