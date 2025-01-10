import SymbolTabelle.components.*;

public class FirstRun extends CppParseTreeVisitor{
    Scope currentScope;

    public ASTNode visit(ASTNode node) {
        switch (node.getValue()) {
            case "program":
                visitProgram(node);
                break;
            case "vardecl":
                visitVardecl(node);
                break;
            case "fndecl":
                visitFndecl(node.children.getFirst());
                break;
            case "fncall":
                visitFncall(node.children.getFirst());
                break;
            case "block":
                visitBlock(node);
                break;
            case "params":
                visitParams(node);
                break;
            case "class":
                visitClass(node);
                break;
            default:
                if (node.children.isEmpty() ) {
                    visitExpr(node);
                } else {
                    visitChildren(node);
                }
                break;
        }
        return node;
    }

    public ASTNode visitProgram(ASTNode program) {
        Scope globalScope = new Scope(null);

        globalScope.bind(new BuiltIn("int"));
        globalScope.bind(new BuiltIn("bool"));
        globalScope.bind(new BuiltIn("char"));

        currentScope = globalScope;

        visitChildren(program);
        return program;
    }

    public ASTNode visitVardecl(ASTNode vardecl){
        String type = "", name = "";
        for( ASTNode child : vardecl.children){
            if( child.getType() == "ID"){
                name = child.getValue();
            } else {
                type = child.getValue();
            }
        }

        Symbol typeSymbol = currentScope.resolve(type);
        Symbol variable = new Variable(name, typeSymbol.name);

        Symbol alreadyDeclared = currentScope.resolve(name);
        if(alreadyDeclared != null){
            System.out.println("Error: such variable " + name + " already exists");
        } else {
            currentScope.bind(variable);
        }

        return vardecl;
    }

    public ASTNode visitFndecl(ASTNode fndecl){
        String name = fndecl.getValue();
        Symbol type = currentScope.resolve(fndecl.getType());
        Function function = new Function(name, type.name);

        Symbol alreadyDeclared = currentScope.resolve(name);
        if(alreadyDeclared != null){
            System.out.println("Error: such function " + name + " already exists");
        } else {
            currentScope.bind(function);
        }

        Scope newScope = new Scope(currentScope);
        currentScope.innerScopes.add(newScope);
        currentScope = newScope;

        visitChildren(fndecl);

        currentScope = currentScope.enclosingScope;
        return fndecl;
    }

    public ASTNode visitFncall(ASTNode fncall){
        String functionName = fncall.getValue();
        Symbol function = currentScope.resolve(functionName);

        if(function == null) System.out.println("Error: no such function: " + functionName);
        if(function instanceof Variable) System.out.println("Error: " + functionName + " is not a function");

        visitChildren(fncall);
        return fncall;
    }

    public ASTNode visitArgs(ASTNode args){
        return args;
    }

    public ASTNode visitParam(ASTNode params){
        for (ASTNode child : params.children) {
            String name = child.getValue();
            Symbol type = currentScope.resolve(child.getType());

            Symbol param = new Variable(name, type.name);
            currentScope.bind(param);
        }

        return params;
    }

    public ASTNode visitBlock(ASTNode block){
        Scope newScope = new Scope(currentScope);
        currentScope.innerScopes.add(newScope);
        currentScope = newScope;

        visitChildren(block);

        currentScope = currentScope.enclosingScope;
        return block;
    }

    public ASTNode visitParams(ASTNode node) {
        for (ASTNode child : node.children) {
            String name = child.getValue();
            Symbol type = currentScope.resolve(child.getType());
            Symbol param = new Variable(name, type.name);
            currentScope.bind(param);
        }
        return node;
    }

    public ASTNode visitClass(ASTNode classNode){
        String name = classNode.getValue();
        Symbol classSymbol = currentScope.resolve(name);

        Symbol alreadyDeclared = currentScope.resolve(name);
        if(alreadyDeclared != null){
            System.out.println("Error: such class " + name + " already exists");
        } else {
            currentScope.bind(classSymbol);
        }

        Scope newScope = new Scope(currentScope);
        currentScope.innerScopes.add(newScope);
        currentScope = newScope;

        visitChildren(classNode);

        currentScope = currentScope.enclosingScope;
        return classNode;
    }

    public ASTNode visitExpr(ASTNode node) {
        if (node.children.isEmpty() && node.getType().equals("ID")) {
            String name = node.getValue();
            Symbol var = currentScope.resolve(name);
            if (var == null) {
                System.out.println("Error: no such variable: " + name);
            }
        } else {
            visitChildren(node);
        }
        return node;
    }

    public ASTNode visitChildren(ASTNode node) {
        for (ASTNode child : node.children) {
            visit(child);
        }
        return node;
    }
}
