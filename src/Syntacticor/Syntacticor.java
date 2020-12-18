package Syntacticor;

import Tokenlizer.Token;
import Tokenlizer.TokenType;

import java.util.List;

public class Syntacticor {
    private static Syntacticor syntacticor;
    private static int privateTable[][];

    private List<Token> tokenList;
    private int location;

    static{
        privateTable = new int[136][136];
    }

    private Syntacticor() {}

    public static Syntacticor getInstance(){
        if(syntacticor == null){
            syntacticor = new Syntacticor();
        }
        return syntacticor;
    }

    private void nextToken(){
        this.location += this.location>=this.tokenList.size() ? 0 : 1;
    }

    private void lastToken(){
        this.location -= this.location<=0 ? 0 : 1;
    }

    private Token getToken(){
        if(this.tokenList==null || this.location<0 || this.location>=this.tokenList.size()){
            return null;
        }
        else{
            return this.tokenList.get(this.location);
        }
    }

    private boolean isEND(){
        return this.location >= this.tokenList.size();
    }

    public void init(List list){
        this.tokenList = list;
        this.location = 0;
    }

    public SyntaxTreeNode analyze() throws SyntacticorError {
        SyntaxTreeNode root = new SyntaxTreeNode(SyntaxTreeNodeType.PROGRAM);
        while(!isEND()){
            if(getToken().getTokenType() == TokenType.FN_KW){
//                System.out.println("to Function");
                root.appendChild(analyzeFunction());
            }
            else if(getToken().getTokenType()==TokenType.LET_KW
                    || getToken().getTokenType() == TokenType.CONST_KW){
                root.appendChild(analyzeDeclStmt());
            }
            else{
                throw new SyntacticorError("Illigal item.");
            }
        }
        return root;
    }

    public void printTree(SyntaxTreeNode node, int deep){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<deep; i++){
            stringBuilder.append("|  ");
        }
        stringBuilder.append('<');
        stringBuilder.append(node.getTypeName());
        stringBuilder.append('>');

        System.out.println(stringBuilder);

        for(SyntaxTreeNode s : node.getChildList()){
            printTree(s, deep+1);
        }
    }

    private SyntaxTreeNode analyzeFunction() throws SyntacticorError {
        SyntaxTreeNode function = new SyntaxTreeNode(SyntaxTreeNodeType.FUNCTION);

        if(isEND() || getToken().getTokenType()!=TokenType.FN_KW){
            throw new SyntacticorError("No 'fn' in a function.");
        }
        function.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a function.");
        }
        function.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.L_PAREN){
            throw new SyntacticorError("No '(' in a function.");
        }
        function.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND()){
            throw new SyntacticorError("No ')' in a function.");
        }
        else if(getToken().getTokenType()==TokenType.CONST_KW || getToken().getTokenType()==TokenType.IDENT){
            function.appendChild(analyzeFunctionParamList());
        }

        if(isEND() || getToken().getTokenType()!=TokenType.R_PAREN){
            throw new SyntacticorError("No ')' in a function.");
        }
        function.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.ARROW) {
            throw new SyntacticorError("No '->' in a function.");
        }
        function.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No ty in a function.");
        }
        function.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        function.appendChild(analyzeBlockStmt());
        return function;
    }

    private SyntaxTreeNode analyzeFunctionParamList() throws SyntacticorError {
        SyntaxTreeNode functionParamList = new SyntaxTreeNode(SyntaxTreeNodeType.FUNCTION_PARAM_LIST);

        if(isEND()
                || (getToken().getTokenType()!=TokenType.CONST_KW
                && getToken().getTokenType()!=TokenType.IDENT)){
            throw new SyntacticorError("Identify function_param_list error.");
        }
        functionParamList.appendChild(analyzeFunctionParam());

        while(isEND() || getToken().getTokenType() == TokenType.COMMA){
            functionParamList.appendChild(new SyntaxTreeNode(getToken()));
            functionParamList.appendChild(analyzeFunctionParam());
        }
        return functionParamList;
    }

    private SyntaxTreeNode analyzeFunctionParam() throws SyntacticorError {
        SyntaxTreeNode functionParam = new SyntaxTreeNode(SyntaxTreeNodeType.FUNCTION_PARAM);

        if(isEND()){
            throw new SyntacticorError("Identify function_param error.");
        }
        else if(getToken().getTokenType() == TokenType.CONST_KW){
            functionParam.appendChild(new SyntaxTreeNode(getToken()));
            nextToken();
        }

        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a function_param.");
        }
        functionParam.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.COLON){
            throw new SyntacticorError("No ':' in a function_param.");
        }
        functionParam.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No ty in a function_param.");
        }
        functionParam.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return functionParam;
    }

    private SyntaxTreeNode analyzeStmt() throws SyntacticorError {
        SyntaxTreeNode stmt = new SyntaxTreeNode(SyntaxTreeNodeType.STMT);

        if(isEND()){
            throw new SyntacticorError("Identify stmt error.");
        }

        switch(getToken().getTokenType()){
            case LET_KW:
            case CONST_KW:
                stmt.appendChild(analyzeDeclStmt());
                break;
            case IF_KW:
                stmt.appendChild(analyzeIfStmt());
                break;
            case WHILE_KW:
                stmt.appendChild(analyzeWhileStmt());
                break;
            case BREAK_KW:
                stmt.appendChild(analyzeBreakStmt());
                break;
            case CONTINUE_KW:
                stmt.appendChild(analyzeContinueStmt());
                break;
            case RETURN_KW:
                stmt.appendChild(analyzeReturnStmt());
                break;
            case L_BRACE:
                stmt.appendChild(analyzeBlockStmt());
                break;
            case SEMICOLON:
                // 直接忽略掉他
                nextToken();
                break;
            default:
                stmt.appendChild(analyzeExprStmt());
        }
        return stmt;
    }

    private SyntaxTreeNode analyzeExprStmt() throws SyntacticorError {
        SyntaxTreeNode exprStmt = new SyntaxTreeNode(SyntaxTreeNodeType.EXPR_STMT);

        exprStmt.appendChild(analyzeExpr());

        if(isEND() || getToken().getTokenType()!=TokenType.SEMICOLON){
            throw new SyntacticorError("No ';' in a expr_stmt.");
        }
        exprStmt.appendChild(new SyntaxTreeNode(getToken()));
        nextToken();
        return exprStmt;
    }

    private SyntaxTreeNode analyzeIfStmt() throws SyntacticorError {
        SyntaxTreeNode ifStmt = new SyntaxTreeNode(SyntaxTreeNodeType.IF_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.IF_KW){
            throw new SyntacticorError("No 'if' in a if_stmt.");
        }
        ifStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        ifStmt.appendChild(analyzeExpr());
        ifStmt.appendChild(analyzeBlockStmt());

        if(isEND() || getToken().getTokenType()!=TokenType.ELSE_KW){
            return ifStmt;
        }
        ifStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        while(!isEND() && getToken().getTokenType()==TokenType.IF_KW){
            ifStmt.appendChild(new SyntaxTreeNode(getToken()));
            nextToken();

            ifStmt.appendChild(analyzeExpr());
            ifStmt.appendChild(analyzeBlockStmt());

            if(isEND() || getToken().getTokenType()!=TokenType.ELSE_KW){
                return ifStmt;
            }
            ifStmt.appendChild(new SyntaxTreeNode(getToken()));
            nextToken();
        }

        ifStmt.appendChild(analyzeBlockStmt());

        return ifStmt;
    }

    private SyntaxTreeNode analyzeWhileStmt() throws SyntacticorError {
        SyntaxTreeNode whileStmt = new SyntaxTreeNode(SyntaxTreeNodeType.WHILE_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.WHILE_KW){
            throw new SyntacticorError("No 'while' in a while_stmt.");
        }
        whileStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();

        whileStmt.appendChild(analyzeExpr());
        whileStmt.appendChild(analyzeBlockStmt());

        return whileStmt;
    }

    private SyntaxTreeNode analyzeBreakStmt() throws SyntacticorError {
        SyntaxTreeNode breakStmt = new SyntaxTreeNode(SyntaxTreeNodeType.BREAK_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.BREAK_KW){
            throw new SyntacticorError("No 'break' in a break_stmt.");
        }
        breakStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.SEMICOLON){
            throw new SyntacticorError("No ';' in a break_stmt.");
        }
        breakStmt.appendChild(new SyntaxTreeNode(getToken()));

        return breakStmt;
    }

    private SyntaxTreeNode analyzeContinueStmt() throws SyntacticorError {
        SyntaxTreeNode continueStmt = new SyntaxTreeNode(SyntaxTreeNodeType.CONTINUE_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.CONTINUE_KW){
            throw new SyntacticorError("No 'continue' in a continue_stmt.");
        }
        continueStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.SEMICOLON){
            throw new SyntacticorError("No ';' in a continue_stmt.");
        }
        continueStmt.appendChild(new SyntaxTreeNode(getToken()));

        return continueStmt;
    }

    private SyntaxTreeNode analyzeReturnStmt() throws SyntacticorError {
        SyntaxTreeNode returnStmt = new SyntaxTreeNode(SyntaxTreeNodeType.RETURN_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.RETURN_KW){
            throw new SyntacticorError("No 'return' in a return_stmt.");
        }
        returnStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND()){
            throw new SyntacticorError("No ';' in a return_stmt.");
        }
        else if(getToken().getTokenType() != TokenType.SEMICOLON){
            returnStmt.appendChild(analyzeExpr());
        }

        if(isEND()){
            throw new SyntacticorError("No ';' in a return_stmt.");
        }
        returnStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return returnStmt;
    }

    private SyntaxTreeNode analyzeBlockStmt() throws SyntacticorError {
        SyntaxTreeNode blockStmt = new SyntaxTreeNode(SyntaxTreeNodeType.BLOCK_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.L_BRACE){
            throw new SyntacticorError("No '{' in a block_stmt.");
        }
        blockStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND()){
            throw new SyntacticorError("No '}' in a block_stmt.");
        }

        while(!isEND() && getToken().getTokenType()!=TokenType.R_BRACE){
            blockStmt.appendChild(analyzeStmt());
        }

        if(isEND() || getToken().getTokenType()!=TokenType.R_BRACE){
            throw new SyntacticorError("No '}' in a block_stmt.");
        }
        blockStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return blockStmt;
    }

    private SyntaxTreeNode analyzeDeclStmt() throws SyntacticorError {
        SyntaxTreeNode declStmt = new SyntaxTreeNode(SyntaxTreeNodeType.DECL_STMT);

        if(isEND()){
            throw new SyntacticorError("Illigal end in dele_stmt.");
        }
        else if(getToken().getTokenType() == TokenType.LET_KW){
            declStmt.appendChild(analyzeLetDeclStmt());
        }
        else if(getToken().getTokenType() == TokenType.CONST_KW){
            declStmt.appendChild(analyzeConstDeclStmt());
        }
        else{
            throw new SyntacticorError("Identify dele_stmt error.");
        }

        return declStmt;
    }

    private SyntaxTreeNode analyzeLetDeclStmt() throws SyntacticorError {
        SyntaxTreeNode letDeclStmt = new SyntaxTreeNode(SyntaxTreeNodeType.LET_DECL_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.LET_KW){
            throw new SyntacticorError("No 'let' in a let_decl_stmt.");
        }
        letDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a let_decl_stmt.");
        }
        letDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.COLON){
            throw new SyntacticorError("No ':' in a let_decl_stmt.");
        }
        letDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No ty in a let_decl_stmt.");
        }
        letDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND()){
            throw new SyntacticorError("No ';' in a let_decl_stmt.");
        }
        else if(getToken().getTokenType() == TokenType.ASSIGN){
            letDeclStmt.appendChild(new SyntaxTreeNode(getToken()));
            nextToken();
            letDeclStmt.appendChild(analyzeExpr());
        }

        if(isEND() || getToken().getTokenType() != TokenType.SEMICOLON){
            throw new SyntacticorError("No ';' in a let_decl_stmt.");
        }
        letDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return letDeclStmt;
    }

    private SyntaxTreeNode analyzeConstDeclStmt() throws SyntacticorError {
        SyntaxTreeNode constDeclStmt = new SyntaxTreeNode(SyntaxTreeNodeType.CONST_DECL_STMT);

        if(isEND() || getToken().getTokenType()!=TokenType.CONST_KW){
            throw new SyntacticorError("No 'const' in a const_decl_stmt.");
        }
        constDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a const_decl_stmt.");
        }
        constDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.COLON){
            throw new SyntacticorError("No ':' in a const_decl_stmt.");
        }
        constDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No ty in a const_decl_stmt.");
        }
        constDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.ASSIGN){
            throw new SyntacticorError("No =' in a const_decl_stmt.");
        }
        constDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        constDeclStmt.appendChild(analyzeExpr());

        if(isEND()){
            throw new SyntacticorError("No ';' in a const_decl_stmt.");
        }
        constDeclStmt.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return constDeclStmt;
    }

    private SyntaxTreeNode analyzeExpr() throws SyntacticorError {
        SyntaxTreeNode lhs = analyzePrimaryExpr();
        return analyzeOPG(lhs, 0);
    }

    private SyntaxTreeNode analyzePrimaryExpr() throws SyntacticorError {
        Token token = getToken();
//        System.out.println("Primary: token " + token.getValue());
        nextToken();
        if(token.getTokenType() == TokenType.IDENT){
            if(!isEND() && getToken().getTokenType() == TokenType.L_PAREN){
                lastToken();
                return analyzeCallExpr();
            }
            else{
                SyntaxTreeNode syntaxTreeNode = new SyntaxTreeNode(SyntaxTreeNodeType.IDENT_EXPR);
                syntaxTreeNode.appendChild(new SyntaxTreeNode(token));
                return syntaxTreeNode;
            }
        }
        else if(token.getTokenType() == TokenType.L_PAREN){
            SyntaxTreeNode syntaxTreeNode = new SyntaxTreeNode(SyntaxTreeNodeType.GROUP_EXPR);
            syntaxTreeNode.appendChild(new SyntaxTreeNode(token));
            syntaxTreeNode.appendChild(analyzeExpr());
            if(isEND() || getToken().getTokenType() != TokenType.R_PAREN){
                throw new SyntacticorError("No ')' in a group_expr.");
            }
            syntaxTreeNode.appendChild(new SyntaxTreeNode(getToken()));
            nextToken();
            return syntaxTreeNode;
        }
        else if(token.getTokenType() == TokenType.MINUS){
            SyntaxTreeNode syntaxTreeNode = new SyntaxTreeNode(SyntaxTreeNodeType.NEGATE_EXPR);
            syntaxTreeNode.appendChild(new SyntaxTreeNode(token));
            syntaxTreeNode.appendChild(analyzePrimaryExpr());
            return syntaxTreeNode;
        }
        else if(token.getTokenType() == TokenType.UINT_LITERAL
                || token.getTokenType() == TokenType.DOUBLE_LITERAL
                || token.getTokenType() == TokenType.STRING_LITERAL){
            SyntaxTreeNode syntaxTreeNode = new SyntaxTreeNode(SyntaxTreeNodeType.LITERAL_EXPR);
            syntaxTreeNode.appendChild(new SyntaxTreeNode(token));
            return syntaxTreeNode;
        }
        else{
            throw new SyntacticorError("Identify primary_expr error: token type " + token.getValue());
        }
    }

    private SyntaxTreeNode analyzeOPG(SyntaxTreeNode lhs, int priority) throws SyntacticorError {
        if(isEND()){
            return lhs;
        }

        while(getToken().isBinaryOp() && getToken().getPriority() >= priority){
            Token op = getToken();
            nextToken();
            SyntaxTreeNode rhs = analyzePrimaryExpr();

            while(getToken().isBinaryOp()
                    && getToken().getPriority() > op.getPriority()
                    || (getToken().isRightAssoc()
                    && getToken().getPriority() == op.getPriority())){
                rhs = analyzeOPG(rhs, getToken().getPriority());
            }

            if(op.getTokenType() == TokenType.AS_KW){
                SyntaxTreeNode operatorExpr = new SyntaxTreeNode(SyntaxTreeNodeType.AS_EXPR);
                operatorExpr.appendChild(lhs);
                operatorExpr.appendChild(new SyntaxTreeNode(op));
                if(rhs.getType() != SyntaxTreeNodeType.IDENT_EXPR){
                    throw new SyntacticorError("Ty is not a ident in as_expr.");
                }
                operatorExpr.appendChild(rhs.getChildList().get(0));
                lhs = operatorExpr;
            }
            else if(op.getTokenType() == TokenType.ASSIGN){
                SyntaxTreeNode operatorExpr = new SyntaxTreeNode(SyntaxTreeNodeType.ASSIGN_EXPR);
                if(lhs.getType() != SyntaxTreeNodeType.IDENT_EXPR){
                    throw new SyntacticorError("Not a ident in assign_expr left.");
                }
                operatorExpr.appendChild(lhs.getChildList().get(0));
                operatorExpr.appendChild(new SyntaxTreeNode(op));
                operatorExpr.appendChild(rhs);
                lhs = operatorExpr;
            }
            else{
                SyntaxTreeNode operatorExpr = new SyntaxTreeNode(SyntaxTreeNodeType.OPERATOR_EXPR);
                operatorExpr.appendChild(lhs);
                operatorExpr.appendChild(new SyntaxTreeNode(op));
                operatorExpr.appendChild(rhs);
                lhs = operatorExpr;
            }
        }
        return lhs;
    }

//    private SyntaxTreeNode analyzeSimpleExpr() throws SyntacticorError {
//        SyntaxTreeNode expr = new SyntaxTreeNode(SyntaxTreeNodeType.EXPR);
//
//        if(isEND()){
//            throw new SyntacticorError("Identify expr error.");
//        }
//
//        SyntaxTreeNode midExpr;
////        System.out.println("expr token: " + getToken().getValue());
//        switch(getToken().getTokenType()){
////            case MINUS:
////                midExpr = analyzeNegateExpr();
////                break;
//            case IDENT:
//                nextToken();
//                if(isEND()){
//                    lastToken();
//                    midExpr = analyzeIdentExpr();
//                }
//                else if(getToken().getTokenType() == TokenType.ASSIGN){
//                    lastToken();
//                    midExpr = analyzeAssignExpr();
//                }
//                else if(getToken().getTokenType() == TokenType.L_PAREN){
//                    lastToken();
//                    midExpr = analyzeCallExpr();
//                }
//                else{
//                    lastToken();
//                    midExpr = analyzeIdentExpr();
//                }
//                break;
//            case UINT_LITERAL:
//            case DOUBLE_LITERAL:
//            case STRING_LITERAL:
//            case CHAR_LITERAL:
//                midExpr = analyzeLiteralExpr();
//                break;
//            case L_PAREN:
//                midExpr = analyzeGroupExpr();
//                break;
//            default:
//                throw new SyntacticorError("Identify expr error.");
//        }
//
////        while(!isEND()
////                && (getToken().getTokenType()==TokenType.AS_KW
////                || getToken().getTokenType()==TokenType.PLUS
////                || getToken().getTokenType()==TokenType.MINUS
////                || getToken().getTokenType()==TokenType.MUL
////                || getToken().getTokenType()==TokenType.DIV
////                || getToken().getTokenType()==TokenType.EQ
////                || getToken().getTokenType()==TokenType.NEQ
////                || getToken().getTokenType()==TokenType.LT
////                || getToken().getTokenType()==TokenType.GT
////                || getToken().getTokenType()==TokenType.LE
////                || getToken().getTokenType()==TokenType.GE)){
////            if(getToken().getTokenType() == TokenType.AS_KW){
////                SyntaxTreeNode asExpr = new SyntaxTreeNode(SyntaxTreeNodeType.AS_EXPR);
////
////                asExpr.appendChild(midExpr);
////                asExpr.appendChild(new SyntaxTreeNode(getToken()));
////
////                nextToken();
////                if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
////                    throw new SyntacticorError("No ty in a as_expr.");
////                }
////                asExpr.appendChild(new SyntaxTreeNode(getToken()));
////                midExpr = asExpr;
////            }
////            else{
////                SyntaxTreeNode operatorExpr = new SyntaxTreeNode(SyntaxTreeNodeType.OPERATOR_EXPR);
////
////                operatorExpr.appendChild(midExpr);
////                operatorExpr.appendChild(new SyntaxTreeNode(getToken()));
////
////                nextToken();
////                operatorExpr.appendChild(analyzeExpr());
////                midExpr = operatorExpr;
////            }
////        }
//
////        expr.appendChild(midExpr);
////        System.out.println("expr exit token: " + getToken().getValue());
//        return midExpr;
//    }



//    private SyntaxTreeNode analyzeNegateExpr() throws SyntacticorError {
//        SyntaxTreeNode negateExpr = new SyntaxTreeNode(SyntaxTreeNodeType.NEGATE_EXPR);
//
//        if(isEND() || getToken().getTokenType()!=TokenType.MINUS){
//            throw new SyntacticorError("No '-' in a negate_expr.");
//        }
//        negateExpr.appendChild(new SyntaxTreeNode(getToken()));
//
//        nextToken();
//
//        negateExpr.appendChild(analyzeExpr());
//
//        return negateExpr;
//    }

    private SyntaxTreeNode analyzeIdentExpr() throws SyntacticorError {
        SyntaxTreeNode identExpr = new SyntaxTreeNode(SyntaxTreeNodeType.IDENT_EXPR);

        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a ident_expr.");
        }
        identExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return identExpr;
    }

    private SyntaxTreeNode analyzeAssignExpr() throws SyntacticorError {
        SyntaxTreeNode assignExpr = new SyntaxTreeNode(SyntaxTreeNodeType.ASSIGN_EXPR);

        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a assign_expr.");
        }
        assignExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.ASSIGN){
            throw new SyntacticorError("No '=' in a assign_expr.");
        }
        assignExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        assignExpr.appendChild(analyzeExpr());

        return assignExpr;
    }

    private SyntaxTreeNode analyzeCallExpr() throws SyntacticorError {
        SyntaxTreeNode callExpr = new SyntaxTreeNode(SyntaxTreeNodeType.CALL_EXPR);

        if(isEND() || getToken().getTokenType()!=TokenType.IDENT){
            throw new SyntacticorError("No IDENT in a call_expr.");
        }
        callExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND() || getToken().getTokenType()!=TokenType.L_PAREN){
            throw new SyntacticorError("No '(' in a call_expr.");
        }
        callExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        if(isEND()){
            throw new SyntacticorError("No ')' in a call_expr.");
        }
        else if(getToken().getTokenType() != TokenType.R_PAREN){
            callExpr.appendChild(analyzeCallParamList());
        }

        if(isEND() || getToken().getTokenType()!=TokenType.R_PAREN){
            throw new SyntacticorError("No ')' in a call_expr.");
        }
        callExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return callExpr;
    }

    private SyntaxTreeNode analyzeCallParamList() throws SyntacticorError {
        SyntaxTreeNode callParamList = new SyntaxTreeNode(SyntaxTreeNodeType.CALL_PARAM_LIST);

        callParamList.appendChild(analyzeExpr());
        while(!isEND() && getToken().getTokenType()==TokenType.COMMA){
            callParamList.appendChild(new SyntaxTreeNode(getToken()));
            nextToken();
            callParamList.appendChild(analyzeExpr());
        }

        return callParamList;
    }

    private SyntaxTreeNode analyzeLiteralExpr() throws SyntacticorError {
        SyntaxTreeNode literalExpr = new SyntaxTreeNode(SyntaxTreeNodeType.LITERAL_EXPR);

        if(isEND()
                || (getToken().getTokenType()!=TokenType.UINT_LITERAL
                && getToken().getTokenType()!=TokenType.DOUBLE_LITERAL
                && getToken().getTokenType()!=TokenType.STRING_LITERAL
                && getToken().getTokenType()!=TokenType.CHAR_LITERAL)){
            throw new SyntacticorError("Identify literal_expr error.");
        }
        literalExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        return literalExpr;
    }

    private SyntaxTreeNode analyzeGroupExpr() throws SyntacticorError {
        SyntaxTreeNode groupExpr = new SyntaxTreeNode(SyntaxTreeNodeType.GROUP_EXPR);

        if(isEND() && getToken().getTokenType()!=TokenType.L_PAREN){
            throw new SyntacticorError("No '(' in a group_expr.");
        }
        groupExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();
        groupExpr.appendChild(analyzeExpr());

        if(isEND() && getToken().getTokenType()!=TokenType.R_PAREN){
            throw new SyntacticorError("No ')' in a group_expr.");
        }
        groupExpr.appendChild(new SyntaxTreeNode(getToken()));

        nextToken();

        return groupExpr;
    }
}
