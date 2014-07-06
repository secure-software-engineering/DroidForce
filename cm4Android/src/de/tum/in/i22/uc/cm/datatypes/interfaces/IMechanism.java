package de.tum.in.i22.uc.cm.datatypes.interfaces;

public interface IMechanism {
	public String getMechanismName();
	public ICondition getCondition();
	public Object getResponse();
	public IHistory getState();
	public IEvent getTriggerEvent();
	public String toXML();
}
