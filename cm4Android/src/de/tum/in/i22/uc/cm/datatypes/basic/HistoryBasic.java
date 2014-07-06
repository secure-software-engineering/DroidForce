package de.tum.in.i22.uc.cm.datatypes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IHistory;

public class HistoryBasic implements IHistory {

	private final List<IEvent> _trace;

	public HistoryBasic(List<IEvent> trace) {
		super();
		_trace = trace;
	}

	@Override
	public List<IEvent> getTrace() {
		return Collections.unmodifiableList(_trace);
	}


	@Override
	public boolean equals(Object obj) {
		boolean isEqual = false;
		if (obj instanceof HistoryBasic) {
			isEqual = Objects.equals(_trace, ((HistoryBasic) obj)._trace);
		}
		return isEqual;
	}
}
