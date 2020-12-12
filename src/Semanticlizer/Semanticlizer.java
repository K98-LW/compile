package Semanticlizer;

import Syntacticor.SyntaxTreeNode;
import Syntacticor.SyntaxTreeNodeType;

public class Semanticlizer {
    private static Semanticlizer semanticlizer;
    private SyntaxTreeNode root;
    private SymbolTable symbolTable;
    private CodeSaver codeSaver;

    private Semanticlizer(){
        this.symbolTable = new SymbolTable();
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
        this.symbolTable = new SymbolTable();
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

    private void analyzeFunction(SyntaxTreeNode stn){

    }

    private void analyzeDeclStmt(SyntaxTreeNode stn){

    }
}
