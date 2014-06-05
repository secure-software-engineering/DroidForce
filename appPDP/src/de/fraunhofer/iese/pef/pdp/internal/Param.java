package de.fraunhofer.iese.pef.pdp.internal;

public class Param<T> {

  private String name;

  private T value;

  private int type;

  public Param(String name, T value, int type) {
      if (name == null) {
          throw new IllegalArgumentException("Name required");
      }
      this.name = name;
      this.value = value;
      this.type = type;
  }

  public String toString() {
      return "  " + name + ": " + value + " (" + Constants.PARAMETER_TYPE_NAMES[type] + ")";
  }

  /**
   * @return the name
   */
  public String getName() {
      return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
      this.name = name;
  }

  /**
   * @return the value
   */
  public T getValue() {
      return value;
  }

  /**
   * @param value the value to set
   */
  public void setValue(T value) {
      this.value = value;
  }

  /**
   * @return the type
   */
  public int getType() {
      return type;
  }

  /**
   * @param type the type to set
   */
  public void setType(int type) {
      this.type = type;
  }

  public static int getIdForName(String type) {
      if (type != null) {
          if (type.equalsIgnoreCase("datausage")) {
              return Constants.PARAMETER_TYPE_DATAUSAGE;
          } else if (type.equalsIgnoreCase("xpath")) {
              return Constants.PARAMETER_TYPE_XPATH;
          } else if (type.equalsIgnoreCase("regex")) {
              return Constants.PARAMETER_TYPE_REGEX;
          } else if (type.equalsIgnoreCase("context")) {
              return Constants.PARAMETER_TYPE_CONTEXT;
          } else if (type.equalsIgnoreCase("binary")) {
              return Constants.PARAMETER_TYPE_BINARY;
          } else if (type.equalsIgnoreCase("int")) {
              return Constants.PARAMETER_TYPE_INT;
          } else if (type.equalsIgnoreCase("long")) {
              return Constants.PARAMETER_TYPE_LONG;
          } else if (type.equalsIgnoreCase("bool")) {
              return Constants.PARAMETER_TYPE_BOOL;
          }
      }
      return Constants.PARAMETER_TYPE_STRING;
  }
}
