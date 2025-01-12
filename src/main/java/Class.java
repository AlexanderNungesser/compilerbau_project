public class Class extends Symbol {
  Scope classScope;

  public Class(String name) {
    super(name, null);
  }

  public Class(String name, String type) {
    super(name, type);
  }

  public Scope getClassScope() {return this.classScope;}
  public void setClassScope(Scope classScope) {this.classScope = classScope;}
}
