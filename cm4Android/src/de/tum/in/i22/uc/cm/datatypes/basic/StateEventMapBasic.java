package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IStateEventMap;

public class StateEventMapBasic
	implements IStateEventMap {

	private final Map<String, IEvent> _map;

	public StateEventMapBasic(Map<String, IEvent> map) {
		super();
		_map = map;
	}


	@Override
	public Map<String, IEvent> getMap() {
		return Collections.unmodifiableMap(_map);
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof StateEventMapBasic) {
			isEqual = Objects.equals(_map, ((StateEventMapBasic)obj)._map);
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_map);
	}

}
