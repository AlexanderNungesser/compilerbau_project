package SymbolTable;

import java.util.ArrayList;

public class Array extends Symbol {
  private ArrayList<Object> array;
  public Object[] length;

  public Array(String name, String type, int dimensions) {
    super(name, type);
    this.length = new Object[dimensions];
  }

  public ArrayList<Object> getArray() {
    return array;
  }

  public void setArray(Object o, int index) {
    this.array.add(o);
  }

  public void setArray(ArrayList<Object> o) {
    this.array = o;
  }

  @Override
  public Object getValue() {
    return this.array;
  }
}
