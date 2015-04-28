package de.tum.in.i22.uc.pdp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.interfaces.IPdp2Pip;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.DataInContainerComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.ElementInListComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.EndsWithComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.EqualsComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.EqualsIgnoreCaseComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.GeComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.GenericComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.GtComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.LeComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.ListInListComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.LtComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.NotEqualsComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.StartsWithComparisonOperator;
import de.tum.in.i22.uc.pdp.core.condition.comparisonOperators.SubstringComparisonOperator;
import de.tum.in.i22.uc.pdp.core.shared.Param;
import de.tum.in.i22.uc.pdp.xsd.ParamMatchType;

public class ParamMatch extends ParamMatchType {
	private static Logger log = LoggerFactory.getLogger(ParamMatch.class);
	private PolicyDecisionPoint _pdp;

	public void setPdp(PolicyDecisionPoint pdp){
		if (_pdp==null) _pdp=pdp;
	}
	
	public String toString() {
		String str = "" + this.getName() + " -> " + this.getValue() + " ("
				+ this.getType() + ")";
		return str;
	}

	public boolean paramMatches(Param<?> param) {
		boolean matches = false;
		if (param == null) {
			log.trace("Parameter [{}] not present", this.getName());
			return false;
		}
		if (this.getName().equals(param.getName())) {
			log.trace("param name [" + this.getName() + "] matches");
			GenericComparisonOperator compOp;
			if (this.getCmpOp() != null) {
				switch (this.getCmpOp()) {
				case ELEMENT_IN_LIST:
					log.debug("parameter value checked for \"element in list\" comparison");
					compOp = new ElementInListComparisonOperator();
					break;
				case ENDS_WITH:
					log.debug("parameter value checked for \"ends with\" comparison");
					compOp = new EndsWithComparisonOperator();
					break;
				case EQUALS:
					log.debug("parameter value checked for \"equals\" comparison");
					compOp = new EqualsComparisonOperator();
					break;
				case EQUALS_IGNORE_CASE:
					log.debug("parameter value checked for \"equals ignore case\" comparison");
					compOp = new EqualsIgnoreCaseComparisonOperator();
					break;
				case GE:
					log.debug("parameter value checked for \"greater or equal than\" comparison");
					compOp = new GeComparisonOperator();
					break;
				case GT:
					log.debug("parameter value checked for \"greater than\" comparison");
					compOp = new GtComparisonOperator();
					break;
				case LE:
					log.debug("parameter value checked for \"smaller or equal than\" comparison");
					compOp = new LeComparisonOperator();
					break;
				case LIST_IN_LIST:
					log.debug("parameter value checked for \"list in list\" comparison");
					compOp = new ListInListComparisonOperator();
					break;
				case LT:
					log.debug("parameter value checked for \"smaller than\" comparison");
					compOp = new LtComparisonOperator();
					break;
				case NOT_EQUALS:
					log.debug("parameter value checked for \"different than\" comparison");
					compOp = new NotEqualsComparisonOperator();
					break;
				case STARTS_WITH:
					log.debug("parameter value checked for \"starts with\" comparison");
					compOp = new StartsWithComparisonOperator();
					break;
				case DATA_IN_CONTAINER:
					log.debug("parameter value checked for \"data in container\" comparison");
					IPdp2Pip pip = _pdp.getPip();
					if (pip == null) {
						log.error("Impossible to run a data-container comparison with empty PIP. Better crash now than live like this.");
						throw new RuntimeException(
								"Impossible to run a data-container comparison with empty PIP. Better crash now than live like this.");
					}
					compOp = new DataInContainerComparisonOperator(pip);
					break;
				case SUBSTRING:
					log.debug("parameter value checked for \"substring\" comparison");
					compOp = new SubstringComparisonOperator();
					break;
				default:
					log.debug("parameter value checked for default comparison (equals)");
					compOp = new EqualsComparisonOperator(); // default
																// comparison is
																// equality
					break;
				}
				matches = compOp.compare((String) param.getValue(),
						this.getValue());
			}
			log.trace("param value [" + (String) param.getValue() + "] does "
					+ (matches ? "" : "NOT ") + "match [" + this.getValue()
					+ "]");
			return matches;
		}
		log.trace("param name [" + this.getName() + "] does NOT match");
		return false;
	}

}
