
package de.tum.pip.structures;

import java.util.Hashtable;
import java.util.List;

public class PIPSemantics {

    // constants to control whether to add new containers for windows or not
    private final boolean CREATE_NEW_CONTAINER = true;

    /**
     * Updates the information flow model according to the semantics of the
     * provided event.
     */
    public int processEvent(PDPEvent incomingEvent, PIPStruct ifModel) {
        if (incomingEvent == null | ifModel == null)
            return -1;

        List<Hashtable> parameters = incomingEvent.parameters;
        String action = incomingEvent.action;

        if (action.equalsIgnoreCase("OpenFile")) {
            return 1;
        } else if (action.equalsIgnoreCase("CloseHandle")) {
            return 1;
        } else if (action.equalsIgnoreCase("ReadFile")) {
            String filename = getValueForKey("InFileName", parameters);

            String PID = getValueForKey("PID", parameters);
            String processName = getValueForKey("ProcessName", parameters);
            int processContainerID = instantiateProcess(PID, processName, ifModel);

            int fileContainerID = ifModel.getContainerByName(new PIPName(-1, filename));

            // check if container for filename exists and create new
            // container if not
            if (fileContainerID == -1) {
                fileContainerID = ifModel.addContainer(null);
                String fileDataID = ifModel.addData(null);

                ifModel.addDataContainer(fileContainerID, fileDataID);

                ifModel.addName(new PIPName(-1, filename), fileContainerID);
            }

            // add data to transitive reflexive closure of process container
            for (int tempContainerID : ifModel.getAliasClosureByID(processContainerID)) {
                ifModel.addDataContainerList(tempContainerID,
                        ifModel.getDataInContainer(fileContainerID));
            }

            return 1;
        } else if (action.equalsIgnoreCase("WriteFile")) {
            String filename = getValueForKey("InFileName", parameters);

            String PID = getValueForKey("PID", parameters);
            String processName = getValueForKey("ProcessName", parameters);
            int processContainerID = instantiateProcess(PID, processName, ifModel);

            int fileContainerID = ifModel.getContainerByName(new PIPName(-1, filename));

            // check if container for filename exists and create new
            // container if not
            if (fileContainerID == -1) {
                fileContainerID = ifModel.addContainer(null);
                String fileDataID = ifModel.addData(null);

                ifModel.addDataContainer(fileContainerID, fileDataID);

                ifModel.addName(new PIPName(-1, filename), fileContainerID);
            }
            ;

            ifModel.addDataContainerList(fileContainerID,
                    ifModel.getDataInContainer(processContainerID));

            return 1;
        } else if (action.equalsIgnoreCase("CreateFile")) {
            return 1;
        } else if (action.equalsIgnoreCase("CopyFile")) {
            return 1;
        } else if (action.equalsIgnoreCase("MoveFile")) {
            return 1;
        } else if (action.equalsIgnoreCase("ReplaceFile")) {
            return 1;
        } else if (action.equalsIgnoreCase("FileCreated")) {
            return 1;
        } else if (action.equalsIgnoreCase("FileChanged")) {
            return 1;
        } else if (action.equalsIgnoreCase("FileDeleted")) {
            return 1;
        } else if (action.equalsIgnoreCase("FileRenamed")) {
            return 1;
        } else if (action.equalsIgnoreCase("CreateWindow")) {
            String PID = getValueForKey("PID", parameters);
            String processName = getValueForKey("ProcessName", parameters);
            int processContainerID = instantiateProcess(PID, processName, ifModel);
            int windowContainerID = -1;

            String windowHandle = getValueForKey("WindowHandle", parameters);

            int windowContainerByHandleID = ifModel
                    .getContainerByName(new PIPName(-1, windowHandle));

            // check if container for window exists and create new container
            // if not
            if (windowContainerByHandleID == -1) {
                windowContainerID = ifModel.addContainer(null);
                ifModel.addName(new PIPName(-1, windowHandle), windowContainerID);
            }
            ;

            ifModel.addDataContainerList(windowContainerID,
                    ifModel.getDataInContainer(processContainerID));
            ifModel.addAlias(processContainerID, windowContainerID);

            return 1;
        } else if (action.equalsIgnoreCase("CreateProcess")) {
            String PID = getValueForKey("PID_Child", parameters);
            String PPID = getValueForKey("PID", parameters);
            String visibleWindows = getValueForKey("VisibleWindows", parameters);

            String processName = getValueForKey("ChildProcessName", parameters);
            String parentProcessName = getValueForKey("ParentProcessName", parameters);

            int processContainerID = instantiateProcess(PID, processName, ifModel);
            int parentProcessContainerID = instantiateProcess(PPID, parentProcessName, ifModel);

            // add data of parent process container to child process
            // container
            ifModel.addDataContainerList(processContainerID,
                    ifModel.getDataInContainer(parentProcessContainerID));

            // add initial windows of process to model
            // TODO: REGEX??
            String[] visibleWindowsArray = visibleWindows.split(",", 0);

            for (String handle : visibleWindowsArray) {
                int windowContainerID = ifModel.getContainerByName(new PIPName(-1, handle));

                if (windowContainerID == -1) {
                    windowContainerID = ifModel.addContainer(null);
                    ifModel.addName(new PIPName(-1, handle), windowContainerID);
                }

                ifModel.addDataContainerList(windowContainerID,
                        ifModel.getDataInContainer(processContainerID));

                ifModel.addAlias(processContainerID, windowContainerID);
            }

            return 1;
        } else if (action.equalsIgnoreCase("KillProcess")) {
            String PID = getValueForKey("PID_Child", parameters);
            String processName = getValueForKey("ChildProcessName", parameters);

            int processContainerID = ifModel.getContainerByName(new PIPName(-1, PID));

            // check if container for process exists
            if (processContainerID != -1) {
                ifModel.emptyContainer(processContainerID);

                // also remove all depending containers

                for (int contID : ifModel.getAliasClosureByID(processContainerID)) {
                    ifModel.removeContainer(contID);
                }

                ifModel.removeAllAliasesFrom(processContainerID);
                ifModel.removeAllAliasesTo(processContainerID);
                ifModel.removeContainer(processContainerID);

                for (PIPName nm : ifModel.getAllNamingsFrom(processContainerID)) {
                    ifModel.removeName(nm);
                }
            }
            ;

            return 1;
        } else if (action.equalsIgnoreCase("SetClipboardData")) {
            String PID = getValueForKey("PID", parameters);
            String processName = getValueForKey("ProcessName", parameters);
            int processContainerID = instantiateProcess(PID, processName, ifModel);

            int clipboardContainerID = ifModel.getContainerByName(new PIPName(-1, "clipboard"));

            // check if container for clipboard exists and create new
            // container if not
            if (clipboardContainerID == -1) {
                clipboardContainerID = ifModel.addContainer(null);
                ifModel.addName(new PIPName(-1, "clipboard"), clipboardContainerID);
            }
            ;

            ifModel.emptyContainer(clipboardContainerID);
            ifModel.addDataContainerList(clipboardContainerID,
                    ifModel.getDataInContainer(processContainerID));

            return 1;
        } else if (action.equalsIgnoreCase("GetClipboardData")) {
            String PID = getValueForKey("PID", parameters);
            String processName = getValueForKey("ProcessName", parameters);
            int processContainerID = instantiateProcess(PID, processName, ifModel);

            int clipboardContainerID = ifModel.getContainerByName(new PIPName(-1, "clipboard"));

            // check if container for clipboard exists and create new
            // container if not
            if (clipboardContainerID == -1) {
                clipboardContainerID = ifModel.addContainer(null);
                ifModel.addName(new PIPName(-1, "clipboard"), clipboardContainerID);
            }
            ;

            // add data to transitive reflexive closure of process container
            for (int tempContainerID : ifModel.getAliasClosureByID(processContainerID)) {
                ifModel.addDataContainerList(tempContainerID,
                        ifModel.getDataInContainer(clipboardContainerID));
            }

            return 1;
        } else if (action.equalsIgnoreCase("EmptyClipboard")) {
            int clipboardContainerID = ifModel.getContainerByName(new PIPName(-1, "clipboard"));

            // check if container for clipboard exists and create new
            // container if not
            if (clipboardContainerID == -1) {
                clipboardContainerID = ifModel.addContainer(null);
                ifModel.addName(new PIPName(-1, "clipboard"), clipboardContainerID);
            }
            ;

            ifModel.emptyContainer(clipboardContainerID);

            return 1;
        } else if (action.equalsIgnoreCase("CreateDC")) {
            String PID = getValueForKey("PID", parameters);
            String processName = getValueForKey("ProcessName", parameters);

            int processContainerID = instantiateProcess(PID, processName, ifModel);

            String deviceName = getValueForKey("lpszDevice", parameters);

            int deviceContainerID = ifModel.getContainerByName(new PIPName(Integer.parseInt(PID),
                    deviceName));

            // check if container for device exists and create new container
            // if not
            if (deviceContainerID == -1) {
                deviceContainerID = ifModel.addContainer(null);
                ifModel.addName(new PIPName(Integer.parseInt(PID), deviceName), deviceContainerID);
            }
            ;

            ifModel.addDataContainerList(deviceContainerID,
                    ifModel.getDataInContainer(processContainerID));

            return 1;
        } else if (action.equalsIgnoreCase("TakeScreenshot")) {
            String visibleWindow = getValueForKey("VisibleWindow", parameters);

            int clipboardContainerID = ifModel.getContainerByName(new PIPName(-1, "clipboard"));

            // check if container for clipboard exists and create new
            // container if not
            if (clipboardContainerID == -1) {
                clipboardContainerID = ifModel.addContainer(null);
                ifModel.addName(new PIPName(-1, "clipboard"), clipboardContainerID);
            }
            ;

            // do not empty as take screenshot events are splitted to one
            // screenshot event per visible window
            // ifModel.emptyContainer(clipboardContainerID);

            int windowContainerID = ifModel.getContainerByName(new PIPName(-1, visibleWindow));
            ifModel.addDataContainerList(clipboardContainerID,
                    ifModel.getDataInContainer(windowContainerID));

            return 1;
        } else {
            return 1;
        }

    }

    /**
     * Returns the value for a specific key inside a parameter set.
     */
    private String getValueForKey(String key, List<Hashtable> parameters) {
        for (Hashtable parameter : parameters) {
            if (parameter.containsKey("name") && parameter.get("name").equals(key))
                return (String)parameter.get("value");
        }

        return "error";
    }

    /**
     * Checks if a process with given parameters already exists, if not create
     * container, data and names for it.
     */
    private int instantiateProcess(String PID, String processName, PIPStruct ifModel) {
        int processContainerID = ifModel.getContainerByName(new PIPName(-1, PID));

        // check if container for process exists and create new container if not
        if (processContainerID == -1) {
            processContainerID = ifModel.addContainer(null);
            ifModel.addDataContainer(processContainerID, ifModel.addData(null));
            ifModel.addName(new PIPName(-1, PID), processContainerID);
            ifModel.addName(new PIPName(Integer.parseInt(PID), processName), processContainerID);
        }
        ;

        return processContainerID;
    }
}
