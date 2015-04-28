package de.tum.in.i22.uc.pdp.core.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tum.in.i22.uc.pdp.xsd.time.TimeUnitType;

public class TimeAmount
{
  private static Logger log              =LoggerFactory.getLogger(TimeAmount.class);

  public long           amount           =0;
  public TimeUnitType   timeUnit         =TimeUnitType.TIMESTEPS;

  public String         unit             ="";
  public long           interval         =0;
  public long           timestepInterval =0;

  public TimeAmount(long amount, String unit)
  {
    this.amount = amount;
    this.unit = unit;
  }
  
  public TimeAmount(long amount, TimeUnitType tu, long mechanismTimestepSize)
  {
    this.amount = amount;
    this.unit = tu.value();
    this.interval = amount * getTimeUnitMultiplier(tu);
    this.timestepInterval = this.interval / mechanismTimestepSize;
    
    log.debug("Interval: {}, timestepInterval: {}", this.interval, this.timestepInterval);
  }
  
  public static long getTimeUnitMultiplier(TimeUnitType tu)
  {
    if(tu==null)
    {
      log.warn("Cannot calculate timeUnit-multiplier for null!");
      return 1;
    }
    switch(tu)
    {
      case MICROSECONDS:
        return 1;
      case MILLISECONDS:
        return 1000;
      case SECONDS:
        return 1000000;
      case MINUTES:
        return 60000000;
      case HOURS:
        return 3600000000L;
      case DAYS:
        return 86400000000L;
      case WEEKS:
        return 604800000000L;
      case MONTHS:
        return 2592000000000L;
      case YEARS:
        return 31104000000000L;
      case NANOSECONDS:
      case TIMESTEPS:
      default:
        log.warn("Unexpected (unsupported) timeunit found: ", tu.value());
        return 1;
    }
  }
  
  public String toString()
  {
    return this.amount + " " + this.unit + "("+this.timestepInterval+")";
  }
}
