package de.tum.in.i22.uc.pdp.core.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.Mechanism;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.xsd.ConditionType;

public class Condition
{
  private static Logger log = LoggerFactory.getLogger(Condition.class);

  public Operator operator = null;
  
  public Condition()
  {}
  
  public Condition(ConditionType cond, Mechanism curMechanism)
  {
    log.debug("Preparing condition from ConditionType");
    this.operator = (Operator)cond.getOperators();
    this.operator.initOperatorForMechanism(curMechanism);
    
//    try
//    {
//      Constructor<? extends Object> constructor = cond.getOperators().getClass().getConstructor((cond.getOperators().getClass().getSuperclass()), Mechanism.class);
//      this.operator = (Operator)constructor.newInstance(cond.getOperators(), curMechanism);
//      log.debug("this.operator = {}", this.operator);
//    }
//    catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
//    {
//      e.printStackTrace();
//    }
    log.debug("condition: {}", this.operator);
  }
  
  public String toString()
  {
    return "Condition: { " + this.operator + " }";
  }
  
  public boolean evaluate(Event curEvent)
  {
    log.debug("Evaluating condition...");
    if (operator==null) {
    	log.error("condition is empty. evaluates to true. Strange, though. Who writes such mechanisms?");
    	return true;
    }
    boolean ret=false;
    try
    {
       ret = this.operator.evaluate(curEvent);
    }
    catch(Exception e)
    {
      log.error("Exception during evaluation: {}", e.getMessage());
      e.printStackTrace();
    }
    log.debug("condition value: [{}]", ret);
    return ret;
  }  
  
  
  
}
