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

public abstract class WarRocketLauncherBrainController extends  WarRocketLauncherBrain {

	WTask ctask;

	static WTask handleMsgs = new WTask(){ 		
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;

			//getMessageEnnemyBase "B"

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

				if (msgE.get(i).getSenderType() == WarAgentType.WarBase) {
					if (msgE.get(i).getMessage().equals("UA")) {
						me.setDebugString("Base en danger ! ");
						me.sendMessage(msgE.get(i).getSenderID(), "AUA");
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
			me.setDebugString("Waiting for Answer");
			if (me.timeOut <= 200) {
				if (msg == null) {
					return WarRocketLauncher.ACTION_MOVE;
				}
				for (int i=0;i<msg.size();i++) {
					if (msg.get(i).getSenderType() == WarAgentType.WarExplorer) {
						if (msg.get(i).getMessage().equals("!A")) {
							me.setDebugString("Mission Accepted");
							me.sendMessage(msg.get(i).getSenderID(), "Coord", "");
							me.ctask = aimbase;
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

			me.

			List<WarMessage> msgE = me.getMessages();

			double angle = null;
			double dist = null;
			double angle2;
			double dist2;

			for(int i=0;i<msgE.size();i++) 
				if (msgE.get(i).getSenderType() == WarAgentType.WarExplorer) {
					if (msgE.get(i).getMessage().equals("Angle")) { 
						me.setDebugString("Angle de base adverse recu ! ");
						angle = (double) Interger.parseInt(msgE.get(i).getContent());
						angle2 = (double) Interger.parseIntmsgE.get(i).getAngle());
					}
					if (msgE.get(i).getMessage().equals("Distance")) { 
						me.setDebugString("Ditance de base adverse recu ! ");
						dist = (double) Interger.parseInt(msgE.get(i).getContent());
						dist2 = (double) Interger.parseInt(msgE.get(i).getDistance());
					}
				}

			if (angle == null || distance == null) {
				me.ctask=randomMove;
				return null;
			}

			PolarCoordinate PC = me.getTargetedAgentPosition(angle2, dist2, angle, dist);

			me.setHeading(PC.getAngle);

			if (PC.getDistance() > 150) {
				me.sendMessage(me.getID(), "DL", PC.getDistance() + "");
				me.ctask=goForward;
				return null;
			}

			else {
				me.ctask = attackbase;
				return null;
			}
		}
	};

	static WTask goForward = new WTask() { 
		String exec(WarBrain bc){
			WarRocketLauncherBrainController me = (WarRocketLauncherBrainController) bc;

			List<WarMessage> msgE = me.getMessages();

			double distance;

			for(int i=0;i<msgE.size();i++) 
				if (msgE.get(i).getSenderID() == me.getID()) 
					if (msgE.get(i).getMessage().equals("DL")) { 
						me.setDebugString("Encore " + msgE.get(i).getContent());
						distance = (double) Interger.parseInt(msgE.get(i).getContent());
					}

			if (distance > 150) {
				me.sendMessage(me.getID(), "DL", PC.getDistance() - 1 + "");
				return ACTION_MOVE;
			}
			else {
				me.ctask = attackbase;
				return null;
			}

		}
	};
	
	

	static WTask attackbase = new WTask() {
		String exec(WarBrain bc) {

			if (me.isReloaded()) {
				return ACTION_FIRE;
				me.timeOut++;
			}
			else if (me.isReloading())
				return ACTION_IDLE;
			else
				return ACTION_RELOAD;
			
			if (me.timeOut > 40) {
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
