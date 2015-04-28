package de.tum.in.i22.uc.pdp.core.shared;

public class Constants
{
  public static final int      TIMESTEP                    =0;
  public static final int      NANOSECOND                  =1;
  public static final int      MICROSECOND                 =2;
  public static final int      MILLISECOND                 =3;
  public static final int      SECOND                      =4;
  public static final int      MINUTE                      =5;
  public static final int      HOUR                        =6;
  public static final int      DAY                         =7;
  public static final int      WEEK                        =8;
  public static final int      MONTH                       =9;
  public static final int      YEAR                        =10;

  public static final boolean  AUTHORIZATION_ALLOW         =true;
  public static final boolean  AUTHORIZATION_INHIBIT       =false;
  
  public static final int      PARAMETER_TYPE_STRING       =0;
  public static final int      PARAMETER_TYPE_DATAUSAGE    =1;
  public static final int      PARAMETER_TYPE_CONTUSAGE    =2;
  public static final int      PARAMETER_TYPE_DATA		   =3;
//  public static final int      PARAMETER_TYPE_XPATH        =2;
//  public static final int      PARAMETER_TYPE_REGEX        =3;
//  public static final int      PARAMETER_TYPE_CONTEXT      =4;
//  public static final int      PARAMETER_TYPE_BINARY       =5;
//  public static final int      PARAMETER_TYPE_INT          =6;
//  public static final int      PARAMETER_TYPE_LONG         =7;
//  public static final int      PARAMETER_TYPE_BOOL         =8;
//  public static final int      PARAMETER_TYPE_STRING_ARRAY =9;
  
  public static final String[] PARAMETER_TYPE_NAMES={"string", "dataUsage", "contUsage", "data"};//,"xpath", "regex", "context", "binary", "int", "long", "boolean","string array"};
  
}
