package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.EvalOperatorType;

public class EvalOperator extends EvalOperatorType
{
  private static Logger log     =LoggerFactory.getLogger(EvalOperator.class);
  
  public EvalOperator()
  {}
  
  @Override
  public void initOperatorForMechanism(IPdpMechanism mech)
  {
	  super.initOperatorForMechanism(mech);
  }  
  
  @Override 
  public String toString()
  {
    return "EvalOperator [Type: "+this.getType()+", ["+this.getContent()+"]]";
  }

  @Override
  public boolean evaluate(Event curEvent)
  {
    log.debug("eval EvalOperator");
    // TODO: evalOperator evaluation NYI; forward to external evaluation engine
    return false;
  }
}
