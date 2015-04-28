package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.condition.Operator;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.RepSinceType;

public class RepSince extends RepSinceType {
	private static Logger log = LoggerFactory.getLogger(RepSince.class);

	public RepSince() {
	}

	@Override
	public void initOperatorForMechanism(IPdpMechanism mech) {
		super.initOperatorForMechanism(mech);
		((Operator) this.getOperators().get(0)).initOperatorForMechanism(mech);
		((Operator) this.getOperators().get(1)).initOperatorForMechanism(mech);
	}

	public String toString() {
		String str = "REPSINCE (" + this.getLimit() + ", "
				+ this.getOperators().get(0) + ", "
				+ this.getOperators().get(1) + " )";
		return str;
	}

	@Override
	public boolean evaluate(Event curEvent) { // repsince(n, A, B); // n = limit
												// / A = op1 / B = op2
												// B(n) >= limit n times
												// subformula B since the last
												// occurrence of subformula A
		Boolean operand1state = ((Operator) this.getOperators().get(0))
				.evaluate(curEvent);
		Boolean operand2state = ((Operator) this.getOperators().get(1))
				.evaluate(curEvent);

		if (operand1state) {
			log.debug("[REPSINCE] Subformula A satisfied this timestep => TRUE");
			this.state.value = true;
		} else {
			long limitComparison = this.state.counter + (operand2state ? 1 : 0);
			log.debug("[REPSINCE] Counter for subformula B [{}]",
					limitComparison);

			if (this.state.subEverTrue) {
				log.debug("[REPSINCE] Subformula A was satisfied any previous timestep");
				if (limitComparison <= this.getLimit()) {
					log.debug("[REPSINCE] Amount of occurrences of subformula B <= limit ==> TRUE");
					this.state.value = true;
				} else {
					log.debug("[REPSINCE] Occurrence limitation exceeded! ==> FALSE");
					this.state.value = false;
				}
			} else {
				log.debug("[REPSINCE] Subformula A NOT satisfied this timestep or any previous timestep");
				if (limitComparison <= this.getLimit()) {
					log.debug("[REPSINCE] Global amount of occurrences of subformula B <= limit ==> TRUE");
					this.state.value = true;
				} else {
					log.debug("[REPSINCE] Global occurrence limitation exceeded! ==> FALSE");
					this.state.value = false;
				}

			}
		}

		if (curEvent == null) {
			if (operand1state) {
				log.debug("[REPSINCE] Subformula A satisfied this timestep => setting flag and resetting counter");
				this.state.subEverTrue = true;

				this.state.counter = 0;
				log.debug("[REPSINCE] Counter for subformula B [{}]",
						this.state.counter);
			}

			if (operand2state) {
				this.state.counter++;
				log.debug("[REPSINCE] Counter for subformula B [{}]",
						this.state.counter);
			}
		}

		log.debug("eval REPSINCE [{}]", this.state.value);
		return this.state.value;
	}
}
