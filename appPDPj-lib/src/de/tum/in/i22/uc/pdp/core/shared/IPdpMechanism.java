package de.tum.in.i22.uc.pdp.core.shared;

import java.util.ArrayList;
import java.util.List;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IResponse;
import de.tum.in.i22.uc.pdp.core.PolicyDecisionPoint;

public interface IPdpMechanism {

	IPdpAuthorizationAction getAuthorizationAction();

	public long getTimestepSize();

	public void setTimestepSize(long timestepSize);

	void setAuthorizationAction(IPdpAuthorizationAction mAuthorizationAction);

	List<IPdpExecuteAction> getExecuteActions();

	public List<IPdpExecuteAction> getExecuteAsyncActions();

	void setExecuteActions(ArrayList<IPdpExecuteAction> mExecuteActions);

	void addExecuteAction(IPdpExecuteAction mExecuteActionTmp);

	IResponse getResponse();

	public String getMechanismName();

	public boolean revoke();
	
	public PolicyDecisionPoint getPolicyDecisionPoint();

}