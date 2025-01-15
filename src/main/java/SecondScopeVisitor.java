import SymbolTable.*;
import java.util.HashSet;
import java.util.Set;

public class SecondScopeVisitor {
  Scope currentScope;
  Set<Scope> visitedScopes = new HashSet<>();

  public SecondScopeVisitor(Scope scope) {
    this.currentScope = scope;
  }

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
        visitClass(node);
        break;
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
    visitChildren(program);
    return program;
  }

  public ASTNode visitFncall(ASTNode fncall) {
    String functionName = fncall.getValue();

    if (!fncall.children.isEmpty() && fncall.children.getFirst().getType() == Type.CLASS) {
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
            case Type.DESTRUCTOR:
              visitDestructor(child, currentScope.resolve(classNode.getValue()));
              break;
            case Type.COPY_CONSTRUCTOR:
              visitCopyConstructor(child, currentScope.resolve(classNode.getValue()));
              break;
            case Type.OPERATOR:
              visitOperator(child, currentScope.resolve(classNode.getValue()));
              break;
          }
        }
        this.currentScope = this.currentScope.enclosingScope;
        visitedScopes.add(scope);
      }
    }
    return classNode;
  }

  private ASTNode visitCopyConstructor(ASTNode copyconstNode, Symbol classSymbol) {
    String copyconstName = copyconstNode.getValue();

    ASTNode paramType = copyconstNode.children.getFirst().children.getFirst();

    if (!copyconstName.equals(classSymbol.name)) {
      System.out.println("Error: Operator return type must match class name: " + classSymbol.name);
    } else if (!paramType.getValue().equals(classSymbol.name)) {
      System.out.println("Error: Param type must match class name: " + classSymbol.name);
    }

    if (!(copyconstName.equals(paramType.getValue()))) {
      System.out.println("Error: Return type ID must match parameter ID");
    }

    Function operator = new Function(copyconstName, classSymbol.name);

    currentScope.bind(operator);

    Scope constructorScope = new Scope(currentScope);
    currentScope.innerScopes.add(constructorScope);
    currentScope = constructorScope;

    ASTNode param = new ASTNode(Type.PARAMS);
    param.addChild(copyconstNode.children.getFirst());
    visitParams(param);

    visit(copyconstNode.children.getLast());

    currentScope = currentScope.enclosingScope;

    return copyconstNode;
  }

  public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {
    String constructorName = constructorNode.getValue();

    if (!constructorName.equals(classSymbol.name)) {
      System.out.println("Error: Constructor name must match class name: " + classSymbol.name);
    }

    Function constructor = new Function(constructorName, classSymbol.name);

    currentScope.bind(constructor);

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

  public ASTNode visitDestructor(ASTNode destructorNode, Symbol classSymbol) {
    String destructorName = destructorNode.getValue();

    Symbol alreadyDeclared = currentScope.resolve(destructorName);
    if (alreadyDeclared != null) {
      System.out.println("Error: Destructor " + destructorName + " already exists.");
    }

    Function destructor = new Function(destructorName, classSymbol.name);
    currentScope.bind(destructor);

    Scope destructorScope = new Scope(currentScope);
    currentScope.innerScopes.add(destructorScope);
    currentScope = destructorScope;

    visitChildren(destructorNode);

    currentScope = currentScope.enclosingScope;

    return destructorNode;
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

  public ASTNode visitOperator(ASTNode operatorNode, Symbol classSymbol) {
    String operatorName = operatorNode.getValue();

    ASTNode returnTypeID = operatorNode.children.getFirst();
    ASTNode paramID = operatorNode.children.get(1).children.getFirst().children.getFirst();

    if (!returnTypeID.getValue().equals(classSymbol.name)) {
      System.out.println("Error: Operator return type must match class name: " + classSymbol.name);
    } else if (!paramID.getValue().equals(classSymbol.name)) {
      System.out.println("Error: Param type must match class name: " + classSymbol.name);
    }

    if (!(returnTypeID.getValue().equals(paramID.getValue()))) {
      System.out.println("Error: Return type ID must match parameter ID");
    }

    Function operator = new Function(operatorName, classSymbol.name);

    currentScope.bind(operator);

    Scope constructorScope = new Scope(currentScope);
    currentScope.innerScopes.add(constructorScope);
    currentScope = constructorScope;

    for (ASTNode child : operatorNode.children) {
      if (child.getType() == Type.PARAMS) {
        visitParams(child);
      }
    }

    visit(operatorNode.children.getLast());

    currentScope = currentScope.enclosingScope;

    return operatorNode;
  }

}
