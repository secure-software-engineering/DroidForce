package de.tum.in.i22.uc.pdp.core.condition.operators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.core.ParamMatch;
import de.tum.in.i22.uc.pdp.core.shared.Event;
import de.tum.in.i22.uc.pdp.core.shared.IPdpMechanism;
import de.tum.in.i22.uc.pdp.xsd.ConditionParamMatchType;

public class ConditionParamMatchOperator extends ConditionParamMatchType
{
  private static Logger log     =LoggerFactory.getLogger(ConditionParamMatchOperator.class);
  
  public ConditionParamMatchOperator()
  {}
  
  @Override
  public void initOperatorForMechanism(IPdpMechanism mech)
  {
	  super.initOperatorForMechanism(mech);
  }  
  
  @Override 
  public String toString()
  {
    return "ConditionParamMatchOperator [Name: "+this.getName()+", Value: "+this.getValue()+", CompOp: "+this.getCmpOp()+"]";
    
  }

  @Override
  public boolean evaluate(Event curEvent)
  {
    log.debug("ConditionParamMatchOperator");
	
    if (curEvent==null){
    	log.debug("null event received. ConditionParamMatchOperator returns false.");
    	return false;
    }
    
    //creates a corresponding paramMatch object
    
    ParamMatch pm=new ParamMatch();
    pm.setCmpOp(this.getCmpOp());
    pm.setName(this.getName());
    pm.setValue(this.getValue());
   
    pm.setPdp(_pdp);
    //use the parmMatches method for the evaluation
    
	return pm.paramMatches(curEvent.getParameterForName(pm.getName()));
  }
}
