package Environment;
import AST.ASTNode;

public class Function extends Environment{
    ASTNode node;

    public Function(ASTNode node, Environment closure){
        super(closure);
        this.node = node;
    }
}
