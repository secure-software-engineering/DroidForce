//package de.tum.in.i22.uc.pdp.requests;
//
//import de.tum.in.i22.uc.cm.datatypes.basic.PxpSpec;
//
//public class RegisterPxpPdpRequest extends PdpRequest<Boolean> {
//	private final PxpSpec _pxp;
//
//	public RegisterPxpPdpRequest(PxpSpec pxp) {
//		_pxp = pxp;
//	}
//
//	@Override
//	public Boolean process(PdpProcessor processor) {
//		//TODO:add nullness checks
//		return processor.registerPxp(_pxp);
//	}
//
//}
