import SymbolTable.*;
import java.util.Objects;

public class TypeCheckVisitor {
  Scope currentScope;

  public TypeCheckVisitor(Scope scope) {
    this.currentScope = scope;
  }

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitProgram(node);
        break;
      case Type.OBJ_USAGE:
        visitObj_usage(node);
        break;
      case Type.BLOCK, Type.CLASS:
        visitChildren(node);
        break;
      case Type.FN_DECL:
        visitFndecl(node);
        break;
      case Type.FN_CALL:
        visitFncall(node);
        break;
      case Type.VAR_DECL, Type.VAR_REF:
        visitVardecl(node);
        break;
      case Type.ARRAY_REF:
        visitArrayRef(node);
      case Type.ASSIGN:
        visitAssign(node);
        break;
      case Type.ARRAY_INIT:
        visitArrayInit(node);
        break;
      case Type.ARRAY_DECL:
        visitArraydecl(node);
        break;
      case Type.ARRAY_ITEM:
        visitArrayItem(node);
        break;
      case Type.EQUAL, Type.NOT_EQUAL, Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL:
        visitCompare(node);
        break;
      case Type.NOT:
        visitNot(node);
        break;
      case Type.AND, Type.OR:
        visitBoolOperator(node);
        break;
      case Type.DEC_INC:
        visitDecInc(node);
        break;
      case Type.ADD, Type.SUB, Type.MUL, Type.DIV, Type.MOD:
        visitCalculate(node);
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

  public ASTNode visitChildren(ASTNode node) {
    for (ASTNode child : node.children) {
      visit(child);
    }
    return node;
  }

//  private ASTNode visitScopes(ASTNode node) {
//    for (Scope scope : this.currentScope.innerScopes) {
//      if (!visitedScopes.contains(scope)) {
//        this.currentScope = scope;
//        if (node.getType() == Type.CLASS) {
//          for (ASTNode child : node.children) {
//            switch (child.getType()) {
//              case Type.FN_DECL: // Methoden
//                visitScopes(child);
//                break;
//              case Type.CONSTRUCTOR:
//                visitConstructor(child, currentScope.resolve(node.getValue()));
//                break;
//                //                        case Type.DESTRUCTOR:
//                //                            visitDestructor(child,
//                // currentScope.resolve(classNode.getValue()));
//                //                            break;
//            }
//          }
//          this.currentScope = this.currentScope.enclosingScope;
//          visitedScopes.add(scope);
//          break;
//        } else {
//          visitChildren(node);
//        }
//        this.currentScope = this.currentScope.enclosingScope;
//        visitedScopes.add(scope);
//      }
//    }
//    return node;
//  }

  public ASTNode visitExpr(ASTNode node) {
    this.currentScope = node.getScope();
    Symbol variable;
    if (node.children.isEmpty() && node.getType() == Type.ID) {
      if (node.getType() == Type.OBJ_USAGE) {
        variable = visitObj_usage(node);
      } else {
        variable = currentScope.resolve(node.getValue());
      }
      if (variable == null) {
        System.out.println("Error: no such variable: " + node.getValue());
      }
    } else {
      visitChildren(node);
    }
    return node;
  }

  public Symbol visitObj_usage(ASTNode node) {
    this.currentScope = node.getScope();
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

  public ASTNode visitNot(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode firstChild = node.children.getFirst();
    if (firstChild.getType() == Type.AND
        || firstChild.getType() == Type.OR
        || firstChild.getType() == Type.NOT) {
      visit(firstChild);
    }
    String type = getEndType(node.children.getFirst());

    if (!typeIsValid(type)) {
      System.out.println("Error: invalid type for negation: " + type);
    }
    return node;
  }

  public ASTNode visitBoolOperator(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode firstChild = node.children.getFirst();
    if (firstChild.getType() == Type.AND
        || firstChild.getType() == Type.OR
        || firstChild.getType() == Type.NOT) {
      visit(firstChild);
    }
    String firstType = getEndType(node.children.getFirst());
    String secondType = getEndType(node.children.getLast());
    if (!typeIsValid(firstType)) {
      System.out.println("Error: invalid type for bool operation: " + firstType);
    } else if (!typeIsValid(secondType)) {
      System.out.println("Error: invalid type for bool operation: " + secondType);
    }
    return node;
  }


  public ASTNode visitFndecl(ASTNode node) {
    this.currentScope = node.getScope();
    this.currentScope = this.currentScope.innerScopes.getFirst();
    ASTNode returnTypeNode = node.children.getFirst();
    Type returnType = returnTypeNode.getType();

    for(int i = 1; i < node.children.size(); i++) {
      ASTNode paramNode = node.children.get(i);
      if(paramNode.getType() == Type.VAR_DECL) {
        ASTNode paramTypeNode = paramNode.children.getFirst();
        String paramType = paramTypeNode.getValue();

        if(!typeIsValid(paramType) && returnType != Type.CLASSTYPE) {
          System.out.println("Error: Invalid parameter type in function declaration: " + paramType);
        }
      }
    }

    if (returnTypeNode.getType() != Type.VOID){
      if(node.children.getLast().getType() == Type.BLOCK){
        if(!visitReturn(node.children.getLast(), returnType.name().toLowerCase())){
          System.out.println("Error: Return from type " + returnType.name().toLowerCase() + " expected.");
        }
      }
    }
    if(node.children.getLast().getType() == Type.BLOCK){
      visit(node.children.getLast());
    }
    this.currentScope = this.currentScope.enclosingScope;

    return node;
  }

  public boolean visitReturn(ASTNode node, String methodType) {
        for (ASTNode child : node.children) {
          this.currentScope = child.getScope();
          if (child.getType() == Type.RETURN) {
            if (child.children.isEmpty()) {
              if (!methodType.equals("void")) {
                System.out.println("Error: Function expects return type " + methodType + ", but got void.");
              }
              return true;
            } else {
              String returnType = getEndType(child.children.getFirst());
              if (!methodType.equals(returnType)) {
                System.out.println("Error: Return type mismatch. Expected " + methodType + ", but got " + returnType + ".");
              }
              return true;
            }
          }

          if (visitReturn(child, methodType)) {
            return true;
          }
        }

    return false;
  }

  public ASTNode visitFncall(ASTNode node) {
    this.currentScope = node.getScope();
    // ASTNode functionNameNode = node.children.getFirst();
    // String functionName = functionNameNode.getValue();

    // TODO params der Funktion kriegen und sie mit den args vergleichen

    return node;
  }

  public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {
    this.currentScope = constructorNode.getScope();

    return constructorNode;
  }

  public ASTNode visitProgram(ASTNode program) {
    this.currentScope = program.getScope();
    visitChildren(program);
    return program;
  }

  public ASTNode visitAssign(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode firstChild = node.children.getFirst();
    String firstType;
    if (firstChild.getType() == Type.ARRAY_ITEM) {
      visitArrayItem(firstChild);
      firstType = Type.ARRAY_ITEM.name();
    } else {
      firstType = getEndType(firstChild);
    }

    ASTNode secondChild = node.children.getLast();
    String secondType;
    switch (secondChild.getType()) {
      case Type.ARRAY_ITEM:
        visitArrayItem(secondChild);
        break;
      case Type.FN_CALL:
        break;
      case Type.DEC_INC,
          Type.NOT,
          Type.ADD,
          Type.SUB,
          Type.MUL,
          Type.DIV,
          Type.MOD,
          Type.EQUAL,
          Type.NOT_EQUAL,
          Type.LESS,
          Type.LESS_EQUAL,
          Type.GREATER,
          Type.GREATER_EQUAL,
          Type.AND,
          Type.OR:
        if (!typeIsValid(firstType)) {
          System.out.println("Error: types need to be built in to use operators in assign");
        }
        break;
      default:
        secondType = getEndType(secondChild);
        if(typeIsValid(secondType) && typeIsValid(firstType)) {
          break;
        }
        if (!firstType.equals(secondType)) {
          System.out.println(
              "Error: types " + firstType + " and " + secondType + " do not match in assign");
        }
    }

    visitChildren(node);
    return node;
  }

  public ASTNode visitCompare(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode firstChild = node.children.getFirst();
    if (firstChild.getType() == Type.EQUAL
        || firstChild.getType() == Type.NOT_EQUAL
        || firstChild.getType() == Type.GREATER_EQUAL
        || firstChild.getType() == Type.LESS_EQUAL
        || firstChild.getType() == Type.GREATER
        || firstChild.getType() == Type.LESS) {
      visit(firstChild);
    }
    String firstType = getEndType(node.children.getFirst());
    String secondType = getEndType(node.children.getLast());
    if (!typeIsValid(firstType)) {
      System.out.println("Error: invalid type for compare operation: " + firstType);
    } else if (!typeIsValid(secondType)) {
      System.out.println("Error: invalid type for compare operation: " + secondType);
    }
    return node;
  }

  public ASTNode visitDecInc(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode child =
        (node.children.getFirst().getType() == Type.DEC
                || node.children.getFirst().getType() == Type.INC)
            ? node.children.getLast()
            : node.children.getFirst();
    if (child.getType() == Type.ARRAY_ITEM) {
      visit(child);
    }
    String type = getEndType(child);
    if (!typeIsValid(type)) {
      System.out.println("Error: Invalid type for decremental or incremental: " + type);
    }
    return node;
  }

  public ASTNode visitVardecl(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode firstChild = node.children.getFirst();
    if (node.children.size() == 2) {
      ASTNode secondChild = node.children.getLast();
      String firstType = getEndType(firstChild), secondType = getEndType(secondChild);

      if (secondChild.getType() == Type.ARRAY_ITEM) {
        visitArrayItem(secondChild);
        return node;
      }

      if (!Objects.equals(firstType, secondType)) {
        System.out.println(firstChild.getValue() + " " + secondChild.getValue());
        System.out.println(
            "Error: type mismatch in vardecl: type " + firstType + " cannot be " + secondType);
      }
    }
    return node;
  }

  public ASTNode visitArrayRef(ASTNode node) {
    this.currentScope = node.getScope();
    visitVardecl(node);
    Symbol arr = currentScope.resolve(node.children.getLast().getValue());

    if (!(arr instanceof Array)) {
      System.out.println("Error: type mismatch in arrayRef: " + arr + " is not an array");
    }

    return node;
  }

  public ASTNode visitArraydecl(ASTNode node) {
    this.currentScope = node.getScope();
    for (ASTNode child : node.children.getFirst().children) {
      String childType = getEndType(child);
      if (!typeIsValid(childType)) {
        System.out.println("Error: type " + childType + " cannot describe array length");
      }
    }

    return node;
  }

  public ASTNode visitArrayInit(ASTNode node) {
    this.currentScope = node.getScope();
    String arrayType = getEndType(node.children.getFirst());

    visitArraydecl(node);

    visitArray(node.children.getLast(), arrayType);

    return node;
  }

  private ASTNode visitArray(ASTNode node, String arrayType) {
    for (ASTNode child : node.children) {
      this.currentScope = child.getScope();
      String childType = getEndType(child);
      if (typeIsValid(arrayType)) {
        if (!typeIsValid(childType)) {
          System.out.println(
              "Error: type mismatch in arrayInit: "
                  + childType
                  + " is not the same as "
                  + arrayType);
        }
      } else if (!childType.equals(arrayType)) {
        System.out.println(
            "Error: type mismatch in arrayInit: " + childType + " is not the same as " + arrayType);
      }
      if (child.getType() == Type.ARRAY) {
        visitArray(child, arrayType);
      }
    }
    return node;
  }

  public ASTNode visitArrayItem(ASTNode node) {
    this.currentScope = node.getScope();
    String type = getEndType(node.children.getFirst());
    if (!typeIsValid(type)) {
      System.out.println("Error: type " + type + " must be built in type");
    }
    return node;
  }

  public ASTNode visitCalculate(ASTNode node) {
    this.currentScope = node.getScope();
    ASTNode firstChild = node.children.getFirst();
    if (firstChild.getType() == Type.ADD
        || firstChild.getType() == Type.SUB
        || firstChild.getType() == Type.MUL
        || firstChild.getType() == Type.DIV
        || firstChild.getType() == Type.MOD) {
      visit(firstChild);
    }
    String firstType = getEndType(node.children.getFirst());
    String secondType = getEndType(node.children.getLast());
    if (!typeIsValid(firstType)) {
      System.out.println("Error: invalid type for calc operation: " + firstType);
    } else if (!typeIsValid(secondType)) {
      System.out.println("Error: invalid type for calc operation: " + secondType);
    }
    return node;
  }

  private boolean typeIsValid(String type) {
    Symbol typeSymbol = currentScope.resolve(type);

    if (typeSymbol instanceof SymbolTable.BuiltIn && !type.equals("void")) {
      return true;
    }
    return false;
  }

  private String getEndType(ASTNode node) {
    this.currentScope = node.getScope();
    if (node.getType() == Type.ID) {
      Symbol classSymbol = currentScope.resolve(node.getValue());
      return currentScope.resolve(classSymbol.type).name;
    }
    if (!node.children.isEmpty()) {
      boolean isRef = false;
      for (ASTNode child : node.children) {
        isRef = child.getType() == Type.REF;
        if (isRef) break;
      }
      if (isRef) {
        return node.getType().name().toLowerCase();
      }

      if (node.getType() == Type.CLASSTYPE || node.getType() == Type.FN_CALL) {
        return currentScope.resolve(node.getValue()).type;
      }

      if (node.getType() == Type.OBJ_USAGE) {
        return visitObj_usage(node).type;
      }
      return getEndType(node.children.getFirst());
    }
    return node.getType().name().toLowerCase();
  }
}
