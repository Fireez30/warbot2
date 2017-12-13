package otakings;

/** TEAM : Otakings
 */

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarAgent;
import edu.warbot.agents.WarResource;
import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.agents.WarLight;
import edu.warbot.agents.agents.WarRocketLauncher;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.agents.percepts.WarPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarExplorerBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarExplorerBrainController extends WarExplorerBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 
		String exec(WarBrain bc){return "";}
	};

	static WTask returnFoodTask = new WTask(){
		String exec(WarBrain bc){
			WarExplorerBrainController me = (WarExplorerBrainController) bc;

			if(me.isBlocked()) {
				me.setRandomHeading();
				return WarExplorer.ACTION_MOVE;
			}
			if(me.isBagEmpty()){
				me.setHeading(me.getHeading() + 180);
				me.ctask = getFoodTask;
				return(null);
			}

			me.setDebugStringColor(Color.green.darker());
			me.setDebugString("Returning Food");

			if(me.isBlocked())
				me.setRandomHeading();

			ArrayList<WarAgentPercept> basePercepts = (ArrayList<WarAgentPercept>) me.getPerceptsAlliesByType(WarAgentType.WarBase);

			//Si je ne vois pas de base
			if(basePercepts == null | basePercepts.size() == 0){

				me.broadcastMessageToAgentType(WarAgentType.WarBase, "?H", (String[]) null);

				WarMessage m = me.getMessageFromBase();
				//Si j'ai un message de la base je vais vers elle
				if(m != null){
					if (m.getDistance() < WarResource.MAX_DISTANCE_TAKE) {
						return WarExplorer.ACTION_GIVE;
					}
					me.setHeading(m.getAngle());						
				}

			}
			else{//si je vois une base
				WarAgentPercept base = basePercepts.get(0);

				if(base.getDistance() > MovableWarAgent.MAX_DISTANCE_GIVE){
					me.setHeading(base.getAngle());
					return(MovableWarAgent.ACTION_MOVE);
				}else{
					me.setIdNextAgentToGive(base.getID());
					return(MovableWarAgent.ACTION_GIVE);
				}

			}

			return MovableWarAgent.ACTION_MOVE;

		}
	};

	public int timeOut = 0;

	static WTask waitForPeople = new WTask() {
		String exec(WarBrain bc) {
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			me.timeOut++;
			me.setDebugString("En attente de reponse");
			ArrayList<WarAgentPercept> EnemyBasePercepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);

			List<WarMessage> msg = me.getMessages();
			if (me.timeOut < 350) {
				if (msg == null) {
					me.setDebugString("No Mail");
					me.broadcastMessageToAll("B","");
					return WarExplorer.ACTION_IDLE;
				}
				for (int i=0;i<msg.size();i++) {
					if (msg.get(0).getMessage().equals("A")){
						me.setDebugString("Got Mail");
						me.sendMessage(msg.get(0).getSenderID(), "!A","");
						if (msg.get(0).getSenderType() == WarAgentType.WarRocketLauncher) {
							timeOut = 0;
							me.ctask = givingCoord;
							return null;
						}
					}
				}
			}

			else {
				me.timeOut = 0;
				me.setHeading(me.getHeading() + 180);
				me.ctask = getFoodTask;
				return null;
			}
			return WarExplorer.ACTION_IDLE;

		}
	};

	static WTask givingCoord = new WTask() {
		String exec(WarBrain bc) {
			WarExplorerBrainController me = (WarExplorerBrainController) bc;

			me.timeOut++;

			ArrayList<WarAgentPercept> EnemyBasePercepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);
			List<WarMessage> msg = me.getMessages();
			for (int i=0;i<msg.size();i++) 			
				if (msg.get(0).getMessage().equals("Coord")) {
					me.sendMessage(msg.get(0).getSenderID(), "Angle", Double.toString(me.getHeading()));
					me.sendMessage(msg.get(0).getSenderID(), "Distance", Double.toString(EnemyBasePercepts.get(0).getDistance()));
				}


			if (me.timeOut > 150) {
				me.ctask = waitForPeople;
				me.timeOut = 0;
				return null;
			}
		}
	};


	static WTask idle = new WTask(){
		String exec(WarBrain bc){
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			ArrayList<WarAgentPercept> EnemyBasePercepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);			
			me.setDebugString("idle time");
			if(EnemyBasePercepts != null && EnemyBasePercepts.size() > 0){
				//me.broadcastMessageToAll("B", "");
				me.ctask = waitForPeople;				
				return null;
			}
			else {
				me.ctask = getFoodTask;
				return null;
			}
		}
	};

	static WTask mangepoursurvivre = new WTask(){
		String exec(WarBrain bc) {
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			if (me.getHealth()/me.getMaxHealth() > 0.8 ) {
				me.ctask = getFoodTask;
				return WarExplorer.ACTION_MOVE;
			}

			if (me.isBagEmpty()) {me.ctask=retourbaseheal;return WarExplorer.ACTION_MOVE;}

			return WarExplorer.ACTION_EAT;
		}
	};

	static WTask getFoodTask = new WTask(){
		String exec(WarBrain bc){
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			ArrayList<WarAgentPercept> EnemyTurretPercepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarTurret);

			if(EnemyTurretPercepts != null && EnemyTurretPercepts.size() > 0){
				WarAgentPercept baseP = EnemyTurretPercepts.get(0); //le 0 est le plus proche normalement
				me.setHeading(EnemyTurretPercepts.get(0).getAngle());
				me.broadcastMessageToAll("B","");
				me.setDebugString("Waiting");
				me.ctask=waitForPeople;
				return WarExplorer.ACTION_IDLE;
			}

			if(me.isBagFull()){
				me.ctask = returnFoodTask;
				return(null);
			}

			if(me.isBlocked())
				me.setRandomHeading();

			me.setDebugString("test");
			me.setDebugStringColor(Color.BLACK);
			me.setDebugString("Searching food");




			for (WarAgentPercept wp : me.getPercepts()) {

				//Si il y a de la nouriture

				if(wp.getType().equals(WarAgentType.WarFood)){

					me.broadcastMessageToAgentType(WarAgentType.WarExplorer, "N", "");
					if(wp.getDistance() > WarResource.MAX_DISTANCE_TAKE){
						me.setHeading(wp.getAngle());
						return(MovableWarAgent.ACTION_MOVE);
					}
					else{
						return(WarExplorer.ACTION_TAKE);
					}
				}
			}

			WarMessage msgf = me.getMessageAboutFood();
			if (msgf != null && msgf.getDistance() < 250){
				me.setHeading(msgf.getAngle());
				return WarExplorer.ACTION_MOVE;
			}

			return WarExplorer.ACTION_MOVE;

		}
	};

	static WTask retourbaseheal = new WTask() {
		String exec(WarBrain bc){
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			if (me.getHealth()/me.getMaxHealth() > 0.9) {
				me.ctask=getFoodTask;
				return WarExplorer.ACTION_MOVE;
			}

			ArrayList<WarAgentPercept> basePercepts = 
					(ArrayList<WarAgentPercept>) me.getPerceptsAlliesByType(WarAgentType.WarBase);
			if(basePercepts != null | basePercepts.size() != 0){
				if (!me.isBagEmpty()) return WarLight.ACTION_EAT;
				else {return WarLight.ACTION_IDLE;}
			}

			WarMessage m = me.getMessageFromBase();
			if (!(m==null) && m.getMessage()=="here") {
				me.setHeading(m.getAngle());
				return WarLight.ACTION_MOVE;
			}

			return WarLight.ACTION_MOVE;

		}
	};



	public void reflexes(){
		setDebugString("reflexes");
		ArrayList<WarAgentPercept> p = (ArrayList<WarAgentPercept>) getPerceptsEnemiesByType(WarAgentType.WarBase);
		if (getHealth()/getMaxHealth() < 0.2 && (p.size()==0)) {
			if (!isBagEmpty()) {ctask=mangepoursurvivre;}
			broadcastMessageToAgentType(WarAgentType.WarBase, "where", "");
			ctask=retourbaseheal;
		}
	}

	public WarExplorerBrainController() {
		super();
		ctask = getFoodTask; // initialisation de la FSM
	}

	@Override
	public String action() {
		reflexes();
		// Develop behaviour here
		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			if (isBlocked())
				setRandomHeading();
			return WarExplorer.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}


	private WarMessage getMessageAboutFood() {
		for (WarMessage m : getMessages()) {
			if(m.getMessage().equals("N"))
				return m;
		}
		return null;
	}

	private WarMessage getMessageFromRocketLauncher() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarRocketLauncher))
				return m;
		}
		return null;
	}

	private WarMessage getMessageFromLight() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarLight))
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

