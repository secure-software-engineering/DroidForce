package de.tum.in.i22.uc.cm.pip;

import java.util.HashSet;
import java.util.Set;

import de.tum.in.i22.uc.cm.settings.Settings;

public enum EInformationFlowModel {
	SCOPE,
	STRUCTURE,
	QUANTITIES;

	public static Set<EInformationFlowModel> from(String str) {
		if (str==null) return null;
		Set<EInformationFlowModel> result = new HashSet<>();

		for (String s : str.split(Settings.getInstance().getSeparator1())) {
			switch (s.trim().toLowerCase()) {
				case "scope":
					result.add(SCOPE);
					break;
				case "quantities":
					result.add(QUANTITIES);
					break;
				case "structure":
					result.add(STRUCTURE);
					break;
			}
			
		}

		return result;
	}
}
