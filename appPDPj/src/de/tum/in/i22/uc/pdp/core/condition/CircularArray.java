package de.tum.in.i22.uc.pdp.core.condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularArray<T>
{
  private static Logger log = LoggerFactory.getLogger(CircularArray.class);
  private T values[] = null;
  
  public int first = 0;
  public int next = 0;
  public int size = 0;
  
  @SuppressWarnings("unchecked")
  public CircularArray(int size)
  {
    values = (T[]) new Object[size];
    this.size = size;
  }
  
  @SuppressWarnings("unchecked")
  public CircularArray(long size)
  {
    values = (T[]) new Object[(int)size];
    this.size = (int)size;
  }
  
  public T get(int pos)
  {
    return values[pos];
  }
  
  public void set(T val, int pos)
  {
    values[pos]=val;
  }
  
  public T readFirst()
  {
    T val = values[this.first];
    log.trace("readFirst (first={}) -> {}", this.first, val);
    return val;
  }
  
  public T pop()
  {
    T val = values[this.first];
    log.trace("pop (first={}) -> {}", this.first, val);
    
    this.first++;
    log.trace("first++ -> {}", this.first);
    if(this.first == this.values.length)
    {
      log.trace("first reached boundary, resetting to 0");
      this.first=0;
    }
    return val;
  }
  
  public void push(T val)
  {
    log.trace("push (next={}) -> {}", this.next, val);
    this.values[this.next] = val;
    
    this.next++;
    log.trace("next++ -> {}", this.next);
    
    if(this.next == this.values.length)
    {
      this.next=0;
      log.trace("next reached boundary, resetting to 0");
    }
  }
  
  public String toString()
  {
    String str="[";
    for(int a=0; a<values.length; a++)
    {
      if(a>0) str+=", ";
      str+=values[a];
      if(a==first)
      {
        str+="(F";
        if(a==next) str+=",N";
        str+=")";
      }
      if(a!=first && a==next) str+="(N)";
    }
    str+="] ("+first+","+next+")";
    
    return str;
  }
}
