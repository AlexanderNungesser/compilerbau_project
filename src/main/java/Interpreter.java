import AST.ASTNode;
import AST.Type;
import Environment.*;
import SymbolTable.BuiltIn;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter {
  Environment env;

  public Interpreter() {
    this.env = new Environment(null);
  }

  public Object eval(ASTNode node) {
    switch (node.getType()) {
      case Type.MAIN:
        eval(node.children.getLast());
        break;
      case Type.OBJ_USAGE:
        return evalObjUsage(node);
      case IF:
        evalIf(node);
        break;
      case WHILE:
        evalWhile(node);
        break;
      case Type.BLOCK:
        evalBlock(node);
        break;
      case Type.RETURN:
        evalReturn(node);
      case Type.CLASS:
        evalClass(node);
        break;
      case Type.FN_DECL:
        evalFnDecl(node);
        break;
      case Type.FN_CALL:
        evalFnCall(node);
        break;
      case Type.VAR_DECL:
        evalVarDecl(node);
        break;
      case Type.VAR_REF:
        evalVarRef(node);
        break;
      case Type.ARRAY_REF:
        evalArrayRef(node);
        break;
      case Type.ASSIGN:
        evalAssign(node);
        break;
      case Type.ARRAY_INIT:
        evalArrayInit(node);
        break;
      case Type.ARRAY_DECL:
        evalArrayDecl(node);
        break;
      case Type.ARRAY_ITEM:
        return evalArrayItem(node);
      case Type.GREATER, Type.GREATER_EQUAL, Type.LESS, Type.LESS_EQUAL, Type.EQUAL, Type.NOT_EQUAL:
        return evaluateComparison(node);
      case Type.AND, Type.OR:
        return evaluateLogical(node);
      case Type.NOT:
        return !convertToBoolean(eval(node));
      case Type.DEC_INC:
        evalDecInc(node);
        break;
      case Type.ADD, Type.SUB, Type.MUL, Type.DIV, Type.MOD:
        return evaluateCalculation(node);
      case Type.NULL:
        return null;
      case Type.INT:
        return Integer.parseInt(node.getValue());
      case Type.BOOL:
        return Boolean.parseBoolean(node.getValue());
      case Type.CHAR:
        return node.getValue().charAt(0);
      case ID:
        return this.env.get(node.getValue());
      default:
        evalChildren(node);
        break;
    }
    return null;
  }

  public Object evalClass(ASTNode node) {
    HashMap<String, Function> methods = new HashMap<>();
    HashMap<String, Attribute> attributes = new HashMap<>();
    for (ASTNode child : node.children) {
      switch (child.getType()) {
        case Type.FN_DECL:
          methods.put(child.children.getFirst().children.getFirst().getValue(), new Function(child, this.env));
          break;
          case Type.CONSTRUCTOR, Type.COPY_CONSTRUCTOR, Type.DESTRUCTOR, Type.OPERATOR:
            methods.put(child.getValue(), new Function(child, this.env));
          break;
          case Type.VAR_DECL, Type.VAR_REF, ARRAY_INIT, ARRAY_DECL:
            attributes.put(child.children.getFirst().getValue(), new Attribute(child, this.env));
            break;
      }
    }
    Clazz clazz = new Clazz(methods, attributes);
    this.env.define(node.getValue(), clazz);
    return null;
  }

  public Object evalReturn(ASTNode node) {
    if (node.children.size() > 1) {
      return eval(node.children.getLast());
    }
    return null;
  }

  public Object evalObjUsage(ASTNode node) {

    ASTNode objNode = node.children.getFirst();
    ASTNode fieldNode = node.children.get(1);
    Object obj = eval(objNode);
    if(obj instanceof Instance){
      if (objNode.getType() == Type.ID){
        return ((Instance)obj).getAttribute(fieldNode.getValue());
      }else {
        return ((Instance)obj).getMethod(fieldNode.getValue());
      }
    }
    throw new RuntimeException("Object of type " + objNode.getValue() + " is not an instance of " + obj.getClass());
  }

  public Object evalFnCall(ASTNode node) {
    if (node.getScope().resolve(node.getValue()) instanceof BuiltIn) {
      ASTNode argsNode = node.children.getFirst();
      switch (node.getValue()) {
        case "print_int" -> print_int(eval(argsNode.children.getFirst()));
        case "print_char" -> print_char(eval(argsNode.children.getFirst()));
        case "print_bool" -> print_bool(eval(argsNode.children.getFirst()));
      }
      return null;
    } else if (node.children.getFirst().getType() == Type.CLASSTYPE) {
      Instance inst = (Instance) eval(node.children.getLast().children.getFirst());
      this.env.define(node.getValue(), inst);
    }
    Function fn = (Function) this.env.get(node.getValue());
    Environment prevEnv = this.env;
    if (!node.children.isEmpty()) {
      ASTNode argsNode = node.children.getFirst();
      ASTNode params =
          fn.node.children.stream()
              .filter(n -> n.getType() == Type.PARAMS)
              .findFirst()
              .orElse(null);
      ArrayList<Object> args = new ArrayList<>();
      if (params != null) {
        for (ASTNode arg : argsNode.children) {
          args.add(eval(arg));
        }
        this.env = new Environment(fn.closure);
        for (int i = 0; i < argsNode.children.size(); i++) {
          this.env.define(params.children.get(i).getValue(), args.get(i));
        }
      }
    }
    ASTNode blockNode = fn.node.children.getLast();
    if (blockNode != null && blockNode.getType() == Type.BLOCK) {
      eval(blockNode);
    }

    this.env = prevEnv;

    return null;
  }

  public Object evalFnDecl(ASTNode node) {
    if (node.children.getLast().getType() != Type.BLOCK) {
      return null;
    }
    ASTNode fnInfo = node.children.getFirst();
    SymbolTable.Function func =
        (SymbolTable.Function) node.getScope().resolve(fnInfo.children.getFirst().getValue());
    if (func == null) {
      System.out.println(
          "Error: the function " + fnInfo.children.getFirst().getValue() + " is not implemented");
      return null;
    }
    Function fn = new Function(node, this.env);
    this.env.define(fnInfo.children.getFirst().getValue(), fn);
    return null;
  }

  public Object evalChildren(ASTNode node) {
    for (ASTNode child : node.children) {
      eval(child);
    }
    return null;
  }

  public Object evalAssign(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    String name = firstChild.getValue();
    ASTNode secondChild = node.children.getLast();
    Object value = eval(secondChild);
    if (firstChild.getType() == Type.OBJ_USAGE) {
      Attribute attribute = (Attribute) evalObjUsage(firstChild);
      name = attribute.node.children.getFirst().getValue();
      int num = convertToInteger(value);
//
//      Attribute clazzAttribute = attribute.closure.get("this");
//      clazzAttribute.closure.assign(name, num);
      return null;
    } else if (firstChild.getType() == Type.ARRAY_ITEM) {
      Object array = this.env.get(name);
      int[] indices = new int[firstChild.children.size()];
      for (int i = 0; i < indices.length; i++) {
        indices[i] = convertToInteger(eval(firstChild.children.get(i)));
      }
      setArrayItem(array, value, indices);
      this.env.assign(name, array);
      return null;
    }
    int old = convertToInteger(this.env.get(secondChild.getValue()));
    int num = convertToInteger(value);
    switch (node.getValue()) {
      case "+=" -> this.env.assign(name, old + num);
      case "-=" -> this.env.assign(name, old - num);
      case "*=" -> this.env.assign(name, old * num);
      case "/=" -> this.env.assign(name, old / num);
      default -> this.env.assign(name, num);
    }
    evalTrailingDecInc(secondChild);
    return null;
  }

  private void evalTrailingDecInc(ASTNode node) {
    Object value;
    if (node.getType() == Type.DEC_INC) {
      ASTNode firstChild = node.children.getFirst();
      ASTNode lastChild = node.children.getLast();
      if (lastChild.getType() == Type.DEC) {
        Object obj = eval(firstChild);
        value =
            switch (obj) {
              case Integer i -> i - 1;
              case Boolean b -> convertToBoolean(convertToInteger(b) - 1);
              case Character c -> c - 1;
              default -> throw new IllegalStateException("Unexpected value: " + obj);
            };
        this.env.assign(firstChild.getValue(), value);
      } else if (lastChild.getType() == Type.INC) {
        Object obj = eval(firstChild);
        value =
            switch (obj) {
              case Integer i -> i + 1;
              case Boolean b -> convertToBoolean(convertToInteger(b) + 1);
              case Character c -> c + 1;
              default -> throw new IllegalStateException("Unexpected value: " + obj);
            };
        this.env.assign(firstChild.getValue(), value);
      }
    }
  }

  public Object evalDecInc(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    ASTNode lastChild = node.children.getLast();
    if (firstChild.getType() == Type.DEC) {
      Object obj = eval(lastChild);
      Object value =
          switch (obj) {
            case Integer i -> i - 1;
            case Boolean b -> convertToBoolean(convertToInteger(b) - 1);
            case Character c -> c - 1;
            default -> throw new IllegalStateException("Unexpected value: " + obj);
          };
      this.env.assign(lastChild.getValue(), value);
      return value;
    } else if (firstChild.getType() == Type.INC) {
      Object obj = eval(lastChild);
      Object value =
          switch (obj) {
            case Integer i -> i + 1;
            case Boolean b -> convertToBoolean(convertToInteger(b) + 1);
            case Character c -> c + 1;
            default -> throw new IllegalStateException("Unexpected value: " + obj);
          };
      this.env.assign(lastChild.getValue(), value);
      return value;
    } else if (lastChild.getType() == Type.DEC || lastChild.getType() == Type.INC) {
      Object obj = eval(firstChild);
      return switch (obj) {
        case Integer i -> i;
        case Boolean b -> convertToBoolean(convertToInteger(b));
        case Character c -> c;
        default -> throw new IllegalStateException("Unexpected value: " + obj);
      };
    }
    return null;
  }

  public Object evalArrayRef(ASTNode node) {
    ASTNode arrayNameNode = node.children.getLast();
    String arrayName = arrayNameNode.getValue();

    Object arrayObject = this.env.get(arrayName);

    if (arrayObject == null) {
      System.out.println("Error: Array " + arrayName + " not found in the current environment");
      return null;
    }

    if (!(arrayNameNode.getType() == Type.ARRAY_ITEM)) {
      arrayObject = eval(arrayNameNode);
    }

    int[] sizes = getArraySizes(arrayObject);
    int[] indices = new int[node.children.getFirst().children.size()];

    for (int i = 0; i < indices.length; i++) {
      indices[i] = convertToInteger(eval(node.children.getFirst().children.get(i)));
    }

    if (arrayNameNode.children.isEmpty()) {
      if (indices.length != sizes.length) {
        System.out.println("Error: Dimension mismatch");
        return null;
      }
    } else if (arrayNameNode.children.getFirst().getType() != Type.ID){
      int index = Integer.parseInt(arrayNameNode.children.getFirst().getValue());

      if(index >= sizes.length || index < 0) {
        System.out.println(
                "Error: Index "
                        + index
                        + " out of bounds for dimensions of array "
                        + arrayName);
        return null;
      } else if (indices[index] != sizes[index]) {
        System.out.println("Error: Dimension mismatch");
        return null;
      }
    }

    for (int i = 0; i < indices.length; i++) {
      if (indices[i] != sizes[i]) {
        System.out.println(
                "Error: Index "
                        + indices[i]
                        + " out of bounds for dimension "
                        + (i + 1)
                        + " of array "
                        + arrayName);
        return null;
      }
    }
    this.env.define(node.getValue(), arrayName);
    return null;
  }

  public Object evalArrayInit(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    ASTNode lastChild = node.children.getLast();

    String name = firstChild.getValue();
    Type type = firstChild.getType();
    int[] sizes = countArray(lastChild);

    Object array =
            switch (type) {
              case Type.INT -> Array.newInstance(int.class, sizes);
              case Type.BOOL -> Array.newInstance(boolean.class, sizes);
              case Type.CHAR -> Array.newInstance(char.class, sizes);
              default -> Array.newInstance(Object.class, sizes);
            };



    env.define(name, array);

    return null;
  }

  public int[] countArray(ASTNode node) {
    int[] sizes = new int[0];
    if (node.children.getFirst().getType() == Type.ARRAY) {
      sizes = new int[node.children.size()];
    } else {
      sizes = new int[1];
    }
    int index = 0;

    for (ASTNode child : node.children) {
      switch (child.getType()) {
        case Type.ARRAY:
          sizes[index] = countArray(child)[0];
          index++;
          break;
        default:
          sizes[index]++;
          break;
      }

    }

    return sizes;
  }


  public Object evalArrayDecl(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    String name = firstChild.getValue();
    Type type = firstChild.getType();
    int dim = firstChild.children.size();
    int[] sizes = new int[dim];
    for (int i = 0; i < dim; i++) {
      sizes[i] = convertToInteger(eval(firstChild.children.get(i)));
    }
    Object array =
        switch (type) {
          case Type.INT -> Array.newInstance(int.class, sizes);
          case Type.BOOL -> Array.newInstance(boolean.class, sizes);
          case Type.CHAR -> Array.newInstance(char.class, sizes);
          default -> Array.newInstance(Object.class, sizes);
        };
    env.define(name, array);
    return null;
  }

  public Object evalVarDecl(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    String name = firstChild.getValue();
    Type type = firstChild.getType();
    Object value =
        switch (type) {
          case Type.INT -> 0;
          case Type.BOOL -> false;
          case Type.CHAR -> (char) 0;
          default -> new Instance((Clazz) this.env.get(firstChild.children.getFirst().getValue()));
        };
    if (node.children.size() == 2) {
      ASTNode secondChild = node.children.getLast();
      value =
          switch (type) {
            case Type.INT -> convertToInteger(eval(secondChild));
            case Type.BOOL -> convertToBoolean(eval(secondChild));
            case Type.CHAR -> convertToCharacter(eval(secondChild));
            default -> eval(secondChild);
          };
    }
    this.env.define(name, value);
    if (node.children.size() == 2) {
      ASTNode secondChild = node.children.getLast();
      evalTrailingDecInc(secondChild);
    }
    return null;
  }

  public Object evalVarRef(ASTNode node) {
    ASTNode firstChild = node.children.getFirst();
    String name = firstChild.getValue();
    ASTNode secondChild = node.children.getLast();
    if (secondChild.getType() == Type.ID) {
      this.env.define(name, secondChild.getValue());
    } else {
      Object obj = eval(node.children.getLast());
      this.env.define(name, obj);
    }
    return null;
  }

  public Object evalArrayItem(ASTNode node) {
    String arrayName = node.getValue();
    Object arrayObject = this.env.get(arrayName);

    if (arrayObject == null) {
      System.out.println("Error: array " + arrayName + " not found in the current environment");
      return null;
    }

    int[] sizes = getArraySizes(arrayObject);
    int[] indices = new int[node.children.size()];
    for (int i = 0; i < node.children.size(); i++) {
      ASTNode indexNode = node.children.get(i);
      Object evaluatedIndex = eval(indexNode);
      int index = convertToInteger(evaluatedIndex);

      if (index < 0 || index >= sizes[i]) {
        System.out.println(
            "Error: index "
                + index
                + " is out of bounds for dimension "
                + (i + 1)
                + " of array "
                + arrayName
                + " (size: "
                + sizes[i]
                + ")");
        return null;
      }
      indices[i] = index;
    }
    return getArrayItem(arrayObject, indices);
  }

  private Object getArrayItem(Object array, int[] indices) {
    Object currentArray = array;

    for (int i = 0; i < indices.length - 1; i++) {
      currentArray = Array.get(currentArray, indices[i]);
    }

    return Array.get(currentArray, indices[indices.length - 1]);
  }

  private void setArrayItem(Object array, Object value, int[] indices) {
    Object currentArray = array;

    for (int i = 0; i < indices.length - 1; i++) {
      currentArray = Array.get(currentArray, indices[i]);
    }

    Array.set(currentArray, indices[indices.length - 1], value);
  }

  private int[] getArraySizes(Object array) {
    int dimensions = 0;
    java.lang.Class<?> clazz = array.getClass();

    // Anzahl der Dimensionen ermitteln
    while (clazz.isArray()) {
      dimensions++;
      clazz = clazz.getComponentType();
    }

    int[] sizes = new int[dimensions];
    Object currentArray = array;

    // Größen der einzelnen Dimensionen ermitteln
    for (int i = 0; i < dimensions; i++) {
      sizes[i] = Array.getLength(currentArray);
      if (sizes[i] > 0) {
        currentArray = Array.get(currentArray, 0);
      }
    }

    return sizes;
  }

  public Object evalWhile(ASTNode node) {
    while (convertToBoolean(eval(node.children.getFirst()))) {
      eval(node.children.getLast());
    }
    return null;
  }

  public Object evalIf(ASTNode node) {
    for (int i = 0; i < node.children.size(); i++) {
      if (node.children.get(i).getType() != Type.BLOCK) {
        if (convertToBoolean(eval(node.children.get(i)))) {
          eval(node.children.get(i + 1));
          return null;
        }
      } else if (node.children.get(i).getType() == Type.BLOCK && i == (node.children.size() - 1)) {
        eval(node.children.get(i));
        return null;
      }
    }
    return null;
  }

  public int evaluateCalculation(ASTNode node) {
    Type operator = node.getType();
    Object l = eval(node.children.getFirst());
    Object r = eval(node.children.getLast());
    int left = convertToInteger(l);
    int right = convertToInteger(r);
    return switch (operator) {
      case Type.ADD -> left + right;
      case Type.SUB -> left - right;
      case Type.MUL -> left * right;
      case Type.DIV -> left / right;
      case Type.MOD -> left % right;
      default -> throw new IllegalStateException("Unexpected value: " + operator);
    };
  }

  public Object evalBlock(ASTNode node) {
    Environment prevEnv = this.env;
    try {
      this.env = new Environment(this.env);
      evalChildren(node);
    } finally {
      this.env.print();
      this.env = prevEnv;
    }
    return null;
  }

  public boolean evaluateComparison(ASTNode node) {
    Type operator = node.getType();

    Object l = eval(node.children.getFirst());
    Object r = eval(node.children.getLast());
    int left = convertToInteger(l);
    int right = convertToInteger(r);

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

    boolean left = convertToBoolean(l);
    boolean right = convertToBoolean(r);

    return switch (operator) {
      case Type.AND -> left && right;
      case Type.OR -> left || right;
      default -> throw new IllegalStateException("Unexpected value: " + operator);
    };
  }

  private boolean convertToBoolean(Object obj) {
    boolean bool = false;
    switch (obj) {
      case null -> bool = false;
      case Character c -> bool = c != 0;
      case Integer i -> bool = i != 0;
      case Boolean b -> bool = b;
      default -> {}
    }
    return bool;
  }

  private int convertToInteger(Object obj) {
    int num = 0;
    switch (obj) {
      case null -> num = 0;
      case Character c -> num = (int) c;
      case Integer i -> num = i;
      case Boolean b -> num = b ? 1 : 0;
      default -> {}
    }
    return num;
  }

  private char convertToCharacter(Object obj) {
    char chr = (char) 0;
    switch (obj) {
      case null -> chr = (char) 0;
      case Character c -> chr = c;
      case Integer i -> chr = (char) i.intValue();
      case Boolean b -> chr = (char) (b ? 1 : 0);
      default -> {}
    }
    return chr;
  }

  private void print_int(Object object) {
    int i = convertToInteger(object);
    System.out.println("Print int: " + i);
  }

  private void print_char(Object object) {
    char c = convertToCharacter(object);
    System.out.println("Print char: " + c);
  }

  private void print_bool(Object object) {
    boolean b = convertToBoolean(object);
    System.out.println("Print bool: " + b);
  }
}
