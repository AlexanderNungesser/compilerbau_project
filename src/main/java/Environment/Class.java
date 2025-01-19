package Environment;

import AST.ASTNode;

public class Class {
  public ASTNode node;
  public Environment closure;

  public Class(ASTNode node, Environment closure) {
    this.node = node;
    this.closure = closure;
  }
}
