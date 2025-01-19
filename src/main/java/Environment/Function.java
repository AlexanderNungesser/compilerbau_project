package Environment;

import AST.ASTNode;

public class Function {
  public ASTNode node;
  public Environment closure;

  public Function(ASTNode node, Environment closure) {
    this.closure = closure;
    this.node = node;
  }
}
