package de.ecspride.pep;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

/**
 * Bidirectional CFG implementation for Runsecure. This enables to add methods
 * created later on to the ICFG.
 * 
 * @author Steven Arzt
 */

public class UpdatableRunsecureCFG extends JimpleBasedInterproceduralCFG {

	/**
	 * Adds a method to the ICFG. With this method, you can register a method
	 * whose body has been loaded after the ICFG has been constructed.
	 * @param method The method to add
	 */
	public void addMethodToICFG(SootMethod method) {
		if (!method.isConcrete())
			throw new RuntimeException("Cannot add non-concrete methods to icfg");

		Body b = method.retrieveActiveBody();
		PatchingChain<Unit> units = b.getUnits();
		for (Unit unit : units) {
			unitToOwner.put(unit, b);
		}
	}
}
