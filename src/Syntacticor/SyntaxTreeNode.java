package Syntacticor;

import Tokenlizer.Token;

import java.util.ArrayList;
import java.util.List;

public class SyntaxTreeNode {
    private SyntaxTreeNodeType type;
    private Token token;
    private List<SyntaxTreeNode> child;

    public SyntaxTreeNode(SyntaxTreeNodeType type){
        this.type = type;
        this.child = new ArrayList<SyntaxTreeNode>();
    }

    public SyntaxTreeNode(Token token){
        this(SyntaxTreeNodeType.TOKEN);
        this.token = token;
    }

    public SyntaxTreeNode appendChild(SyntaxTreeNode childNode){
        this.child.add(childNode);
        return this;
    }

    public List<SyntaxTreeNode> getChildList(){
        return new ArrayList<SyntaxTreeNode>(this.child);
    }

    public SyntaxTreeNodeType getType(){
        return this.type;
    }

    public String getTypeName(){
        switch(this.type){
            case TOKEN:
                return "token:" + this.token.getValue();
            case PROGRAM:
                return "program";
            case STMT:
                return "stmt";
            case EXPR_STMT:
                return "expr_stmt";
            case DECL_STMT:
                return "decl_stmt";
            case IF_STMT:
                return "if_stmt";
            case WHILE_STMT:
                return "while_stmt";
            case BREAK_STMT:
                return "break_stmt";
            case CONTINUE_STMT:
                return "continue_stmt";
            case RETURN_STMT:
                return "return_stmt";
            case BLOCK_STMT:
                return "block_stmt";
            case EMPTY_STMT:
                return "empty_stmt";
            case LET_DECL_STMT:
                return "let_decl_stmt";
            case CONST_DECL_STMT:
                return "const_decl_stmt";
            case EXPR:
                return "expr";
            case OPERATOR_EXPR:
                return "operator_expr";
            case NEGATE_EXPR:
                return "negate_expr";
            case ASSIGN_EXPR:
                return "assign_expr";
            case AS_EXPR:
                return "as_expr";
            case CALL_EXPR:
                return "call_expr";
            case LITERAL_EXPR:
                return "literal_expr";
            case IDENT_EXPR:
                return "ident_expr";
            case GROUP_EXPR:
                return "group_expr";
            case CALL_PARAM_LIST:
                return "call_param_list";
            case FUNCTION:
                return "function";
            case FUNCTION_PARAM:
                return "function_param";
            case FUNCTION_PARAM_LIST:
                return "function_param_list";
            default:
                return "unknown";
        }
    }
}
