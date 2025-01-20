package Environment;

public class Instance {
    private final Class clazz;

    public Instance(Class clazz) {
        this.clazz = clazz;
    }

    public Function get(String functionName) {
        Function function = clazz.getMethod(functionName);
        if(function != null) {
            return function.bind(this);
        }
        throw new RuntimeException("Function '" + functionName + "' not found");
    }
}
