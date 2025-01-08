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

    // Process type information
    if (ctx.type() != null) {
        node.addChild(new ASTNode(Type.ID, ctx.type().getText()));
    }

    // Process variable name or reference
    if (ctx.ref() != null) {
        node.addChild(new ASTNode(Type.REF, ctx.ref().getText()));
    } else if (ctx.ID() != null) {
        node.addChild(new ASTNode(Type.ID, ctx.ID().getText()));
    }

    // Process initialization if present
    if (ctx.expr() != null) {
        ASTNode initNode = new ASTNode(Type.ASSIGN);
        initNode.addChild(visit(ctx.expr()));
        node.addChild(initNode);
    }

    // Process array-specific constructs
    if (ctx.array() != null) {
        node.addChild(visit(ctx.array()));
    } else if (ctx.getText().contains("[")) {
        ASTNode arrayNode = new ASTNode(Type.ARRAY);
        node.addChild(arrayNode);
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
    // TODO: const? und default wert
    for (int i = 0; i < ctx.type().size(); i++) {
      node.addChild(new ASTNode(Type.valueOf(visit(ctx.type(i)).getType())));
    }

    for (int i = 0; i < ctx.getChildCount(); i++) {
      if (ctx.getChild(i).getText().equals("const")) {
        node.addChild(new ASTNode(ctx.getChild(i).getText()));
      } else if (ctx.getChild(i) instanceof CppParser.RefContext) {
        node.addChild(visitRef((CppParser.RefContext) ctx.getChild(i)));
      } else if (ctx.getChild(i) instanceof TerminalNode) {
        node.children.getLast().setValue(ctx.getChild(i).getText());
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
    node.addChild(visit(ctx.getChild(2)));
    node.addChild(visit(ctx.getChild(4)));
    // TODO: beliebig viele else if
    if (ctx.getChildCount() >= 5
        && !ctx.getChild(ctx.getChildCount() - 2).getText().equals("else")) {
      node.addChild(visit(ctx.getChild(6)));
    } else {
      node.addChild(visit(ctx.getChild(ctx.getChildCount() - 1)));
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
      if (ctx.children.getFirst().equals(ctx.NULL())) {
        return new ASTNode(Type.NULL, ctx.getChild(0).getText());
      } else if (ctx.children.getFirst().equals(ctx.BOOL())) {
        return new ASTNode(Type.BOOL, ctx.getChild(0).getText());
      } else if (ctx.children.getFirst().equals(ctx.INT())) {
        return new ASTNode(Type.INT, ctx.getChild(0).getText());
      } else if (ctx.children.getFirst().equals(ctx.CHAR())) {
        return new ASTNode(Type.CHAR, ctx.getChild(0).getText());
      } else if (ctx.children.getFirst().equals(ctx.ID())) {
        return new ASTNode(Type.ID, ctx.getChild(0).getText());
      } else if (ctx.getChildCount() == 3) {
        ASTNode node;
        if (ctx.getChild(1).equals(ctx.CALC_OP())) {
          node = new ASTNode(Type.CALC_OP, ctx.getChild(1).getText());
        } else if (ctx.getChild(1).equals(ctx.COMPARE_OP())) {
          node = new ASTNode(Type.COMPARE_OP, ctx.getChild(1).getText());
        } else if (ctx.getChild(1).equals(ctx.BOOL_OP())) {
          node = new ASTNode(Type.BOOL_OP, ctx.getChild(1).getText());
        } else {
          return visit(ctx.getChild(1));
        }
        node.addChild(visit(ctx.getChild(0)));
        node.addChild(visit(ctx.getChild(2)));
        return node;
      } else {
        return visitChildren(ctx);
      }
    }
    return null;
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
    return null;
  }

  /**
   * Visit a parse tree produced by {@link CppParser#class}.
   *
   * @param ctx the parse tree
   * @return the visitor result
   */
  @Override
  public ASTNode visitClass(CppParser.ClassContext ctx) {
    return null;
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
    }else {
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
    return null;
  }
}
