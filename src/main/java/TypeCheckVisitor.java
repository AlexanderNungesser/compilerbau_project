import SymbolTable.*;

import java.util.HashSet;
import java.util.Set;

public class TypeCheckVisitor {
    Scope currentScope;
    Set<Scope> visitedScopes = new HashSet<>();

    public TypeCheckVisitor(Scope scope) {this.currentScope = scope;}

    public ASTNode visit(ASTNode node) {
        switch (node.getType()) {
            case Type.PROGRAM:
                visitProgram(node);
                break;
            case Type.BLOCK, Type.FN_DECL:
                visitScopes(node);
                break;
            case Type.CLASS:
                visitClass(node);
                break;
            case Type.FN_CALL:
                visitFncall(node);
                break;
            case Type.ASSIGN:
                visitAssign(node);
                break;
            case Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL:
                visitCompare(node);
                break;
            case Type.NOT:
                visitNot(node);
                break;
            case Type.DEC_INC:
                visitDecInc(node);
                break;
            case Type.ADD, Type.SUB, Type.MUL, Type.DIV, Type.MOD:
                visitCalculate(node);
                break;
            default:
            if (node.children.isEmpty()) {
                visitExpr(node);
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

    private ASTNode visitScopes(ASTNode node) {
        for (Scope scope : this.currentScope.innerScopes) {
            if (!visitedScopes.contains(scope)) {
                this.currentScope = scope;
                visitChildren(node);
                this.currentScope = this.currentScope.enclosingScope;
                visitedScopes.add(scope);
            }
        }
        return node;
    }

    public ASTNode visitExpr(ASTNode node) {
        return node;
    }

    private ASTNode visitFncall(ASTNode node) {
        return node;
    }

    public ASTNode visitClass(ASTNode classNode) {
        for (Scope scope : this.currentScope.innerScopes) {
            if (!visitedScopes.contains(scope)) {
                this.currentScope = scope;
                for (ASTNode child : classNode.children) {
                    switch (child.getType()) {
                        case Type.FN_DECL: // Methoden
                            visitScopes(child);
                            break;
                        case Type.CONSTRUCTOR:
                            visitConstructor(child, currentScope.resolve(classNode.getValue()));
                            break;
//                        case Type.DESTRUCTOR:
//                            visitDestructor(child, currentScope.resolve(classNode.getValue()));
//                            break;
                    }
                }
                this.currentScope = this.currentScope.enclosingScope;
                visitedScopes.add(scope);
            }
        }
        return classNode;
    }

    private ASTNode visitNot(ASTNode node) {
        return node;
    }

    public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {
        return constructorNode;
    }

    public ASTNode visitProgram(ASTNode program) {
        visitChildren(program);
        return program;
    }

    public ASTNode visitAssign(ASTNode node) {
        return node;
    }

    public ASTNode visitCompare(ASTNode node) {
        return node;
    }

    public ASTNode visitDecInc(ASTNode node) {return node;}

    public ASTNode visitCalculate(ASTNode node) {return node;}
}
