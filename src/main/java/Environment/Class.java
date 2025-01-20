package Environment;

import AST.ASTNode;

import java.util.HashMap;

public class Class {
  public ASTNode node;
  public Environment closure;
  HashMap<String, Function> methods;
  HashMap<String, Object> attributes;

  public Class(HashMap<String, Function> methods, HashMap<String, Object> attributes) {
    this.methods = methods;
    this.attributes = attributes;
  }

  public Function getMethod(String name) {
    return methods.get(name);
  }
}
