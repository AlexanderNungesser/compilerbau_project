import java.util.HashMap;
import java.util.Map;

public class Environment {
    public Environment enclosingEnv;
    public Map<String, Object> values = new HashMap<String, Object>();

    public Environment(Environment enclosing) {
        this.enclosingEnv = enclosing;
    }

    public void define(String name, Object value) {
        this.values.put(name, value);
    }

    public void assign(String name, Object value) {
        if(this.values.containsKey(name)){
            this.values.put(name, value);
        } else if (this.enclosingEnv != null) {
            this.enclosingEnv.assign(name, value);
        }else {
            throw new RuntimeException("No value defined for " + name);
        }
    }

    public Object get(String name) {
        if (values.containsKey(name)) return values.get(name);
        try {
            return enclosingEnv.get(name);
        } catch (Exception e) {
            return null;
        }
    }
}
