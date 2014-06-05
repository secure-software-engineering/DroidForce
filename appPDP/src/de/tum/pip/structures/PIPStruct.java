
package de.tum.pip.structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PIPStruct {
    // counter for the unique data and container ids
    private int dataIDCounter = 0;

    private int containerIDCounter = 0;

    // the container set of the GUSM model
    private List<PIPContainer> containerSet;

    // the data set of the GUSM model
    private List<String> dataSet;

    // the storage set of the GUSM model [Container.identifier ->
    // List[Data.identifier]]
    private Map<Integer, Set> dataContainerSet;

    // the alias set of the GUSM model [Container.identifier ->
    // List[Container.identifier]]
    private Map<Integer, Set> aliasSet;

    // the naming set of the GUSM model [name -> List[Container.identifier]]
    private Map<PIPName, Integer> namingSet;

    // ToDo: Track past events (all persistent eventTraces)
    // private Map<String, Stack<PDPEvent>> eventTraces;

    /**
	 *
	 */
    public PIPStruct() {
        // TODO: Linked List May Be Better... ??
        containerSet = new ArrayList<PIPContainer>();
        dataSet = new ArrayList<String>();
        // TODO: Is HashMap, SortedMap or TreeMap?
        dataContainerSet = new HashMap<Integer, Set>();
        aliasSet = new HashMap<Integer, Set>();
        namingSet = new HashMap<PIPName, Integer>();

        // ToDo: Track past events
        // eventTraces = new Map<String, Stack<PDPEvent>>();
    }

    // Data set management functions:

    /**
     * Adds a new data item to the data set, or create a new one if a null data
     * item was passed to this function.
     */
    public String addData(String data) {
        if (dataSet.contains(data))
            return "error";

        // if (data == null) data = new PIPData();
        if (data == null) {
            data = "D" + dataIDCounter;
            dataIDCounter++;
        }

        dataSet.add(data);

        return data;
    }

    /**
     * Removes a data item form the data set.
     */
    public boolean removeData(String data) {
        // if ((data == null) | !(dataSet.contains(data))) return false;
        if (!dataSet.contains(data))
            return false;

        dataSet.remove(data);
        return true;
    }

    /**
     * Returns the data item identified by the passed data ID.
     */
    public String getDataByID(String identifier) {
        return dataSet.get(dataSet.indexOf(identifier));
    }

    /**
     * Returns true if the data set includes a data item with the passed data
     * ID, false otherwise.
     */
    public boolean hasDataByID(String identifier) {
        if (getDataByID(identifier) != null) {
            return true;
        } else {
            return false;
        }
    }

    // Container set management functions:

    /**
     * Adds a new data container to the container set, or create a new one if a
     * null container was passed to this function.
     */
    public int addContainer(PIPContainer container) {
        if (containerSet.contains(container))
            return -1;

        if (container == null)
            container = new PIPContainer();

        container.identifier = containerIDCounter;

        containerSet.add(container);

        containerIDCounter++;
        return containerIDCounter - 1;
    }

    /**
     * Removes the specified container from the container set.
     */
    public boolean removeContainer(PIPContainer container) {
        if (!(containerSet.contains(container)))
            return false;

        containerSet.remove(container);

        // ToDo: remove also entries in dataContainer list

        return true;
    }

    /**
     * Removes the container, identified by the provided container id.
     */
    public boolean removeContainer(int contID) {
        PIPContainer container = containerSet.get(contID);

        for (int i = containerSet.size(); i >= 0; i--) {
            if (container.identifier == containerSet.get(i).identifier) {
                containerSet.remove(i);
            }
        }

        return true;
    }

    /**
     * Empties the container (= its containing data items), specified via the
     * passed container ID.
     */
    public boolean emptyContainer(int contID) {
        if (!dataContainerSet.containsKey(contID))
            return false;

        dataContainerSet.get(contID).clear();

        return true;
    }

    /**
     * Returns the data container, referrenced by the provided container ID.
     */
    public PIPContainer getContainerByID(int contID) {
        return containerSet.get(contID);
    }

    /**
     * Returns true if the container set includes a data container with the
     * passed container ID, false otherwise.
     */
    public boolean hasContainerByID(int contID) {
        if (getContainerByID(contID) == null) {
            return false;
        } else {
            return true;
        }
    }

    // Alias set manipulation functions:

    /**
     * Adds an alias relation from a container to a container.
     */
    public boolean addAlias(int from, int to) {
        if (aliasSet.containsKey(from)) {
            aliasSet.get(from).add(to);
        } else {
            Set newList = new HashSet();
            newList.add(to);

            aliasSet.put(from, newList);
        }

        return true;
    }

    /**
     * Remove the alias relation identified by the tuple (from, to).
     */
    public boolean removeAlias(int from, int to) {
        if (aliasSet.containsKey(from)) {
            aliasSet.get(from).remove(to);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns all aliases of the container with the passed container ID.
     */
    public Set getAliasByID(Object contID) {
        if (aliasSet.containsKey(contID)) {
            return aliasSet.get(contID);
        } else {
            return new HashSet();
        }
    }

    /**
     * Returns the reflexive, transitive closure of the alias function for
     * container with ID contID.
     */
    public Set<Integer> getAliasClosureByID(int contID) {
        Set closure = getAliasClosure(contID, new HashSet());

        // add self to set ==> reflexive
        closure.add(contID);

        return closure;
    }

    /**
     * Removes all aliases that start from the container with container ID =
     * from.
     */
    public boolean removeAllAliasesFrom(int from) {
        if (aliasSet.containsKey(from)) {
            aliasSet.get(from).clear();
            return true;
        } else {
            return false;
        }

    }

    /**
     * Removes all aliases that end in the container with container ID = to.
     */
    public boolean removeAllAliasesTo(int to) {
        if (getAliasesTo(to).size() > 0) {
            for (Set entry : aliasSet.values()) {
                if (entry.contains(to)) {
                    aliasSet.remove(to);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns all aliases that go to the container with the passed container ID
     * to.
     */
    public Set getAliasesTo(int to) {
        Set result = new HashSet();

        for (Entry<Integer, Set> entry : aliasSet.entrySet()) {
            if (entry.getValue().contains(to))
                result.add(entry.getKey());
        }

        return result;
    }

    /**
     * Calculates the transitive reflexive closure of the alisa function.
     */
    private Set getAliasClosure(int contID, Set oldSet) {
        Set<Integer> closure = getAliasByID(contID);

        if (closure.size() == oldSet.size()) {
            return closure;
        }

        Set tempClosure = new HashSet();

        for (int cont : closure) {
            tempClosure.addAll(getAliasClosure(cont, closure));
        }

        closure.addAll(tempClosure);

        return closure;
    }

    // Naming set manipulation functions:

    /**
     * Adds an entry to the naming mapping for container contID, with the
     * naming/representation name.
     */
    public boolean addName(PIPName name, int contID) {
        if (name == null || name.name == "")
            return false;

        namingSet.put(name, contID);

        return true;
    }

    /**
     * Removes the naming/representation name from the naming set.
     */
    public boolean removeName(PIPName name) {
        if (name == null)
            return false;

        if (namingSet.containsKey(name)) {
            namingSet.remove(name);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the container that is referenced by the naming name.
     */
    public int getContainerByName(PIPName name) {
        if (name == null)
            return -1;

        for (PIPName nm : namingSet.keySet()) {
            if ((nm.name.equals(name.name)) && (nm.PID == name.PID))
                return namingSet.get(nm);
        }

        return -1;
    }

    /**
     * Returns the container that is referenced by the naming name. The search
     * is done in a less strict way, already matching if a name only partially
     * fits an entry of the naming mapping.
     */
    public int getContainerByNameRelaxed(PIPName name) {
        if (name == null)
            return -1;

        for (PIPName nm : namingSet.keySet()) {
            if ((nm.name.contains(name.name)) && (nm.PID == name.PID)) {
                return namingSet.get(nm);
            }
        }

        return -1;
    }

    /**
     * Return all namings that refer to a container wit ID contID.
     */
    public List<PIPName> getAllNames(int contID) {
        // TODO: Linked List May Be Better... ??
        List<PIPName> result = new ArrayList<PIPName>();

        if (namingSet.containsValue(contID)) {
            for (Entry<PIPName, Integer> entry : namingSet.entrySet()) {
                if (entry.getValue() == contID)
                    result.add(entry.getKey());
            }

        }

        return result;
    }

    /**
     * Returns all representations that correspond to a process with ID PID.
     */
    public List<PIPName> getAllNamingsFrom(int PID) {
        // TODO: Linked List May Be Better... ??
        List<PIPName> result = new ArrayList<PIPName>();

        for (Entry<PIPName, Integer> entry : namingSet.entrySet()) {
            if (entry.getKey().PID == PID)
                result.add(entry.getKey());
        }

        return result;
    }

    // DataContainer mapping functions:

    /**
     * Adds an new mapping between the data container containerID and the data
     * item dataID.
     */
    public boolean addDataContainer(int containerID, String dataID) {
        if (dataContainerSet.containsKey(containerID)) {
            dataContainerSet.get(containerID).add(dataID);
        } else {
            Set newDataSet = new HashSet();
            newDataSet.add(dataID);

            dataContainerSet.put(containerID, newDataSet);
        }

        return true;
    }

    /**
     * Removes an entry from the data-container mapping.
     */
    public boolean removeDataContainer(int dataID, int containerID) {
        if (dataContainerSet.containsKey(containerID)) {
            dataContainerSet.get(containerID).remove(dataID);
            return true;
        }

        return false;
    }

    /**
     * Returns all data items that are stored in the specified container.
     */
    public Set getDataInContainer(int containerID) {
        Set result = new HashSet();

        if (dataContainerSet.containsKey(containerID)) {
            result = dataContainerSet.get(containerID);
        }

        return result;
    }

    /**
     * Returns all containers that contain a data item with ID dataID.
     */
    public Set getContainerOfData(String dataID) {
        Set result = new HashSet();

        for (Entry<Integer, Set> entry : dataContainerSet.entrySet()) {
            if (entry.getValue().contains(dataID))
                result.add(entry.getKey());
        }

        return result;
    }

    /**
     * Adds a list of data items to a container.
     */
    public boolean addDataContainerList(int contID, Set dataList) {
        if (dataList == null)
            return false;

        if (dataContainerSet.containsKey(contID)) {
            dataContainerSet.get(contID).addAll(dataList);
        } else {
            dataContainerSet.put(contID, dataList);
        }

        return true;
    }

    /**
     * Returns the formated status of all mappings within one string for
     * debugging purposes.
     */
    public String printModel() {
        String dataSetString = "";
        String containerSetString = "";
        String aliasSetString = "";
        String namingSetString = "";
        String dataContainerSetString = "";

        String combinedModel = "";

        for (String data : dataSet) {
            dataSetString += data + "|";
        }
        // delete the last |
        if (dataSetString != "")
            dataSetString = dataSetString.substring(0, dataSetString.length() - 1);

        for (PIPContainer container : containerSet) {
            containerSetString += container.identifier + "|";
        }
        // delete the last |
        if (containerSetString != "")
            containerSetString = containerSetString.substring(0, containerSetString.length() - 1);

        for (Entry<Integer, Set> alias : aliasSet.entrySet()) {
            aliasSetString += "ContainerID=" + alias.getKey() + " -> ContainerIDs=("
                    + Arrays.deepToString(alias.getValue().toArray()) + ")\n";
        }

        for (Entry<PIPName, Integer> name : namingSet.entrySet()) {
            namingSetString += "Name=" + name.getKey().PID + "x" + name.getKey().name
                    + " -> ContainerID=" + name.getValue() + "\n";
        }

        for (Entry<Integer, Set> dataContainer : dataContainerSet.entrySet()) {
            dataContainerSetString += "ContainerID=" + dataContainer.getKey() + " ===> DataIDs=("
                    + Arrays.deepToString(dataContainer.getValue().toArray()) + ")\n";
        }

        for (Entry<PIPName, Integer> name : namingSet.entrySet()) {
            combinedModel += "Name=" + name.getKey().PID + "x" + name.getKey().name
                    + " -> ContainerID=" + name.getValue() + " -> DataIDs="
                    + getDataInContainer(name.getValue()).toString() + "\n";
        }

        String model = "DataSet: [" + dataSetString + "]\n++++\n" + "ContainerSet: ["
                + containerSetString + "]\n++++\n" + "AliasSet: \n[\n" + aliasSetString
                + "]\n++++\n" + "NamingSet: \n[\n" + namingSetString + "]\n++++\n"
                + "DataContainerSet: \n[\n" + dataContainerSetString + "]\n++++\n"
                + "Combined: \n[\n" + combinedModel + "]\n";

        return model;
    }

}
