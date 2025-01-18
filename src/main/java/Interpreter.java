import SymbolTable.*;
import java.util.HashSet;
import java.util.Set;

public class Interpreter {
  Scope currentScope;
  Set<Scope> visitedScopes = new HashSet<>();

  public Interpreter(Scope scope) {
    this.currentScope = scope;
  }

  public ASTNode visit(ASTNode node) {
    switch (node.getType()) {
      case Type.PROGRAM:
        visitChildren(node);
        break;
      case Type.OBJ_USAGE:
        break;
      case Type.BLOCK, Type.CLASS:
        break;
      case Type.FN_DECL:
        break;
      case Type.FN_CALL:
        break;
      case Type.VAR_DECL, Type.VAR_REF:
        break;
      case Type.ARRAY_REF:
        break;
      case Type.ASSIGN:
        break;
      case Type.ARRAY_INIT:
        break;
      case Type.ARRAY_DECL:
        break;
      case Type.ARRAY_ITEM:
        break;
      case Type.EQUAL, Type.NOT_EQUAL, Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL:
        break;
      case Type.NOT:
        break;
      case Type.AND, Type.OR:
        break;
      case Type.DEC_INC:
        break;
      case Type.ADD, Type.SUB, Type.MUL, Type.DIV, Type.MOD:
        break;
      case Type.NULL, Type.INT, Type.BOOL, Type.CHAR:
        return node;
      default:
          visitChildren(node);
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

  private char convertToChar(ASTNode node) {
    return switch (node.getType()) {
      case Type.INT -> (char) Integer.parseInt(node.getValue());
      case Type.NULL -> (char) 0;
      case Type.BOOL -> (char) ((Boolean.parseBoolean(node.getValue())) ? 1 : 0);
      default -> node.getValue().charAt(0);
    };
  }

  private int convertToInt(ASTNode node) {
    return switch (node.getType()) {
      case Type.CHAR -> (char) Integer.parseInt(node.getValue());
      case Type.NULL -> 0;
      case Type.BOOL -> (Boolean.parseBoolean(node.getValue())) ? 1 : 0;
      default -> Integer.parseInt(node.getValue());
    };
  }

  private boolean convertToBoolean(ASTNode node) {
    return switch (node.getType()) {
      case Type.CHAR -> ((int) node.getValue().charAt(0)) != 0;
      case Type.INT -> Integer.parseInt(node.getValue()) != 0;
      case Type.NULL -> false;
      default -> Boolean.parseBoolean(node.getValue());
    };
  }
}
