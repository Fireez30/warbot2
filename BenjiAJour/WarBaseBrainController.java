package FSM2;

import java.util.ArrayList;
import java.util.List;

import edu.warbot.agents.MovableWarAgent;
import edu.warbot.agents.agents.WarBase;
import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarBaseBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarBaseBrainController extends WarBaseBrain {

	private boolean _alreadyCreated;

	public WarBaseBrainController() {
		super();

		ctask = idle;
		_alreadyCreated = false;
	}

	WTask ctask;



	static WTask idle = new WTask(){
		String exec(WarBrain bc){
			WarBaseBrainController me = (WarBaseBrainController) bc;
			ArrayList<WarAgentPercept> EnemyPercepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemies();		
			me.setDebugString("idle time");
			if(EnemyPercepts != null && EnemyPercepts.size() > 0){
				me.setDebugString("Ennemi repéré dans la base ");
				me.broadcastMessageToAll("UA", "");
				me.ctask = waitForPeople;				
				return null;
			}
		
			WarMessage msg2 = me.getMessageFromExplorer();
			if (!(msg2==null)) {
				me.sendMessage(msg2.getSenderID(), "here", "");
			}
			
			if (me.getNbElementsInBag() >= 0 && me.getHealth() <= 0.9 * me.getMaxHealth())
				return WarBase.ACTION_EAT;

			if (!me._alreadyCreated) {
				double rand = Math.random() %100;
				
				if (rand < 80)
					me.setNextAgentToCreate(WarAgentType.WarLight);
				else 
					me.setNextAgentToCreate(WarAgentType.WarExplorer);
				
				return WarBase.ACTION_CREATE;
			}

			if (me.getMaxHealth() == me.getHealth()) {
				me._alreadyCreated = true;
			}



			return WarBase.ACTION_IDLE;
		}
	};

	public int timeOut = 0;

	static WTask waitForPeople = new WTask() {
		String exec(WarBrain bc){
			WarBaseBrainController me = (WarBaseBrainController) bc;
			me.timeOut++;
			me.setDebugString("En attente de réponse");
			WarMessage msg = me.getMessageFromLight();			
			if (me.timeOut < 200) {
				if (msg == null) {
					me.setDebugString("No Mail");
					return WarBase.ACTION_IDLE;
				}

				if (msg.getMessage().equals("AUA")){
					me.setDebugString("Got Mail");
					me.sendMessage(msg.getSenderID(), "!UA", "");
				}
				else {
					me.sendMessage(msg.getSenderID(), "|UA", "");
				}

				me.timeOut--;
				return WarBase.ACTION_IDLE;
			}
			else {
				me.timeOut = 0;
				me.ctask = idle;
				return null;	
			}
		}
	};


	@Override
	public String action() {

		// Develop behaviour here
		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			return WarBase.ACTION_IDLE;
		}
		
		return toReturn;

		
	}
	
	private WarMessage getMessageFromLight() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarLight))
				return m;
		}
		return null;
	}
	
	private WarMessage getMessageFromExplorer() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarExplorer))
				return m;
		}
		return null;
	}


}
