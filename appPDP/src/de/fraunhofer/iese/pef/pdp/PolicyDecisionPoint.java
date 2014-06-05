package de.fraunhofer.iese.pef.pdp;

import java.util.ArrayList;

import android.util.Log;

import de.fraunhofer.iese.pef.pdp.internal.Decision;
import de.fraunhofer.iese.pef.pdp.internal.Event;
import de.fraunhofer.iese.pef.pdp.internal.IPolicyDecisionPoint;

public class PolicyDecisionPoint implements IPolicyDecisionPoint
{
  private static final long          serialVersionUID =1L;

  public static IPolicyDecisionPoint curInstance      =null;
  public static boolean              pdpRunning       =false;
  private static final String        TAG              ="PolicyDecisionPoint";

  private PolicyDecisionPoint()
  {
    Log.d(TAG, "Loading native PDP library");
    try
    {
      System.loadLibrary("log");
      System.loadLibrary("glib");
      System.loadLibrary("pdp");
      pdpRunning=true;
      Log.d(TAG, "Native PDP library loaded...");
    }
    catch(Exception e)
    {
      Log.e(TAG, "Could not load native PDP library!");
      Log.e(TAG, e.getMessage());
    }
  }

  public static IPolicyDecisionPoint getInstance()
  {
    if(curInstance == null) curInstance=new PolicyDecisionPoint();
    return curInstance;
  }

  public int handlePIPeval(String method, String params)
  {
    Log.d(TAG, "received PIP request for evaluation: " + method);
    return 1;
  }

  public String handlePIPinit(String method, String params)
  {
    Log.d(TAG, "received PIP request for initialization: " + method);
    return "bliblablub";
  }

  // Native method declaration
  public native int pdpStart();

  public native int pdpStop();

  public native int registerPEP(String pepName, String className, String methodName, String methodSignature);

  public native int registerAction(String actionName, String pepName);

  public native int registerPXP(String pepName, String className, String methodName, String methodSignature);

  public native int registerExecutor(String actionName, String pepName);

  public native String pdpNotifyEventXML(String event);

  public native Decision pdpNotifyEventJNI(Event event);

  public native int pdpDeployPolicy(String mechanism_doc_path);

  public native int pdpDeployPolicyString(String policy);

  public native int pdpDeployMechanism(String mechanism_doc_path, String mechName);

  public native int pdpDeployMechanismString(String policy, String mechName);

  public native int pdpRevokeMechanism(String mechName);

  public native String listDeployedMechanisms();

  public native ArrayList<String> listDeployedMechanismsJNI();

  public native int setRuntimeLogLevel(int newLevel);

}
