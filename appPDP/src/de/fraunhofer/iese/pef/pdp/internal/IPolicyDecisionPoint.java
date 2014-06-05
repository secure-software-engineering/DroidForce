package de.fraunhofer.iese.pef.pdp.internal;

import java.util.ArrayList;

public interface IPolicyDecisionPoint
{
  // PDP exported methods
  public int      pdpStart();
  public int      pdpStop();
  
  public int      registerPEP(String pepName, String className, String methodName, String methodSignature);
  public int      registerAction(String actionName, String pepName);

  public int      registerPXP(String pepName, String className, String methodName, String methodSignature);
  public int      registerExecutor(String actionName, String pxpName);

  public String   pdpNotifyEventXML(String event);
  public Decision pdpNotifyEventJNI(Event event);
  
  public int      pdpDeployPolicy(String filename);
  public int      pdpDeployPolicyString(String policy);
  public int      pdpDeployMechanism(String filename, String mechName);
  public int      pdpDeployMechanismString(String policy, String mechName);
  public int      pdpRevokeMechanism(String mechName);
  
  public String            listDeployedMechanisms();
  public ArrayList<String> listDeployedMechanismsJNI();
  
  public int      setRuntimeLogLevel(int newLevel);     
}
