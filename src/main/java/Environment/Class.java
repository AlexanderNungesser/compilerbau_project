package Environment;

import AST.ASTNode;

public class Class extends Environment{

    ASTNode node;

    public Class(ASTNode node, Environment closure) {
        super(closure);
        this.node = node;
    }
}
