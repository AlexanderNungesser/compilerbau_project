package Environment;

import java.util.HashMap;

public class Clazz {
  HashMap<String, Function> methods;
  HashMap<String, Attribute> attributes;

  public Clazz(HashMap<String, Function> methods, HashMap<String, Attribute> attributes) {
    this.methods = methods;
    this.attributes = attributes;
  }

  public Function getMethod(String name) {
    return methods.get(name);
  }
  public Attribute getAttribute(String name) {return attributes.get(name);}
}
