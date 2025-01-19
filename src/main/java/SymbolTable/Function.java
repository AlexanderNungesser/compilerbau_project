package SymbolTable;

import AST.ASTNode;
import java.util.ArrayList;

public class Function extends Symbol {
  private ArrayList<ASTNode> params = new ArrayList<>();

  public Function(String name, String type) {
    super(name, type);
  }

  public int getParamCount() {
    return this.params.size();
  }

  public void setParam(ASTNode param) {
    this.params.add(param);
  }

  public ArrayList<ASTNode> getParams() {
    return params;
  }
}
