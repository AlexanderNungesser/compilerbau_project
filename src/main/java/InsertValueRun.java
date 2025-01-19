import AST.ASTNode;
import AST.Type;
import SymbolTable.*;
import SymbolTable.Class;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class InsertValueRun {
  Scope currentScope;
  Set<Scope> visitedScopes = new HashSet<>();

  public InsertValueRun(Scope currentScope) {
    this.currentScope = currentScope;
  }

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgramm(node);
        break;
      case Type.ASSIGN:
        visitAssign(node);
        break;
      case Type.VAR_DECL:
        visitVardecl(node);
        break;
      case Type.ARRAY_INIT:
        visitArrayInit(node);
        break;
      case Type.BLOCK, Type.CLASS:
        visitScopes(node);
        break;
      default:
        visitChildren(node);
        break;
    }
    return node;
  }

  public ASTNode visitArrayInit(ASTNode node) {
    Symbol variable = currentScope.resolve(node.children.getFirst().getValue());
    ((Array) variable).setArray(visitArray(node));
    return node;
  }

  private ArrayList<Object> visitArray(ASTNode node) {
    ArrayList<Object> array = new ArrayList<>();
    for (ASTNode child : node.children) {
      if (child.getType() == Type.ARRAY) {
        array.add(visitArray(child));
      } else {
        if (typeIsValid(child.getType().name().toString())) {
          array.add(parseObject(child.getValue(), child.getType().name().toString()));
        } else {
          array.add(child.getValue());
        }
      }
    }
    return array;
  }

  public ASTNode visitVardecl(ASTNode node) {
    if (node.children.size() == 2) {
      Symbol variable = currentScope.resolve(node.children.getFirst().getValue());
      ASTNode assign = node.children.getLast();
      Object value = null;

      if (assign.getType() == Type.ID || typeIsValid(assign.getType().name().toLowerCase())) {
        if (assign.getType() == Type.ID) {
          Symbol symbol = currentScope.resolve(assign.getValue());
          if (typeIsValid(symbol.type)) {
            value = parseObject(symbol.getValue().toString(), symbol.type);
          }
        } else {
          value = parseObject(assign.getValue(), assign.getType().name().toLowerCase());
        }
        if (value != null) variable.setValue(value);
      }
    }
    return node;
  }

  public ASTNode visitAssign(ASTNode node) {
    Symbol variable = currentScope.resolve(node.children.getFirst().getValue());
    ASTNode assign = node.children.getLast();
    Object value = null;

    if (assign.getType() == Type.ID || typeIsValid(assign.getType().name().toLowerCase())) {
      if (assign.getType() == Type.ID) {
        Symbol symbol = currentScope.resolve(assign.getValue());
        if (typeIsValid(symbol.type)) {
          value = parseObject(symbol.getValue().toString(), symbol.type);
        }
      } else {
        value = parseObject(assign.getValue(), assign.getType().name().toLowerCase());
      }
      if (node.children.getFirst().getType() == Type.ARRAY_ITEM) {
        int place;
        if (node.children.getFirst().children.getFirst().getType() == Type.ID) {
          place =
              (int)
                  parseObject(
                      currentScope
                          .resolve(node.children.getFirst().children.getFirst().getValue())
                          .getValue()
                          .toString(),
                      "int");
        } else {
          place = (int) parseObject(node.children.getFirst().children.getFirst().getValue(), "int");
        }
        if (value != null) ((Array) variable).setArray(value, place);
      }
      if (value != null) variable.setValue(value);
    }

    return node;
  }

  public ASTNode visitChildren(ASTNode node) {
    for (ASTNode child : node.children) {
      visit(child);
    }
    return node;
  }

  public ASTNode visitProgramm(ASTNode node) {
    visitChildren(node);
    return node;
  }

  private ASTNode visitScopes(ASTNode node) {
    for (Scope scope : this.currentScope.innerScopes) {
      if (!visitedScopes.contains(scope)) {
        this.currentScope = scope;
        if (node.getType() == Type.CLASS) {
          for (ASTNode child : node.children) {
            switch (child.getType()) {
              case Type.FN_DECL: // Methoden
                visitScopes(child);
                break;
                //                            case AST.Type.CONSTRUCTOR:
                //                                visitConstructor(child,
                // currentScope.resolve(node.getValue()));
                //                                break;
                //                        case AST.Type.DESTRUCTOR:
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

  private boolean typeIsValid(String type) {
    Symbol typeSymbol = currentScope.resolve(type);

    if (typeSymbol instanceof BuiltIn && !type.equals("void")) {
      return true;
    }
    return false;
  }

  private Symbol getSymbolOfObjUsage(ASTNode node) {
    ASTNode classObject = node.children.getFirst();

    if (classObject.getType() == Type.OBJ_USAGE) {
      return getSymbolOfObjUsage(classObject);
    }

    Symbol objectSymbol = currentScope.resolve(classObject.getValue());
    Symbol classSymbol = currentScope.resolve(objectSymbol.type);
    Scope classScope = ((Class) classSymbol).getClassScope();

    return classScope.resolve(node.children.getLast().getValue());
  }

  private Object parseObject(String object, String type) {
    switch (type) {
      case "int" -> {
        return Integer.parseInt(object);
      }
      case "char" -> {
        return (object);
      }
      case "boolean" -> {
        return Boolean.parseBoolean(object);
      }
    }
    return object;
  }
}
