package SymbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scope {

  public Scope enclosingScope;
  public ArrayList<Scope> innerScopes = new ArrayList<>();
  public Map<String, Symbol> symbols = new HashMap<String, Symbol>();

  public Scope() {}

  public Scope(Scope scope) {
    this.enclosingScope = scope;
  }

  public void bind(Symbol symbol) {
    this.symbols.put(symbol.name, symbol);
    symbol.scope = this;
  }

  public Symbol resolve(String name) {
    if (symbols.containsKey(name)) return symbols.get(name);
    try {
      return enclosingScope.resolve(name);
    } catch (Exception e) {
      return null;
    }
  }

  public void print() {
    print(0);
  }

  private void print(int indentLevel) {
    String indent = "  ".repeat(indentLevel);
    System.out.println(indent + "SymbolTable.Scope {");

    for (Map.Entry<String, Symbol> entry : symbols.entrySet()) {
      System.out.println(
          indent
              + "  "
              + entry.getValue().getClass().getName()
              + " "
              + entry.getKey()
              + ": "
              + entry.getValue().type);
    }

    // Gib alle inneren Scopes aus
    for (Scope scope : innerScopes) {
      System.out.println(indent + "  Inner SymbolTable.Scope:");
      scope.print(indentLevel + 1);
    }

    System.out.println(indent + "}");
  }
}
