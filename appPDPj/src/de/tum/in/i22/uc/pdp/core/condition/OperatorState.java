package de.tum.in.i22.uc.pdp.core.condition;

public class OperatorState
{
  public boolean value = false;
  public boolean immutable = false;
  
  public long counter = 0;
  public boolean subEverTrue = false;
  
  public CircularArray<Boolean> circArray = null;
}
