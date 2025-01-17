package SymbolTable;

public class Array extends Symbol {
  private Object[] array;
  public int[] length;

  public Array(String name, String type, int dimensions) {
    super(name, type);
    this.length = new int[dimensions];
  }

  public Object[] getArray() {
    return array;
  }

  public void setArray(Object o, int index) {
    this.array[index] = o;
  }
}
