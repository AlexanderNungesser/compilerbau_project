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
      case Type.FN_DECL:
        visitFndecl(node.children.getFirst());
        break;
      case Type.FN_CALL:
        visitFncall(node.children.getFirst());
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

    currentScope = globalScope;

    visitChildren(program);
    return program;
  }

  public ASTNode visitVardecl(ASTNode vardecl) {
    ASTNode variableNode = vardecl.children.getFirst();
    Symbol typeSymbol = currentScope.resolve(variableNode.getType().name().toLowerCase());
    Symbol variable = new Variable(variableNode.getValue(), typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(variable.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + variable.name + " already exists");
    } else {
      currentScope.bind(variable);
    }

    return vardecl;
  }

  public ASTNode visitFndecl(ASTNode fndecl) {
    String name = fndecl.getValue();
    Symbol type = currentScope.resolve(fndecl.getType().name());
    Function function = new Function(name, type.name);

    Symbol alreadyDeclared = currentScope.resolve(name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such function " + name + " already exists");
    } else {
      currentScope.bind(function);
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    visitChildren(fndecl);

    currentScope = currentScope.enclosingScope;
    return fndecl;
  }

  public ASTNode visitFncall(ASTNode fncall) {
    String functionName = fncall.getValue();
    Symbol function = currentScope.resolve(functionName);

    if (function == null) System.out.println("Error: no such function: " + functionName);
    if (function instanceof Variable)
      System.out.println("Error: " + functionName + " is not a function");

    visitChildren(fncall);
    return fncall;
  }

  public ASTNode visitArgs(ASTNode args) {
    return args;
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
      Symbol type = currentScope.resolve(child.getType().name());
      Symbol param = new Variable(name, type.name);
      currentScope.bind(param);
    }
    return node;
  }

  public ASTNode visitClass(ASTNode classNode) {
    String name = classNode.getValue();
    Symbol classSymbol = currentScope.resolve(name);

    Symbol alreadyDeclared = currentScope.resolve(name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such class " + name + " already exists");
    } else {
      currentScope.bind(classSymbol);
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    visitChildren(classNode);

    currentScope = currentScope.enclosingScope;
    return classNode;
  }

  public ASTNode visitExpr(ASTNode node) {
    if (node.children.isEmpty() && node.getType().equals("ID")) {
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
}
