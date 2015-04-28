package de.tum.in.i22.uc.pdp.core.shared;

import java.util.List;

public interface IPdpAuthorizationAction {

	boolean getAuthorizationAction();

	List<IPdpExecuteAction> getExecuteActions();

	List<Param<?>> getModifiers();

	void setExecuteActions(List<IPdpExecuteAction> executeActions);

	void setModifiers(List<Param<?>> modifiers);

	void addModifier(Param<?> parameter);

	void addExecuteAction(IPdpExecuteAction executeAction);

	long getDelay();

	void setDelay(long delay);

	boolean getType();

	void setType(boolean type);

	String getName();

	void setName(String name);

	IPdpAuthorizationAction getFallback();

	void setFallback(IPdpAuthorizationAction fallback);

	String getFallbackName();

	void setFallbackName(String fallbackName);

}