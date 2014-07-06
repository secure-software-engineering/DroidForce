package de.tum.in.i22.uc.pdp.core.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.cm.datatypes.basic.EventBasic;
import de.tum.in.i22.uc.cm.datatypes.basic.ResponseBasic;
import de.tum.in.i22.uc.cm.datatypes.basic.StatusBasic;
import de.tum.in.i22.uc.cm.datatypes.basic.StatusBasic.EStatus;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IResponse;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IStatus;
import de.tum.in.i22.uc.pdp.PxpManager;

/**
 * Decision is the object produced by the PDP as a result of an event. It
 * contains information about permissiveness of the event and desired actions to
 * be performed.
 */
public class Decision implements java.io.Serializable {
	private static Logger log = LoggerFactory.getLogger(Decision.class);

	private static final long serialVersionUID = 4922446035665121547L;

	// public static final Decision RESPONSE_ALLOW =new Decision("ALLOW",
	// Constants.AUTHORIZATION_ALLOW);
	// public static final Decision RESPONSE_INHIBIT =new Decision("INHIBIT",
	// Constants.AUTHORIZATION_INHIBIT);

	private IPdpAuthorizationAction _mAuthorizationAction;

	/** 'optional' executeActions processed by PEP */
	private ArrayList<IPdpExecuteAction> _mExecuteActions = new ArrayList<IPdpExecuteAction>();
	private PxpManager _pxpManager;

	public Decision(IPdpAuthorizationAction authAction, PxpManager pxpManager) {
		_mAuthorizationAction = authAction;
		_pxpManager = pxpManager;
	}

	public IPdpAuthorizationAction getAuthorizationAction() {
		return _mAuthorizationAction;
	}

	public void setAuthorizationAction(
			IPdpAuthorizationAction mAuthorizationAction) {
		this._mAuthorizationAction = mAuthorizationAction;
	}

	public ArrayList<IPdpExecuteAction> getExecuteActions() {
		return _mExecuteActions;
	}

	public void setExecuteActions(ArrayList<IPdpExecuteAction> mExecuteActions) {
		this._mExecuteActions = mExecuteActions;
	}

	public void addExecuteAction(IPdpExecuteAction mExecuteActionTmp) {
		_mExecuteActions.add(mExecuteActionTmp);
	}

	public void processMechanism(IPdpMechanism mech, Event curEvent) {
		log.debug("Processing mechanism={} for decision",
				mech.getMechanismName());

		IPdpAuthorizationAction curAuthAction = mech.getAuthorizationAction();
		if (this.getAuthorizationAction().getType() == Constants.AUTHORIZATION_ALLOW) {
			log.debug("Decision still allowing event, processing mechanisms authActions");
			do {
				log.debug("Processing authorizationAction {}",
						curAuthAction.getName());
				if (curAuthAction.getType() == Constants.AUTHORIZATION_ALLOW) {
					log.debug("Executing specified executeActions: {}",
							curAuthAction.getExecuteActions().size());
					boolean executionReturn = false;
					if (curAuthAction.getExecuteActions().size() == 0)
						executionReturn = true;
					for (IPdpExecuteAction execAction : curAuthAction
							.getExecuteActions()) {
						log.debug("Executing [{}]", execAction.getName());

						// TODO: Execution should be forwarded to appropriate
						// execution instance!
						executionReturn = _pxpManager.execute(execAction, true);
					}

					if (!executionReturn) {
						log.warn("Execution failed; continuing with fallback authorization action (if present)");
						curAuthAction = curAuthAction.getFallback();
						if (curAuthAction == null) {
							log.warn("No fallback present; implicit INHIBIT");
							this.getAuthorizationAction().setType(
									Constants.AUTHORIZATION_INHIBIT);
							break;
						}
						continue;
					}

					log.debug("All specified execution actions executed successfully!");
					this.getAuthorizationAction().setType(
							curAuthAction.getType());
					break;
				} else {
					log.debug(
							"Authorization action={} requires inhibiting event; adjusting decision",
							curAuthAction.getName());
					this.getAuthorizationAction().setType(
							Constants.AUTHORIZATION_INHIBIT);
					break;
				}
			} while (true);
		}

		if (this.getAuthorizationAction().getType() == Constants.AUTHORIZATION_INHIBIT) {
			log.debug("Decision requires inhibiting event; adjusting delay");
			this.getAuthorizationAction().setDelay(
					Math.max(this.getAuthorizationAction().getDelay(),
							curAuthAction.getDelay()));
		} else {
			log.debug("Decision allows event; copying modifiers (if present)");
			// TODO: modifier collision is not resolved here!
			for (Param<?> curParam : curAuthAction.getModifiers())
				this.getAuthorizationAction().addModifier(curParam);
		}

		List<IPdpExecuteAction> asyncActions = mech.getExecuteAsyncActions();
		if (asyncActions == null)
			return;
		log.debug("Processing asynchronous executeActions ({})",
				asyncActions.size());
		for (IPdpExecuteAction execAction : asyncActions) {
			if (execAction.getProcessor().equals("pep")) {
				log.debug("Copying executeAction {} for processing by pep",
						execAction.getName());
				this.addExecuteAction(execAction);
			} else {
				log.debug("Execute asynchronous action [{}]",
						execAction.getName());
				_pxpManager.execute(execAction, false);
			}
		}

	}

	@Override
	public String toString() {
		if (_mAuthorizationAction == null && _mExecuteActions == null)
			return "Decision: null";

		String str = "Decision: ";
		if (_mAuthorizationAction == null)
			str += "[]";
		else
			str += this._mAuthorizationAction.toString();

		str += "; optional executeActions: [";
		for (IPdpExecuteAction a : _mExecuteActions)
			str += a.toString();
		str += "]";

		return str;
	}

	public IResponse getResponse() {
		// Convert an (IESE) Decision object into a (TUM) Response
		IStatus status;

		try {
			if (getAuthorizationAction().getAuthorizationAction()) {
				if (getAuthorizationAction().getModifiers() != null
						&& getAuthorizationAction().getModifiers().size() != 0)
					status = new StatusBasic(EStatus.MODIFY);
				else
					status = new StatusBasic(EStatus.ALLOW);
			} else {
				status = new StatusBasic(EStatus.INHIBIT);
			}
		} catch (Exception e) {
			status = new StatusBasic(EStatus.ERROR,
					"PDP returned wrong status (" + e + ")");
		}

		List<IEvent> list = new ArrayList<IEvent>();

		for (IPdpExecuteAction ea : getExecuteActions()) {
			Event e = new Event(ea.getName(), true);
			for (Param<?> p : ea.getParams())
				e.addParam(p);
			list.add(e.toIEvent());
			// TODO: take care of processor. for the time being ignored by TUM
		}

		List<Param<?>> modifiedParameters = getAuthorizationAction().getModifiers();
		Map<String,String> modifiedParamI = new HashMap<String,String>();
		
		for (Param<?> p : modifiedParameters){
			modifiedParamI.put(p.getName(), p.getValue().toString());
		}
		
		IEvent modifiedEvent = new EventBasic("triggerEvent", modifiedParamI);
		IResponse res = new ResponseBasic(status, list, modifiedEvent);

		return res;
	}
}
