package SymbolTable;

public class Symbol {

  public String name;
  public String type;
  private Object value;
  public Scope scope;

  public Symbol(String name, String type, Object value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  public Symbol(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public Object getValue() {
    return value;
  }
}
