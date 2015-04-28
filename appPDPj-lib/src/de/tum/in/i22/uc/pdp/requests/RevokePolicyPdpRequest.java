//package de.tum.in.i22.uc.pdp.requests;
//
//import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
//
//public class RevokePolicyPdpRequest extends PdpRequest<IStatus> {
//	private final String _policyName;
//
//	public RevokePolicyPdpRequest(String policyName) {
//		_policyName= policyName;
//	}
//
//	@Override
//	public IStatus process(PdpProcessor processor) {
//		return processor.revokePolicy(_policyName);
//	}
//
//}
