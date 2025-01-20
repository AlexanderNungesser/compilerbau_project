package Environment;

import AST.ASTNode;

public class Attribute {
  public ASTNode node;
  public Environment closure;

  public Attribute(ASTNode node, Environment closure) {
    this.closure = closure;
    this.node = node;
  }

  public Attribute bind(Instance instance) {
    Environment environment = new Environment(this.closure);
    environment.define("this", instance);
    return new Attribute(this.node, environment);
  }
}
