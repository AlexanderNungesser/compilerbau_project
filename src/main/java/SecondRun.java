import SymbolTable.*;
import java.util.HashSet;
import java.util.Set;

public class SecondRun extends CppParseTreeVisitor {
  Scope currentScope;
  Set<Scope> visitedScopes = new HashSet<>();

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgram(node);
        break;
      case Type.FN_CALL:
        visitFncall(node);
        break;
      case Type.ARGS:
        visitArgs(node);
        break;
      case Type.ASSIGN:
        visitAssign(node);
        break;
      case Type.BLOCK:
        visitScopes(node);
        break;
      case Type.FN_DECL:
        visitScopes(node);
        break;
      case Type.CLASS:
        visitScopes(node);
        //      case Type.CONSTRUCTOR:
        //        visitFndecl(node.children.getFirst());
        //        break;
      case null:
        System.out.println("Type: " + node.getType().name() + "Value: " + node.getValue());
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

  public ASTNode visitProgram(ASTNode program) {
    Scope globalScope = new Scope(null);

    currentScope = globalScope;

    visitChildren(program);
    return program;
  }

  public ASTNode visitFncall(ASTNode fncall) {
    String functionName = fncall.getValue();

    if (!fncall.children.isEmpty() && fncall.children.getFirst().getType() == Type.CONSTRUCTOR) {
      String className = fncall.children.getFirst().getValue();
      Symbol classSymbol = currentScope.resolve(className);
      if (classSymbol == null) {
        System.out.println("Error: no such class: " + className);
      } else {
        ASTNode args =
            (!fncall.children.isEmpty() && fncall.children.getLast().getType() == Type.ARGS)
                ? fncall.children.getLast()
                : null;
        if (args != null) {
          int args_count = args.children.size();
          if (args_count != 1) {
            System.out.println("Error: arg and param count mismatch at function " + functionName);
            return fncall;
          } else {
            currentScope.bind(new Variable(functionName, classSymbol.name));
          }
          visitArgs(args);
        }
      }
    } else if (!fncall.children.isEmpty() && fncall.children.getFirst().getType() == Type.CLASS) {
      String className = fncall.children.getFirst().getValue();
      Symbol classSymbol = currentScope.resolve(className);
      if (classSymbol == null) {
        System.out.println("Error: no such class: " + className);
      } else {
        Symbol function = currentScope.resolve(functionName);

        if (function == null) {
          System.out.println("Error: no such function: " + functionName);
          return fncall;
        }

        if (function instanceof Variable) {
          System.out.println("Error: " + functionName + " is not a function");
          return fncall;
        }

        ASTNode args =
            (!fncall.children.isEmpty() && fncall.children.getLast().getType() == Type.ARGS)
                ? fncall.children.getLast()
                : null;
        int args_count = 0;
        int params_count = 0;

        if (args != null) {
          args_count = args.children.size();
          if (function instanceof BuiltIn) {
            params_count = 1;
          } else {
            params_count = ((Function) function).getParamCount();
          }

          if (args_count != params_count) {
            System.out.println("Error: arg and param count mismatch at function " + functionName);
            return fncall;
          }

          visitArgs(args);
        }
      }
    } else {

      Symbol function = currentScope.resolve(functionName);

      if (function == null) {
        System.out.println("Error: no such function: " + functionName);
        return fncall;
      }

      if (function instanceof Variable) {
        System.out.println("Error: " + functionName + " is not a function");
        return fncall;
      }

      ASTNode args =
          (!fncall.children.isEmpty() && fncall.children.getLast().getType() == Type.ARGS)
              ? fncall.children.getLast()
              : null;
      int args_count = 0;
      int params_count = 0;

      if (args != null) {
        args_count = args.children.size();
        if (function instanceof BuiltIn) {
          params_count = 1;
        } else {
          params_count = ((Function) function).getParamCount();
        }

        if (args_count != params_count) {
          System.out.println("Error: arg and param count mismatch at function " + functionName);
          return fncall;
        }

        visitArgs(args);
      }
    }

    return fncall;
  }

  public ASTNode visitArgs(ASTNode args) {
    for (ASTNode child : args.children) {
      if (child.getType() == Type.OBJ_USAGE || child.getType() == Type.ID) {
        currentScope.resolve(child.getType().name().toLowerCase());
      }
    }

    return args;
  }

  public ASTNode visitParams(ASTNode node) {
    for (ASTNode child : node.children) {
      String name = child.getValue();
      String type = child.getType().name().toLowerCase();
      Symbol typeSymbol = getTypeEqual(type, child);
      Symbol param = new Variable(name, typeSymbol.name);
      currentScope.bind(param);
    }
    return node;
  }

  public ASTNode visitExpr(ASTNode node) {
    if (node.children.isEmpty() && node.getType() == Type.ID) {
      String name = node.getValue();
      Symbol var = currentScope.resolve(name);
      if (var == null) {
        System.out.println("Error: no such variable: " + name);
      }
    } else {
      visitChildren(node);
    }
    return node;
  }

  public ASTNode visitChildren(ASTNode node) {
    for (ASTNode child : node.children) {
      visit(child);
    }
    return node;
  }

  public ASTNode visitAssign(ASTNode node) {
    Symbol variable;
    ASTNode firstChild = node.children.getFirst();

    if (firstChild.getType() == Type.OBJ_USAGE) {
      variable = visitObj_usage(firstChild);
    } else {
      variable = currentScope.resolve(firstChild.getValue());
    }

    if (variable == null) {
      System.out.println("Error: no such variable: " + node.children.getFirst().getValue());
    }

    ASTNode value = node.children.getLast();
    if (value.getType() == Type.ID
        || value.getType() == Type.OBJ_USAGE
        || value.getType() == Type.ARRAY_ITEM) {
      Symbol valueSymbol = currentScope.resolve(value.getValue());
      if (valueSymbol == null) {
        System.out.println("Error: no such variable: " + value.getValue());
      }
    }

    return visitChildren(node);
  }

  public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {
    String constructorName = constructorNode.getValue();

    if (!(classSymbol instanceof SymbolTable.Class)) {
      System.out.println("Error: The symbol must be an instance of class");
      return constructorNode;
    }

    if (!constructorName.equals(classSymbol.name)) {
      System.out.println("Error: Constructor name must match class name: " + classSymbol.name);
      return constructorNode;
    }

    Function constructor = new Function(constructorName, classSymbol.name);
    Symbol alreadyDeclared = currentScope.resolve(constructorName);

    if (alreadyDeclared != null) {
      System.out.println("Error: Constructor " + constructorName + " already exists");
    } else {
      currentScope.bind(constructor);
    }

    Scope constructorScope = new Scope(currentScope);
    currentScope.innerScopes.add(constructorScope);
    currentScope = constructorScope;

    for (ASTNode child : constructorNode.children) {
      if (child.getType() == Type.PARAMS) {
        visitParams(child);
      }
    }

    visitChildren(constructorNode);

    currentScope = currentScope.enclosingScope;

    return constructorNode;
  }

  public Symbol getTypeEqual(String type, ASTNode node) {
    Symbol typeSymbol;

    if (type.equals("classtype")) {
      typeSymbol = currentScope.resolve(node.children.getFirst().getValue());
    } else {
      typeSymbol = currentScope.resolve(type);
    }

    return typeSymbol;
  }

  public Symbol visitObj_usage(ASTNode node) {
    Symbol usage = currentScope.resolve(node.getValue());
    if (!(usage instanceof SymbolTable.Class)) {
      usage = currentScope.resolve(usage.type);
    }
    return ((SymbolTable.Class) usage).getClassScope().resolve(node.children.getFirst().getValue());
  }
}
