import SymbolTable.*;
import SymbolTable.Class;

public class SecondScopeVisitor {
  Scope currentScope;

  public SecondScopeVisitor(Scope scope) {}

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
      case Type.BLOCK, Type.FN_DECL:
        visitChildren(node);
        break;
        case Type.CLASS:
        visitClass(node);
        break;
      case Type.OBJ_USAGE:
        visitObjUsage(node);
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

  public ASTNode visitProgram(ASTNode program) {
    visitChildren(program);
    this.currentScope = program.getScope();
    return program;
  }

  public ASTNode visitFncall(ASTNode fncall) {
    this.currentScope = fncall.getScope();
    String functionName = fncall.getValue();

    if (!fncall.children.isEmpty() && fncall.children.getFirst().getType() == Type.CLASSTYPE) {
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
        Symbol typeSymbol = currentScope.resolve(function.type);
        if (!(typeSymbol instanceof Class)) {
          System.out.println("Error: " + functionName + " is not a function");
        }
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
          if(function instanceof Reference) {
            function = ((Reference) function).getOrigin();
          }
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
    this.currentScope = args.getScope();
    for (ASTNode child : args.children) {
      if (child.getType() == Type.OBJ_USAGE || child.getType() == Type.ID) {
        currentScope.resolve(child.getType().name().toLowerCase());
      }
    }

    return args;
  }

  public ASTNode visitParams(ASTNode node) {
    this.currentScope = node.getScope();
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
    this.currentScope = node.getScope();
    Symbol variable;
    if (node.children.isEmpty() && node.getType() == Type.ID && !node.getValue().equals("this")) {
      variable = currentScope.resolve(node.getValue());
      if (variable == null) {
        System.out.println("Error: no such variable: " + node.getValue());
      }
    } else {
      if (node.getType() == Type.OBJ_USAGE) {
        variable = getSymbolOfObjUsage(node);
        if (variable == null) {
          System.out.println("Error: no such variable: " + node.getValue());
        }
      } else {
        visitChildren(node);
      }
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
    this.currentScope = node.getScope();
    Symbol variable;
    ASTNode firstChild = node.children.getFirst();

    if (firstChild.getType() == Type.OBJ_USAGE) {
      variable = getSymbolOfObjUsage(firstChild);
    } else {
      variable = currentScope.resolve(firstChild.getValue());
    }

    if (variable == null) {
      System.out.println("Error: no such variable: " + node.children.getFirst().getValue());
    }

    ASTNode value = node.children.getLast();
    if (value.getType() == Type.OBJ_USAGE) {
      Symbol valueSymbol = getSymbolOfObjUsage(value);
      if (valueSymbol == null) {
        System.out.println("Error: no such variable: " + value.getValue());
      }
    }
    if (value.getType() == Type.ID || value.getType() == Type.ARRAY_ITEM) {
      Symbol valueSymbol = currentScope.resolve(value.getValue());
      if (valueSymbol == null) {
        System.out.println("Error: no such variable: " + value.getValue());
      }
    }

    visitChildren(node);

    return node;
  }

  public ASTNode visitClass(ASTNode classNode) {
        this.currentScope = classNode.getScope();
        for (ASTNode child : classNode.children) {
          switch (child.getType()) {
            case Type.FN_DECL: // Methoden
              visitChildren(child);
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

    return classNode;
  }

  private ASTNode visitCopyConstructor(ASTNode copyconstNode, Symbol classSymbol) {
    this.currentScope = copyconstNode.getScope();
    String copyconstName = copyconstNode.getValue().replaceFirst("copy_", "");

    ASTNode paramType = copyconstNode.children.getFirst().children.getFirst();

    if (!copyconstName.equals(classSymbol.name)) {
      System.out.println("Error: Operator return type must match class name: " + classSymbol.name);
    } else if (!paramType.getValue().equals(classSymbol.name)) {
      System.out.println("Error: Param type must match class name: " + classSymbol.name);
    }

    if (!(copyconstName.equals(paramType.getValue()))) {
      System.out.println("Error: Return type ID must match parameter ID");
    }

    Function operator = new Function("copy_" + copyconstName, classSymbol.name);

    currentScope.bind(operator);

    ASTNode param = new ASTNode(Type.PARAMS);
    param.setScope(currentScope);
    param.addChild(copyconstNode.children.getFirst());
    visitParams(param);

    visit(copyconstNode.children.getLast());

    return copyconstNode;
  }

  public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {
    this.currentScope = constructorNode.getScope();
    String constructorName = constructorNode.getValue();

    if (!constructorName.equals(classSymbol.name)) {
      System.out.println("Error: Constructor name must match class name: " + classSymbol.name);
    }

    Function constructor = new Function(constructorName, classSymbol.name);

    currentScope.bind(constructor);

    for (ASTNode child : constructorNode.children) {
      this.currentScope = child.getScope();
      if (child.getType() == Type.PARAMS) {
        visitParams(child);
      }
    }

    visitChildren(constructorNode);

    return constructorNode;
  }

  public ASTNode visitDestructor(ASTNode destructorNode, Symbol classSymbol) {
    this.currentScope = destructorNode.getScope();
    String destructorName = "~" + destructorNode.children.getFirst().getValue();

    Symbol alreadyDeclared = currentScope.resolve(destructorName);
    if (alreadyDeclared != null) {
      System.out.println("Error: Destructor " + destructorName + " already exists.");
    }

    Function destructor = new Function(destructorName, classSymbol.name);
    currentScope.bind(destructor);

    visitChildren(destructorNode);


    return destructorNode;
  }

  public Symbol getTypeEqual(String type, ASTNode node) {
    this.currentScope = node.getScope();
    Symbol typeSymbol;

    if (type.equals("classtype")) {
      typeSymbol = currentScope.resolve(node.children.getFirst().getValue());
    } else {
      typeSymbol = currentScope.resolve(type);
    }

    return typeSymbol;
  }

  public ASTNode visitObjUsage(ASTNode node) {
    currentScope = node.getScope();
    ASTNode classObject = node.children.getFirst();

    if (classObject.getType() == Type.OBJ_USAGE) {
      return visit(classObject);
    }

    if(node.getValue() != null &&( node.getValue().equals("this") || node.getValue().equals("*this"))) {
      if(classObject.getValue().equals("this")) {
        return null;
      }
      return node;
    }

    Symbol objectSymbol = currentScope.resolve(classObject.getValue());
    Symbol classSymbol = currentScope.resolve(objectSymbol.type, "Class");
    Scope classScope = ((SymbolTable.Class) classSymbol).getClassScope();

//    visit(node.children.getLast());

    return node;
  }

  public Symbol getSymbolOfObjUsage(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode classObject = node.children.getFirst();

    if (classObject.getType() == Type.OBJ_USAGE) {
      return getSymbolOfObjUsage(classObject);
    }

    if(node.getValue() != null &&( node.getValue().equals("this") || node.getValue().equals("*this"))) {
      if(classObject.getValue().equals("this")) {
        return null;
      }
      return currentScope.resolve(classObject.getValue());
    }

    Symbol objectSymbol = currentScope.resolve(classObject.getValue());
    Symbol classSymbol = currentScope.resolve(objectSymbol.type, "Class");
    Scope classScope = ((SymbolTable.Class) classSymbol).getClassScope();

    Symbol usedValueOfObject = classScope.resolve(node.children.getLast().getValue());
    return usedValueOfObject;
  }

  public ASTNode visitOperator(ASTNode operatorNode, Symbol classSymbol) {
    this.currentScope = operatorNode.getScope();
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

    for (ASTNode child : operatorNode.children) {
      this.currentScope = child.getScope();
      if (child.getType() == Type.PARAMS) {
        visitParams(child);
      }
    }

    visit(operatorNode.children.getLast());

    return operatorNode;
  }
}
