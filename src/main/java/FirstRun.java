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
      case Type.ARGS:
        visitArgs(node);
        break;
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

  public ASTNode visitProgram(ASTNode program) {
    Scope globalScope = new Scope(null);

    globalScope.bind(new BuiltIn("int"));
    globalScope.bind(new BuiltIn("bool"));
    globalScope.bind(new BuiltIn("char"));
    globalScope.bind(new BuiltIn("void"));

    currentScope = globalScope;

    visitChildren(program);
    return program;
  }

  public ASTNode visitVardecl(ASTNode vardecl) {
    ASTNode variableNode = vardecl.children.getFirst();
    String type = variableNode.children.getFirst().getType().name().toLowerCase();
    Symbol typeSymbol;

    typeSymbol = getType(type, variableNode);

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
    ASTNode functionInformation = fndecl.children.getFirst();
    String name = functionInformation.getValue();
    String type = functionInformation.getType().name().toLowerCase();
    Symbol typeSymbol;

    typeSymbol = getType(type, functionInformation);

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

    ASTNode params = visitChildren(fndecl);
    for (ASTNode param : params.children) {
      function.increaseAnzParams();
    }

    currentScope = currentScope.enclosingScope;
    return fndecl;
  }

  public ASTNode visitFncall(ASTNode fncall) {
    String functionName = fncall.getValue();
    Symbol function = currentScope.resolve(functionName);

    if (function == null) System.out.println("Error: no such function: " + functionName);
    if (function instanceof Variable)
      System.out.println("Error: " + functionName + " is not a function");

    ASTNode args = visitChildren(fncall);
    int args_count = args.children.size();

    if (args_count != ((Function) function).getAnzParams()) {
      System.out.println("Error: arg and param count mismatch");
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
      Symbol typeSymbol;

        typeSymbol = getType(type, child);

      Symbol param = new Variable(name, typeSymbol.name);
      currentScope.bind(param);
    }
    return node;
  }

  public ASTNode visitClass(ASTNode classNode) {
    String name = classNode.children.getFirst().getValue();
    Symbol classType = currentScope.resolve(name);
    Symbol classSymbol = new Class(name, name);
    if (classType == null) {
      currentScope.bind(classSymbol);
    }else {
      if(!(classType instanceof Class)) {
        currentScope.bind(classSymbol);
      } else {
        System.out.println("Error: such class " + name + " already exists");
        //throw new RuntimeException("Error: such class " + name + " already exists");
      }
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;
    ((Class) classSymbol).setClassScope(currentScope);

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

    public ASTNode visitChildren (ASTNode node){
      for (ASTNode child : node.children) {
        visit(child);
      }
      return node;
    }

    public Symbol getType (String type, ASTNode node){
      Symbol typeSymbol;

      if (type.equals("classtype")) {
        typeSymbol = currentScope.resolve(node.children.getFirst().getValue());
      } else {
        typeSymbol = currentScope.resolve(type);
      }
      return typeSymbol;
    }

  }
