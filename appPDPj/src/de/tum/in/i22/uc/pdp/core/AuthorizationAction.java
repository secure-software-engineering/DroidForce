package de.tum.in.i22.uc.pdp.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.condition.TimeAmount;
import de.tum.in.i22.uc.pdp.core.shared.Constants;
import de.tum.in.i22.uc.pdp.core.shared.IPdpAuthorizationAction;
import de.tum.in.i22.uc.pdp.core.shared.IPdpExecuteAction;
import de.tum.in.i22.uc.pdp.core.shared.Param;
import de.tum.in.i22.uc.pdp.xsd.AuthorizationActionType;
import de.tum.in.i22.uc.pdp.xsd.AuthorizationAllowType;
import de.tum.in.i22.uc.pdp.xsd.AuthorizationInhibitType;
import de.tum.in.i22.uc.pdp.xsd.ExecuteActionType;
import de.tum.in.i22.uc.pdp.xsd.ParameterType;

public class AuthorizationAction implements Serializable, IPdpAuthorizationAction
{
  private static final long serialVersionUID =3456284152512343695L;
  private static Logger log = LoggerFactory.getLogger(AuthorizationAction.class);

  public static final IPdpAuthorizationAction AUTHORIZATION_INHIBIT =new AuthorizationAction("INHIBIT", Constants.AUTHORIZATION_INHIBIT);
  public static final IPdpAuthorizationAction AUTHORIZATION_ALLOW   =new AuthorizationAction("ALLOW", Constants.AUTHORIZATION_ALLOW);

  private boolean                         type                  =false;
  private String                          name                  ="";
  private IPdpAuthorizationAction             fallback              =AUTHORIZATION_INHIBIT;
  private String                          fallbackName          ="";
  private List<IPdpExecuteAction>             executeSyncActions    =new ArrayList<IPdpExecuteAction>();
  private List<Param<?>>                  modifiers             =new ArrayList<Param<?>>();
  private long                            delay                 =0;

  public AuthorizationAction()
  {}
  
  public AuthorizationAction(int start, String name, boolean type, List<IPdpExecuteAction> executeActions, long delay, List<Param<?>> modifiers,
      IPdpAuthorizationAction fallback)
  {
    this.type=type;
    if(name!=null)           this.name=name;
    if(executeActions!=null) this.executeSyncActions=executeActions;
    if(fallback!=null)       this.fallback=fallback;
    if(type==Constants.AUTHORIZATION_ALLOW)
    {
      this.modifiers=modifiers;
      this.delay=delay;
    }
  }

  public AuthorizationAction(String name, boolean type)
  {
    this.name=name;
    this.type=type;
  }
  
  public AuthorizationAction(AuthorizationActionType auth)
  {
    log.debug("Preparing AuthorizationAction from AuthorizationActionType");
    this.name = auth.getName();
    this.setFallbackName(auth.getFallback());
    
    Object obj = auth.getAllowOrInhibit();
    AuthorizationAllowType allow = null;
    AuthorizationInhibitType inhibit = null;
    
    if(obj instanceof AuthorizationAllowType) allow = (AuthorizationAllowType)obj;
    else inhibit = (AuthorizationInhibitType)obj;
    
    if(inhibit != null)
    { 
      this.type = Constants.AUTHORIZATION_INHIBIT;
      if(inhibit.getDelay()!=null)
        this.delay = inhibit.getDelay().getAmount() * TimeAmount.getTimeUnitMultiplier(inhibit.getDelay().getUnit());
    }
    else
    {
      this.type = Constants.AUTHORIZATION_ALLOW;
      try
      {
        if(allow.getDelay()!=null)
          this.delay = allow.getDelay().getAmount() * TimeAmount.getTimeUnitMultiplier(allow.getDelay().getUnit());
        else
          this.delay = 0;
        
        if(allow.getModify()!=null)
        {
          for(ParameterType param : allow.getModify().getParameter())
          {
            log.debug("modify: {} -> {}", param.getName(), param.getValue());
            // TODO: use different parameter types?!
            this.modifiers.add(new Param<String>(param.getName(), param.getValue()));
          }
        }
        for(ExecuteActionType execAction : allow.getExecuteSyncAction())
        {
          this.executeSyncActions.add(new ExecuteAction(execAction));
        }
      }
      catch(Exception e)
      {
        log.error("exception occured...");
        e.printStackTrace();
        log.error(e.getMessage());
      }
    }
  }
  
  public boolean getAuthorizationAction()
  {
    return type;
  }

  public List<IPdpExecuteAction> getExecuteActions()
  {
    return executeSyncActions;
  }

  public List<Param<?>> getModifiers()
  {
    return modifiers;
  }

  public void setExecuteActions(List<IPdpExecuteAction> executeActions)
  {
    this.executeSyncActions=executeActions;
  }

  public void setModifiers(List<Param<?>> modifiers)
  {
    if(type == Constants.AUTHORIZATION_ALLOW)
      this.modifiers=modifiers;
  }

  public void addModifier(Param<?> parameter)
  {
    modifiers.add(parameter);
  }

  public void addExecuteAction(IPdpExecuteAction executeAction)
  {
    executeSyncActions.add(executeAction);
  }

  public long getDelay()
  {
    return delay;
  }

  public void setDelay(long delay)
  {
    this.delay=delay;
  }

  public boolean getType()
  {
    return type;
  }

  public void setType(boolean type)
  {
    this.type=type;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    if(name != null) this.name=name;
  }

  public IPdpAuthorizationAction getFallback()
  {
    return fallback == null ? AUTHORIZATION_INHIBIT : fallback;
  }

  public void setFallback(IPdpAuthorizationAction fallback)
  {
    this.fallback=fallback;
  }

  public String getFallbackName()
  {
    return fallbackName;
  }

  public void setFallbackName(String fallbackName)
  {
    this.fallbackName = fallbackName;
  }

  @Override
  public String toString()
  {
    String str="[" + this.getName() + ": " + (this.getType() ? "ALLOW" : "INHIBIT") + "; ";
    if(this.getDelay()!= 0) str+=" Delay: " + this.getDelay();
    
    str+="; Modifiers: {";
    for(Param<?> p : this.getModifiers())
      str+=p.toString() + ",";
    str+="}; mandatory execs: {";
    
    for(IPdpExecuteAction a : this.executeSyncActions)
      str+=a.toString() + ", ";
    str+="}; Fallback: [" + getFallbackName() + "]";
    
    return str;
  }
}
