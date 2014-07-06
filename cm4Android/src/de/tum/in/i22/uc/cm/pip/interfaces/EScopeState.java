package de.tum.in.i22.uc.cm.pip.interfaces;

public enum EScopeState {
	OPEN, CLOSED, UNKNOWN;

	public static EScopeState from(String str) {
		if (str == null)
			return UNKNOWN;
		switch (str.trim().toLowerCase()) {
		case "open":
			return OPEN;
		case "closed":
			return CLOSED;
		default:
			return UNKNOWN;
		}
	}
}
