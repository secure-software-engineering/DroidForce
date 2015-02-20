package de.tum.in.i22.uc.pdp.android;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import de.ecspride.events.EventInformation;
import de.ecspride.events.EventInformationParser;
import de.ecspride.events.Pair;
import de.tum.in.i22.uc.cm.datatypes.basic.XmlPolicy;
import de.tum.in.i22.uc.pdp.core.PolicyDecisionPoint;
import de.tum.in.i22.uc.pdp.core.shared.Constants;
import de.tum.in.i22.uc.pdp.core.shared.Decision;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPolicyDecisionPoint;
import de.tum.in.i22.uc.pdp.core.shared.Param;
import de.tum.pip.IPIPCommunication;
import de.tum.pip.PolicyInformationPoint;
import de.util.FileUtil;

public class pdpService extends Service
{
  private final static String        eventInformationFileName ="eventInformation.xml";
  private File                       eventInformationFile     =null;

  private static final String        TAG                      ="pdpService";

  private Messenger                  decisionMessenger;
  private Messenger                  setPolicyMessenger;
  private Messenger                  revokePolicyMessenger;

  public static boolean              pdpRunning               =false;
  public static IPolicyDecisionPoint lpdp                     =null;
  public static IPIPCommunication    lpip                     =null;

  public final static String         ACTION_PDP_DECISION      ="de.tum.in.i22.uc.pdp.android.pdpService";
  public final static String         ACTION_PDP_SETPOLICY     ="de.tum.in.i22.uc.pdp.android.setPolicy";
  public final static String         ACTION_PDP_REVPOLICY     ="de.tum.in.i22.uc.pdp.android.revPolicy";

  @Override
  public void onCreate()
  {
    this.eventInformationFile=copyFileFromAssetsToInternalStorage(eventInformationFileName, true);

    this.decisionMessenger=new Messenger(new PDPDecisionHandler(getApplicationContext(), eventInformationFile));
    this.setPolicyMessenger=new Messenger(new SetPolicyHandler(getApplicationContext()));
    this.revokePolicyMessenger=new Messenger(new RevokePolicyHandler(getApplicationContext()));
  }

  private File copyFileFromAssetsToInternalStorage(String fileName, boolean forceOverwrite)
  {
    String fileInternally=this.getFilesDir().toString() + File.separator + fileName;
    File file=new File(fileInternally);
    try
    {
      if(!file.exists() || forceOverwrite) FileUtil.copyPolicyFileFromAssetsToInternalStorage(getApplicationContext(), fileName, fileInternally);
    }
    catch(Exception ex)
    {
      Log.e("ERROR", ex.getMessage());
    }

    try
    {
      Runtime.getRuntime().exec("chmod 777 " + fileInternally);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }

    return file;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    super.onStartCommand(intent, flags, startId);
    Log.d(TAG, "Starting pdpService");

    try
    {
      lpdp=PolicyDecisionPoint.getInstance();
      // Log.i(TAG, "Starting PDP: " + lpdp.pdpStart());
      lpip=PolicyInformationPoint.getInstance();
      Log.i(TAG, "Starting PIP: " + lpip.initializePIP());
    }
    catch(Exception e)
    {
      Log.e(TAG, "exception while starting pdp");
      Log.e(TAG, e.getMessage());
    }
    Log.d(TAG, "native pdp started");
    Toast.makeText(this, "pdpService started", Toast.LENGTH_SHORT).show();

    if(this.eventInformationFile == null) throw new RuntimeException("Oops, something went all wonky");

    return START_STICKY;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    Log.d(TAG, "Stopping pdpService");
    Toast.makeText(this, "Stopping pdpService", Toast.LENGTH_SHORT).show();
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    Log.d(TAG, "pdpService - onBind");

    if(intent.getAction().equals(ACTION_PDP_DECISION)) return this.decisionMessenger.getBinder();
    else if(intent.getAction().equals(ACTION_PDP_SETPOLICY)) return this.setPolicyMessenger.getBinder();
    else if(intent.getAction().equals(ACTION_PDP_REVPOLICY)) return this.revokePolicyMessenger.getBinder();
    else throw new RuntimeException("Unknown action");
  }

  /**
   * Abstract base class for all policy handlers
   * 
   * @author Steven Arzt
   */
  private static class AbstractPolicyHandler extends Handler
  {
    protected final Context context;

    public AbstractPolicyHandler(Context context)
    {
      this.context=context;
    }

  }

  /**
   * Handler for setting a new PDP policy
   * 
   * @author Steven Arzt
   */
  private static class SetPolicyHandler extends AbstractPolicyHandler
  {

    public SetPolicyHandler(Context context)
    {
      super(context);
    }

    @Override
    public void handleMessage(Message msg)
    {
      String policy=msg.getData().getString("policy");
      Log.d("pdpService", "setPolicyHandler-handleMessage");
      Log.d("pdpService", "policy: " + policy);
      lpdp=PolicyDecisionPoint.getInstance();
      //lpdp.pdpDeployPolicyString(policy);
      // FIXME: policyName hardcoded
      lpdp.deployPolicyXML(new XmlPolicy("name", policy));
    }

  }

  /**
   * Handler for revoking a PDP policy
   * 
   * @author Steven Arzt
   */
  private static class RevokePolicyHandler extends AbstractPolicyHandler
  {

    public RevokePolicyHandler(Context context)
    {
      super(context);
    }

    @Override
    public void handleMessage(Message msg)
    {
      String mechName=msg.getData().getString("mechName");
      lpdp=PolicyDecisionPoint.getInstance();
      //lpdp.pdpRevokeMechanism(mechName);
      // FIXME: policyName hardcoded
      lpdp.revokeMechanism("name", mechName);
    }

  }

  private static class PDPDecisionHandler extends AbstractPolicyHandler
  {
    private Map<String, EventInformation> eventNameToInfoMap =null;

    int                                   counter            =0;

    public PDPDecisionHandler(Context context, File eventInformationFile)
    {
      super(context);

      // Initialize the event information
      EventInformationParser eventInfoParser=new EventInformationParser(eventInformationFile.getAbsolutePath());
      Map<String, EventInformation> eventInformation=eventInfoParser.parseEventInformation();
      this.eventNameToInfoMap=setupEventNameToInformationMap(eventInformation);
    }

    @Override
    public void handleMessage(Message msg)
    {
      /*
       * lpdp = PolicyDecisionPoint.getInstance(); for (int i = 1; i < 20; i++)
       * { Event event = new Event("sentTextMessage", true);
       * event.addStringParameter("message", "xxx");
       * event.addStringParameter("destination", "12345");
       * event.addStringParameter("DATA_UNIQUE_IDENTIFIER", "true");
       * 
       * Decision d = lpdp.pdpNotifyEventJNI(event); Log.d("PDP", "xxx " +
       * d.getAuthorizationAction().getType()); }
       */

      Log.d(TAG, "pdpService received message: " + counter);
      lpdp=PolicyDecisionPoint.getInstance();

      Event event=reconstructEvent(msg.getData().getString("eventname"), msg);

      Decision d=lpdp.notifyEvent(event);

      Toast.makeText(context, "Event " + event.getEventAction() + (d.getAuthorizationAction().getType() ? " allowed" : "inhibited"), Toast.LENGTH_LONG).show();

      Message message=Message.obtain(null, 2, 0, 0);
      Bundle b=new Bundle(); // or use more complex (key,value)-pairs.
      b.putString("data", "" + d.getAuthorizationAction().getType());
      message.setData(b);
      Log.d(TAG, "answer sent to pep!!!!: ");

      try
      {
        Messenger replyTo=msg.replyTo;
        replyTo.send(message);
      }
      catch(RemoteException rme)
      {
        Log.e(TAG, "sending answer failed!");
      }
    }

    private Event reconstructEvent(String eventname, Message msg)
    {
      Event event=new Event(eventname, true);

      EventInformation eventInfo=eventNameToInfoMap.get(eventname);

      if (eventInfo == null) {
    	  Log.w(TAG, "warning: unknown event '"+ eventname +"'");
    	  Log.w(TAG, "available events: ");
    	  for (String k : eventNameToInfoMap.keySet()) {
    		  Log.w(TAG, " -> "+ k);
    	  }
      }
      
      for(Pair<Integer, String> parameterInformation : eventInfo.getParameterInformation())
      {
        String paramName=parameterInformation.getRight();
        String paramValue=msg.getData().getString("param" + parameterInformation.getLeft() + "value");

        event.addParam(new Param<String>(paramName, paramValue, Constants.PARAMETER_TYPE_STRING));
      }

      for(String key : msg.getData().keySet())
      {
        if(key.startsWith("DATA_"))
        {
          String paramName=key;
          String paramValue=msg.getData().getString(key);

          event.addParam(new Param<String>(paramName, paramValue, Constants.PARAMETER_TYPE_STRING));
        }
      }

      return event;
    }

    private Map<String, EventInformation> setupEventNameToInformationMap(Map<String, EventInformation> eventInfo)
    {
      Map<String, EventInformation> eventNameToInfoMap=new HashMap<String, EventInformation>();

      for(Map.Entry<String, EventInformation> entry : eventInfo.entrySet())
      {
        eventNameToInfoMap.put(entry.getValue().getEventName(), entry.getValue());
      }

      return eventNameToInfoMap;

    }
  }

}
