package de.tum.in.i22.uc.cm.pip.interfaces;

public enum EBehavior {
	IN, OUT, INTRA, INTRAIN, INTRAOUT, UNKNOWN;

	public static EBehavior from(String str) {
		if (str == null)
			return UNKNOWN;
		switch (str.trim().toLowerCase()) {
		case "in":
			return IN;
		case "out":
			return OUT;
		case "intra":
			return INTRA;
		case "intrain":
		case "intra-in":
			return INTRAIN;
		case "intraout":
		case "intra-out":
			return OUT;
		default:
			return UNKNOWN;
		}
	}
}
