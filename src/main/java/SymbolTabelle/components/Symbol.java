package SymbolTabelle.components;

public class Symbol {

    public String name;
    public String type;
    public String value;
    public Scope scope;

    public Symbol(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Symbol(String name, String type) {
        this.name = name;
        this.type = type;
    }
}
