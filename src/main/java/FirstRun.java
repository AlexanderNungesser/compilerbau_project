import SymbolTable.*;

public class FirstRun extends CppParseTreeVisitor {
  Scope currentScope;

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgram(node);
        break;
      case Type.VAR_DECL:
        visitVardecl(node);
        break;
      case Type.ARRAY:
        visitArray(node);
        break;
      case Type.FN_DECL:
        visitFndecl(node);
        break;
      case Type.ABSTRACT_FN:
        visitAbstractFn(node);
        break;
      case Type.BLOCK:
        visitBlock(node);
        break;
      case Type.PARAMS:
        visitParams(node);
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

  public ASTNode visitProgram(ASTNode program) {
    Scope globalScope = new Scope(null);

    globalScope.bind(new BuiltIn("int"));
    globalScope.bind(new BuiltIn("bool"));
    globalScope.bind(new BuiltIn("char"));
    globalScope.bind(new BuiltIn("void"));
    globalScope.bind(new BuiltIn("print_int"));
    globalScope.bind(new BuiltIn("print_bool"));
    globalScope.bind(new BuiltIn("print_char"));

    currentScope = globalScope;

    visitChildren(program);
    return program;
  }

  public ASTNode visitVardecl(ASTNode variableNode) {
    String type = variableNode.children.getFirst().getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, variableNode.children.getFirst());
    // TODO: Arrays?
    Symbol variable = new Variable(variableNode.children.getFirst().getValue(), typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(variable.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + variable.name + " already exists");
    } else {
      currentScope.bind(variable);
    }

    return variableNode;
  }

  public ASTNode visitArray(ASTNode node) {
    for (ASTNode child : node.children) {
      switch (child.getType()) {
        case Type.ARRAY:
          visitArray(child);
          break;
        default:
          visitExpr(child);
          break;
      }
    }
    return node;
  }

  public ASTNode visitArrayItem(ASTNode node) {
    String arrayName = node.getValue();
    Symbol symbol = currentScope.resolve(arrayName);
    if (symbol == null) {
      System.out.println("Error: array " + arrayName + " not found");
    }
    return node;
  }

  public ASTNode visitFndecl(ASTNode fndecl) {
    ASTNode functionInformation = fndecl.children.getFirst();
    String name = functionInformation.children.getFirst().getValue();
    String type = functionInformation.getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, functionInformation);

    Function function = new Function(name, typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such function " + name + " already exists");
    } else {
      currentScope.bind(function);
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    for (ASTNode child : fndecl.children) {
      if (child.getType() == Type.PARAMS) {
        for (ASTNode param : child.children) {
          function.increaseParamCount();
        }
      }
    }

    visitChildren(fndecl);

    currentScope = currentScope.enclosingScope;
    return fndecl;
  }

  public ASTNode visitArgs(ASTNode args) {
    for (ASTNode child : args.children) {
      if (child.getType() == Type.OBJ_USAGE || child.getType() == Type.ID) {
        currentScope.resolve(child.getType().name().toLowerCase());
      }
    }

    return args;
  }

  public ASTNode visitAbstractFn(ASTNode node) {
    if (!node.children.getLast().getValue().equals("0")) {
      System.out.println(
          "Error: function "
              + node.getValue()
              + " is not abstract, "
              + node.children.getLast().getValue()
              + " must be 0");
    }
    ASTNode funcInfo = node.children.getFirst();
    String name = funcInfo.children.getFirst().getValue();
    String type = funcInfo.getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, funcInfo);

    Function function = new Function(name, typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such abstract function " + name + " already exists");
    } else {
      currentScope.bind(function);
    }
    return node;
  }

  public ASTNode visitBlock(ASTNode block) {
    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    visitChildren(block);

    currentScope = currentScope.enclosingScope;
    return block;
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

  // TODO: Big 3 erstellen destructo copyconstructor constructor!
  public ASTNode visitClass(ASTNode classNode) {
    String name = classNode.getValue();
    Symbol classType = currentScope.resolve(name);
    SymbolTable.Class classSymbol = new SymbolTable.Class(name);

    if (classType == null) {
      currentScope.bind(classSymbol);
    } else {
      if (!(classType instanceof SymbolTable.Class)) {
        currentScope.bind(classSymbol);
      } else {
        System.out.println("Error: such class " + name + " already exists");
      }
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    classSymbol.setClassScope(currentScope);

    for (ASTNode child : classNode.children) {
      switch (child.getType()) {
        case VAR_DECL: // Attribute
          visitVardecl(child);
          break;
        case FN_DECL: // Methoden
          visitFndecl(child);
          break;
        case CONSTRUCTOR: // Konstruktor
          visitConstructor(child, classSymbol);
          break;
        case DESTRUCTOR:
          break;
        case ABSTRACT_FN:
          visitAbstractFn(child);
          break;
      }
    }

    currentScope = currentScope.enclosingScope;

    return classNode;
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

  public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {
    String constructorName = constructorNode.children.getFirst().getValue();

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

  public Symbol getTypeEqual(String type, ASTNode node) {
    Symbol typeSymbol;

    if (type.equals("classtype")) {
      typeSymbol = currentScope.resolve(node.children.getFirst().getValue());
    } else {
      typeSymbol = currentScope.resolve(type);
    }

    return typeSymbol;
  }
}
