public class Array extends Symbol {
  private Object[] array;

  public Array(String name, String type) {
    super(name, type);
  }

  public Object[] getArray() {
    return array;
  }

  public void setArray(Object o, int index) {
    this.array[index] = o;
  }
}
