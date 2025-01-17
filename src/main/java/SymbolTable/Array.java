package SymbolTable;

public class Array extends Symbol {
  public int[] array;
  public int length;

  public Array(String name, String type, int dimensions) {
    super(name, type);
    this.length = dimensions;
    this.array = new int[length];
  }

  public int[] getArray() {
    return array;
  }

  public void setArray(int o, int index) {
    this.array[index] = o;
  }
}
