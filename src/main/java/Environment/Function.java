package Environment;

import AST.ASTNode;

public class Function {
  public ASTNode node;
  public Environment closure;

  public Function(ASTNode node, Environment closure) {
    this.closure = closure;
    this.node = node;
  }

  public Function bind(Instance instance) {
    Environment environment = new Environment(this.closure);
    environment.define("this", instance);
    environment.define("self", instance);
    return new Function(this.node, environment);
  }
}
