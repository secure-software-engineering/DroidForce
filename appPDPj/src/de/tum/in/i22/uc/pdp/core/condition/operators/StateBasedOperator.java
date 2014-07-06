package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.interfaces.IPdp2Pip;
import de.tum.in.i22.uc.cm.settings.Settings;
import de.tum.in.i22.uc.pdp.core.Mechanism;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.StateBasedOperatorType;

public class StateBasedOperator extends StateBasedOperatorType {
	private static Logger log = LoggerFactory
			.getLogger(StateBasedOperator.class);

	public StateBasedOperator() {
	}

	public StateBasedOperator(StateBasedOperatorType op, Mechanism curMechanism) {
		log.debug("Processing StateBasedFormula from StateBasedOperatorType");
		this.operator = op.getOperator();
		this.param1 = op.getParam1();
		this.param2 = op.getParam2();
		this.param3 = op.getParam3();
	}

	@Override
	public void initOperatorForMechanism(IPdpMechanism mech) {
		super.initOperatorForMechanism(mech);
	}

	@Override
	public String toString() {
		return "StateBasedFormula [operator='" + this.getOperator()
				+ "', param1='" + this.getParam1() + "', param2='"
				+ this.getParam2() + "', param3='" + this.getParam3() + "']";
	}

	@Override
	public boolean evaluate(Event curEvent) {

		IPdp2Pip pip = this._pdp.getPip();
		String separator= Settings.getInstance().getSeparator1();
		
		if (pip == null) {
			log.error("Impossible to evaluate state based operator ["
					+ getOperator() + " [" + getParam1() + "][" + getParam2()
					+ "][" + getParam3() + "]] without a pip reference");
		}

		String p = this.operator + separator + this.param1 + separator
				+ this.param2 + separator
				+ this.param3;
		
		if (curEvent==null) return pip.evaluatePredicateCurrentState(p);
		else return pip.evaluatePredicateSimulatingNextState(curEvent.toIEvent(),p);
	}
}
