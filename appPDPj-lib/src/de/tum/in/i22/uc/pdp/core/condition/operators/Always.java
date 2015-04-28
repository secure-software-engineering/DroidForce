package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.condition.Operator;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.AlwaysType;

public class Always extends AlwaysType {
	private static Logger log = LoggerFactory.getLogger(Always.class);

	public Always() {
	}

	public Always(Operator operand1) {
		this.setOperators(operand1);
	}

	@Override
	public void initOperatorForMechanism(IPdpMechanism mech) {
		super.initOperatorForMechanism(mech);
		((Operator) this.getOperators()).initOperatorForMechanism(mech);
	}

	public String toString() {
		return "ALWAYS (" + this.getOperators() + ")";
	}

	@Override
	public boolean evaluate(Event curEvent) {
		if (!this.state.immutable) {
			this.state.value = ((Operator) this.getOperators())
					.evaluate(curEvent);
			if (!this.state.value && curEvent == null) {
				log.debug("evaluating ALWAYS: activating IMMUTABILITY");
				this.state.immutable = true;
			}
		}
		log.debug("eval ALWAYS [{}]", this.state.value);
		return this.state.value;
	}
}
