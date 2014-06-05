package de.ecspride.pep;

import soot.jimple.infoflow.solver.InfoflowCFG;


/**
 * ICFG implementation based on an updatable CFG
 * 
 * @author Steven Arzt
 */
public class RunsecureICFG extends InfoflowCFG {

	public RunsecureICFG() {
		super(new UpdatableRunsecureCFG());
	}
	
	public UpdatableRunsecureCFG getDelegate() {
		return (UpdatableRunsecureCFG) delegate;
	}
	
}
