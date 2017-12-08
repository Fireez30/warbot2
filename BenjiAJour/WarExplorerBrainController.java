package FSM2;

/** TEAM : Eat 7 and Die
 * explorer : cherche base - cherche nourriture - rapporte nourriture (done)
 * light : déplace aléatoirement - tire sur les ennemis au hasard - se dirige automatiquement vers la base dès que réception du message
 * heavy : reste fixe - défends la base
 * engineer : ???
 * kamikaze : déplace vers nourriture - explose à vue
 */

import java.awt.Color;
import java.util.ArrayList;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.WarAgent;
import edu.warbot.agents.WarResource;
import edu.warbot.agents.agents.WarExplorer;
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
					ArrayList<WarAgentPercept> basePercepts2 =
							(ArrayList<WarAgentPercept>) me.getPerceptsAlliesByType(WarAgentType.WarBase);

					if(basePercepts == null | basePercepts.size() == 0){

						WarMessage m = me.getMessageFromBase();
						//Si j'ai un message de la base je vais vers elle
						if(m != null){
							if (m.getDistance() < WarResource.MAX_DISTANCE_TAKE) {
								return WarExplorer.ACTION_GIVE;
							}
							me.setHeading(m.getAngle());						
						}

						return(MovableWarAgent.ACTION_MOVE);
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
			me.setDebugString("En attente de r�ponse");
			WarMessage msg = me.getMessageFromLight();			
			if (me.timeOut < 200) {
				if (msg == null) {
					me.setDebugString("No Mail");
					return MovableWarAgent.ACTION_IDLE;
				}

				if (msg.getMessage().equals("A")){
					me.setDebugString("Got Mail");
					me.sendMessage(msg.getSenderID(), "!A", me.getPerceptsEnemiesByType(WarAgentType.WarBase).get(0) + "");
				}
				else {
					me.sendMessage(msg.getSenderID(), "|A", "");
				}

				me.timeOut--;
				return MovableWarAgent.ACTION_IDLE;
			}
			else {
				me.setHeading(me.getHeading() + 180);
				me.timeOut = 0;
				me.ctask = idle;
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


	static WTask getFoodTask = new WTask(){
		String exec(WarBrain bc){
			WarExplorerBrainController me = (WarExplorerBrainController) bc;
			ArrayList<WarAgentPercept> EnemyBasePercepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemiesByType(WarAgentType.WarBase);

			if(EnemyBasePercepts != null && EnemyBasePercepts.size() > 0){
				WarAgentPercept baseP = EnemyBasePercepts.get(0); //le 0 est le plus proche normalement
				me.setHeading(EnemyBasePercepts.get(0).getAngle());
				me.broadcastMessageToAll("B","");
				me.setDebugString("Waiting");
				me.ctask=waitForPeople;
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



	public WarExplorerBrainController() {
		super();
		ctask = getFoodTask; // initialisation de la FSM
	}

	@Override
	public String action() {

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


