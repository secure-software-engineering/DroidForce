//package de.tum.in.i22.uc.pdp.requests;
//
//import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
//
//public class DeployPolicyURIPdpRequest extends PdpRequest<IStatus> {
//	private final String _policyPath;
//
//	public DeployPolicyURIPdpRequest(String policyPath) {
//		_policyPath= policyPath;
//	}
//
//	@Override
//	public IStatus process(PdpProcessor processor) {
//		return processor.deployPolicyURI(_policyPath);
//	}
//
//}
