import SymbolTable.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Interpreter {
    Scope currentScope;
    Set<Scope> visitedScopes = new HashSet<>();

    public Interpreter(Scope scope) {
        this.currentScope = scope;
    }

    public ASTNode visit(ASTNode node) {
        switch (node.getType()) {
            case Type.PROGRAM:

                break;
            case Type.OBJ_USAGE:

                break;
            case Type.BLOCK, Type.CLASS:

                break;
            case Type.FN_DECL:

                break;
            case Type.FN_CALL:

                break;
            case Type.VAR_DECL, Type.VAR_REF:

                break;
            case Type.ARRAY_REF:

            case Type.ASSIGN:

                break;
            case Type.ARRAY_INIT:

                break;
            case Type.ARRAY_DECL:

                break;
            case Type.ARRAY_ITEM:

                break;
            case Type.EQUAL, Type.NOT_EQUAL, Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL:

                break;
            case Type.NOT:

                break;
            case Type.AND, Type.OR:

                break;
            case Type.DEC_INC:

                break;
            case Type.ADD, Type.SUB, Type.MUL, Type.DIV, Type.MOD:

                break;
            default:
                if (node.children.isEmpty()) {

                } else {
                    visitChildren(node);
                }
                break;
        }
        return node;
    }

    public ASTNode visitChildren(ASTNode node) {
        for (ASTNode child : node.children) {
            visit(child);
        }
        return node;
    }



}
