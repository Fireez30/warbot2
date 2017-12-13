package otakings;

import java.util.ArrayList;
import java.util.List;

import edu.warbot.agents.agents.WarExplorer;
import edu.warbot.agents.agents.WarHeavy;
import edu.warbot.agents.agents.WarKamikaze;
import edu.warbot.agents.enums.WarAgentType;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.WarBrain;
import edu.warbot.brains.brains.WarKamikazeBrain;

public abstract class WarKamikazeBrainController extends WarKamikazeBrain {

	public WarKamikazeBrainController() {
		super();
		ctask = randomMove;
	}

	WTask ctask;

	static WTask attackennemy = new WTask() {
		String exec(WarBrain bc){
			WarKamikazeBrainController me = (WarKamikazeBrainController) bc;
			ArrayList<WarAgentPercept> Percepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemies();

			for (int i=0;i<Percepts.size();i++) {
				if(!(Percepts.get(0).getType() == WarAgentType.WarExplorer) && !(Percepts.get(0).getType() == WarAgentType.WarFood)){
					me.setHeading(Percepts.get(0).getAngle());
					me.setDebugString("A l'attaque");
					return ACTION_FIRE;
				}
			}

			me.ctask=randomMove;
			return WarKamikaze.ACTION_MOVE;
		}

	};

	static WTask randomMove = new WTask() { 
		String exec(WarBrain bc){
			WarHeavyBrainController me = (WarHeavyBrainController) bc;
			
			ArrayList<WarAgentPercept> Percepts = (ArrayList<WarAgentPercept>) me.getPerceptsEnemies();

			for (int i=0;i<Percepts.size();i++) {
				if(!(Percepts.get(0).getType() == WarAgentType.WarExplorer)){
					me.setHeading(Percepts.get(0).getAngle());
					me.ctask=attackennemy;
					return WarKamikaze.ACTION_MOVE;
				}		
			}
			
			return null;
		}
	};
	
	public void reflexes(){

	}



	public String action() {
		String toReturn = ctask.exec(this);   // le run de la FSM

		if(toReturn == null){
			if (isBlocked())
				setRandomHeading();
			return WarKamikaze.ACTION_MOVE;
		} else {
			return toReturn;
		}
	}
}
