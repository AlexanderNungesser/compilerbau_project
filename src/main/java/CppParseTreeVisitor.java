import java.util.ArrayList;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CppParseTreeVisitor extends CppBaseVisitor<ASTNode> {

  /**
   * Visit a parse tree produced by {@link CppParser#program}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitProgram(CppParser.ProgramContext ctx) {
    ASTNode root = new ASTNode(Type.PROGRAM);
    for (int i = 0; i < ctx.getChildCount() - 1; i++) {
      ASTNode child = visit(ctx.getChild(i));
      root.addChild(child);
    }
    return root;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#stmt}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitStmt(CppParser.StmtContext ctx) {
    if (ctx.getChildCount() == 2) {
      return visit(ctx.children.getFirst());
    } else {
      return visitChildren(ctx);
    }
  }

  /**
   * Visit a parse tree produced by {@link CppParser#var_decl}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitVar_decl(CppParser.Var_declContext ctx) {
    ASTNode node = new ASTNode(Type.VAR_DECL);

    // Process const or static if available
    if (ctx.getChild(0).getText().contains("const")
        || ctx.getChild(1).getText().contains("const")) {
      node.addChild(new ASTNode("const"));
    }
    if (ctx.getChild(0).getText().contains("static")
        || ctx.getChild(1).getText().contains("static")) {
      node.addChild(new ASTNode("static"));
    }

    // Process type information
    if (ctx.type() != null) {
      node.addChild(visit(ctx.type()));
    }

    // Process variable name or reference
    if (ctx.ref() != null) {
      node.addChild(visit(ctx.ref()));
    } else if (ctx.ID() != null) {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    }

    // Process initialization if present
    if (ctx.expr() != null && !(ctx.expr() instanceof ArrayList)) {
      ASTNode initNode = new ASTNode(Type.ASSIGN);
      initNode.addChild(visit((ParseTree) ctx.expr()));
      node.addChild(initNode);
    } else if (ctx.expr() != null && ctx.expr() instanceof ArrayList) {
      for (int i = 0; i < ctx.expr().size(); i++) {
        node.children.getLast().addChild(visit(ctx.expr(i)));
      }
    }

    // Process array-specific constructs
    if (ctx.array() != null) {
      node.addChild(visit(ctx.array()));
    }

    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#assign}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitAssign(CppParser.AssignContext ctx) {
    ASTNode node = new ASTNode(Type.ASSIGN);
    if (ctx.getChild(1).equals(ctx.ASSIGN_OP())) {
      node.setValue(ctx.ASSIGN_OP().getText());
    } else {
      node.setValue(ctx.getChild(1).getText());
    }
    if (ctx.children.getFirst().equals(ctx.ID())) {
      node.addChild(new ASTNode(Type.ID, ctx.children.getFirst().getText()));
    } else {
      node.addChild(visit(ctx.children.getFirst()));
    }
    node.addChild(visit(ctx.getChild(2)));
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#dec_inc}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitDec_inc(CppParser.Dec_incContext ctx) {
    ASTNode node = new ASTNode(Type.DEC_INC);
    for (int i = 0; i < ctx.getChildCount(); i++) {
      if (ctx.getChild(i).equals(ctx.DEC_INC_OP())) {
        node.addChild(new ASTNode(ctx.DEC_INC_OP().getText()));
      } else if (ctx.getChild(i).equals(ctx.ID())) {
        node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
      } else {
        node.addChild(visit(ctx.getChild(i)));
      }
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#fn_decl}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitFn_decl(CppParser.Fn_declContext ctx) {
    ASTNode node = new ASTNode(Type.FN_DECL);

    // Process const or static modifiers
    if (ctx.getChild(0).getText().contains("const")
        || ctx.getChild(1).getText().contains("const")) {
      node.addChild(new ASTNode("const"));
    }
    if (ctx.getChild(0).getText().contains("static")
        || ctx.getChild(1).getText().contains("static")) {
      node.addChild(new ASTNode("static"));
    }

    // Process return type
    if (ctx.type() != null) {
      node.addChild(visit(ctx.type()));
    } else {
      node.addChild(new ASTNode("void"));
    }

    // Process function name or operator
    if (ctx.ID() instanceof ArrayList<TerminalNode>) {
      ASTNode classNode = new ASTNode(Type.CLASS);
      classNode.addChild(new ASTNode(Type.ID, ctx.ID().getFirst().getText()));
      node.addChild(classNode);
    }

    if (ctx.ref() != null) {
      node.addChild(visit(ctx.ref()));
    } else if (ctx.operator() != null) {
      node.addChild(visit(ctx.operator()));
    } else if (ctx.ID() instanceof ArrayList<TerminalNode>) {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getLast().getText()));
    } else {
      node.addChild(new ASTNode(Type.ID, ((ParseTree) ctx.ID()).getText()));
    }

    // Process parameters
    if (ctx.params() != null) {
      node.addChild(visit(ctx.params()));
    }

    // Process block
    if (ctx.block() != null) {
      node.addChild(visit(ctx.block()));
    }

    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#operator}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitOperator(CppParser.OperatorContext ctx) {
    ASTNode node = new ASTNode(Type.OPERATOR);
    if (ctx.children.getLast().equals(ctx.DEC_INC_OP())) {
      node.setValue(ctx.DEC_INC_OP().getChild(0).getText());
    } else {
      node.setValue(ctx.children.getLast().getText());
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#abstract_fn}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitAbstract_fn(CppParser.Abstract_fnContext ctx) {
    ASTNode node = new ASTNode(Type.ABSTRACT_FN);
    // Process return type
    if (ctx.type() != null) {
      node.addChild(visit(ctx.type()));
    } else {
      node.addChild(new ASTNode("void"));
    }

    if (ctx.ref() != null) {
      node.addChild(visit(ctx.ref()));
    } else if (ctx.operator() != null) {
      node.addChild(visit(ctx.operator()));
    } else {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    }

    // Process parameters
    if (ctx.params() != null) {
      node.addChild(visit(ctx.params()));
    }

    node.addChild(new ASTNode(Type.INT, ctx.getChild(ctx.getChildCount() - 2).getText()));
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#params}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitParams(CppParser.ParamsContext ctx) {
    ASTNode node = new ASTNode(Type.PARAMS);
    for (int i = 0; i < ctx.type().size(); i++) {
      node.addChild(new ASTNode(Type.valueOf(visit(ctx.type(i)).getType())));
    }
    int current = 0;
    for (ASTNode type : node.children) {
      for (int i = current; i < ctx.getChildCount(); i++) {
        if (ctx.getChild(i).getText().equals(",")) {
          current = i + 1;
          break;
        }
        if (ctx.getChild(i).getText().equals("const")) {
          type.addChild(new ASTNode(ctx.getChild(i).getText()));
        } else if (ctx.ID().contains(ctx.getChild(i))) {
          type.addChild(new ASTNode(Type.ID, ctx.getChild(i).getText()));
        } else if (ctx.ref().contains(ctx.getChild(i)) || ctx.expr().contains(ctx.getChild(i))) {
          type.addChild(visit(ctx.getChild(i)));
        }
      }
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#return}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitReturn(CppParser.ReturnContext ctx) {
    ASTNode node = new ASTNode(Type.RETURN);
    node.addChild(visit(ctx.getChild(1)));
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#block}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitBlock(CppParser.BlockContext ctx) {
    ASTNode node = new ASTNode(Type.BLOCK);
    for (int i = 1; i < ctx.getChildCount() - 1; i++) {
      node.addChild(visit(ctx.getChild(i)));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#while}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitWhile(CppParser.WhileContext ctx) {
    ASTNode node = new ASTNode(Type.WHILE);
    node.addChild(visit(ctx.getChild(2)));
    node.addChild(visit(ctx.getChild(4)));
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#if}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitIf(CppParser.IfContext ctx) {
    ASTNode node = new ASTNode(Type.IF);
    for (int i = 0; i < ctx.expr().size(); i++) {
      node.addChild(visit(ctx.expr(i)));
      node.addChild(visit(ctx.block(i)));
    }
    if (ctx.getChild(ctx.getChildCount() - 2).getText().equals("else")) {
      node.addChild(visit(ctx.block().getLast()));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#fn_call}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitFn_call(CppParser.Fn_callContext ctx) {
    ASTNode node = new ASTNode(Type.FN_CALL);
    if (ctx.getChild(1).getText().equals(":")) {
      node.addChild(new ASTNode(Type.CLASS, ctx.getChild(1).getText()));
    } else {
      node.setValue(ctx.children.getFirst().getText());
    }
    if (ctx.getChild(ctx.getChildCount() - 2).equals(ctx.args())) {
      node.addChild(visit(ctx.getChild(ctx.getChildCount() - 2)));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#args}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitArgs(CppParser.ArgsContext ctx) {
    ASTNode node = new ASTNode(Type.ARGS);
    for (int i = 0; i < ctx.getChildCount(); i += 2) {
      node.addChild(visit(ctx.getChild(i)));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#expr}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitExpr(CppParser.ExprContext ctx) {
    if (ctx.getChildCount() == 1) {
      String text = ctx.getChild(0).getText();
      if (ctx.children.getFirst().equals(ctx.NULL())) {
        return new ASTNode(Type.NULL, text);
      } else if (ctx.children.getFirst().equals(ctx.BOOL())) {
        return new ASTNode(Type.BOOL, text);
      } else if (ctx.children.getFirst().equals(ctx.INT())) {
        return new ASTNode(Type.INT, text);
      } else if (ctx.children.getFirst().equals(ctx.CHAR())) {
        return new ASTNode(Type.CHAR, text);
      } else if (ctx.children.getFirst().equals(ctx.ID())) {
        return new ASTNode(Type.ID, text);
      } else {
        return visit(ctx.children.getFirst());
      }
    } else {
      if (ctx.children.getFirst().getText().equals("(")
          && ctx.children.getLast().getText().equals(")")) {
        return visit(ctx.getChild(1));
      }
      ASTNode node = new ASTNode(ctx.getChild(1).getText());
      node.addChild(visit(ctx.children.getFirst()));
      node.addChild(visit(ctx.children.getLast()));
      return node;
    }
  }

  /**
   * Visit a parse tree produced by {@link CppParser#delete}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitDelete(CppParser.DeleteContext ctx) {
    ASTNode node = new ASTNode(Type.DELETE);
    int index = (ctx.getChildCount() == 5) ? 3 : 1;
    if (ctx.getChild(index).equals(ctx.ID())) {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    } else {
      node.addChild(visit(ctx.getChild(index)));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#constructor}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitConstructor(CppParser.ConstructorContext ctx) {
    return null;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#destructor}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitDestructor(CppParser.DestructorContext ctx) {
    ASTNode node = new ASTNode(Type.DESTRUCTOR);

    if (ctx.children.getFirst().getText().equals("virtual")){
      node.setValue("virtual");
    }

    node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));

    if (ctx.params() != null) {
      node.addChild(visit(ctx.params()));
    }

    if (ctx.block() != null) {
      node.addChild(visit(ctx.block()));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#class}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitClass(CppParser.ClassContext ctx) {
    ASTNode node = new ASTNode(Type.CLASS);

    // Process class name
    node.addChild(new ASTNode(Type.ID, ctx.getChild(1).getText()));

    int index;

    // Process base class (if present)
    if (ctx.ID() instanceof ArrayList<TerminalNode>) {
      ASTNode baseClassNode = new ASTNode(Type.ID, ctx.ID(1).getText());
      node.addChild(new ASTNode("extends"));
      node.addChild(baseClassNode);
      index = 8;
    } else {
      index = 5;
    }

    boolean virtual = false;
    // Process members: constructors, destructors, methods, variables
    for (int i = index; i < ctx.getChildCount() - 2; i++) {
      ParseTree child = ctx.getChild(i);
      if (child.getText().equals("virtual")) {
        virtual = true;
      } else {
        node.addChild(visit(child));
        if (virtual) {
          node.children.getLast().setValue("virtual");
          virtual = false;
        }
      }
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#main}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitMain(CppParser.MainContext ctx) {
    return null;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#type}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitType(CppParser.TypeContext ctx) {
    return new ASTNode(Type.valueOf(ctx.children.getFirst().getText().toUpperCase()));
  }

  /**
   * Visit a parse tree produced by {@link CppParser#array}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitArray(CppParser.ArrayContext ctx) {
    ASTNode node = new ASTNode(Type.ARRAY);
    if (ctx.getChild(1).equals(ctx.args())) {
      node.addChildren(visitArgs((CppParser.ArgsContext) ctx.getChild(1)).children);
      return node;
    }
    for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
      node.addChild(visit(ctx.getChild(i)));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#array_item}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitArray_item(CppParser.Array_itemContext ctx) {
    ASTNode node = new ASTNode(Type.ARRAY_ITEM);
    if (ctx.getChild(0).equals(ctx.ID())) {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    } else {
      node.addChild(visit(ctx.getChild(0)));
    }
    for (int i = 2; i < ctx.getChildCount() - 1; i += 2) {
      node.addChild(visit(ctx.getChild(i)));
    }
    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#ref}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitRef(CppParser.RefContext ctx) {
    return new ASTNode(Type.REF, ctx.ID().getText());
  }

  /**
   * Visit a parse tree produced by {@link CppParser#obj_usage}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitObj_usage(CppParser.Obj_usageContext ctx) {
    ASTNode node = new ASTNode(Type.OBJ_USAGE);

    // Process "this" keyword if present
    if (ctx.getChild(0).getText().equals("this")) {
      node.addChild(new ASTNode("this"));
    }
    // Process object identifier
    else {
      node.addChild(new ASTNode(Type.ID, ctx.children.getFirst().getText()));
    }

    // Process optional member access
    if (ctx.getChildCount() > 1) {
      if (ctx.array_item() != null) {
        node.addChild(visit(ctx.array_item()));
      } else if (ctx.assign() != null) {
        node.addChild(visit(ctx.assign()));
      } else if (ctx.dec_inc() != null) {
        node.addChild(visit(ctx.dec_inc()));
      } else if (ctx.fn_call() != null) {
        node.addChild(visit(ctx.fn_call()));
      } else if (ctx.ID() instanceof ArrayList<TerminalNode>) {
        node.addChild(new ASTNode(Type.ID, ctx.ID().getLast().getText()));
      } else {
        node.addChild(new ASTNode(Type.ID, ((ParseTree) ctx.ID()).getText()));
      }
    }
    return node;
  }
}
