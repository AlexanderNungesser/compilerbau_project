package SymbolTable;

import java.util.ArrayList;

public class Function extends Symbol {
  private ArrayList<Symbol> params = new ArrayList<>();

  public Function(String name, String type) {
    super(name, type);
  }

  public int getParamCount() {
    return this.params.size();
  }

  public void setParam(Symbol param) {
    this.params.add(param);
  }
}
