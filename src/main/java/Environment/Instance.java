package Environment;

public class Instance {
    private final Clazz clazz;

    public Instance(Clazz clazz) {
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
