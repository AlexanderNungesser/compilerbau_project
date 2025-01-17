package SymbolTable;

public class Reference extends Symbol {
  private Symbol origin;

  public Reference(String name, String type, String value) {
    super(name, type, value);
  }

  public Reference(String name, String type) {
    super(name, type);
  }

  public Symbol getOrigin() {
    return this.origin;
  }

  public void setOrigin(Symbol origin) {
    this.origin = origin;
  }
}
