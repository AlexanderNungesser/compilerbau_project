package Environment;

public class Instance {
  private final Clazz clazz;

  public Instance(Clazz clazz) {
    this.clazz = clazz;
  }

  public Function getMethod(String functionName) {
    Function function = clazz.getMethod(functionName);
    if (function != null) {
      return function.bind(this);
    }
    throw new RuntimeException("Function '" + functionName + "' not found");
  }

  public Attribute getAttribute(String attributeName) {
    Attribute attribute = clazz.getAttribute(attributeName);
    if (attribute != null) {
      return attribute.bind(this);
    }
    throw new RuntimeException("Attribute '" + attributeName + "' not found");
  }
}
