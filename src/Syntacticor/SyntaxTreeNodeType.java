package Syntacticor;

public enum SyntaxTreeNodeType {
    TOKEN,

    PROGRAM,

    STMT,
    EXPR_STMT,
    DECL_STMT,
    IF_STMT,
    WHILE_STMT,
    BREAK_STMT,
    CONTINUE_STMT,
    RETURN_STMT,
    BLOCK_STMT,
    EMPTY_STMT,

    //decl_stmt
    LET_DECL_STMT,
    CONST_DECL_STMT,

    EXPR,
    OPERATOR_EXPR,
    NEGATE_EXPR,
    ASSIGN_EXPR,
    AS_EXPR,
    CALL_EXPR,
    LITERAL_EXPR,
    IDENT_EXPR,
    GROUP_EXPR,

    //call_expr
    CALL_PARAM_LIST,

    FUNCTION,
    FUNCTION_PARAM,
    FUNCTION_PARAM_LIST
}
