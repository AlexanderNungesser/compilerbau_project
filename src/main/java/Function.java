public class Function extends Symbol {
  private int anzParams = 0;

  public Function(String name, String type) {
    super(name, type);
  }

  public int getAnzParams() {
    return this.anzParams;
  }

  public void setAnzParams(int anzParams) {
    this.anzParams = anzParams;
  }

  public void increaseAnzParams() {this.anzParams++;}
}
