package Semanticlizer;

import Syntacticor.SyntaxTreeNode;
import Syntacticor.SyntaxTreeNodeType;
import Tokenlizer.TokenType;

import java.util.LinkedList;
import java.util.List;

public class Semanticlizer {
    private static Semanticlizer semanticlizer;
    private SyntaxTreeNode root;
    private SymbolTable localSymbolTable;
    private SymbolTable globalSymbolTable;
    private CodeSaver codeSaver;

    private Semanticlizer(){
        this.localSymbolTable = new SymbolTable();
        this.globalSymbolTable = new SymbolTable();
        this.codeSaver = new CodeSaver();
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
    }

    public void analyze() throws SemanticError {
        for(SyntaxTreeNode stn : this.root.getChildList()){
            if(stn.getType() == SyntaxTreeNodeType.FUNCTION){
                analyzeFunction(stn);
            }
            else if(stn.getType() == SyntaxTreeNodeType.DECL_STMT){
                analyzeDeclStmt(stn);
            }
            else{
                throw new SemanticError("Analyze <program> error.");
            }
        }
    }

    private CodeSaver analyzeFunction(SyntaxTreeNode stn) throws SemanticError {
        CodeSaver functionCode = new CodeSaver();

        // 局部变量数量的统计
        Integer localVariableCount = new Integer(0);

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

        functionCode.append(analyzeBlockStmt(stn.getChildList().get(stn.getChildList().size()-1)), localVariableCount);

        functionCode.appendFront(functionCode.size(), 4)
                .appendFront(localVariableCount, 4)
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

    private CodeSaver analyzeDeclStmt(SyntaxTreeNode stn, int depth) throws SemanticError {
        return analyzeDeclStmt(stn, depth, 0);
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

        // 定位该变量
        if(depth == 0){
            symbolTableItem.location = this.globalSymbolTable.size();
        }
        else{
            symbolTableItem.location = location;
        }

        declStmtCode.append(analyzeExpr(stn, depth));
    }
}
