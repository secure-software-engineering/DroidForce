package de.tum.in.i22.uc.pdp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.datatypes.basic.EventBasic;
import de.tum.in.i22.uc.cm.datatypes.basic.PxpSpec;
import de.tum.in.i22.uc.cm.datatypes.basic.StatusBasic.EStatus;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
import de.tum.in.i22.uc.cm.distribution.IPLocation;
import de.tum.in.i22.uc.pdp.core.shared.IPdpExecuteAction;
import de.tum.in.i22.uc.pdp.core.shared.Param;

/**
 * This class manages the connection with the Pxp. It handles the registrations
 * of Pxp components and it dispatches the to-be-executed action to the proper
 * Pxp.
 *
 * @author Enrico Lovat
 *
 */

public class PxpManager {
	private static Logger _logger = LoggerFactory.getLogger(PxpManager.class);
	private static PxpManager _instance;
	public static HashMap<String, PxpSpec> pxpSpec = new HashMap<>();

//	public static PxpManager getInstance() {
//		/*
//		 * This implementation may seem odd, overengineered, redundant, or all
//		 * of it. Yet, it is the best way to implement a thread-safe singleton,
//		 * cf.
//		 * http://www.journaldev.com/171/thread-safety-in-java-singleton-classes
//		 * -with-example-code -FK-
//		 */
//		if (_instance == null) {
//			synchronized (PxpManager.class) {
//				if (_instance == null)
//					_instance = new PxpManager();
//			}
//		}
//		return _instance;
//	}

	public boolean execute(IPdpExecuteAction execAction, boolean synchronous) {
		_logger.info("[PXPStub] Executing "+(synchronous==true?"":"a")+"synchronous action {} with parameters: {}",
				execAction.getName(), execAction.getParams());

		String pxpId = execAction.getId();
		IStatus res = null;
		if (pxpId != null) {
			if (pxpSpec.containsKey(pxpId)) {
				PxpSpec pxp = pxpSpec.get(pxpId);

//				try {
//					Any2PxpClient client = new ThriftClientFactory().createAny2PxpClient(new IPLocation(pxp.getIp(), pxp.getPort()));
//
//					try {
//						client.connect();
//					} catch (Exception e) {
//						throw new RuntimeException(e.getMessage(), e);
//					}
//
//					List<IEvent> listOfEventsToBeExecuted = new LinkedList<IEvent>();
//					Map<String, String> par = new HashMap<String, String>();
//
//					for (Param p : execAction.getParams()){
//						par.put(p.getName(),p.getValue().toString());
//					}
//
//					// Parameter olderthan is added as a string parameter
//					// instead of short
//
//					listOfEventsToBeExecuted.add(new EventBasic(execAction.getName(), par));
//
//					if (synchronous==true) res = client.executeSync(listOfEventsToBeExecuted);
//					else client.executeAsync(listOfEventsToBeExecuted);
//
//				} catch (NumberFormatException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

			}
		}
		if (res == null)
			return false;
		else
			return res.isStatus(EStatus.OKAY);
	}

	public boolean registerPxp(PxpSpec pxp) {
		boolean b = false;
		if (!pxpSpec.containsKey(pxp.getId())) {
			b = pxpSpec.put(pxp.getId(), pxp) == null;
			_logger.info("PXP "+pxp.getId()+" registered.");
		}
		return b;
	}
}
