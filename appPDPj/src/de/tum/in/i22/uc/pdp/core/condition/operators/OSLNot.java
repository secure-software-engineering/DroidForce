package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.condition.Operator;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.NotType;

public class OSLNot extends NotType {
	private static Logger log = LoggerFactory.getLogger(OSLNot.class);

	public OSLNot() {
	}

	public OSLNot(Operator operand) {
		this.operators = operand;
	}

	@Override
	public void initOperatorForMechanism(IPdpMechanism mech) {
		super.initOperatorForMechanism(mech);
		((Operator) this.getOperators()).initOperatorForMechanism(mech);
	}

	public String toString() {
		return "! " + this.getOperators();
	}

	@Override
	public boolean evaluate(Event curEvent) {
		this.state.value = !((Operator) this.getOperators()).evaluate(curEvent);
		log.debug("eval NOT [{}]", this.state.value);
		return this.state.value;
	}
}
