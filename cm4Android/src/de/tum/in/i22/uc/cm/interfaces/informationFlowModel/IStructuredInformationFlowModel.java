package de.tum.in.i22.uc.cm.interfaces.informationFlowModel;

import java.util.Map;
import java.util.Set;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IData;

public interface IStructuredInformationFlowModel {

	void reset();

	/**
	 * Simulation step: push. Stores the current IF state, if not already stored
	 * 
	 * @return true if the state has been successfully pushed, false otherwise
	 */
	void push();

	/**
	 * Simulation step: pop. Restore a previously pushed IF state, if any.
	 * 
	 * @return true if the state has been successfully restored, false otherwise
	 */
	void pop();

	/**
	 * This method takes as parameter a list of pairs (label - set of data) that
	 * represents the structure to be associated to a new structured data item,
	 * which should be returned. The behavior is to add another entry in our
	 * _structureMap table where a new IData is associated to the structure
	 * given as parameter.
	 * 
	 * The new data item associated to the structured is returned.
	 * 
	 */
	IData newStructuredData(Map<String, Set<IData>> structure);

	/**
	 * This method takes as parameter a data item and returns the structure
	 * associated to it. If no structure for it exists, then the
	 * <code>null</code> value is returned.
	 */
	Map<String, Set<IData>> getStructureOf(IData data);

	/**
	 * This method receives a (structured) data item in input and returns the
	 * list of all the structured and non-structured data-items it corresponds
	 * to. If the initial item is not structured, this method returns only it.
	 * 
	 * Because every structured data-item is freshly created, it is not possible
	 * to have circular dependency that would lead to a loop.
	 * 
	 */
	Set<IData> flattenStructure(IData data);

	String niceString();

	String toString();

}