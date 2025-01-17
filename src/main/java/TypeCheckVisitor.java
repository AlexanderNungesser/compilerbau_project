import SymbolTable.*;
import java.util.HashSet;
import java.util.Objects;
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
        visitScopes(node);
        break;
      case Type.FN_CALL:
        visitFncall(node);
        break;
      case Type.VAR_DECL:
        visitVardecl(node);
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
        if(node.getType() == Type.CLASS) {
          for (ASTNode child : node.children) {
            switch (child.getType()) {
              case Type.FN_DECL: // Methoden
                visitScopes(child);
                break;
              case Type.CONSTRUCTOR:
                visitConstructor(child, currentScope.resolve(node.getValue()));
                break;
              //                        case Type.DESTRUCTOR:
              //                            visitDestructor(child,
              // currentScope.resolve(classNode.getValue()));
              //                            break;
            }
          }
          this.currentScope = this.currentScope.enclosingScope;
          visitedScopes.add(scope);
          break;
        } else {
          visitChildren(node);
        }
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
    ASTNode classObject = node.children.getFirst();

    if(classObject.getType() == Type.OBJ_USAGE) {
      return visitObj_usage(classObject);
    }

    Symbol objectSymbol = currentScope.resolve(classObject.getValue());
    Symbol classSymbol = currentScope.resolve(objectSymbol.type);
    Scope classScope = ((SymbolTable.Class) classSymbol).getClassScope();

    Symbol usedValueOfObject = classScope.resolve(node.children.getLast().getValue());
    return usedValueOfObject;
  }

  public ASTNode visitClass(ASTNode classNode) {
    for (Scope scope : this.currentScope.innerScopes) {
      if (!visitedScopes.contains(scope)) {
        this.currentScope = scope;

        this.currentScope = this.currentScope.enclosingScope;
        visitedScopes.add(scope);
      }
    }
    return classNode;
  }

  private ASTNode visitNot(ASTNode node) {
    return node;
  }

  public ASTNode visitFncall(ASTNode node) {
    visitChildren(node);
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

  public ASTNode visitVardecl(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    if(node.children.size()==2){
      ASTNode secondChild = node.children.getLast();
      String firstType = firstChild.getType().name(), secondType = secondChild.getType().name();
      if(firstChild.getType() == Type.CLASSTYPE){
        firstType = currentScope.resolve(firstChild.getValue()).type;
        secondType = currentScope.resolve(currentScope.resolve(secondChild.getValue()).type).name;
      }
      if(!Objects.equals(firstType, secondType)){
        System.out.println("Error: type mismatch in vardecl: type " + firstChild.getType() + " cannot be " + secondChild.getType());
      }
    }
    return node;
  }

  public ASTNode visitCalculate(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    if (firstChild.getType() == Type.ADD) {
      firstChild = visitCalculate(firstChild);
    }

    String firstType = getEndType(node.children.getFirst());
    if (firstType.equals("id")) {
      firstType = this.currentScope.resolve(firstChild.getValue()).type;
    }

    if (!typeIsValid(firstType)) {
      System.out.println("ERROR: Expected int, got " + firstType);
    }

    ASTNode secondChild = node.children.get(1);
    String secondType = getEndType(secondChild);
    if (secondType.equals("id")) {
      secondType = this.currentScope.resolve(secondChild.getValue()).type;
    }

    if (!typeIsValid(secondType)) {
      System.out.println("ERROR: Expected int, got " + secondType);
    }

    visitChildren(node);

    return node;
  }

  private boolean typeIsValid(String type) {
    Symbol typeSymbol = currentScope.resolve(type);

    if (typeSymbol instanceof SymbolTable.BuiltIn && !type.equals("void")) {
      return true;
    }
    return false;
  }

  private String  getEndType(ASTNode node) {
    if (!node.children.isEmpty()) {
      if(node.getType() == Type.OBJ_USAGE) {
        return visitObj_usage(node).type;
      }
      return getEndType(node.children.getFirst());
    }
    return node.getType().name().toLowerCase();
  }
}
