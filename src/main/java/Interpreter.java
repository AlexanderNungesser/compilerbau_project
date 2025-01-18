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
      case Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL, Type.EQUAL, Type.NOT_EQUAL:
        evaluateComparison(node);
        break;
      case Type.AND, Type.OR:
        evaluateLogical(node);
        break;
      case Type.NOT:
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

  public ASTNode visitCompare(ASTNode node) {
        return node;
  }

  public ASTNode visitLogical(ASTNode node) {
      return node;
  }

  public boolean evaluateComparison(ASTNode node) {
      Type operator = node.getType();

      Object l = eval(node.children.getFirst());
      Object r = eval(node.children.getLast());
      int left = 0;
      int right = 0;

      left = convertToInteger(l, left);
      right = convertToInteger(r, right);

      return switch (operator) {
          case Type.GREATER -> left > right;
          case Type.GREATER_EQUAL -> left >= right;
          case Type.LESS -> left < right;
          case Type.LESS_EQUAL -> left <= right;
          case Type.EQUAL -> left == right;
          case Type.NOT_EQUAL -> left != right;
          default -> throw new IllegalStateException("Unexpected value: " + operator);
      };
  }

  public boolean evaluateLogical(ASTNode node) {
      Type operator = node.getType();

      Object l = eval(node.children.getFirst());
      Object r = eval(node.children.getLast());
      boolean left = false;
      boolean right = false;

      left = convertToBoolean(l, left);
      right = convertToBoolean(r, right);

      return switch (operator) {
          case Type.AND -> left && right;
          case Type.OR -> left || right;
          default -> throw new IllegalStateException("Unexpected value: " + operator);
      };
  }

    private static boolean convertToBoolean(Object obj, boolean bool) {
        switch (obj) {
            case null -> bool = false;
            case Character c -> bool = c != 0;
            case Integer i -> bool = i != 0;
            case Boolean b -> bool = b;
            default -> {
            }
        }
        return bool;
    }

    private static int convertToInteger(Object obj, int num) {
        switch (obj) {
            case null -> num = 0;
            case Character c -> num = (int) c;
            case Integer i -> num = i;
            case Boolean b -> num = b ? 1 : 0;
            default -> {
            }
        }
        return num;
    }

    private static char convertToCharacter(Object obj, char chr) {
        switch (obj) {
            case null -> chr = (char) 0;
            case Character c -> chr = c;
            case Integer i -> chr = (char) i.intValue();
            case Boolean b -> chr = (char) (b ? 1 : 0);
            default -> {
            }
        }
        return chr;
    }
}
