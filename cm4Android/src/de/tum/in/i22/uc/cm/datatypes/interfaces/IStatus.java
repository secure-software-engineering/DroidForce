package de.tum.in.i22.uc.cm.datatypes.interfaces;

import de.tum.in.i22.uc.cm.datatypes.basic.StatusBasic.EStatus;

public interface IStatus {
	public EStatus getEStatus();
	public String getErrorMessage();
	boolean isStatus(EStatus status);
}
