package de.tum.in.i22.uc.cm.datatypes.interfaces;

import java.util.Map;

public interface IEvent {
	/**
	 * This event's name.
	 * @return
	 */
	public String getName();

	/**
	 * The PEP that issued this event
	 * @return the identifier of the PEP that issued this event
	 */
	public String getPep();

	/**
	 * Event parameters.
	 * @return Empty or non-empty map containing the parameters.
	 */
	public Map<String, String> getParameters();

	/**
	 * @return Timestamp which is inserted when the event is received.
	 */
	public long getTimestamp();

	/**
	 *
	 * @return true if the event is actual.
	 */
	public boolean isActual();


	/**
	 * If this method returns true and if the PDP allows this event to be executed,
	 * then the PIP and the PDP will immediately be notified about the actual event.
	 * They will not wait for the PEP to signal it. In fact, if this method
	 * returns true, the PEP should never send the actual event.
	 *
	 * Notice, that if this method returns true, {@link IEvent#isActual()} should
	 * return false.
	 *
	 * @return
	 */
	public boolean allowImpliesActual();
}
