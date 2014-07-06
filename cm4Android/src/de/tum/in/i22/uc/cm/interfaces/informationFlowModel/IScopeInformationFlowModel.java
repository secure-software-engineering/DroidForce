package de.tum.in.i22.uc.cm.interfaces.informationFlowModel;

import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IContainer;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IEvent;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IScope;
import de.tum.in.i22.uc.cm.pip.interfaces.EBehavior;
import de.tum.in.i22.uc.cm.pip.interfaces.EScopeState;
import de.tum.in.i22.uc.cm.pip.interfaces.IEventHandler;

public interface IScopeInformationFlowModel {

	public abstract String toString();

	public abstract void reset();

	/**
	 * Simulation step: push. Stores the current IF state, if not already stored
	 * 
	 * @return true if the state has been successfully pushed, false otherwise
	 */
	public abstract void push();

	/**
	 * Simulation step: pop. Restore a previously pushed IF state, if any.
	 * 
	 * @return true if the state has been successfully restored, false otherwise
	 */
	public abstract void pop();

	/**
	 * opens a new scope.
	 * 
	 * @param the
	 *            new scope to open
	 * @return true if the scope is not already opened. false otherwise.
	 * 
	 */
	public abstract boolean openScope(IScope scope);

	/**
	 * Close a specific scope.
	 * 
	 * @param the
	 *            scope to be closed
	 * @return true if the scope is successfully closed. false otherwise.
	 * 
	 */
	public abstract boolean closeScope(IScope scope);

	/**
	 * Checks whether a specific scope has been opened. Note that the scope can
	 * be under-specified with respect to the matching element in the set.
	 * 
	 * @param the
	 *            (possibly under-specified) scope to be found
	 * @return true if the scope is in the set. false otherwise.
	 * 
	 */
	public abstract boolean isScopeOpened(IScope scope);

	/**
	 * Returns the only element that should match the (possibly under-specified)
	 * scope in the set of currently opened scopes. Note that if more than one
	 * active (i.e. opened but not closed) scope matches the parameter, the
	 * method returns null. Similarly, if no scope is found, the method returns
	 * null.
	 * 
	 * There must exists only one matching otherwise the information about the
	 * scope are not enough to identify to which scope a certain event belongs
	 * 
	 * @param the
	 *            (possibly under-specified) scope to be found
	 * @return the opened scope, if found. null if more than one match or no
	 *         match is found.
	 * 
	 */
	public abstract IScope getOpenedScope(IScope scope);

	/**
	 * XBehav function described in the Cross-layer paper. Returns the behavior
	 * of the given event in the current state. If the behavior is a cross-layer
	 * behavior (e.g. IN or OUT) then it also returns w.r.t. which scope.
	 * 
	 * @param event
	 *            event to be checked in current state
	 * @return the behavior and, in case of cross-layer behaviors, the
	 *         respective scope
	 * 
	 */
	public abstract Entry<EBehavior, IScope> XBehav(IEventHandler eventHandler);

	/**
	 * XDelim function described in the Cross-layer paper.Given an event returns
	 * the set of scopes opened or closed by it.
	 * 
	 * @param event
	 *            the event
	 * @return the set of scopes modified by it, together with the modifier
	 *         (open or close)
	 */
	public abstract Set<Entry<IScope, EScopeState>> XDelim(IEventHandler eventHandler);

	/**
	 * XAlias function described in the Cross-layer paper. Given an event
	 * returns the new state of the cross-layer aliases
	 * 
	 * @param event
	 *            the event that (possibly) modifies the cross-layer aliases
	 * 
	 * @return the new cross-layer alias function
	 */
	public abstract Map<IContainer, Set<IContainer>> XAlias(IEventHandler eventHandler);

	public abstract String niceString();

}