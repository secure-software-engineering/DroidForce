package de.tum.in.i22.uc.cm.interfaces.informationFlowModel;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
import de.tum.in.i22.uc.cm.pip.EInformationFlowModel;

public interface IInformationFlowModel extends IBasicInformationFlowModel, IScopeInformationFlowModel, IStructuredInformationFlowModel {

	public abstract boolean isEnabled(EInformationFlowModel ifm);

	public abstract boolean isSimulating();

	public abstract IStatus startSimulation();

	public abstract IStatus stopSimulation();

	@Override
	public abstract void reset();

	@Override
	public abstract String niceString();

}