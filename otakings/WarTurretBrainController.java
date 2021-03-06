package otakings;

import java.util.List;

import edu.warbot.agents.agents.WarTurret;
import edu.warbot.agents.percepts.WarAgentPercept;
import edu.warbot.brains.brains.WarTurretBrain;

public abstract class WarTurretBrainController extends WarTurretBrain {

	private int _sight;

	public WarTurretBrainController() {
		super();

		_sight = 0;
	}

	@Override
	public String action() {
		_sight += 90;
		if (_sight == 360) {
			_sight = 0;
		}
		setHeading(_sight);

		List <WarAgentPercept> percepts = getPercepts();
		for (WarAgentPercept p : percepts) {
			if (isEnemy(p)) {
				setHeading(p.getAngle());
				if (isReloaded()) {
					return WarTurret.ACTION_FIRE;
				} else
					return WarTurret.ACTION_RELOAD;
			}

		}
		return WarTurret.ACTION_IDLE;
	}
}
