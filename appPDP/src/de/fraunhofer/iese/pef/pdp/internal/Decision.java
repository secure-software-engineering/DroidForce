package de.fraunhofer.iese.pef.pdp.internal;

import java.util.ArrayList;

/**
 * pdpResponse is the object produced by the PDP as a result of an event. It
 * contains information about permissiveness of the event and desired actions to
 * be performed.
 */
public class Decision
{
  /**
   * Default instance of a pdpResponse object for allowing the action
   */
  public static final Decision     RESPONSE_ALLOW   =new Decision("ALLOW", Constants.AUTHORIZATION_ALLOW);

  /**
   * Default instance of a pdpResponse object for inhibiting the action
   */
  public static final Decision     RESPONSE_INHIBIT =new Decision("INHIBIT", Constants.AUTHORIZATION_INHIBIT);

  /**
   * The AuthorizationAction wrt. the event (starting point for
   * authorizationActions)
   */
  private AuthorizationAction      mAuthorizationAction;

  /**
   * List of actions to be performed ('optional' executeActions; no guarantee
   * for successful execution)
   */
  private ArrayList<ExecuteAction> mExecuteActions  =new ArrayList<ExecuteAction>();

  public Decision()
  {}

  public Decision(String name, boolean type)
  {
    this.mAuthorizationAction=new AuthorizationAction(name, type);
  }
  
  /**
   * @return the mAuthorizationAction
   */
  public AuthorizationAction getAuthorizationAction()
  {
    return mAuthorizationAction;
  }

  /**
   * @param mAuthorizationAction
   *          the mAuthorizationAction to set
   */
  public void setAuthorizationAction(AuthorizationAction mAuthorizationAction)
  {
    this.mAuthorizationAction=mAuthorizationAction;
  }

  /**
   * @return the mExecuteActions
   */
  public ArrayList<ExecuteAction> getExecuteActions()
  {
    return mExecuteActions;
  }

  /**
   * @param mExecuteActions
   *          the mExecuteActions to set
   */
  public void setExecuteActions(ArrayList<ExecuteAction> mExecuteActions)
  {
    this.mExecuteActions=mExecuteActions;
  }

  public void addExecuteAction(ExecuteAction mExecuteActionTmp)
  {
    mExecuteActions.add(mExecuteActionTmp);
  }

  @Override
  public String toString()
  {
    if(mAuthorizationAction == null && mExecuteActions == null) return "null";

    String str="pdpResponse: ";
    if(mAuthorizationAction == null) str+="[]";
    else str+=this.mAuthorizationAction.toString();

    str+="\n(";
    for(ExecuteAction a : mExecuteActions)
      str+=a.toString();
    str+=")";

    return str;
  }

}


