//package de.tum.in.i22.uc.pdp.requests;
//
//import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
//
//public class RevokeMechanismPdpRequest extends PdpRequest<IStatus> {
//	private final String _policyName;
//	private final String _mechName;
//
//	public RevokeMechanismPdpRequest(String policyName, String mechName) {
//		_policyName= policyName;
//		_mechName= mechName;
//
//	}
//
//	@Override
//	public IStatus process(PdpProcessor processor) {
//		return processor.revokeMechanism(_policyName, _mechName);
//	}
//
//}
