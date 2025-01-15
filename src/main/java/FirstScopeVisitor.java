import SymbolTable.*;
import SymbolTable.Class;
import java.util.HashMap;

public class FirstScopeVisitor extends CppParseTreeVisitor {
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
      case Type.MAIN:
        visitMain(node);
        break;
      case Type.ARRAY_ITEM:
        visitArrayItem(node);
      case null:
        System.out.println("Type: " + node.getType().name() + " Value: " + node.getValue());
        break;
      default:
        if (node.children.isEmpty()) {
          break;
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
    boolean isArray = false;
    for (ASTNode child : variableNode.children) {
      if (child.getType() == Type.ARRAY) {
        isArray = true;
      }
    }
    Symbol variable;
    if (isArray) {
      variable = new Array(variableNode.children.getFirst().getValue(), typeSymbol.name);
    } else {
      variable = new Variable(variableNode.children.getFirst().getValue(), typeSymbol.name);
    }

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

    for (ASTNode child : fndecl.children) {
      if (child.getType() == Type.PARAMS) {
        visitParams(child);
      }
      if (child.getType() == Type.BLOCK) {
        visitBlock(child);
      }
    }

    currentScope = currentScope.enclosingScope;
    return fndecl;
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
      if (!child.children.isEmpty() && child.children.getLast().getType() != Type.REF) {
        visitExpr(child.children.getLast());
      }
    }
    return node;
  }

  // TODO: Big 3 erstellen destructo copyconstructor constructor!
  public ASTNode visitClass(ASTNode classNode) {
    String name = classNode.getValue();
    Symbol classType = currentScope.resolve(name);
    Class classSymbol = new Class(name);

    if (classType == null) {
      currentScope.bind(classSymbol);
    } else {
      if (!(classType instanceof Class)) {
        currentScope.bind(classSymbol);
      } else {
        System.out.println("Error: such class " + name + " already exists");
      }
    }

    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    classSymbol.setClassScope(currentScope);
    HashMap<Type, Boolean> mustHave = new HashMap<>();
    mustHave.put(Type.CONSTRUCTOR, false);
    mustHave.put(Type.COPY_CONSTRUCTOR, false);
    mustHave.put(Type.DESTRUCTOR, false);
    mustHave.put(Type.OPERATOR, false);
    for (ASTNode child : classNode.children) {
      switch (child.getType()) {
        case Type.VAR_DECL: // Attribute
          visitVardecl(child);
          break;
        case Type.CONSTRUCTOR:
          mustHave.put(Type.CONSTRUCTOR, true);
          break;
        case Type.COPY_CONSTRUCTOR:
          mustHave.put(Type.COPY_CONSTRUCTOR, true);
          break;
        case Type.DESTRUCTOR:
          mustHave.put(Type.DESTRUCTOR, true);
          break;
        case Type.OPERATOR:
          mustHave.put(Type.OPERATOR, true);
          break;
        case Type.FN_DECL: // Methoden
          visitFndecl(child);
          break;
        case Type.ABSTRACT_FN:
          visitAbstractFn(child);
          break;
      }
    }
    if (!mustHave.get(Type.CONSTRUCTOR).booleanValue()) {
      ASTNode constructorNode = new ASTNode(Type.CONSTRUCTOR, classNode.getValue());
      String superclassName =
          classNode.children.stream()
              .filter(c -> c.getType() == Type.CLASSTYPE)
              .map(n -> n.children.getFirst().getValue())
              .findFirst()
              .orElse("Unknown");
      if (!superclassName.equals("Unknown")) {
        constructorNode.addChild(new ASTNode(Type.ID, superclassName));
        // TODO: args of superclass constructor????
      }
      constructorNode.addChild(new ASTNode(Type.BLOCK));
      classNode.addChild(constructorNode);
    }
    if (!mustHave.get(Type.COPY_CONSTRUCTOR).booleanValue()) {
      ASTNode copyConstructorNode = new ASTNode(Type.COPY_CONSTRUCTOR, classNode.getValue());
      ASTNode child = new ASTNode(Type.CLASSTYPE, "ref");
      child.addChild(new ASTNode(Type.REF));
      copyConstructorNode.addChild(child);
      String superclassName =
          classNode.children.stream()
              .filter(c -> c.getType() == Type.CLASSTYPE)
              .map(n -> n.children.getFirst().getValue())
              .findFirst()
              .orElse("Unknown");
      if (!superclassName.equals("Unknown")) {
        copyConstructorNode.addChild(new ASTNode(Type.ID, superclassName));
        ASTNode arg = new ASTNode(Type.ARGS);
        arg.addChild(new ASTNode(Type.ID, child.getValue()));
        copyConstructorNode.addChild(arg);
      }
      copyConstructorNode.addChild(new ASTNode(Type.BLOCK));
      classNode.addChild(copyConstructorNode);
    }
    if (!mustHave.get(Type.DESTRUCTOR).booleanValue()) {
      ASTNode destructorNode = new ASTNode(Type.DESTRUCTOR);
      // TODO: how to handle "virtual" -> should be value of destructorNode
      destructorNode.addChild(new ASTNode(Type.ID, classNode.getValue()));
      destructorNode.addChild(new ASTNode(Type.BLOCK));
      classNode.addChild(destructorNode);
    }
    if (!mustHave.get(Type.OPERATOR).booleanValue()) {
      ASTNode operatorNode = new ASTNode(Type.OPERATOR, "operator=");
      ASTNode child = new ASTNode(Type.ID, classNode.getValue());
      child.addChild(new ASTNode(Type.REF));
      operatorNode.addChild(child);
      ASTNode param = new ASTNode(Type.PARAMS);
      ASTNode ref = new ASTNode(Type.CLASSTYPE, "ref");
      ref.addChild(new ASTNode(Type.ID, classNode.getValue()));
      ref.addChild(new ASTNode(Type.REF));
      param.addChild(ref);
      operatorNode.addChild(param);
      operatorNode.addChild(new ASTNode(Type.BLOCK));
      classNode.addChild(operatorNode);
    }

    currentScope = currentScope.enclosingScope;

    return classNode;
  }

  public ASTNode visitExpr(ASTNode node) {
    if (node.children.isEmpty() && node.getType() == Type.ID) {
      String name = node.getValue();
      Symbol var = currentScope.resolve(name);
      if (var == null) {
        System.out.println("Error: in Expr no such variable: " + name);
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

  public ASTNode visitMain(ASTNode node) {
    String name = node.getType().name().toLowerCase();
    String type = node.children.getFirst().getType().name().toLowerCase();
    Symbol typeSymbol = currentScope.resolve(type);
    if (typeSymbol == null) {
      System.out.println("Error: no such return type " + type);
    } else {
      Function function = new Function(name, typeSymbol.name);
      Symbol alreadyImplemented = currentScope.resolve(name);
      if (alreadyImplemented != null) {
        System.out.println("Error: such function " + name + " already exists");
      } else {
        currentScope.bind(function);
      }

      visitBlock(node.children.getLast());
    }
    return node;
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
