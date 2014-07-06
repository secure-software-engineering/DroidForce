package de.tum.in.i22.uc.pdp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ae.javax.xml.bind.ValidationEvent;
import ae.javax.xml.bind.ValidationEventHandler;

public class PolicyValidationEventHandler implements ValidationEventHandler
{
  private static Logger log =LoggerFactory.getLogger(PolicyValidationEventHandler.class);

  public boolean handleEvent(ValidationEvent event)
  {
    log.warn("Validation-Severity: {}, ", event.getSeverity());
    //log.warn("          -Message : [{}, {}]: {} ", event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(), event.getMessage());
    return true;
  }

}