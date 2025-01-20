package Environment;

import AST.ASTNode;

import java.util.HashMap;

public class Clazz {
  HashMap<String, Function> methods;
  HashMap<String, Object> attributes;

  public Clazz(HashMap<String, Function> methods, HashMap<String, Object> attributes) {
    this.methods = methods;
    this.attributes = attributes;
  }

  public Function getMethod(String name) {
    return methods.get(name);
  }
}
