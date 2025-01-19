package SymbolTable;

public class Class extends Symbol {
  Scope classScope;
  Class superClass;

  public Class(String name) {
    super(name, null);
  }

  public Class(String name, String type) {
    super(name, type);
  }

  public Scope getClassScope() {
    return this.classScope;
  }

  public void setClassScope(Scope classScope) {
    this.classScope = classScope;
  }

  public Class getSuperClass() {
    return this.superClass;
  }

  public void setSuperClass(Class superClass) {
    this.superClass = superClass;
  }
}
