package SymbolTable;

public class Variable extends Symbol {

  public Variable(String name, String type, String value) {
    super(name, type, value);
  }

  public Variable(String name, String type) {
    super(name, type);
  }
}
