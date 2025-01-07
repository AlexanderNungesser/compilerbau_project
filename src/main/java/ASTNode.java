import java.util.ArrayList;

public class ASTNode {

  private Type type;
  private String value;
  public ArrayList<ASTNode> children = new ArrayList<>();

  public ASTNode(Type type) {
    this.value = null;
    this.type = type;
  }

  public ASTNode(String value) {
    this.value = value;
    this.type = null;
  }

  public ASTNode(Type type, String value) {
    this.type = type;
    this.value = value;
  }

  public String getType() {
    return type.name();
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void addChild(ASTNode child) {
    this.children.add(child);
  }

  public void addChildren(ArrayList<ASTNode> children) {
    this.children.addAll(children);
  }

  public void print() {
    print("", true);
  }

  private void print(String prefix, boolean isLast) {
    // Anzeige des aktuellen Knotens
    System.out.println(
        prefix
            + (isLast ? "'__" : "|--")
            + (this.value == null ? "" : " " + this.value)
            + (this.type == null ? "" : " (" + this.type.name() + ")"));

    // Anzeige der Kinder
    for (int i = 0; i < children.size(); i++) {
      ASTNode child = children.get(i);
      boolean lastChild = (i == children.size() - 1);
      child.print(prefix + (isLast ? "    " : "|   "), lastChild);
    }
  }
}
