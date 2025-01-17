import SymbolTable.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TypeCheckVisitor {
  Scope currentScope;
  Set<Scope> visitedScopes = new HashSet<>();

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
        visitScopes(node);
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
      case Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL:
        visitCompare(node);
        break;
      case Type.NOT:
        visitNot(node);
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

  private ASTNode visitScopes(ASTNode node) {
    for (Scope scope : this.currentScope.innerScopes) {
      if (!visitedScopes.contains(scope)) {
        this.currentScope = scope;
        if(node.getType() == Type.CLASS) {
          for (ASTNode child : node.children) {
            switch (child.getType()) {
              case Type.FN_DECL: // Methoden
                visitScopes(child);
                break;
              case Type.CONSTRUCTOR:
                visitConstructor(child, currentScope.resolve(node.getValue()));
                break;
              //                        case Type.DESTRUCTOR:
              //                            visitDestructor(child,
              // currentScope.resolve(classNode.getValue()));
              //                            break;
            }
          }
          this.currentScope = this.currentScope.enclosingScope;
          visitedScopes.add(scope);
          break;
        } else {
          visitChildren(node);
        }
        this.currentScope = this.currentScope.enclosingScope;
        visitedScopes.add(scope);
      }
    }
    return node;
  }

  public ASTNode visitExpr(ASTNode node) {
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
    ASTNode classObject = node.children.getFirst();

    if(classObject.getType() == Type.OBJ_USAGE) {
      return visitObj_usage(classObject);
    }

    Symbol objectSymbol = currentScope.resolve(classObject.getValue());
    Symbol classSymbol = currentScope.resolve(objectSymbol.type);
    Scope classScope = ((SymbolTable.Class) classSymbol).getClassScope();

    Symbol usedValueOfObject = classScope.resolve(node.children.getLast().getValue());
    return usedValueOfObject;
  }

  private ASTNode visitNot(ASTNode node) {

    return node;
  }

  public ASTNode visitFndecl(ASTNode node) {
    ASTNode returnTypeNode = node.children.getFirst();
    String returnType = returnTypeNode.getValue();

    if (!typeIsValid(returnType)) {
      System.out.println("Error: Invalid return type in function declaration: " + returnType);
    }

    for (int i = 1; i < node.children.size(); i++) {
      ASTNode paramNode = node.children.get(i);
      if (paramNode.getType() == Type.VAR_DECL) {
        ASTNode paramTypeNode = paramNode.children.getFirst();
        String paramType = paramTypeNode.getValue();

        if (!typeIsValid(paramType)) {
          System.out.println("Error: Invalid parameter type in function declaration: " + paramType);
        }
      }
    }

    visitChildren(node);

    return node;
  }

  public ASTNode visitFncall(ASTNode node) {
    ASTNode functionNameNode = node.children.getFirst();
    String functionName = functionNameNode.getValue();

    Symbol functionSymbol = currentScope.resolve(functionName);
    if (functionSymbol == null || !(functionSymbol instanceof SymbolTable.Function)) {
      System.out.println("Error: Function " + functionName + " is not declared.");
      return node;
    }

    //TODO params der Funktion kriegen und sie mit den args vergleichen

    return node;
  }

  public ASTNode visitConstructor(ASTNode constructorNode, Symbol classSymbol) {

    return constructorNode;
  }

  public ASTNode visitProgram(ASTNode program) {
    visitChildren(program);
    return program;
  }

  public ASTNode visitAssign(ASTNode node) {
    visitChildren(node);
    return node;
  }

  public ASTNode visitCompare(ASTNode node) {

    return node;
  }

  public ASTNode visitDecInc(ASTNode node) {

    return node;
  }

  public ASTNode visitVardecl(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    if(node.children.size()==2){
      ASTNode secondChild = node.children.getLast();
      String firstType = getEndType(firstChild), secondType = getEndType(secondChild);

      if(!Objects.equals(firstType, secondType)){
        System.out.println(firstChild.getValue() + " " + secondChild.getValue());
        System.out.println("Error: type mismatch in vardecl: type " + firstType + " cannot be " + secondType);
      }
    }
    return node;
  }

  public ASTNode visitArrayRef(ASTNode node) {
    visitVardecl(node);
    Symbol arr = currentScope.resolve(node.children.getLast().getValue());

    if(!(arr instanceof Array)){
      System.out.println("Error: type mismatch in arrayRef: " + arr + " is not an array");
    }

    return node;
  }

  public ASTNode visitArraydecl(ASTNode node) {
    for(ASTNode child : node.children.getFirst().children ) {
      String childType = getEndType(child);
      if(!childType.equals("int")){
        System.out.println("Error: type " + childType + " not cannot describe array length");
      }
    }

    return node;
  }

  public ASTNode visitArrayInit(ASTNode node) {
    String arrayType = getEndType(node.children.getFirst());

    visitArraydecl(node);


    visitArray(node.children.getLast(), arrayType);

    return node;
  }

  private void visitArray(ASTNode node, String arrayType) {
    if(node.children.getFirst().getType() == Type.ARRAY){
      visitArray(node.children.getFirst(), arrayType);
    } else {
      for (ASTNode child : node.children) {
        String childType = getEndType(child);
        if(!childType.equals(arrayType)){
          System.out.println("Error: type mismatch in arrayInit: " + childType + " is not the same as " + arrayType);
        }
      }
    }
  }

  public ASTNode visitCalculate(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    if (firstChild.getType() == Type.ADD) {
      firstChild = visitCalculate(firstChild);
    }

    String firstType = getEndType(node.children.getFirst());
    if (firstType.equals("id")) {
      firstType = this.currentScope.resolve(firstChild.getValue()).type;
    }

    if (!typeIsValid(firstType)) {
      System.out.println("ERROR: Expected valid type, got " + firstType);
    }

    ASTNode secondChild = node.children.get(1);
    String secondType = getEndType(secondChild);
    if (secondType.equals("id")) {
      secondType = this.currentScope.resolve(secondChild.getValue()).type;
    }

    if (!typeIsValid(secondType)) {
      System.out.println("ERROR: Expected valid type, got " + secondType);
    }

    visitChildren(node);

    return node;
  }

  private boolean typeIsValid(String type) {
    Symbol typeSymbol = currentScope.resolve(type);

    if (typeSymbol instanceof SymbolTable.BuiltIn && !type.equals("void")) {
      return true;
    }
    return false;
  }

  private String  getEndType(ASTNode node) {
    if(node.getType() == Type.ID) {
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

      if(node.getType() == Type.CLASSTYPE ){
        return currentScope.resolve(node.getValue()).type;
      }
      if(node.getType() == Type.OBJ_USAGE) {
        return visitObj_usage(node).type;
      }
      return getEndType(node.children.getFirst());
    }
    return node.getType().name().toLowerCase();
  }
}
