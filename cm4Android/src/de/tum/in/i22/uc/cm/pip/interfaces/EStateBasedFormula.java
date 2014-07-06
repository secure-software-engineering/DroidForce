package de.tum.in.i22.uc.cm.pip.interfaces;

public enum EStateBasedFormula {
	IS_NOT_IN,
	IS_ONLY_IN,
	IS_COMBINED_WITH;

	public static EStateBasedFormula from(String s) {
		switch(s.toLowerCase()) {
			case "isnotin":
				return IS_NOT_IN;
			case "isonlyin":
				return IS_ONLY_IN;
			case "iscombinedwith":
				return IS_COMBINED_WITH;
		}
		return null;
	}
}
