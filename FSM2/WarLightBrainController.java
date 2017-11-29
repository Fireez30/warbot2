package FSM2;


import edu.warbot.agents.enums.WarAgentCategory;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.brains.WarLightBrain;
import edu.warbot.communications.WarMessage;

public abstract class WarLightBrainController extends  WarLightBrain {


	public WarLightBrainController() {
		super();
	}

	@Override
	public String action() {

		for (WarMessage m : getMessages()) {
			if (m.getMessage() == "I'm under attack") {
				if (m.getDistance() < 250){
					this.setHeading(m.getAngle()); 
					return ACTION_MOVE;
				}
			}
			if (m.getMessage() == "base ennemie") {
				if (m.getDistance() < 250){
					this.setHeading(m.getAngle());
					return ACTION_MOVE;
				}
			}
		}

		for (WarAgentPercept wp : getPercepts()) {

			if (isEnemy(wp) && (wp.getType().getCategory().equals(WarAgentCategory.Soldier) || wp.getType().getCategory().equals(WarAgentCategory.Building))) {
				setHeading(wp.getAngle());
				this.setDebugString("Attaque");
				if (isReloaded())
					return ACTION_FIRE;
				else if (isReloading())
					return ACTION_IDLE;
				else
					return ACTION_RELOAD;
			}
		}

		if (isBlocked())
			setRandomHeading();

		return ACTION_MOVE;
	}

}