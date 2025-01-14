import SymbolTable.*;

import java.lang.Class;

public class FirstRun extends CppParseTreeVisitor {
  Scope currentScope;

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgram(node);
        break;
      case Type.VAR_DECL:
        visitVardecl(node.children.getFirst());
        break;
      case Type.FN_DECL:
        visitFndecl(node);
        break;
      case Type.FN_CALL:
        visitFncall(node);
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
      case Type.ASSIGN:
        visitAssign(node);
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
    globalScope.bind(new BuiltIn("print_int"));
    globalScope.bind(new BuiltIn("print_bool"));
    globalScope.bind(new BuiltIn("print_char"));

    currentScope = globalScope;

    visitChildren(program);
    return program;
  }

  public ASTNode visitVardecl(ASTNode variableNode) {
    String type = variableNode.getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, variableNode);

    Symbol variable = new Variable(variableNode.getValue(), typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(variable.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + variable.name + " already exists");
    } else {
      currentScope.bind(variable);
    }

    return variableNode;
  }

  public ASTNode visitFndecl(ASTNode fndecl) {
    ASTNode functionInformation = fndecl.children.getFirst();
    String name = functionInformation.children.getFirst().getValue();
    String type = functionInformation.getType().name().toLowerCase();
    Symbol typeSymbol;

    typeSymbol = getTypeEqual(type, functionInformation);

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
      if(child.getType() == Type.PARAMS) {
        for (ASTNode param : child.children) {
          function.increaseParamCount();
        }
      }
    }

    visitChildren(fndecl);

    currentScope = currentScope.enclosingScope;
    return fndecl;
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
          }else {
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
    Symbol classSymbol = new SymbolTable.Class(name);
    if (classType == null) {
      currentScope.bind(classSymbol);
    } else {
      if (!(classType instanceof SymbolTable.Class)) {
        currentScope.bind(classSymbol);
      } else {
        System.out.println("Error: such class " + name + " already exists");
        //throw new RuntimeException("Error: such class " + name + " already exists");
      }
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;
    ((SymbolTable.Class) classSymbol).setClassScope(currentScope);

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

    public ASTNode visitAssign(ASTNode node) {
      Symbol variable = currentScope.resolve(node.children.getFirst().getValue());
      if (variable == null) {
        System.out.println("Error: no such variable: " + node.children.getFirst().getValue());
      }

      ASTNode value = node.children.getLast();
      if(value.getType() == Type.ID || value.getType() == Type.OBJ_USAGE || value.getType() == Type.ARRAY_ITEM ) {
        Symbol valueSymbol = currentScope.resolve(value.getValue());
        if(valueSymbol == null) {
          System.out.println("Error: no such variable: " + value.getValue());
        }
      }


      return visitChildren(node);
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
