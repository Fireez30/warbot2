package otakings;


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
import edu.warbot.brains.brains.WarRocketLauncherBrain;
import edu.warbot.communications.WarMessage;
import edu.warbot.tools.geometry.PolarCoordinates;

public abstract class WarRocketLauncherBrainController extends  WarRocketLauncherBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 		
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;

			List<WarMessage> msgE = me.getMessages();

			for(int i=0;i<msgE.size();i++) {
				if (msgE.get(i).getSenderType() == WarAgentType.WarExplorer) {
					if (msgE.get(i).getMessage().equals("B")) {
						me.setDebugString("Message base adverse recu ! ");
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
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;			
			List<WarMessage> msg = me.getMessages();
			me.timeOut++;
			if (me.timeOut <= 200) {
				if (msg == null) {
					return WarRocketLauncher.ACTION_MOVE;
				}
				for (int i=0;i<msg.size();i++) {
					if (msg.get(i).getSenderType() == WarAgentType.WarExplorer) {
						if (msg.get(i).getMessage().equals("!A")) {
							me.setDebugString("Mission Accepted");
							me.sendMessage(msg.get(i).getSenderID(), "Coord", "");
							me.ctask = aimBase;
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
	};

	static WTask randomMove = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			me.setDebugString("WarRocketLauncher");
			
			if(me.isBlocked())
				me.setRandomHeading();

			me.ctask=handleMsgs;
			return null;
		}
	};

	public WarRocketLauncherBrainController() {
		super();
		ctask= randomMove;
	}

	static WTask aimBase = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			
			me.timeOut++;

			List<WarMessage> msgE = me.getMessages();

			double angle = -1;
			double dist = -1;
			double angle2 = -1;
			double dist2 = -1;

			for(int i=0;i<msgE.size();i++) 
				if (msgE.get(i).getSenderType() == WarAgentType.WarExplorer) {
					if (msgE.get(i).getMessage().equals("COORD")) { 
						me.setDebugString("Coordonées de base adverse recu ! ");
						angle = (double) Integer.parseInt(msgE.get(i).getContent()[0]);
						angle2 = msgE.get(i).getAngle();
						dist = (double) Integer.parseInt(msgE.get(i).getContent()[1]);
						dist2 = msgE.get(i).getDistance();
					}
				}

			if (me.timeOut > 300) {
				me.setDebugString("fuck off");
				me.ctask=randomMove;
				return null;
			}
			else if (angle == -1 || dist == -1 || angle2 == -1 || dist2 == -1) {
				return ACTION_MOVE;
			}

			me.setHeading(me.getTargetedAgentPosition(angle2, dist2, angle, dist).getAngle());
			me.setTargetDistance(me.getTargetedAgentPosition(angle2, dist2, angle, dist).getDistance());

			me.timeOut = 0;
			
			me.ctask = attackbase;
			return null;
		}
	};	

	static WTask attackbase = new WTask() {
		String exec(WarBrain bc) {

			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;
			
			if (me.timeOut <= 100) { 
				if (me.isReloaded()) {
					me.timeOut++;
					return ACTION_FIRE;
				}
				else if (me.isReloading())
					return ACTION_IDLE;
				else
					return ACTION_RELOAD;
			}
			
			else {
				me.timeOut = 0;
				me.ctask = randomMove;
				return null;
			}
		}

	};

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

	private WarMessage getMessageFromLight() {
		for (WarMessage m : getMessages()) {
			if(m.getSenderType().equals(WarAgentType.WarLight))
				return m;
		}
		return null;	
	}

}
