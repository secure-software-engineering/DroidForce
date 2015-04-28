//package de.tum.in.i22.uc.pdp.requests;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import de.tum.in.i22.uc.cm.datatypes.basic.ResponseBasic;
//import de.tum.in.i22.uc.cm.datatypes.basic.StatusBasic;
//import de.tum.in.i22.uc.cm.datatypes.basic.StatusBasic.EStatus;
//import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
//import de.tum.in.i22.uc.cm.datatypes.interfaces.IResponse;
//
///**
// * 
// * @author Florian Kelbert & Enrico Lovat
// * 
// */
//public class NotifyEventPdpRequest extends PdpRequest<IResponse> {
//	private final IEvent _event;
//	private final boolean _sync;
//
//	private static Logger log = LoggerFactory
//			.getLogger(NotifyEventPdpRequest.class);
//
//	public NotifyEventPdpRequest(IEvent event, boolean sync) {
//		log.info("NotifyEventPdpRequest for event " + event);
//		_event = event;
//		_sync = sync;
//	}
//
//	public NotifyEventPdpRequest(IEvent event) {
//		this(event, false);
//	}
//
//	@Override
//	public IResponse process(PdpProcessor processor) {
//		if (_sync) {
//			return processor.notifyEventSync(_event);
//		} else {
//			processor.notifyEventAsync(_event);
//			return new ResponseBasic(new StatusBasic(EStatus.ALLOW), null, null);
//		}
//	}
//
//}
