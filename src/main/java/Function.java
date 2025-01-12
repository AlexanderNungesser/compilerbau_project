public class Function extends Symbol {
  private int anzParams;

  public Function(String name, String type) {
    super(name, type);
  }

  public int getAnzParams() {return this.anzParams;}
  public void setAnzParams(int anzParams) {this.anzParams = anzParams;}
}
