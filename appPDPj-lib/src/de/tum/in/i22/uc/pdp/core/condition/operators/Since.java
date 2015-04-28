package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.condition.Operator;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.SinceType;

public class Since extends SinceType {
	private static Logger log = LoggerFactory.getLogger(Since.class);

	public Since() {
	}

	public Since(Operator operand1, Operator operand2) {
		this.getOperators().add(operand1);
		this.getOperators().add(operand2);
	}

	@Override
	public void initOperatorForMechanism(IPdpMechanism mech) {
		super.initOperatorForMechanism(mech);
		((Operator) this.getOperators().get(0)).initOperatorForMechanism(mech);
		((Operator) this.getOperators().get(1)).initOperatorForMechanism(mech);
	}

	public String toString() {
		String str = "SINCE (" + this.getOperators().get(0) + ", "
				+ this.getOperators().get(1) + " )";
		return str;
	}

	@Override
	public boolean evaluate(Event curEvent) { // A occurs, SINCE is satisfied
												// (LTL doesn't state anything
												// about B in the timestep when
												// A happens)
		Boolean operand1state = ((Operator) this.getOperators().get(0))
				.evaluate(curEvent);
		Boolean operand2state = ((Operator) this.getOperators().get(1))
				.evaluate(curEvent);

		if (operand1state) {
			log.debug("[SINCE] Subformula A satisfied this timestep => TRUE");
			this.state.value = true;
		} else {
			if (!this.state.immutable) { // until now B occurred every following
											// timestep

				if (this.state.counter == 1) {
					log.debug("[SINCE] Subformula A was satisfied any previous timestep");

					if (operand2state) {
						log.debug("[SINCE] Subformula B is satisfied this timestep => TRUE");
						this.state.value = true;
					} else {
						log.debug("[SINCE] Subformula B NOT satisfied this timestep => FALSE");
						this.state.value = false;
					}
				} else {
					log.debug("[SINCE] Subformula A NOT satisfied this timestep or any previous timestep");
					log.debug("[SINCE] Not yet immutable; check (ALWAYS B) part of since");

					if (operand2state) {
						log.debug("[SINCE] Subformula B is satisfied this timestep => TRUE");
						this.state.value = true;
					} else {
						log.debug("[SINCE] Subformula B NOT satisfied this timestep => FALSE");
						this.state.value = false;
					}
				}
			}
		}

		if (curEvent == null) {
			if (!this.state.value) {
				if (!this.state.immutable) { // immutable until next occurence
												// of subformula A
					log.debug("[SINCE] Evaluating current state value was FALSE =>  activating IMMUTABILITY");
					this.state.immutable = true;
				}
			}

			if (operand1state) {
				log.debug("[SINCE] Subformula A satisfied this timestep => setting counter flag");
				this.state.counter = 1;
				if (this.state.immutable) {
					log.debug("[SINCE] Deactivating immutability");
					this.state.immutable = false;
				}
			}

			if (!this.state.subEverTrue && !operand2state) {
				log.debug("[SINCE] Subformula B was previously always satisfied, but NOT this timestep => 2nd part of since can never be satisfied any more");
				log.debug("[SINCE] Setting subEverFalse flag and activating immutability");
				this.state.subEverTrue = true; // intention here subformula was
												// ever FALSE (in contrast to
												// name...)
				this.state.immutable = true;
			}
		}

		log.debug("eval SINCE [{}]", this.state.value);
		return this.state.value;
	}
}
