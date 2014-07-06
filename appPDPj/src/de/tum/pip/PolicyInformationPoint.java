package de.tum.pip;

import java.util.Set;

import android.util.Log;

import de.tum.pip.structures.PDPEvent;
import de.tum.pip.structures.PIPName;
import de.tum.pip.structures.PIPSemantics;
import de.tum.pip.structures.PIPStruct;

public class PolicyInformationPoint implements IPIPCommunication
{
  private static final String     TAG         ="PolicyInformationPoint";

  private PIPStruct               pipModel;
  private PIPSemantics            pipSemantics;
  public static IPIPCommunication curInstance =null;

  private PolicyInformationPoint()
  {}

  public static IPIPCommunication getInstance()
  {
    if(curInstance == null) curInstance=new PolicyInformationPoint();
    return curInstance;
  }

  @Override
  public boolean initializePIP()
  {
    pipModel=new PIPStruct();
    pipSemantics=new PIPSemantics();

    Log.d(TAG, "Initialize PIP" + "[PIPLib]");
    return true;
  }

  @Override
  public String init(int PID, String rep)
  {
    if(pipSemantics == null | pipModel == null)
    {
      Log.d(TAG, "PIP not yet initialized => call initializePIP() first!" + "[PIPLib]");
      return "error";
    }

    String initialDataID;
    int containerID=pipModel.getContainerByName(new PIPName(PID, rep));

    if(containerID == -1)
    {
      int initialContainerID=pipModel.addContainer(null);
      pipModel.addName(new PIPName(PID, rep), initialContainerID);
      initialDataID=pipModel.addData(null);
      pipModel.addDataContainer(initialContainerID, initialDataID);
    }
    else initialDataID=(String)pipModel.getDataInContainer(containerID).toArray()[0];
    return initialDataID;
  }

  @Override
  public String init(int PID, String rep, String initialDataID)
  {
    if(pipSemantics == null | pipModel == null)
    {
      Log.d(TAG, "PIP not yet initialized => call initializePIP() first!" + "[PIPLib]");
      return "error";
    }

    int containerID=pipModel.getContainerByName(new PIPName(PID, rep));
    String dataID;
    if(containerID == -1)
    {
      int initialContainerID=pipModel.addContainer(null);
      pipModel.addName(new PIPName(PID, rep), initialContainerID);
      Log.d(TAG, "adding data to model");
      dataID=pipModel.addData(initialDataID);
      Log.d(TAG, "dataID=" + dataID);

      pipModel.addDataContainer(initialContainerID, dataID);
    }
    else dataID=(String)pipModel.getDataInContainer(containerID).toArray()[0];

    return dataID;
  }

  @Override
  public String init(String rep)
  {
    return init(-1, rep);
  }

  public String init(String rep, String dataID)
  {
    Log.d(TAG, "[PIPHandler] received PIP request for initialization: param=" + rep + "; initialDataID=" + dataID);
    String ret=init(-1, rep, dataID);
    Log.d(TAG, "ret: " + ret);
    return ret;
  }

  @Override
  public int updatePIP(PDPEvent newEvent)
  {
    if(pipSemantics == null | pipModel == null)
    {
      Log.d(TAG, "PIP not yet initialized => call initializePIP() first!" + "[PIPLib]");
      return -1;
    }

    return pipSemantics.processEvent(newEvent, pipModel);
  }

  @Override
  public int eval(int PID, String rep, String dataID, boolean strict)
  {
    // overload setting for strict to make is always false => needed by
    // implementation for smart meter
    strict=false;

    if(pipSemantics == null | pipModel == null)
    {
      Log.d(TAG, "PIP not yet initialized => call initializePIP() first!" + "[PIPLib]");
      return -1;
    }

    int foundContainerID;

    if(strict)
    {
      foundContainerID=pipModel.getContainerByName(new PIPName(PID, rep));
    }
    else
    {
      foundContainerID=pipModel.getContainerByNameRelaxed(new PIPName(PID, rep));
    }

    if(pipModel.hasDataByID(dataID) & pipModel.getContainerOfData(dataID).contains(foundContainerID))
    {
      return 1;
    }
    else
    {
      return 0;
    }
  }

  @Override
  public int eval(String rep, String dataID, boolean strict)
  {
    return eval(-1, rep, dataID, strict);
  }

  @Override
  public int eval(String rep, String dataID)
  {
    return eval(-1, rep, dataID, false);
  }

  @Override
  public Set getDataIDbyRepresentation(int PID, String rep, boolean strict)
  {
    if(pipSemantics == null | pipModel == null)
    {
      Log.d(TAG, "PIP not yet initialized => call initializePIP() first!" + "[PIPLib]");
      return null;
    }

    int foundContainerID;
    Set dataItems=null;

    if(strict)
    {
      foundContainerID=pipModel.getContainerByName(new PIPName(PID, rep));
      dataItems=pipModel.getDataInContainer(foundContainerID);
    }
    else
    {
      foundContainerID=pipModel.getContainerByNameRelaxed(new PIPName(PID, rep));
      dataItems=pipModel.getDataInContainer(foundContainerID);
    }

    return dataItems;
  }

  @Override
  public String printModel()
  {
    if(pipModel == null)
    {
      Log.d(TAG, "PIP not yet initialized => call initializePIP() first!" + "[PIPLib]");
      return "error";
    }

    String printedModel=pipModel.printModel();
    return printedModel;
  }

}
