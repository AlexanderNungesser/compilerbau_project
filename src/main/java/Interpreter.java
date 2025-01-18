public class Interpreter {
  Environment env;

  public Interpreter() {
    this.env = new Environment(null);
  }

  public Object eval(ASTNode node) {
    switch (node.getType()) {
      case Type.OBJ_USAGE:
        break;
      case Type.BLOCK:
        evalBlock(node);
        break;
      case Type.CLASS:
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
      case Type.NULL:
        return null;
      case Type.INT:
        return Integer.parseInt(node.getValue());
      case Type.BOOL:
        return Boolean.parseBoolean(node.getValue());
      case Type.CHAR:
        return node.getValue().charAt(0);
      default:
        evalChildren(node);
        break;
    }
    return null;
  }

  private Object evalBlock(ASTNode node) {return null;}

  public Object evalChildren(ASTNode node) {
    for (ASTNode child : node.children) {
      eval(child);
    }
    return null;
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
