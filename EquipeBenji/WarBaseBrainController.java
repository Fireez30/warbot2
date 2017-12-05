package FSM2;

import java.util.List;

import edu.warbot.agents.agents.WarBase;
import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.brains.WarBaseBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarBaseBrainController extends WarBaseBrain {

	private boolean _alreadyCreated;
	private boolean _inDanger;

	public WarBaseBrainController() {
		super();

		_alreadyCreated = false;
		_inDanger = false;
	}


	@Override
	public String action() {

		List<WarMessage> messages = getMessages();

		for (WarMessage message : messages) {
			if (message.getMessage().equals("Where is the base"))
				reply(message, "I'm here");
		}

		for (WarAgentPercept percept : getPercepts()) {
			setDebugString("Bonjour !");
			if (isEnemy(percept) && percept.getType().getCategory().equals(WarAgentCategory.Soldier)) {
				setDebugString("help");
				broadcastMessageToAll("I'm under attack",
						String.valueOf(percept.getAngle()),
						String.valueOf(percept.getDistance()));
			}
		}

		for (WarAgentPercept percept : getPerceptsResources()) {
			if (percept.getType().equals(WarAgentType.WarFood))
				broadcastMessageToAgentType(WarAgentType.WarExplorer, "I detected food",
						String.valueOf(percept.getAngle()),
						String.valueOf(percept.getDistance()));
		}
		
		if (!_alreadyCreated) {
			setNextAgentToCreate(WarAgentType.WarEngineer);
			return WarBase.ACTION_CREATE;
		}

		if (getNbElementsInBag() >= 0 && getHealth() <= 0.8 * getMaxHealth())
			return WarBase.ACTION_EAT;

		if (getMaxHealth() == getHealth()) {
			_alreadyCreated = true;
		}



		return WarBase.ACTION_IDLE;
	}

}
