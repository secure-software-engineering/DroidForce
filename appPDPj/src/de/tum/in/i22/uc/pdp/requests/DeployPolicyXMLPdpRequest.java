//package de.tum.in.i22.uc.pdp.requests;
//
//import de.tum.in.i22.uc.cm.datatypes.basic.XmlPolicy;
//import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
//
//public class DeployPolicyXMLPdpRequest extends PdpRequest<IStatus> {
//	private final XmlPolicy _xmlPolicy;
//
//	public DeployPolicyXMLPdpRequest(XmlPolicy xmlPolicy) {
//		_xmlPolicy= xmlPolicy;
//	}
//
//	@Override
//	public IStatus process(PdpProcessor processor) {
//		return processor.deployPolicyXML(_xmlPolicy);
//	}
//}
