package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.ICondition;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IHistory;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IMechanism;

public class MechanismBasic implements IMechanism {
	private ICondition _condition;
	private String _mechanismName;
	private Object _response;
	private IHistory _state;
	private IEvent _triggerEvent;
	private String _xml;

	public MechanismBasic() {
	}

	public MechanismBasic(String xml) {
		_xml = xml;
		//TODO implement: create mechanism from xml
	}

	@Override
	public String toXML() {
		return _xml;
	}


	@Override
	public String getMechanismName() {
		return _mechanismName;
	}

	@Override
	public ICondition getCondition() {
		return _condition;
	}

	@Override
	public Object getResponse() {
		return _response;
	}

	@Override
	public IHistory getState() {
		return _state;
	}

	@Override
	public IEvent getTriggerEvent() {
		return _triggerEvent;
	}

	// I doubt that these setters are necessary. Use an appropriate constructor instead.
	@Deprecated
	public void setCondition(ICondition condition) {
		_condition = condition;
	}



	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof MechanismBasic) {
			MechanismBasic o = (MechanismBasic)obj;
			isEqual = Objects.equals(_condition, o._condition)
				&& Objects.equals(_mechanismName, o._mechanismName)
				&& Objects.equals(_response, o._response)
				&& Objects.equals(_state, o._state)
				&& Objects.equals(_triggerEvent, o._triggerEvent);
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_condition, _mechanismName, _response, _state, _triggerEvent);
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this)
				.add("_condition", _condition)
				.add("_mechanismName", _mechanismName)
				.add("_response", _response)
				.add("_state", _state)
				.add("_triggerEvent", _triggerEvent)
				.toString();
	}



}
