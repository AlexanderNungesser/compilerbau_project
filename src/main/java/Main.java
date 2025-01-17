import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {

  public static String readFileWithPaths(String relativePath) {
    Path filePath = Paths.get(relativePath);
    try {
      return Files.readString(filePath);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void main(String... args) throws IOException {

    String relativePath = "src/main/antlr/test_files/class.cpp";

    String input = readFileWithPaths(relativePath);
    if (input != null) {
      System.out.println("Input file:\n" + input);
    }

    CppLexer lexer = new CppLexer(CharStreams.fromString(String.valueOf(input)));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    CppParser parser = new CppParser(tokens);

    ParseTree parseTree = parser.program();

    CppParseTreeVisitor parseTreeVisitor = new CppParseTreeVisitor();
    ASTNode ast = parseTreeVisitor.visit(parseTree);
    ast.print();

    FirstScopeVisitor scopeVisitor = new FirstScopeVisitor();
    scopeVisitor.visit(ast);
    System.out.println("First Scope Run:");
    scopeVisitor.currentScope.print();
    ast.print();

    SecondScopeVisitor scopeVisitor2 = new SecondScopeVisitor(scopeVisitor.currentScope);
    scopeVisitor2.visit(ast);
    System.out.println("\nSecond Scope Run:");
    scopeVisitor2.currentScope.print();
    ast.print();

    TypeCheckVisitor typeCheckVisitor = new TypeCheckVisitor(scopeVisitor.currentScope);
    typeCheckVisitor.visit(ast);
    System.out.println("\nType Check Run:");
    typeCheckVisitor.currentScope.print();
  }
}
