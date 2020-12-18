package Semanticlizer;

import Syntacticor.SyntaxTreeNode;
import Syntacticor.SyntaxTreeNodeType;
import Tokenlizer.TokenType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Semanticlizer {
    private static Semanticlizer semanticlizer;
    private SyntaxTreeNode root;
    private SymbolTable localSymbolTable;
    private SymbolTable globalSymbolTable;
    private CodeSaver finalCode;
    private final static String[] stdFunctionName = {
            "getint",
            "getdouble",
            "getchar",
            "putint",
            "putdouble",
            "putchar",
            "putstr",
            "putln"
    };

    private Semanticlizer(){
        this.localSymbolTable = new SymbolTable();
        this.globalSymbolTable = new SymbolTable();
        this.finalCode = new CodeSaver();
    }

    public static Semanticlizer getInstance(){
        if(semanticlizer == null){
            semanticlizer = new Semanticlizer();
        }
        return semanticlizer;
    }

    public void init(SyntaxTreeNode root){
        this.root = root;
        this.localSymbolTable = new SymbolTable();
        this.globalSymbolTable = new SymbolTable();
        this.finalCode = new CodeSaver();
    }

    public CodeSaver analyze() throws SemanticError {
        // _start
        SymbolTableItem _start = new SymbolTableItem(
                "_start",
                SymbolType.FUNCTION,
                0
        );
        this.globalSymbolTable.addSymbol(_start);

        CodeSaver _startCode = new CodeSaver();

        for(SyntaxTreeNode stn : this.root.getChildList()){
            if(stn.getType() == SyntaxTreeNodeType.FUNCTION){
                this.finalCode.append(analyzeFunction(stn));
            }
            else if(stn.getType() == SyntaxTreeNodeType.DECL_STMT){
                _startCode.append(analyzeGlobalDeclStmt(stn));
            }
            else{
                throw new SemanticError("Analyze <program> error.");
            }
        }

        // build _start function
        // find main
        int main_loc = -1;
        for(int i=0; i<this.globalSymbolTable.size(); i++){
            if(this.globalSymbolTable.get(i).name.equals("main")){
                main_loc = i;
                break;
            }
        }
        if(main_loc == -1){
            throw new SemanticError("Can not find function main.");
        }
        // call main function
        _startCode.append(CodeBuilder.call(main_loc));
        // append to finalCode
        this.finalCode.appendFront(_startCode);
        this.finalCode.appendFront(_startCode.size(), 4);
        this.finalCode.appendFront(0, 4);
        this.finalCode.appendFront(0, 4);
        this.finalCode.appendFront(0, 4);
        this.finalCode.appendFront(0, 4);

        // 统计函数的数量
        int count = 0;
        for(int i=0; i<this.globalSymbolTable.size(); i++){
            if(this.globalSymbolTable.get(i).type == SymbolType.FUNCTION){
                count++;
            }
        }
        this.finalCode.appendFront(count, 4);

        // 生成全局变量的数据
        CodeSaver global = new CodeSaver();
        for(int i=0; i<this.globalSymbolTable.size(); i++){
            SymbolTableItem sti = this.globalSymbolTable.get(i);

            // const
            int _const = sti.isConst ? 1 : 0;
            global.append(_const, 1);

            // count and value
            if(sti.type == SymbolType.INT || sti.type == SymbolType.DOUBLE){
                global.append(8, 4);
                global.append(0, 8);
            }
            else if(sti.type == SymbolType.FUNCTION){
                global.append(sti.name.length(), 4);
                StringBuilder value = new StringBuilder();
                for(int j=0; j<sti.name.length(); j++){
                    value.append(String.format("%02d", sti.name.charAt(j)));
                }
                global.append(value.toString());
            }
            else{
                throw new SemanticError("Global error.");
            }
        }
        this.finalCode.appendFront(global);

        // global.count
        this.finalCode.appendFront(this.globalSymbolTable.size(), 4);

        // version
        this.finalCode.appendFront("00000001");

        // magic
        this.finalCode.appendFront("72303b3e");

        return this.finalCode;
    }

    private CodeSaver analyzeFunction(SyntaxTreeNode stn) throws SemanticError {
        CodeSaver functionCode = new CodeSaver();

        // 局部变量数量的统计
        Integer localVariableCount = new Integer(1);

        // 函数名
        String functionName = stn.getChildList().get(1).getToken().getValue();

        // 返回值
        SymbolType returnType;
        if(stn.getChildList().get(5).getToken().getTokenType() == TokenType.IDENT){
            returnType = analyzeTy(stn.getChildList().get(5).getToken().getValue());
        }
        else{
            returnType = analyzeTy(stn.getChildList().get(6).getToken().getValue());
        }

        int paramsCount = 0;
        SyntaxTreeNode params = stn.getChildList().get(3);
        LinkedList<SymbolType> types = new LinkedList<SymbolType>();
        if(params.getType() == SyntaxTreeNodeType.FUNCTION_PARAM_LIST){
            List<SyntaxTreeNode> paramsList = params.getChildList();
            boolean constFlag = false;
            for(int i=0; i<paramsList.size(); i++){
                SyntaxTreeNode p = paramsList.get(i);
                // 变量名的查重
                if(p.getToken().getTokenType() == TokenType.IDENT
                        && this.localSymbolTable.hasSameName(p.getToken().getValue(), 1)){
                    throw new SemanticError("Duplicate param " + p.getToken().getValue());
                }
                // 常量定义标志
                else if(p.getToken().getTokenType() == TokenType.CONST_KW){
                    constFlag = true;
                }
                // 参数名加入
                else if(p.getToken().getTokenType() == TokenType.IDENT){
                    paramsCount++;
                    i += 2;
                    SymbolTableItem symbolTableItem = new SymbolTableItem(
                                                            p.getToken().getValue(),
                                                            analyzeTy(paramsList.get(i).getToken().getValue()),
                                                            1);
                    types.add(analyzeTy(paramsList.get(i).getToken().getValue()));
                    if(constFlag){
                        symbolTableItem.setIsConst();
                    }
                    symbolTableItem.location = paramsCount - 1;
                    symbolTableItem.setIsParam();
                    this.localSymbolTable.addSymbol(symbolTableItem);
                }
                constFlag = false;
            }
        }

        // 函数名存放点
        SymbolTableItem functionSymbol = new SymbolTableItem(
                functionName,
                SymbolType.FUNCTION,
                0,
                returnType);
        functionSymbol.location = this.globalSymbolTable.size();
        functionSymbol.setIsConst();

        // 记录参数类型，里面会判断参数类型
        for(SymbolType t : types){
            functionSymbol.appendParam(t);
        }

        // 函数名查重
        if(this.globalSymbolTable.hasSameName(functionName, 0)){
            throw new SemanticError("Duplicate function: " + functionName);
        }

        // 添加到全局变量表
        this.globalSymbolTable.addSymbol(functionSymbol);

        // 顺带返回局部变量个数
        StmtParams sp = new StmtParams();
        sp.stn = stn.getChildList().get(stn.getChildList().size()-1);
        sp.localVariableCount = localVariableCount;
        sp.depth = 1;
        sp.returnType = returnType;
        functionCode.append(analyzeBlockStmt(sp));

        functionCode.appendFront(functionCode.size(), 4)
                .appendFront(localVariableCount-1, 4)
                .appendFront(paramsCount, 4)
                .appendFront(returnType==SymbolType.VOID ? 0 : 1, 4)
                .appendFront(functionSymbol.location, 4);
        return functionCode;
}

    private SymbolType analyzeTy(String ident) throws SemanticError {
        switch(ident){
            case "int":
                return SymbolType.INT;
            case "double":
                return SymbolType.DOUBLE;
            case "void":
                return SymbolType.VOID;
            default:
                throw new SemanticError("Unknown type: " + ident);
        }
    }

    private CodeSaver analyzeGlobalDeclStmt(SyntaxTreeNode stn) throws SemanticError {
        return analyzeDeclStmt(stn, 0, 0);
    }

    private CodeSaver analyzeDeclStmt(SyntaxTreeNode stn, int depth, int location) throws SemanticError {
        CodeSaver declStmtCode = new CodeSaver();
        SymbolTable symbolTable = depth == 0 ? this.globalSymbolTable : this.localSymbolTable;
        SyntaxTreeNode declStmt = stn.getChildList().get(0);
        boolean constFlag = declStmt.getType() == SyntaxTreeNodeType.CONST_DECL_STMT;

        String varName = declStmt.getChildList().get(1).getToken().getValue();
        SymbolType varType = analyzeTy(declStmt.getChildList().get(3).getToken().getValue());
        
        // 变量名查重
        if(symbolTable.hasSameName(varName, depth)){
            throw new SemanticError("Duplicate variable " + varName);
        }

        SymbolTableItem symbolTableItem = new SymbolTableItem(varName, varType, depth);
        if(constFlag){
            symbolTableItem.setIsConst();
        }

        // 变量有初始化
        if(declStmt.getChildList().size() == 7){
            StmtParams sp = new StmtParams();
            sp.stn = declStmt.getChildList().get(5);
            sp.depth = depth;
            declStmtCode.append(analyzeExpr(sp));

            // 添加赋值代码
            if(depth == 0){
                declStmtCode.appendFront(CodeBuilder.globa(this.globalSymbolTable.size()));
            }
            else{
                declStmtCode.appendFront(CodeBuilder.loca(location));
            }
        }

        // 定位该变量，
        if(depth == 0){
            // 全局变量直接顺延即可
            symbolTableItem.location = this.globalSymbolTable.size();
        }
        else{
            // 局部变量取决于调用者
            symbolTableItem.location = location;
        }
        declStmtCode.append(CodeBuilder.store64);

        // 加入符号表
        symbolTable.addSymbol(symbolTableItem);

        return declStmtCode;
    }

    private class StmtParams{
        public SyntaxTreeNode stn = null;
        public Integer localVariableCount = 0;
//        public int localVariableStart;
        public int depth = 0;
        public SymbolType returnType = null;
        public boolean isInWhile = false;

        public StmtParams(){}

        public StmtParams(StmtParams sp){
            this.stn = sp.stn;
            this.localVariableCount = sp.localVariableCount;
            this.depth = sp.depth;
            this.returnType = sp.returnType;
            this.isInWhile = sp.isInWhile;
        }
    }

    private CodeSaver analyzeBlockStmt(StmtParams params) throws SemanticError {
        CodeSaver blockCode = new CodeSaver();
        for(int i=0; i<params.stn.getChildList().size(); i++){
            StmtParams sp = new StmtParams(params);
            sp.stn = params.stn.getChildList().get(i);
            blockCode.append(analyzeStmt(sp));
        }
        this.localSymbolTable.pop(params.depth);
        return blockCode;
    }

    private CodeSaver analyzeStmt(StmtParams params) throws SemanticError {
        StmtParams sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(0);
        switch(params.stn.getChildList().get(0).getType()){
            case EXPR_STMT:
                sp.stn = sp.stn.getChildList().get(0);
                return analyzeExpr(sp);
            case DECL_STMT:
                return analyzeDeclStmt(sp.stn, sp.depth, sp.localVariableCount++);
            case IF_STMT:
                return analyzeIfStmt(sp);
            case WHILE_STMT:
                return analyzeWhileStmt(sp);
            case BREAK_STMT:
                return analyzeBreakStmt(sp);
            case CONTINUE_STMT:
                return analyzeContinueStmt(sp);
            case RETURN_STMT:
                return analyzeReturnStmt(sp);
            case BLOCK_STMT:
                sp.depth += 1;
                return analyzeBlockStmt(sp);
            case EMPTY_STMT:
                return new CodeSaver();
            default:
                throw new SemanticError("Analyze stmt error.");
        }
    }

    private CodeSaver analyzeIfStmt(StmtParams params) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();
        ArrayList<CodeSaver> compareStmt = new ArrayList<CodeSaver>();
        ArrayList<CodeSaver> content = new ArrayList<CodeSaver>();

        // 解析出执行代码和比较代码
        int i = 0;
        while(params.stn.getChildList().get(i).getToken().getTokenType() == TokenType.IF_KW ||
                (params.stn.getChildList().get(i).getToken().getTokenType() == TokenType.ELSE_KW
                        && params.stn.getChildList().get(i).getToken().getTokenType() == TokenType.IF_KW)){
            if(params.stn.getChildList().get(i).getToken().getTokenType() == TokenType.IF_KW ){
                StmtParams sp = new StmtParams(params);
                sp.stn = params.stn.getChildList().get(i+1);
                compareStmt.add(analyzeExpr(sp));
                sp.stn = params.stn.getChildList().get(i+2);
                sp.depth += 1;
                content.add(analyzeBlockStmt(sp));
                i += 3;
            }
            else{
                StmtParams sp = new StmtParams(params);
                sp.stn = params.stn.getChildList().get(i+2);
                compareStmt.add(analyzeExpr(sp));
                sp.stn = params.stn.getChildList().get(i+3);
                sp.depth += 1;
                content.add(analyzeBlockStmt(sp));
                i += 4;
            }
        }

        // 分析 else 的代码
        CodeSaver elseCode = new CodeSaver();
        if(params.stn.getChildList().size() > i){
            StmtParams sp = new StmtParams(params);
            sp.stn = params.stn.getChildList().get(i+1);
            sp.depth += 1;
            elseCode = analyzeBlockStmt(sp);
        }

        // 拼接各个代码
        for(i=0; i<compareStmt.size(); i++){
            codeSaver.append(compareStmt.get(i));
            codeSaver.append(CodeBuilder.brtrue(1));
            codeSaver.append(CodeBuilder.br(content.get(i).size() + 1));
            codeSaver.append(content.get(i));

            int count = 0;
            for(int j=i+1; j<compareStmt.size(); j++){
                count += compareStmt.get(j).size();
                count += 3;
                count += content.get(j).size();
            }
            count += elseCode.size();
            codeSaver.append(CodeBuilder.br(count));
        }
        codeSaver.append(elseCode);

        return codeSaver;
    }

    private CodeSaver analyzeWhileStmt(StmtParams params) throws SemanticError {
        CodeSaver compare, content, codeSaver = new CodeSaver();
        StmtParams sp = new StmtParams(params);
        sp.isInWhile = true;

        // compare
        sp.stn = params.stn.getChildList().get(1);
        compare = analyzeExpr(sp);

        // content
        sp.stn = params.stn.getChildList().get(2);
        sp.depth += 1;
        content = analyzeBlockStmt(sp);

        // 拼接各个代码
        codeSaver.append(CodeBuilder.br(0));
        codeSaver.append(compare);
        codeSaver.append(CodeBuilder.brtrue(1));
        codeSaver.append(CodeBuilder.br(content.size() + 1));
        codeSaver.append(content);
        codeSaver.append(CodeBuilder.br(-(compare.size() + 2 + content.size())));

        codeSaver.appendFront(CodeBuilder.whileStartFlag);
        codeSaver.append(CodeBuilder.whileEndFlag);

        return codeSaver;
    }

    private CodeSaver analyzeBreakStmt(StmtParams params) throws SemanticError {
        if(!params.isInWhile){
            throw new SemanticError("Break must in a while.");
        }
        CodeSaver codeSaver = new CodeSaver();
        codeSaver.append(CodeBuilder.breakFlag);
        return codeSaver;
    }

    private CodeSaver analyzeContinueStmt(StmtParams params) throws SemanticError {
        if(!params.isInWhile){
            throw new SemanticError("Continue must in a while.");
        }
        CodeSaver codeSaver = new CodeSaver();
        codeSaver.append(CodeBuilder.continueFlag);
        return codeSaver;
    }

    private CodeSaver analyzeReturnStmt(StmtParams params) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();

        if(params.returnType == SymbolType.VOID){
            if(params.stn.getChildList().size() != 2){
                throw new SemanticError("Return void error.");
            }
        }
        else if(params.returnType == SymbolType.INT){
            codeSaver.append(CodeBuilder.arga(0));
            StmtParams sp = new StmtParams(params);
            ExprType exprType = new ExprType();
            sp.stn = params.stn.getChildList().get(1);
            if(sp.stn.getType() != SyntaxTreeNodeType.EXPR){
                throw new SemanticError("Need to return a int.");
            }
            codeSaver.append(analyzeExpr(sp, exprType));
            if(exprType.symbolType != SymbolType.INT){
                throw new SemanticError("Return value is not a int.");
            }
            codeSaver.append(CodeBuilder.store64);
        }
        else if(params.returnType == SymbolType.DOUBLE){
            codeSaver.append(CodeBuilder.arga(0));
            StmtParams sp = new StmtParams(params);
            ExprType exprType = new ExprType();
            sp.stn = params.stn.getChildList().get(1);
            if(sp.stn.getType() != SyntaxTreeNodeType.EXPR){
                throw new SemanticError("Need to return a double.");
            }
            codeSaver.append(analyzeExpr(sp, exprType));
            if(exprType.symbolType != SymbolType.DOUBLE){
                throw new SemanticError("Return value is not a double.");
            }
            codeSaver.append(CodeBuilder.store64);
        }
        else{
            throw new SemanticError("Unknown return type.");
        }

        codeSaver.append(CodeBuilder.ret);
        return codeSaver;
    }

    private class ExprType{
        public SymbolType symbolType;
    }

    private CodeSaver analyzeExpr(StmtParams params) throws SemanticError {
        return analyzeExpr(params, new ExprType());
    }

    private CodeSaver analyzeExpr(StmtParams params, ExprType exprType) throws SemanticError {
        StmtParams sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(0);
        switch(params.stn.getChildList().get(0).getType()){
            case OPERATOR_EXPR:
                return analyzeOperatorExpr(sp, exprType);
            case NEGATE_EXPR:
                return analyzeNegateExpr(sp, exprType);
            case ASSIGN_EXPR:
                return analyzeAssignExpr(sp);
            case AS_EXPR:
                return analyzeAsExpr(sp, exprType);
            case CALL_EXPR:
                return analyzeCallExpr(sp, exprType);
            case LITERAL_EXPR:
                return analyzeLiteralExpr(sp, exprType);
            case IDENT_EXPR:
                return analyzeIdentExpr(sp, exprType);
            case GROUP_EXPR:
                return analyzeGroupExpr(sp, exprType);
            default:
                throw new SemanticError("Analyze expr error.");
        }
    }

    private CodeSaver analyzeOperatorExpr(StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();
        ExprType exprType1 = new ExprType(), exprType2 = new ExprType();

        StmtParams sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(0);
        codeSaver.append(analyzeExpr(sp, exprType1));

        sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(2);
        codeSaver.append(analyzeExpr(sp, exprType2));

        if(exprType1.symbolType != exprType2.symbolType){
            throw new SemanticError("ET1 != ET2.");
        }
        exprType.symbolType = exprType1.symbolType;

        switch(params.stn.getChildList().get(1).getToken().getTokenType()){
            case PLUS:
                if(exprType.symbolType == SymbolType.INT){
                    codeSaver.append(CodeBuilder.addi);
                }
                else{
                    codeSaver.append(CodeBuilder.addf);
                }
                break;
            case MINUS:
                if(exprType.symbolType == SymbolType.INT){
                    codeSaver.append(CodeBuilder.subi);
                }
                else{
                    codeSaver.append(CodeBuilder.subf);
                }
                break;
            case MUL:
                if(exprType.symbolType == SymbolType.INT){
                    codeSaver.append(CodeBuilder.muli);
                }
                else{
                    codeSaver.append(CodeBuilder.mulf);
                }
                break;
            case DIV:
                if(exprType.symbolType == SymbolType.INT){
                    codeSaver.append(CodeBuilder.divi);
                }
                else{
                    codeSaver.append(CodeBuilder.divf);
                }
                break;
            case EQ:
            case NEQ:
            case LT:
            case GT:
            case LE:
            case GE:
                if(exprType.symbolType == SymbolType.INT){
                    codeSaver.append(CodeBuilder.cmpi);
                }
                else{
                    codeSaver.append(CodeBuilder.cmpf);
                }
                break;
            default:
                throw new SemanticError("Oparator error.");
        }

        switch(params.stn.getChildList().get(1).getToken().getTokenType()){
            case EQ:
                codeSaver.append(CodeBuilder.not);
                break;
            case LT:
                codeSaver.append(CodeBuilder.setlt);
                break;
            case GT:
                codeSaver.append(CodeBuilder.setgt);
                break;
            case LE:
                codeSaver.append(CodeBuilder.setgt);
                codeSaver.append(CodeBuilder.not);
                break;
            case GE:
                codeSaver.append(CodeBuilder.setlt);
                codeSaver.append(CodeBuilder.not);
                break;
        }

        return codeSaver;
    }

    private CodeSaver analyzeNegateExpr(StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();

        StmtParams sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(1);
        codeSaver.append(analyzeExpr(sp, exprType));

        if(exprType.symbolType == SymbolType.INT){
            codeSaver.append(CodeBuilder.negi);
        }
        else{
            codeSaver.append(CodeBuilder.negf);
        }

        return codeSaver;
    }

    private CodeSaver analyzeAssignExpr(StmtParams params) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();
        String varName = params.stn.getChildList().get(0).getToken().getValue();
        SyntaxTreeNode expr = params.stn.getChildList().get(2);

        // 加载变量地址
        SymbolTableItem var;
        var = this.localSymbolTable.find(varName);
        if(var != null){
            if(var.isParam){
                codeSaver.append(CodeBuilder.arga(var.location));
            }
            else{
                codeSaver.append(CodeBuilder.loca(var.location));
            }
        }
        else{
            var = this.globalSymbolTable.find(varName);
            if(var != null){
                codeSaver.append(CodeBuilder.globa(var.location));
            }
            else{
                throw new SemanticError(varName + " is not defined before.");
            }
        }

        ExprType exprType1 = new ExprType(), exprType2 = new ExprType();
        exprType1.symbolType = var.type;

        // 处理表达式
        StmtParams sp = new StmtParams(params);
        sp.stn = expr;
        codeSaver.append(analyzeExpr(sp, exprType2));

        // 验证类型是否是相等的
        if(exprType1.symbolType != exprType2.symbolType){
            throw new SemanticError("Assign wrong type.");
        }

        // 验证是否是常数
        if(var.isConst){
            throw new SemanticError("Can not assign to a const.");
        }

        codeSaver.append(CodeBuilder.store64);

        return codeSaver;
    }

    private CodeSaver analyzeAsExpr(StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();

        StmtParams sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(0);
        codeSaver.append(analyzeExpr(sp, exprType));

        SymbolType ty = analyzeTy(params.stn.getChildList().get(2).getToken().getValue());

        // 拦截非法的 ty
        if(ty == SymbolType.VOID){
            throw new SemanticError("Can not as a void.");
        }
        if(ty == SymbolType.FUNCTION){
            throw new SemanticError("Can not as a function.");
        }

        // 转类型
        if(exprType.symbolType==SymbolType.DOUBLE && ty==SymbolType.INT){
            codeSaver.append(CodeBuilder.ftoi);
            exprType.symbolType = SymbolType.INT;
        }
        else if(exprType.symbolType==SymbolType.INT && ty==SymbolType.DOUBLE){
            codeSaver.append(CodeBuilder.itof);
            exprType.symbolType = SymbolType.DOUBLE;
        }
        else if(exprType.symbolType != ty){
            throw new SemanticError("Illigal as expr.");
        }

        return codeSaver;
    }

    private CodeSaver analyzeCallExpr(StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();

        // 寻找对应函数
        String functionName = params.stn.getChildList().get(0).getToken().getValue();
        SymbolTableItem function = this.globalSymbolTable.find(functionName);
        if(function == null){
            for(int i=0; i<stdFunctionName.length; i++){
                if(stdFunctionName[i].equals(functionName)){
                    return callStdFunction(functionName, params, exprType);
                }
            }
            throw new SemanticError("Function " + functionName + " is not defined.");
        }
        if(function.type != SymbolType.FUNCTION){
            throw new SemanticError(functionName + " is not a function.");
        }

        // 处理返回值
        if(function.returnType==SymbolType.INT || function.returnType==SymbolType.DOUBLE){
            codeSaver.append(CodeBuilder.stackalloc(1));
        }
        else{
            codeSaver.append(CodeBuilder.stackalloc(0));
        }
        exprType.symbolType = function.returnType;

        // 处理函数的参数
        // 把调用的参数取出来
        ArrayList<SyntaxTreeNode> paramsList = new ArrayList<>();
        if(params.stn.getChildList().get(2).getType() == SyntaxTreeNodeType.CALL_PARAM_LIST){
            List<SyntaxTreeNode> cpl = params.stn.getChildList().get(2).getChildList();
            for(int i=0; i<cpl.size(); i+=2){
                paramsList.add(cpl.get(i));
            }
        }
        // 获得标准的参数类型
        List<SymbolType> typeList = function.getParamList();
        // 判断参数数量
        if(paramsList.size() != typeList.size()){
            throw new SemanticError("The number of params in call function " + functionName + " is wrong.");
        }
        // 依次比较参数的类型并生成对应的代码
        for(int i=0; i<paramsList.size(); i++){
            ExprType et = new ExprType();
            StmtParams sp = new StmtParams(params);
            sp.stn = paramsList.get(i);
            codeSaver.append(analyzeExpr(sp, et));
            if(et.symbolType != typeList.get(i)){
                throw new SemanticError("Param " + (i+1) + " should be " + typeList.get(i) + " but not " + et.symbolType + ".");
            }
        }

        codeSaver.append(CodeBuilder.call(function.location));

        return codeSaver;
    }

    private CodeSaver callStdFunction(String functionName, StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();

        // 获取参数类型
        List<SymbolType> typeList = new LinkedList<SymbolType>();
        switch(functionName){
            case "putint":
                typeList.add(SymbolType.INT);
                break;
            case "putdouble":
                typeList.add(SymbolType.DOUBLE);
                break;
            case "putchar":
                typeList.add(SymbolType.INT);
                break;
            case "putstr":
                typeList.add(SymbolType.INT);
                break;
        }

        // 验证参数类型
        // 把调用的参数取出来
        ArrayList<SyntaxTreeNode> paramsList = new ArrayList<>();
        if(params.stn.getChildList().get(2).getType() == SyntaxTreeNodeType.CALL_PARAM_LIST){
            List<SyntaxTreeNode> cpl = params.stn.getChildList().get(2).getChildList();
            for(int i=0; i<cpl.size(); i+=2){
                paramsList.add(cpl.get(i));
            }
        }
        // 判断参数数量
        if(paramsList.size() != typeList.size()){
            throw new SemanticError("The number of params in call function " + functionName + " is wrong.");
        }
        // 依次比较参数的类型并生成对应的代码
        for(int i=0; i<paramsList.size(); i++){
            ExprType et = new ExprType();
            StmtParams sp = new StmtParams(params);
            sp.stn = paramsList.get(i);
            codeSaver.append(analyzeExpr(sp, et));
            if(et.symbolType != typeList.get(i)){
                throw new SemanticError("Param " + (i+1) + " should be " + typeList.get(i) + " but not " + et.symbolType + ".");
            }
        }

        // 生成函数调用代码
        switch(functionName){
            case "getint":
                codeSaver.append(CodeBuilder.scani);
                exprType.symbolType = SymbolType.INT;
                break;
            case "getdouble":
                codeSaver.append(CodeBuilder.scanf);
                exprType.symbolType = SymbolType.DOUBLE;
                break;
            case "getchar":
                codeSaver.append(CodeBuilder.scanc);
                exprType.symbolType = SymbolType.INT;
                break;
            case "putint":
                codeSaver.append(CodeBuilder.printi);
                exprType.symbolType = SymbolType.VOID;
                break;
            case "putdouble":
                codeSaver.append(CodeBuilder.printf);
                exprType.symbolType = SymbolType.VOID;
                break;
            case "putchar":
                codeSaver.append(CodeBuilder.printc);
                exprType.symbolType = SymbolType.VOID;
                break;
            case "putstr":
                codeSaver.append(CodeBuilder.prints);
                exprType.symbolType = SymbolType.VOID;
                break;
            case "putln":
                codeSaver.append(CodeBuilder.println);
                exprType.symbolType = SymbolType.VOID;
                break;
            default:
                throw new SemanticError("Error use std.");
        }

        return codeSaver;
    }

    private CodeSaver analyzeLiteralExpr(StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();
        switch(params.stn.getChildList().get(0).getToken().getTokenType()){
            case UINT_LITERAL:
                exprType.symbolType = SymbolType.INT;
                codeSaver.append(CodeBuilder.push(new Integer(params.stn.getChildList().get(0).getToken().getValue())));
                break;
            case DOUBLE_LITERAL:
                exprType.symbolType = SymbolType.DOUBLE;
                codeSaver.append(CodeBuilder.push(transDouble(new Double(params.stn.getChildList().get(0).getToken().getValue()))));
                break;
            case STRING_LITERAL:
                System.out.println("What shold I do? ");
                exprType.symbolType = SymbolType.INT;
                break;
            case CHAR_LITERAL:
                exprType.symbolType = SymbolType.INT;
                codeSaver.append(CodeBuilder.push((int)(new Character(params.stn.getChildList().get(0).getToken().getValue().charAt(0)))));
                break;
            default:
                throw new SemanticError("Error use literal.");
        }
        return codeSaver;
    }

    private long transDouble(double num){
        return Double.doubleToLongBits(num);
    }

    private CodeSaver analyzeIdentExpr(StmtParams params, ExprType exprType) throws SemanticError {
        CodeSaver codeSaver = new CodeSaver();
        SymbolTableItem var;
        String varName = params.stn.getToken().getValue();
        var = this.localSymbolTable.find(varName);
        if(var != null){
            if(var.isParam){
                codeSaver.append(CodeBuilder.arga(var.location));
            }
            else{
                codeSaver.append(CodeBuilder.loca(var.location));
            }
        }
        else{
            var = this.globalSymbolTable.find(varName);
            if(var != null){
                codeSaver.append(CodeBuilder.globa(var.location));
            }
            else{
                throw new SemanticError(varName + " is not defined before.");
            }
        }
        codeSaver.append(CodeBuilder.load64);
        exprType.symbolType = var.type;
        return codeSaver;
    }

    private CodeSaver analyzeGroupExpr(StmtParams params, ExprType exprType) throws SemanticError {
        StmtParams sp = new StmtParams(params);
        sp.stn = params.stn.getChildList().get(1);
        return analyzeExpr(params, exprType);
    }
}
