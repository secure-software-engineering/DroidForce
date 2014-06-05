package de.fraunhofer.iese.pef.pdp.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * An action to be performed. Actions have a name and a set of name-value
 * parameters.
 */
public class ExecuteAction
{

  private String          name      =null;
  private List<Param<?>> parameter =new ArrayList<Param<?>>();

  public ExecuteAction(String name, List<Param<?>> params)
  {
    this.name=name;
    this.parameter=params;
  }

  public ExecuteAction()
  {}

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
    this.name=name;
  }

  /**
   * @return the pairs
   */
  public List<Param<?>> getParams()
  {
    return parameter;
  }

  /**
   * @param params
   *          the pairs to set
   */
  public void setParams(List<Param<?>> params)
  {
    this.parameter=params;
  }

  /**
   * Adds a pair
   * 
   * @param key
   *          The key to be added
   * @param value
   *          The value to be added
   */
  public void addParameter(Param<?> param)
  {
    this.parameter.add(param);
  }

  public void removeParameter(String name)
  {
    this.parameter.remove(name);
  }

  public Param<?> getParameterForName(String name)
  {
    for(int i=0; i < parameter.size(); i++)
      if(parameter.get(i).getName().equalsIgnoreCase(name)) return parameter.get(i);
    return null;
  }

  public String toString()
  {
    String str="ExecuteAction name=[" + this.name + "]; Parameter:{";

    for(Param<?> param : this.parameter)
      str+=param.toString() + ";";
    str+="}";
    return str;
  }
}
