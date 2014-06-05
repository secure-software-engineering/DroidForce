package de.fraunhofer.iese.pef.pdp.internal;

import android.util.Log;

public class DefaultDecision extends Decision
{
  private static final String        TAG  ="DefaultDecision";
  public DefaultDecision()
  {
    //this.setAuthorizationAction(new AuthorizationAction("ALLOW", Constants.AUTHORIZATION_ALLOW));
    //this.setAuthorizationAction(new AuthorizationAction("INHIBIT", Constants.AUTHORIZATION_INHIBIT));

    AuthorizationAction tmpAuth=new AuthorizationAction("MODIFIER", Constants.AUTHORIZATION_ALLOW);
    tmpAuth.setDelay(5);
    tmpAuth.setDelayUnit(Constants.MINUTE);
    tmpAuth.addModifier(new Param<String>("paramName", "newValue", Constants.PARAMETER_TYPE_STRING));
    
    this.setAuthorizationAction(tmpAuth);
    Log.i(TAG, "Prepared decision: " + this);
  }

  public static Decision getDefaultDecision()
  {
    return new DefaultDecision();
  } 

}
