import AST.ASTNode;
import AST.Type;
import org.antlr.v4.runtime.ParserRuleContext;
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

  @Override
  public ASTNode visitVar_declaration(CppParser.Var_declarationContext ctx) {
    ASTNode node = new ASTNode(Type.VAR_DECL);
    ASTNode type = visit(ctx.type());
    type.setValue(ctx.ID().getText());
    node.addChild(type);
    if (ctx.expr() != null) {
      node.addChild(visit(ctx.expr()));
    }
    return node;
  }

  @Override
  public ASTNode visitVar_ref(CppParser.Var_refContext ctx) {
    ASTNode node = new ASTNode(Type.VAR_REF);
    ASTNode type = visit(ctx.type());
    type.setValue(ctx.ID().getText());
    node.addChild(type);
    node.addChild(visit(ctx.expr()));
    return node;
  }

  @Override
  public ASTNode visitArray_decl(CppParser.Array_declContext ctx) {
    ASTNode node = new ASTNode(Type.ARRAY_DECL);
    ASTNode type = visit(ctx.type());
    type.setValue(ctx.ID().getText());
    for (int i = 0; i < ctx.expr().size(); i++) {
      type.addChild(visit(ctx.expr(i)));
    }
    node.addChild(type);
    return node;
  }

  @Override
  public ASTNode visitArray_init(CppParser.Array_initContext ctx) {
    ASTNode node = new ASTNode(Type.ARRAY_INIT);
    ASTNode type = visit(ctx.type());
    type.setValue(ctx.ID().getText());
    if (ctx.expr() != null) {
      for (int i = 0; i < ctx.expr().size(); i++) {
        type.addChild(visit(ctx.expr(i)));
      }
    }
    node.addChild(type);
    node.addChild(visitArray(ctx.array()));
    return node;
  }

  @Override
  public ASTNode visitArray_ref(CppParser.Array_refContext ctx) {
    ASTNode node = new ASTNode(Type.ARRAY_REF);
    ASTNode type = visit(ctx.type());
    type.setValue(ctx.ID(0).getText());
    for (int i = 0; i < ctx.expr().size(); i++) {
      type.addChild(visit(ctx.expr(i)));
    }
    node.addChild(type);
    if (ctx.array_item() != null) {
      node.addChild(visit(ctx.array_item()));
    } else {
      node.addChild(new ASTNode(Type.ID, ctx.ID(1).getText()));
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
        node.addChild(
            new ASTNode(
                (ctx.DEC_INC_OP().getText().equals("++")) ? Type.INC : Type.DEC,
                ctx.DEC_INC_OP().getText()));
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

    ASTNode function;

    // Process return type
    if (ctx.type() != null) {
      function = visit(ctx.type());
    } else {
      function = new ASTNode(Type.VOID);
    }

    if (ctx.ID() != null) {
      function.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    }

    if (ctx.REF() != null) {
      function.addChild(new ASTNode(Type.REF));
    }

    node.addChild(function);

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
   * Visit a parse tree produced by {@link CppParser#abstract_fn}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitAbstract_fn(CppParser.Abstract_fnContext ctx) {
    ASTNode node = new ASTNode(Type.ABSTRACT_FN);

    ASTNode function;

    // Process return type
    if (ctx.type() != null) {
      function = visit(ctx.type());
    } else {
      function = new ASTNode(Type.VOID);
    }

    if (ctx.ID() != null) {
      function.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    }

    if (ctx.REF() != null) {
      function.addChild(new ASTNode(Type.REF));
    }

    node.addChild(function);

    // Process parameters
    if (ctx.params() != null) {
      node.addChild(visit(ctx.params()));
    }

    node.addChild(new ASTNode(Type.INT, ctx.INT().getText()));
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
      ASTNode typeNode = visit(ctx.type(i));
      typeNode.setValue(ctx.ID(i).getText());
      node.addChild(typeNode);
    }
    int current = 0;
    for (ASTNode type : node.children) {
      for (int i = current; i < ctx.getChildCount(); i++) {
        if (ctx.getChild(i).getText().equals(",")) {
          current = i + 1;
          break;
        }
        if (ctx.REF().contains(ctx.getChild(i))) {
          type.addChild(new ASTNode(Type.REF));
        }
        if (ctx.expr().contains(ctx.getChild(i))) {
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
    if (ctx.expr() != null) {
      node.addChild(visit(ctx.expr()));
    }
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
    if (!ctx.stmt().isEmpty()) {
      for (CppParser.StmtContext child : ctx.stmt()) {
        node.addChild(visit(child));
      }
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
    node.addChild(visit(ctx.expr()));
    node.addChild(visit(ctx.block()));
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

    switch (ctx.ID().size()) {
      case 1:
        node.setValue(ctx.children.getFirst().getText());
        break;
      case 2:
        node.setValue(ctx.ID().getLast().getText());
        node.addChild(new ASTNode(Type.CLASSTYPE, ctx.ID().getFirst().getText()));
        break;
    }

    if (ctx.args() != null) {
      node.addChild(visit(ctx.args()));
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

  private ASTNode calculate(ParserRuleContext ctx, Type type) {
    ASTNode node = new ASTNode(type, ctx.getChild(1).getText());
    node.addChild(visit(ctx.children.getFirst()));
    node.addChild(visit(ctx.children.getLast()));
    return node;
  }

  @Override
  public ASTNode visitMul(CppParser.MulContext ctx) {
    return calculate(ctx, Type.MUL);
  }

  @Override
  public ASTNode visitDiv(CppParser.DivContext ctx) {
    return calculate(ctx, Type.DIV);
  }

  @Override
  public ASTNode visitAdd(CppParser.AddContext ctx) {
    return calculate(ctx, Type.ADD);
  }

  @Override
  public ASTNode visitSub(CppParser.SubContext ctx) {
    return calculate(ctx, Type.SUB);
  }

  @Override
  public ASTNode visitEqual(CppParser.EqualContext ctx) {
    return calculate(ctx, Type.EQUAL);
  }

  @Override
  public ASTNode visitNot_Equal(CppParser.Not_EqualContext ctx) {
    return calculate(ctx, Type.NOT_EQUAL);
  }

  @Override
  public ASTNode visitLess_Equal(CppParser.Less_EqualContext ctx) {
    return calculate(ctx, Type.LESS_EQUAL);
  }

  @Override
  public ASTNode visitGreater_Equal(CppParser.Greater_EqualContext ctx) {
    return calculate(ctx, Type.GREATER_EQUAL);
  }

  @Override
  public ASTNode visitLess(CppParser.LessContext ctx) {
    return calculate(ctx, Type.LESS);
  }

  @Override
  public ASTNode visitGreater(CppParser.GreaterContext ctx) {
    return calculate(ctx, Type.GREATER);
  }

  @Override
  public ASTNode visitAnd(CppParser.AndContext ctx) {
    return calculate(ctx, Type.AND);
  }

  @Override
  public ASTNode visitOr(CppParser.OrContext ctx) {
    return calculate(ctx, Type.OR);
  }

  @Override
  public ASTNode visitMod(CppParser.ModContext ctx) {
    return calculate(ctx, Type.MOD);
  }

  @Override
  public ASTNode visitNot(CppParser.NotContext ctx) {
    ASTNode node = new ASTNode(Type.NOT, ctx.getChild(0).getText());
    node.addChild(visit(ctx.e));
    return node;
  }

  @Override
  public ASTNode visitNull(CppParser.NullContext ctx) {
    return new ASTNode(Type.NULL, ctx.getText());
  }

  @Override
  public ASTNode visitBool(CppParser.BoolContext ctx) {
    return new ASTNode(Type.BOOL, ctx.getText());
  }

  @Override
  public ASTNode visitInt(CppParser.IntContext ctx) {
    if (ctx.NEG() != null) {
      return new ASTNode(Type.INT, ctx.NEG().getText() + ctx.INT().getText());
    }
    return new ASTNode(Type.INT, ctx.INT().getText());
  }

  @Override
  public ASTNode visitChar(CppParser.CharContext ctx) {
    return new ASTNode(Type.CHAR, ctx.getText().split("")[1]);
  }

  @Override
  public ASTNode visitId(CppParser.IdContext ctx) {
    return new ASTNode(Type.ID, ctx.getText());
  }

  @Override
  public ASTNode visitNested(CppParser.NestedContext ctx) {
    return visit(ctx.e);
  }

  /**
   * Visit a parse tree produced by {@link CppParser#constructor}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitConstructor(CppParser.ConstructorContext ctx) {
    ASTNode node = new ASTNode(Type.CONSTRUCTOR, ctx.ID().getFirst().getText());

    if (ctx.params() != null) {
      node.addChild(visit(ctx.params()));
    }

    if (ctx.ID().size() > 1) {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getLast().getText()));
      if (ctx.args() != null) {
        node.addChild(visit(ctx.args()));
      }
    }

    if (ctx.block() != null) {
      node.addChild(visit(ctx.block()));
    }

    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#copy_constructor}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitCopy_constructor(CppParser.Copy_constructorContext ctx) {
    ASTNode node = new ASTNode(Type.COPY_CONSTRUCTOR, "copy_" + ctx.ID().getFirst().getText());

    ASTNode child = new ASTNode(Type.CLASSTYPE, ctx.ID(2).getText());
    child.addChild(new ASTNode(Type.ID, ctx.ID(1).getText()));
    child.addChild(new ASTNode(Type.REF));
    node.addChild(child);

    if (ctx.args() != null) {
      node.addChild(new ASTNode(Type.ID, ctx.ID().getLast().getText()));
      node.addChild(visit(ctx.args()));
    }

    if (ctx.block() != null) {
      node.addChild(visit(ctx.block()));
    }

    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#destructor}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitDestructor(CppParser.DestructorContext ctx) {
    ASTNode node = new ASTNode(Type.DESTRUCTOR, "~" + ctx.ID().getText());

    if (ctx.children.getFirst().getText().equals("virtual")) {
      node.addChild(new ASTNode("virtual"));
    }

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
    ASTNode node =
        new ASTNode(Type.OPERATOR, ctx.getChild(2).getText() + ctx.getChild(3).getText());

    if (ctx.ID() != null) {
      ASTNode child = new ASTNode(Type.ID, ctx.ID().getText());
      child.addChild(new ASTNode(Type.REF));
      node.addChild(child);
    }

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
    ASTNode node = new ASTNode(Type.CLASS, ctx.getChild(1).getText());
    int index;

    // Process base class (if present)
    if (ctx.ID().size() == 2) {
      ASTNode baseClassNode = new ASTNode(Type.CLASSTYPE, "extends");
      baseClassNode.addChild(new ASTNode(Type.ID, ctx.ID(1).getText()));
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
    ASTNode node = new ASTNode(Type.MAIN);

    node.addChild(new ASTNode(Type.INT));

    if (ctx.block() != null) {
      node.addChild(visit(ctx.block()));
    }

    return node;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#type}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitType(CppParser.TypeContext ctx) {
    if (ctx.ID() != null) {
      ASTNode toReturn = new ASTNode(Type.CLASSTYPE, ctx.ID().getText());
      toReturn.addChild(new ASTNode(Type.ID, ctx.children.getFirst().getText()));
      return toReturn;
    }
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
    if (ctx.args() != null) {
      node.addChildren(visit(ctx.args()).children);
      return node;
    } else {
      for (int i = 1; i < ctx.getChildCount() - 1; i += 2) {
        node.addChild(visit(ctx.getChild(i)));
      }
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
    ASTNode node = new ASTNode(Type.ARRAY_ITEM, ctx.ID().getText());
    for (int i = 0; i < ctx.expr().size(); i++) {
      node.addChild(visit(ctx.expr(i)));
    }
    return node;
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

    // Handle the optional 'this' or '(*this)' part
    if (ctx.getText().startsWith("this")) {
      node.setValue("this");
      if (ctx.children.size() > 1) {
        for (int i = 2; i < ctx.getChildCount() - 1; i += 2) {
          if (ctx.getChild(i) instanceof TerminalNode) {
            node.addChild(new ASTNode(Type.ID, ctx.getChild(i).getText()));
          } else if (ctx.getChild(i) instanceof CppParser.Fn_callContext) {
            node.addChild(new ASTNode(Type.FN_CALL, ctx.getChild(i).getText()));
          }
        }
      }
    } else if (ctx.getText().startsWith("*this")) {
      node.setValue("*this");
    } else if (ctx.getText().startsWith("(*this)")) {
      node.setValue("*this");
      for (int i = 5; i < ctx.getChildCount() - 1; i += 2) {
        if (ctx.getChild(i) instanceof TerminalNode) {
          node.addChild(new ASTNode(Type.ID, ctx.getChild(i).getText()));
        } else if (ctx.getChild(i) instanceof CppParser.Fn_callContext) {
          node.addChild(new ASTNode(Type.FN_CALL, ctx.getChild(i).getText()));
        }
      }
    } else {
      for (int i = 0; i < ctx.getChildCount() - 1; i += 2) {
        if (ctx.getChild(i) instanceof TerminalNode) {
          node.addChild(new ASTNode(Type.ID, ctx.getChild(i).getText()));
        } else if (ctx.getChild(i) instanceof CppParser.Fn_callContext) {
          node.addChild(new ASTNode(Type.FN_CALL, ctx.getChild(i).getText()));
        }
      }
    }

    if (ctx.array_item() != null) {
      node.addChild(visit(ctx.array_item()));
    } else if (ctx.dec_inc() != null) {
      node.addChild(visit(ctx.dec_inc()));
    } else {
      if (ctx.children.getLast() instanceof TerminalNode) {
        node.addChild(new ASTNode(Type.ID, ctx.children.getLast().getText()));
      } else if (ctx.children.getLast() instanceof CppParser.Fn_callContext) {
        node.addChild(new ASTNode(Type.FN_CALL, ctx.children.getLast().getText()));
      }
    }

    return node;
  }
}
