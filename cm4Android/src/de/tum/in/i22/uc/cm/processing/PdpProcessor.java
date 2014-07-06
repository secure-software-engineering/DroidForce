//package de.tum.in.i22.uc.cm.processing;
//
//import de.tum.in.i22.uc.cm.distribution.Location;
//import de.tum.in.i22.uc.cm.interfaces.IAny2Pdp;
//import de.tum.in.i22.uc.cm.interfaces.IAny2Pip;
//import de.tum.in.i22.uc.cm.interfaces.IAny2Pmp;
//
///**
// * An abstract PDP processor, extending {@link Processor} to take interfaces to
// * a {@link PipProcessor} and to a {@link PmpProcessor} as an argument.
// *
// * @author Florian Kelbert
// *
// */
//public abstract class PdpProcessor extends Processor<PipProcessor, PmpProcessor> implements IAny2Pdp {
//
//	public PdpProcessor(Location location) {
//		super(location);
//	}
//
//	/**
//	 * Returns the PMP associated with this PDP.
//	 *
//	 * @return
//	 */
//	protected IAny2Pmp getPmp() {
//		return _iface2;
//	}
//
//	/**
//	 * Returns the PIP associated with this PDP.
//	 *
//	 * @return
//	 */
//	protected IAny2Pip getPip() {
//		return _iface1;
//	}
//
//}
