import SymbolTable.*;
import java.util.HashSet;
import java.util.Set;

public class TypeCheckVisitor {
  Scope currentScope;
  Set<Scope> visitedScopes = new HashSet<>();

  public TypeCheckVisitor(Scope scope) {
    this.currentScope = scope;
  }

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgram(node);
        break;
      case Type.OBJ_USAGE:
        visitObj_usage(node);
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
    Symbol variable;
    if (node.children.isEmpty() && node.getType() == Type.ID) {
      if (node.getType() == Type.OBJ_USAGE) {
        variable = visitObj_usage(node);
      } else {
        variable = currentScope.resolve(node.getValue());
      }
      if (variable == null) {
        System.out.println("Error: no such variable: " + node.getValue());
      }
    } else {
      visitChildren(node);
    }
    return node;
  }

  public Symbol visitObj_usage(ASTNode node) {
    Symbol usage = currentScope.resolve(node.getValue());
    if (!(usage instanceof SymbolTable.Class)) {
      usage = currentScope.resolve(usage.type);
    }
    return ((SymbolTable.Class) usage).getClassScope().resolve(node.children.getFirst().getValue());
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
              //                            visitDestructor(child,
              // currentScope.resolve(classNode.getValue()));
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
    visitChildren(node);
    return node;
  }

  public ASTNode visitCompare(ASTNode node) {
    return node;
  }

  public ASTNode visitDecInc(ASTNode node) {
    return node;
  }

  public ASTNode visitCalculate(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    String firstType = getEndType(node.children.getFirst()).name().toLowerCase();
    if (firstType.equals("id")) {
      firstType = this.currentScope.resolve(firstChild.getValue()).type;
    }

    if (!(firstType.equals("int"))) {
      System.out.println("ERROR: Expected int, got " + firstType);
    }

    ASTNode secondChild = node.children.get(1);
    String secondType = getEndType(secondChild).name().toLowerCase();
    if (secondType.equals("id")) {
      secondType = this.currentScope.resolve(secondChild.getValue()).type;
    }

    if (!(secondType.equals("int"))) {
      System.out.println("ERROR: Expected int, got " + secondType);
    }

    visitChildren(node);

    return node;
  }

  private Type getEndType(ASTNode node) {
    if (!node.children.isEmpty()) {
      return getEndType(node.children.getFirst());
    }
    return node.getType();
  }
}
