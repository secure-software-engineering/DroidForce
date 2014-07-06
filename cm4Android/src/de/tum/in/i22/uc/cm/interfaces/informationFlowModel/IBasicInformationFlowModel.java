package de.tum.in.i22.uc.cm.interfaces.informationFlowModel;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import de.tum.in.i22.uc.cm.datatypes.interfaces.IContainer;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IData;
import de.tum.in.i22.uc.cm.datatypes.interfaces.IName;

public interface IBasicInformationFlowModel {

	/**
	 * Removes data object.
	 *
	 * @param data
	 */
	public abstract void remove(IData data);

	/**
	 * Removes the given container completely by deleting
	 * associated names, aliases, and data.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param cont the container to be removed.
	 */
	public abstract void remove(IContainer cont);

	/**
	 * Removes all data from the specified container
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param container the container of which the data is to be removed.
	 */
	public abstract void emptyContainer(IContainer container);

	/**
	 * Removes all data from the container identified by the given container name.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param containerName a name of the container that is to be emptied.
	 */
	public abstract void emptyContainer(IName containerName);

	/**
	 * Adds an alias relation from one container to another.
	 *
	 * @return
	 */
	public abstract void addAlias(IContainer fromContainer, IContainer toContainer);

	public abstract void addAlias(IName fromContainerName, IName toContainerName);

	/**
	 * Removes the alias from fromContainer to toContainer.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param fromContainer the container of which the alias is outgoing
	 * @param toContainer the container of which the alias is incoming
	 */
	public abstract void removeAlias(IContainer fromContainer, IContainer toContainer);

	/**
	 * Returns an immutable view onto the set of all aliases *from* the specified container.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param container the container whose outgoing aliases will be returned.
	 * @return An immutable view onto the set of all aliases *from* the specified container.
	 */
	public abstract Collection<IContainer> getAliasesFrom(IContainer container);

	/**
	 * Returns the reflexive, transitive closure of the alias function for
	 * container with id containerId.
	 *
	 * @param containerId
	 * @return
	 */
	public abstract Set<IContainer> getAliasTransitiveReflexiveClosure(IContainer container);

	/**
	 * Removes all aliases that start from the container with given id.
	 *
	 * @param fromContainerId
	 * @return
	 */
	public abstract void removeAllAliasesFrom(IContainer fromContainer);

	/**
	 * Removes all aliases that end in the container with the given id.
	 *
	 * @param toContainerId
	 * @return
	 */
	public abstract void removeAllAliasesTo(IContainer toContainer);

	/**
	 * Returns an immutable view onto the set of all aliases *to* the specified container.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param container the container whose incoming aliases will be returned.
	 * @return An immutable view onto the set of all aliases *to* the specified container.
	 */
	public abstract Set<IContainer> getAliasesTo(IContainer container);

	/**
	 * Returns the non-reflexive transitive alias closure of the specified container.
	 * The resulting set will NOT contain the specified container.
	 * @param container
	 * @return
	 */
	public abstract Set<IContainer> getAliasTransitiveClosure(IContainer container);

	/**
	 * Adds the given data to the given container. If data
	 * or container is null, nothing will happen.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param data the data to add
	 * @param container to which container the data is added.
	 */
	public abstract void addData(IData data, IContainer container);

	/**
	 * Removes the given data from the given container.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param data the data to remove
	 * @param container the container from which the data will be removed
	 * @return true, if the data has been removed
	 */
	public abstract void removeData(IData data, IContainer container);

	/**
	 * Returns an immutable view onto the set of data within the given container.
	 * In doubt, returns an empty set; never null.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param container the container of which we want to get the data
	 * @return an immutable view onto the set of data items stored in the given container
	 */
	public abstract Set<IData> getData(IContainer container);

	/**
	 * Returns the data contained in the container identified by the given name,
	 * cf. {@link #getData(IContainer)}.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param containerName a name of the container of which the containing data will be returned.
	 * @return an immutable view onto the set of data within the container
	 */
	public abstract Set<IData> getData(IName containerName);

	/**
	 * Copies all data contained in the container identified by srcContainerName to
	 * the container identified by dstContainerName.
	 *
	 * @param srcContainerName
	 * @param dstContainerName
	 * @return true if both containers existed and data (possibly none, if fromContainer was empty) was copied.
	 */
	public abstract boolean copyData(IName srcContainerName, IName dstContainerName);

	public abstract boolean copyData(IContainer srcContainer, IContainer dstContainer);

	public abstract void addDataTransitively(Collection<IData> data, IName dstContainerName);

	public abstract void addDataTransitively(Collection<IData> data, IContainer dstContainer);

	/**
	 * Returns all containers in which the specified data is in
	 *
	 * ~ Double checked, 2014/04/10. FK.
	 *
	 * @param data the data whose containers are returned.
	 * @return The set of containers containing the specified data.
	 */
	public abstract Set<IContainer> getContainers(IData data);

	/**
	 * Returns all containers of the specified type
	 * in which the specified data is in.
	 *
	 * ~ Double checked, 2014/04/11. FK.
	 *
	 * @param data the data whose containers are returned.
	 * @param type the type of the container to be returned
	 * @return all containers of type <T> containing the specified data.
	 */
	public abstract <T extends IContainer> Set<T> getContainers(IData data, Class<T> type);

	public abstract void addData(Collection<IData> data, IContainer container);

	/**
	 * Makes the given name point to the given container.
	 *
	 * If the given name was already assigned to another container,
	 * this old name/container mapping is overwritten. If this was the
	 * last name for that container, the corresponding container is deleted.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param name the new name for the given container.
	 * @param container the container for which the new name applies.
	 */
	public abstract void addName(IName name, IContainer container);

	/**
	 * Adds an additional name, newName, for the container that is
	 * already identified by another name, oldName.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param oldName a name identifying an already existing container
	 * @param newName the additional new name for the container identified by oldName.
	 */
	public abstract void addName(IName oldName, IName newName);

	/**
	 * Removes the name.
	 *
	 * @param name
	 * @return
	 */
	public abstract void removeName(IName name);

	/**
	 * Returns the container that is referenced by the naming name.
	 *
	 * @param name
	 * @return
	 */
	public abstract IContainer getContainer(IName name);

	/**
	 * Returns an unmodifiable view onto all containers.
	 *
	 * ~ Double checked, 2014/03/30. FK.
	 *
	 * @return an unmodifiable view onto all containers.
	 */
	public abstract Set<IContainer> getAllContainers();

	/**
	 * Returns an unmodifiable view onto all names.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @return an unmodifiable view onto all names.
	 */
	public abstract Collection<IName> getAllNames();

	/**
	 * Returns an unmodifiable view onto all names
	 * of the specified type.
	 *
	 * ~ Double checked, 2014/04/1. FK.
	 *
	 * @param the type of the names to be returned.
	 * @return an unmodifiable view onto all names of the specified type.
	 */
	public abstract <T extends IName> Collection<T> getAllNames(Class<T> type);

	/**
	 * Returns an unmodifiable view onto all names
	 * for the given container.
	 *
	 * ~ Double checked, 2014/03/14. FK.
	 *
	 * @param container the container whose names are returned.
	 * @return an unmodifiable view onto all names for the given container
	 */
	public abstract Collection<IName> getAllNames(IContainer container);

	/**
	 * Get all names of the container identified by the given containerName.
	 * It is ensured that all names within the result are of the specified type.
	 * @param containerName
	 * @param type
	 * @return
	 */
	public abstract <T extends IName> List<T> getAllNames(IName containerName, Class<T> type);

	/**
	 * Get all names of the specified container.
	 * It is ensured that all names within the result are of the specified type.
	 *
	 * ~ Double checked, 2014/04/11. FK.
	 *
	 * @param cont the {@link IContainer} whose {@link IName}s will be returned
	 * @param type the type of the {@link IName}s to be returned
	 * @return all names of type <T> of the specified container
	 */
	public abstract <T extends IName> List<T> getAllNames(IContainer cont, Class<T> type);

	/**
	 * Returns all representations that correspond to the process with pid.
	 *
	 */
	public abstract List<IName> getAllNamingsFrom(IContainer pid);

	public abstract String niceString();

	public abstract String toString();

}