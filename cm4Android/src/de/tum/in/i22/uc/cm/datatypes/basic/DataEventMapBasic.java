package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IData;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IDataEventMap;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;

public class DataEventMapBasic implements IDataEventMap {
	private final Map<IData, IEvent> _map;

	public DataEventMapBasic(Map<IData, IEvent> map) {
		_map = map;
	}


	@Override
	public Map<IData, IEvent> getMap() {
		return Collections.unmodifiableMap(_map);
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof DataEventMapBasic) {
			isEqual = Objects.equals(_map, ((DataEventMapBasic) obj)._map);
		}
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_map);
	}
}
