package de.fraunhofer.iese.pef.pdp.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about the permissiveness of the event, including modifiers and
 * delayers
 */
public class AuthorizationAction
{

  public static final AuthorizationAction AUTHORIZATION_INHIBIT=new AuthorizationAction("INHIBIT", Constants.AUTHORIZATION_INHIBIT);

  /**
   * Type of this {@link AuthorizationAction}.
   */
  private boolean             type;

  /**
   * Type of this {@link AuthorizationAction}.
   */
  private String              name="";

  /**
   * fallback authorizationAction if execution of actions failed
   */
  private AuthorizationAction fallback=AUTHORIZATION_INHIBIT;

  /**
   * List of required actions to be performed
   */
  private List<ExecuteAction> executeActions=new ArrayList<ExecuteAction>();

  /**
   * List of modifiers for an event
   */
  private List<Param<?>>     modifiers=new ArrayList<Param<?>>();

  /**
   * amount of units the event should be delayed the time unit for delaying
   */
  private int                 delay=0;
  private int                 delayUnit=2;

  public AuthorizationAction(int start, String name, boolean type, List<ExecuteAction> executeActions, int delay, int delayUnit, List<Param<?>> modifiers,
      AuthorizationAction fallback)
  {
    this.type=type;
    if(name!=null)           this.name=name;
    if(executeActions!=null) this.executeActions=executeActions;
    if(fallback!=null)       this.fallback=fallback;
    if(type==Constants.AUTHORIZATION_ALLOW)
    {
      this.modifiers=modifiers;
      this.delay=delay;
      this.delayUnit=delayUnit;
    }
  }

  public AuthorizationAction(String name, boolean type)
  {
    this.name=name;
    this.type=type;
  }

  public AuthorizationAction()
  {}
  
  /**
   * @return type
   */
  public boolean getAuthorizationAction()
  {
    return type;
  }

  /**
   * @return executeActions
   */
  public List<ExecuteAction> getExecuteActions()
  {
    return executeActions;
  }

  /**
   * @return modifiers
   */
  public List<Param<?>> getModifiers()
  {
    return modifiers;
  }

  /**
   * Sets executeActions for this authorizationAction
   * 
   * @param modifiers
   */
  public void setExecuteActions(List<ExecuteAction> executeActions)
  {
    this.executeActions=executeActions;
  }

  /**
   * Sets modifiers for the event. Note that modifiers are possible if the event
   * is allowed.
   * 
   * @param modifiers
   */
  public void setModifiers(List<Param<?>> modifiers)
  {
    if(type == Constants.AUTHORIZATION_ALLOW)
      this.modifiers=modifiers;
  }

  /**
   * Adds a new modifier to the list
   * 
   * @param name
   *          The name of the modifier (usually a parameter name to be modified)
   * @param value
   *          The new value of the parameter
   */
  public void addModifier(Param<?> parameter)
  {
    modifiers.add(parameter);
  }

  /**
   * Adds a new execute action to the list
   * 
   * @param executeAction
   *          the execute action to set
   */
  public void addExecuteAction(ExecuteAction executeAction)
  {
    executeActions.add(executeAction);
  }

  /**
   * @return delay
   */
  public int getDelay()
  {
    return delay;
  }

  /**
   * Sets the amount of the delay.
   * 
   * @param delay
   *          The amount of timesteps to be delayed
   */
  public void setDelay(int delay)
  {
    this.delay=delay;
  }

  /**
   * @return delayUnit
   */
  public int getDelayUnit()
  {
    return delayUnit;
  }

  /**
   * Sets the unit of the delay.
   * 
   * @param delayUnit
   *          The unit of the delay
   */
  public void setDelayUnit(int delayUnit)
  {
    this.delayUnit=delayUnit;
  }

  /**
   * @return type
   */
  public boolean getType()
  {
    return type;
  }

  /**
   * Changes the Type of the {@link AuthorizationAction}
   * 
   * @param type
   *          the type to set
   */
  public void setType(boolean type)
  {
    this.type=type;
  }

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name)
  {
    if(name != null) this.name=name;
  }

  /**
   * @return the fallback
   */
  public AuthorizationAction getFallback()
  {
    return fallback == null ? AUTHORIZATION_INHIBIT : fallback;
  }

  /**
   * @param fallback
   *          the fallback to set
   */
  public void setFallback(AuthorizationAction fallback)
  {
    this.fallback=fallback;
  }

  @Override
  public String toString()
  {
    String str="[" + this.getType();
    if(this.getDelay() != 0) str+=", Delay: " + this.getDelay() + " " + this.getDelayUnit();
    str+=" Fallback: " + getFallback().getName() + ", Modifiers: {";
    for(Param<?> p : this.getModifiers())
      str+=p.toString() + ",";

    str+="}, required actions: {";
    for(ExecuteAction a : this.executeActions)
      str+=a.toString();
    str+="}\n";

    if(!(getFallback().getName().equalsIgnoreCase("ALLOW") || getFallback().getName().equalsIgnoreCase("INHIBIT")))
      str+=getFallback().toString();

    return str;
  }
}
