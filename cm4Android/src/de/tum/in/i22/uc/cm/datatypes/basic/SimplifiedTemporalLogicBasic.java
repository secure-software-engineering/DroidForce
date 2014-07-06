package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IDataEventMap;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IOslFormula;
import de.tum.in.i22.uc.cm.datatypes.interfaces.ISimplifiedTemporalLogic;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IStateEventMap;

public class SimplifiedTemporalLogicBasic
	implements ISimplifiedTemporalLogic {

	private IDataEventMap _dataEventMap;
	private IOslFormula _formula;
	private IStateEventMap _stateEventMap;

	public SimplifiedTemporalLogicBasic() {
	}

	public SimplifiedTemporalLogicBasic(IDataEventMap dataEventMap,
			IOslFormula formula, IStateEventMap stateEventMap) {
		super();
		_dataEventMap = dataEventMap;
		_formula = formula;
		_stateEventMap = stateEventMap;
	}


	@Override
	public IDataEventMap getDataEventMap() {
		return _dataEventMap;
	}

	@Override
	public IOslFormula getFormula() {
		return _formula;
	}

	@Override
	public IStateEventMap getStateEventMap() {
		return _stateEventMap;
	}


	// I doubt that these setters are necessary. Use an appropriate constructor instead.
	@Deprecated
	public void setDataEventMap(IDataEventMap dataEventMap) {
		_dataEventMap = dataEventMap;
	}

	// I doubt that these setters are necessary. Use an appropriate constructor instead.
	@Deprecated
	public void setFormula(IOslFormula formula) {
		_formula = formula;
	}

	// I doubt that these setters are necessary. Use an appropriate constructor instead.
	@Deprecated
	public void setStateEventMap(IStateEventMap stateEventMap) {
		_stateEventMap = stateEventMap;
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof SimplifiedTemporalLogicBasic) {
			SimplifiedTemporalLogicBasic o = (SimplifiedTemporalLogicBasic)obj;
			isEqual = Objects.equals(_dataEventMap, o._dataEventMap)
					&& Objects.equals(_formula, o._formula)
					&& Objects.equals(_stateEventMap, o._stateEventMap);
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_dataEventMap, _formula, _stateEventMap);
	}

}
