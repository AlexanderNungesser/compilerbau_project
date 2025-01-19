import SymbolTable.*;
import SymbolTable.Class;
import java.util.ArrayList;
import java.util.HashMap;

public class FirstScopeVisitor {
  Scope currentScope;

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgram(node);
        break;
      case Type.VAR_DECL:
        visitVardecl(node);
        break;
      case Type.VAR_REF:
        visitVarRef(node);
        break;
      case Type.ARRAY_DECL:
        visitArrayDecl(node);
        break;
      case Type.ARRAY_INIT:
        visitArrayInit(node);
        break;
      case Type.ARRAY_REF:
        visitArrayRef(node);
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
        break;
      case Type.FN_CALL:
        visitFncall(node);
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

  private void setChildrensScope(ASTNode node) {
    for(ASTNode child : node.children) {
      child.setScope(currentScope);
    }
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
    program.setScope(currentScope);

    visitChildren(program);
    return program;
  }

  public ASTNode visitFncall(ASTNode fncall) {
    fncall.setScope(currentScope);
    for (ASTNode child : fncall.children) {
      child.setScope(currentScope);
      if (child.getType() == Type.CLASSTYPE) {
        Symbol classtype = currentScope.resolve(child.getValue());
        if (classtype != null) {
          currentScope.bind(new Variable(fncall.getValue(), classtype.name));
        } else {
          System.out.println(
              "Error: cannot create object of class, cause class "
                  + classtype.name
                  + " does not exist");
        }
      }
      visitChildren(child);
    }
    return fncall;
  }

  public ASTNode visitVardecl(ASTNode variableNode) {
    variableNode.setScope(currentScope);
    setChildrensScope(variableNode);
    ASTNode firstChild = variableNode.children.getFirst();
    String type = firstChild.getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, firstChild);
    // TODO: an die richtige stelle packen; BSP: B z = y; --> als FNCALL 'operator='
    // --operator= (FN_CALL)
    //  |-- z (CLASSTYPE)
    //  |   '__ B (ID)
    //  '__ (ARGS)
    //      '__ y (ID)
    //    if(firstChild.getType() == Type.CLASSTYPE && variableNode.children.size() > 1) {
    //      variableNode.setType(Type.FN_CALL);
    //      variableNode.setValue("operator=");
    //      ASTNode arg = variableNode.children.removeLast();
    //      ASTNode args = new ASTNode(Type.ARGS);
    //      args.addChild(arg);
    //      variableNode.addChild(args);
    //    }

    Variable variable = new Variable(firstChild.getValue(), typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(variable.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + variable.name + " already exists");
    } else {
      currentScope.bind(variable);
    }

    visitChildren(variableNode);

    if (variableNode.children.size() == 2) {
      visit(variableNode.children.getLast());
    }

    return variableNode;
  }

  public ArrayList<Integer> countArray(ASTNode node) {
    ArrayList<Integer> sizes = new ArrayList<Integer>();
    int length = 0;
    for (ASTNode child : node.children) {
      switch (child.getType()) {
        case Type.ARRAY:
          length = 0;
          countArray(child);
          break;
        default:
          length++;
          break;
      }
      sizes.add(length);
    }
    return sizes;
  }

  public ASTNode visitArray(ASTNode node) {
    node.setScope(currentScope);
    ArrayList<Integer> sizes = new ArrayList<>();
    int length = 0;
    for (ASTNode child : node.children) {
      child.setScope(currentScope);
      switch (child.getType()) {
        case Type.ARRAY:
          length = 0;
          visitArray(child);
          break;
        default:
          length++;
          visitExpr(child);
          break;
      }
      sizes.add(length);
    }
    return node;
  }

  public ASTNode visitArrayDecl(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
    String type = node.children.getFirst().getType().name().toLowerCase();
    ASTNode firstChild = node.children.getFirst();
    Symbol typeSymbol = getTypeEqual(type, firstChild);

    Array arr = new Array(firstChild.getValue(), typeSymbol.name, firstChild.children.size());
    for (int i = 0; i < firstChild.children.size(); i++) {
      ASTNode expr = visitExpr(firstChild.children.get(i));
      // TODO was wenn expr kein Int?
      if (expr.getType() == Type.INT) {
        arr.length[i] = Integer.parseInt(expr.getValue());
      }
    }

    Symbol alreadyDeclared = currentScope.resolve(arr.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + arr.name + " already exists");
    } else {
      currentScope.bind(arr);
    }

    return node;
  }

  public ASTNode visitArrayInit(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
    String type = node.children.getFirst().getType().name().toLowerCase();
    ASTNode firstChild = node.children.getFirst();
    Symbol typeSymbol = getTypeEqual(type, firstChild);

    int dimensions = 0;
    Array arr = new Array(firstChild.getValue(), typeSymbol.name, dimensions);
    ArrayList<Integer> sizes = countArray(node.children.getLast());

    if (!firstChild.children.isEmpty()) {
      dimensions = firstChild.children.size();
      arr = new Array(firstChild.getValue(), typeSymbol.name, dimensions);
      for (int i = 0; i < firstChild.children.size(); i++) {
        ASTNode expr = visitExpr(firstChild.children.get(i));
        // TODO was wenn expr kein Int?
        if (expr.getType() == Type.INT) {
          arr.length[i] = Integer.parseInt(expr.getValue());
        }
      }
    } else {
      dimensions = sizes.size();
    }

    if (dimensions != sizes.size()) {
      System.out.println("Error: initial and declaration dimensions mismatch");
    }
    if (firstChild.children.isEmpty()) {
      arr = new Array(firstChild.getValue(), typeSymbol.name, dimensions);
      for (int i = 0; i < dimensions; i++) {
        arr.length[i] = sizes.get(i);
      }
    }

    Symbol alreadyDeclared = currentScope.resolve(arr.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + arr.name + " already exists");
    } else {
      currentScope.bind(arr);
    }

    visitArray(node.children.getLast());
    return node;
  }

  public ASTNode visitArrayRef(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
    ASTNode lastChild = node.children.getLast();
    Symbol lastSymbol = currentScope.resolve(lastChild.getValue());

    if (lastSymbol == null) {
      System.out.println("Error: such variable " + lastChild.getValue() + " does not exist");
    }

    ASTNode firstChild = node.children.getFirst();
    String type = firstChild.getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, firstChild);

    Array arr = new Array(firstChild.getValue(), typeSymbol.name, firstChild.children.size());
    for (int i = 0; i < firstChild.children.size(); i++) {
      ASTNode expr = visitExpr(firstChild.children.get(i));
      if (expr.getType() == Type.INT || expr.getType() == Type.ID) {
        arr.length[i] = expr.getValue();
      } else {
        System.out.println(
            "Error: type " + expr.getType().name() + " not cannot describe array length");
      }
    }
    Reference arrRef = new Reference(firstChild.getValue(), typeSymbol.name);

    Symbol alreadyDeclared = currentScope.resolve(firstChild.getValue());
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + firstChild.getValue() + " already exists");
    } else {
      //      if (lastSymbol instanceof Array) {
      //        if (!Arrays.equals(((Array) lastSymbol).length, arr.length)) {
      //          System.out.println("Error: initial and reference dimensions mismatch");
      //        }
      //      }
      currentScope.bind(arrRef);
    }

    return node;
  }

  public Symbol visitObj_usage(ASTNode node) {
    ASTNode classObject = node.children.getFirst();

    if (classObject.getType() == Type.OBJ_USAGE) {
      return visitObj_usage(classObject);
    }

    Symbol objectSymbol = currentScope.resolve(classObject.getValue());
    Symbol classSymbol = currentScope.resolve(objectSymbol.type);
    Scope classScope = ((SymbolTable.Class) classSymbol).getClassScope();

    Symbol usedValueOfObject = classScope.resolve(node.children.getLast().getValue());
    return usedValueOfObject;
  }

  public ASTNode visitVarRef(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
    ASTNode lastChild = node.children.getLast();

    if (lastChild.getType() != Type.ID && lastChild.getType() != Type.OBJ_USAGE) {
      System.out.println("Error: reference got assigned to a value");
      return null;
    }

    Symbol lastSymbol;
    if (lastChild.getType() == Type.OBJ_USAGE) {
      lastSymbol = visitObj_usage(lastChild);
    } else {
      lastSymbol = currentScope.resolve(lastChild.getValue());
    }

    if (lastSymbol == null) {
      System.out.println("Error: such variable " + lastChild.getValue() + " does not exist");
    }

    Variable var = new Variable(lastChild.getValue(), lastSymbol.name);

    ASTNode firstChild = node.children.getFirst();
    String type = firstChild.getType().name().toLowerCase();
    Symbol typeSymbol = getTypeEqual(type, firstChild);

    Reference refVariable = new Reference(firstChild.getValue(), typeSymbol.name);
    Symbol alreadyDeclared = currentScope.resolve(refVariable.name);
    if (alreadyDeclared != null) {
      System.out.println("Error: such variable " + refVariable.name + " already exists");
    } else {
      currentScope.bind(refVariable);
    }

    refVariable.setOrigin(var);

    return node;
  }

  public ASTNode visitArrayItem(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
    String arrayName = node.getValue();
    Symbol symbol = currentScope.resolve(arrayName);
    if (symbol == null) {
      System.out.println("Error: array " + arrayName + " not found");
    }

    if (symbol instanceof Array) {
      //      for (int i = 0; i < node.children.size(); i++) {
      //        ASTNode expr = visitExpr(node.children.get(i));
      //        // TODO was wenn expr kein Int?
      //        if (expr.getType() == Type.INT && ((Array) symbol).length[i] instanceof Integer) {
      //          if (((Array) symbol).length[i] <= Integer.parseInt(expr.getValue())
      //                  || Integer.parseInt(expr.getValue()) < 0) {
      //            System.out.println(
      //                    "Error: index "
      //                            + expr.getValue()
      //                            + " is out of bounds "
      //                            + ((Array) symbol).length[i]);
      //          }
      //        }
      //      }
    } else {
      System.out.println("Error: no such array " + arrayName);
    }

    return node;
  }

  public ASTNode visitFndecl(ASTNode fndecl) {
    fndecl.setScope(currentScope);
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
      child.setScope(currentScope);
      if (child.getType() == Type.PARAMS) {
        visitParams(child);
      }
      if (child.getType() == Type.BLOCK) {
        visitBlock(child);
      }
      visit(child);
    }

    currentScope = currentScope.enclosingScope;
    return fndecl;
  }

  public ASTNode visitAbstractFn(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
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
    block.setScope(currentScope);
    Scope newScope = new Scope(currentScope);
    currentScope.innerScopes.add(newScope);
    currentScope = newScope;

    visitChildren(block);

    currentScope = currentScope.enclosingScope;
    return block;
  }

  public ASTNode visitParams(ASTNode node) {
    node.setScope(currentScope);
    for (ASTNode child : node.children) {
      child.setScope(currentScope);
      String name = child.getValue();
      String type = child.getType().name().toLowerCase();
      Symbol typeSymbol = getTypeEqual(type, child);
      Symbol param = new Variable(name, typeSymbol.name);
      currentScope.bind(param);
      if (!child.children.isEmpty() && child.children.getLast().getType() != Type.REF) {
        visitExpr(child.children.getLast());
      } else {
        setChildrensScope(child);
      }
    }
    return node;
  }

  public ASTNode visitClass(ASTNode classNode) {
    classNode.setScope(currentScope);
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
      child.setScope(currentScope);
      switch (child.getType()) {
//        case Type.VAR_DECL: // Attribute
//          visitVardecl(child);
//          break;
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
//        case Type.FN_DECL: // Methoden
//          visitFndecl(child);
//          break;
//        case Type.ABSTRACT_FN:
//          visitAbstractFn(child);
//          break;
      }
    }
    if (!mustHave.get(Type.CONSTRUCTOR)) {
      ASTNode constructorNode = new ASTNode(Type.CONSTRUCTOR, classNode.getValue());
      constructorNode.setScope(currentScope);
      String superclassName =
          classNode.children.stream()
              .filter(c -> c.getType() == Type.CLASSTYPE)
              .map(n -> n.children.getFirst().getValue())
              .findFirst()
              .orElse("Unknown");
      if (!superclassName.equals("Unknown")) {
        constructorNode.addChild(new ASTNode(Type.ID, superclassName));
        Scope superclassScope = currentScope.resolve(superclassName).scope;
        Symbol superclassConstructor = superclassScope.resolve(superclassName);
        if (superclassConstructor != null) {
          if (superclassConstructor instanceof Function) {
            if (((Function) superclassConstructor).getParamCount() > 0) {
              System.out.println(
                  "Error: constructor must be implemented, because superclass has no base constructor");
            }
          }
        }
      }
      classNode.addChild(constructorNode);
      setChildrensScope(constructorNode);
    }
    if (!mustHave.get(Type.COPY_CONSTRUCTOR)) {
      ASTNode copyConstructorNode =
          new ASTNode(Type.COPY_CONSTRUCTOR, "copy_" + classNode.getValue());
      copyConstructorNode.setScope(currentScope);
      ASTNode ref = new ASTNode(Type.CLASSTYPE, "ref");
      ref.addChild(new ASTNode(Type.ID, classNode.getValue()));
      ref.addChild(new ASTNode(Type.REF));
      copyConstructorNode.addChild(ref);
      setChildrensScope(ref);
      String superclassName =
          classNode.children.stream()
              .filter(c -> c.getType() == Type.CLASSTYPE)
              .map(n -> n.children.getFirst().getValue())
              .findFirst()
              .orElse("Unknown");
      if (!superclassName.equals("Unknown")) {
        copyConstructorNode.addChild(new ASTNode(Type.ID, superclassName));
        ASTNode arg = new ASTNode(Type.ARGS);
        arg.addChild(new ASTNode(Type.ID, ref.children.getFirst().getValue()));
        copyConstructorNode.addChild(arg);
      }
      classNode.addChild(copyConstructorNode);
      setChildrensScope(copyConstructorNode);
    }
    if (!mustHave.get(Type.DESTRUCTOR)) {
      ASTNode destructorNode = new ASTNode(Type.DESTRUCTOR);
      destructorNode.setScope(currentScope);
      // TODO: how to handle "virtual" -> should be value of destructorNode
      destructorNode.addChild(new ASTNode(Type.ID, classNode.getValue()));
      classNode.addChild(destructorNode);
      setChildrensScope(destructorNode);
    }
    if (!mustHave.get(Type.OPERATOR)) {
      ASTNode operatorNode = new ASTNode(Type.OPERATOR, "operator=");
      operatorNode.setScope(currentScope);
      ASTNode child = new ASTNode(Type.ID, classNode.getValue());
      child.addChild(new ASTNode(Type.REF));
      operatorNode.addChild(child);
      ASTNode param = new ASTNode(Type.PARAMS);
      ASTNode ref = new ASTNode(Type.CLASSTYPE, "ref");
      ref.addChild(new ASTNode(Type.ID, classNode.getValue()));
      ref.addChild(new ASTNode(Type.REF));
      param.addChild(ref);
      operatorNode.addChild(param);
      classNode.addChild(operatorNode);
      setChildrensScope(operatorNode);
      setChildrensScope(param);
      setChildrensScope(ref);
      setChildrensScope(child);
    }

    visitChildren(classNode);
    currentScope = currentScope.enclosingScope;

    return classNode;
  }

  public ASTNode visitExpr(ASTNode node) {
    node.setScope(currentScope);
    if (!node.children.isEmpty() && node.getType() != Type.ID) {
      if (node.getType() == Type.OBJ_USAGE) {
        visitObj_usage(node);
      }
      visitChildren(node);
    }
    return node;
  }

  public ASTNode visitChildren(ASTNode node) {
    node.setScope(currentScope);
    for (ASTNode child : node.children) {
      child.setScope(currentScope);
      visit(child);
    }
    return node;
  }

  public ASTNode visitMain(ASTNode node) {
    node.setScope(currentScope);
    setChildrensScope(node);
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
