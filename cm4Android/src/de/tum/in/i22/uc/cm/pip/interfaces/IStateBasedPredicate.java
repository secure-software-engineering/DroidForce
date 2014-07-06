package de.tum.in.i22.uc.cm.pip.interfaces;

import de.tum.in.i22.uc.cm.datatypes.basic.exceptions.InvalidStateBasedFormulaException;


public interface IStateBasedPredicate {
	public Boolean evaluate() throws InvalidStateBasedFormulaException;
}
