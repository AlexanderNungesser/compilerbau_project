package SymbolTable;

public class Function extends Symbol {
  private int paramCount = 0;

  public Function(String name, String type) {
    super(name, type);
  }

  public int getParamCount() {
    return this.paramCount;
  }

  public void setParamCount(int paramCount) {
    this.paramCount = paramCount;
  }

  public void increaseParamCount() {
    this.paramCount++;
  }
}
