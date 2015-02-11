package de.tum.in.i22.uc.pdp.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ae.javax.xml.bind.JAXBContext;
import ae.javax.xml.bind.JAXBElement;
import ae.javax.xml.bind.JAXBException;
import ae.javax.xml.bind.UnmarshalException;
import ae.javax.xml.bind.Unmarshaller;
import de.tum.in.i22.uc.cm.datatypes.basic.XmlPolicy;
import de.tum.in.i22.uc.cm.interfaces.IPdp2Pip;
import de.tum.in.i22.uc.pdp.PxpManager;
import de.tum.in.i22.uc.pdp.core.exceptions.InvalidMechanismException;
import de.tum.in.i22.uc.pdp.core.shared.Constants;
import de.tum.in.i22.uc.pdp.core.shared.Decision;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.core.shared.IPolicyDecisionPoint;
import de.tum.in.i22.uc.pdp.xsd.MechanismBaseType;
import de.tum.in.i22.uc.pdp.xsd.PolicyType;

public class PolicyDecisionPoint implements IPolicyDecisionPoint, Serializable
{
  private static Logger                                   _logger                 =LoggerFactory.getLogger(PolicyDecisionPoint.class);
  private static final long                               serialVersionUID        =-6823961095919408237L;

  private static IPdp2Pip                                 _pip;

  private static ActionDescriptionStore                          _actionDescriptionStore =null;

  private final HashMap<String, ArrayList<IPdpMechanism>> _policyTable            =new HashMap<String, ArrayList<IPdpMechanism>>();
  private static PxpManager                                _pxpManager = null;
  private static IPolicyDecisionPoint sInstance = null;

  private PolicyDecisionPoint()
  {
    _actionDescriptionStore=new ActionDescriptionStore();
    _pxpManager=new PxpManager();
  }
  
  private PolicyDecisionPoint(IPdp2Pip pip, PxpManager pxpManager)
  {
    _pxpManager=pxpManager;
    _actionDescriptionStore=new ActionDescriptionStore();
    _pip=pip;
  }
  
  public static IPolicyDecisionPoint getInstance()
  {
    if(sInstance!=null) return sInstance;
    
    sInstance = new PolicyDecisionPoint();
    return sInstance;
  }
  
  public static IPolicyDecisionPoint getInstance(IPdp2Pip pip, PxpManager pxpManager)
  {
    if(sInstance!=null) return sInstance;
    
    sInstance = new PolicyDecisionPoint(pip, pxpManager);
    return sInstance;
  }

  @Override
  public boolean deployPolicyXML(XmlPolicy XMLPolicy)
  {
    _logger.debug("deployPolicyXML (before) : " + XMLPolicy.getName());
    InputStream is=new ByteArrayInputStream(XMLPolicy.getXml().getBytes());
    _logger.debug("deployPolicyXML (IS created) : " + XMLPolicy.getName());
    boolean b=deployXML(is);
    _logger.debug("deployPolicyXML (after) : " + XMLPolicy.getName());
    return b;
  }

  @Override
  public boolean deployPolicyURI(String policyFilename)
  {
    if(policyFilename.endsWith(".xml"))
    {
      InputStream inp=null;
      try
      {
        inp=new FileInputStream(policyFilename);
      }
      catch(FileNotFoundException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return deployXML(inp);
    }
    _logger.warn("Unsupported message format of policy! " + policyFilename);
    return false;
  }

  public boolean deployXML(InputStream inp)
  {
    if(inp == null) return false;
    try
    {
    	// SA, 11.02.2015: The following line leads to code that will throw and catch a
    	// NullPointerException. Just ignore that.
    	//
    	//     try
    	//        {
    	//            XmlSchema s = null;
    	//            s.location();
    	//          }
    	//          catch (NullPointerException e) {}catch (NoSuchMethodError e)
    	//          {
    	//    		  ...
    	//    		}
    	//
      JAXBContext jc=JAXBContext.newInstance("de.tum.in.i22.uc.pdp.xsd");
      Unmarshaller u=jc.createUnmarshaller();
//      u.setSchema(null);

      JAXBElement<?> poElement=(JAXBElement<?>)u.unmarshal(inp);
      PolicyType curPolicy=(PolicyType)poElement.getValue();

      _logger.debug("curPolicy [name={}]: {}", curPolicy.getName(), curPolicy.toString());

      List<MechanismBaseType> mechanisms=curPolicy.getDetectiveMechanismOrPreventiveMechanism();

      if(_policyTable.containsKey(curPolicy.getName()))
      {
        // log.error("Policy [{}] already deployed! Aborting...",
        // curPolicy.getName());
        // return false;
      }

      for(MechanismBaseType mech : mechanisms)
      {
        try
        {
          _logger.debug("Processing mechanism: {}", mech.getName());
          IPdpMechanism curMechanism=new Mechanism(mech, this);
          ArrayList<IPdpMechanism> mechanismList=_policyTable.get(curPolicy.getName());
          if(mechanismList == null) mechanismList=new ArrayList<IPdpMechanism>();
          if(mechanismList.contains(curMechanism))
          {
            _logger.error("Mechanism [{}] is already deployed for policy [{}]", curMechanism.getMechanismName(), curPolicy.getName());
            continue;
          }

          mechanismList.add(curMechanism);
          _policyTable.put(curPolicy.getName(), mechanismList);

          _logger.debug("Starting mechanism update thread...");
          if(curMechanism instanceof Mechanism)
          {
            ((Mechanism)curMechanism).init();
            ((Mechanism)curMechanism).start();
          }
          _logger.info("Mechanism {} started...", curMechanism.getMechanismName());
        }
        catch(InvalidMechanismException e)
        {
          _logger.error("Invalid mechanism specified: {}", e.getMessage());
          return false;
        }
      }
      return true;
    }
    catch(UnmarshalException e)
    {
      _logger.error("Syntax error in policy: " + e.getMessage());
    }
    catch(JAXBException | ClassCastException e)
    {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public boolean revokePolicy(String policyName)
  {
    boolean ret=false;
    if(_policyTable == null)
    {
      _logger.error("Empty Policy Table. impossible to revoke policy");
      return false;
    }
    List<IPdpMechanism> mlist=_policyTable.get(policyName);
    if(mlist == null) return false;

    for(IPdpMechanism mech : mlist)
    {
      _logger.info("Revoking mechanism: {}", mech.getMechanismName());
      ret=mech.revoke();
    }
    return ret;
  }

  @Override
  public boolean revokeMechanism(String policyName, String mechName)
  {
    boolean ret=false;
    if(_policyTable == null)
    {
      _logger.error("Empty Policy Table. impossible to revoke policy");
      return false;
    }
    ArrayList<IPdpMechanism> mechanisms=_policyTable.get(policyName);
    if(mechanisms != null)
    {
      IPdpMechanism mech=null;
      for(IPdpMechanism m : mechanisms)
      {
        if(m.getMechanismName().equals(mechName))
        {
          _logger.info("Revoking mechanism: {}", m.getMechanismName());
          ret=m.revoke();
          mech=m;
          break;
        }
      }
      if(mech != null)
      {
        mechanisms.remove(mech);
        if(mech instanceof Mechanism)
        {
          _actionDescriptionStore.removeMechanism(((Mechanism)mech).getTriggerEvent().getAction());
        }
      }
    }
    return ret;
  }

  @Override
  public Decision notifyEvent(Event event)
  {
    ArrayList<EventMatch> eventMatchList=_actionDescriptionStore.getEventList(event.getEventAction());
    if(eventMatchList == null) eventMatchList=new ArrayList<EventMatch>();
    _logger.debug("Searching for subscribed condition nodes for event=[{}] -> subscriptions: {}", event.getEventAction(), eventMatchList.size());
    for(EventMatch eventMatch : eventMatchList)
    {
      _logger.info("Processing EventMatchOperator for event [{}]", eventMatch.getAction());
      eventMatch.evaluate(event);
    }

    ArrayList<Mechanism> mechanismList=_actionDescriptionStore.getMechanismList(event.getEventAction());
    if(mechanismList == null) mechanismList=new ArrayList<Mechanism>();
    _logger.debug("Searching for triggered mechanisms for event=[{}] -> subscriptions: {}", event.getEventAction(), mechanismList.size());

    Decision d=new Decision(new AuthorizationAction("default", Constants.AUTHORIZATION_ALLOW), _pxpManager);
    for(Mechanism mech : mechanismList)
    {
      _logger.info("Processing mechanism [{}] for event [{}]", mech.getMechanismName(), event.getEventAction());
      mech.notifyEvent(event, d);
    }
    return d;
  }

  @Override
  public Map<String, List<String>> listDeployedMechanisms()
  {
    Map<String, List<String>> map=new HashMap<String, List<String>>();

    for(String policyName : _policyTable.keySet())
    {
      List<String> mechanismList=new ArrayList<String>();
      for(IPdpMechanism m : _policyTable.get(policyName))
      {
        mechanismList.add(m.getMechanismName());
      }
      map.put(policyName, mechanismList);
    }

    return map;
  }

  @Override
  public IPdp2Pip getPip()
  {
    return _pip;
  }

  @Override
  public ActionDescriptionStore getActionDescriptionStore()
  {
    return _actionDescriptionStore;
  }

  @Override
  public PxpManager getPxpManager()
  {
    return _pxpManager;
  }

  @Override
  public void stop()
  {
    // TODO Auto-generated method stub
    Set<String> _policyTableKeys=this._policyTable.keySet();
    Iterator<String> _policyTableKeysIt=_policyTableKeys.iterator();
    while(_policyTableKeysIt.hasNext())
    {
      String _policyTableKey=_policyTableKeysIt.next();
      ArrayList<IPdpMechanism> mechanisms=_policyTable.get(_policyTableKey);
      Iterator<IPdpMechanism> mechanismsIt=mechanisms.iterator();
      while(mechanismsIt.hasNext())
      {
        IPdpMechanism mechanism=mechanismsIt.next();
        if(mechanism instanceof Runnable)
        {
          ((Thread)mechanism).interrupt();
        }
      }
    }
    this._policyTable.clear();
  }

}
