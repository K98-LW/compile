package Semanticlizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class SymbolTable{
    private LinkedList<SymbolTableItem> list;

    public SymbolTable(){
        this.list = new LinkedList<SymbolTableItem>();
    }

    public SymbolTable addSymbol(SymbolTableItem item) throws SemanticError {
        if(item == null){
            throw new SemanticError("Item is null.");
        }
        else if(item.type == SymbolType.VOID){
            throw new SemanticError("Item can not be VOID.");
        }
        for(SymbolTableItem s : this.list){
            if(s.name.equals(item.name) && s.depth==item.depth){
                throw new SemanticError("Error: add same name item in table.");
            }
        }
        this.list.add(item);
        return this;
    }

    public SymbolTableItem pop(){
        return this.list.pop();
    }

    public List<SymbolTableItem> pop(int depth){
        ArrayList<SymbolTableItem> popList = new ArrayList<SymbolTableItem>();
        while(!this.list.isEmpty()){
            SymbolTableItem item = this.list.pop();
            if(item.depth < depth){
                this.list.add(item);
                break;
            }
            popList.add(item);
        }
        return popList;
    }

    public boolean hasSameName(String name, int depth){
        int i = this.list.size();
        SymbolTableItem item;
        while(i>=0 && (item=this.list.get(i)).depth==depth){
            if(item.name.equals(name)){
                return true;
            }
        }
        if(i>=0 && (item=this.list.get(i)).type==SymbolType.FUNCTION){
            for(SymbolTableItem s : item.getParamList()){
                if(s.name.equals(name)){
                    return true;
                }
            }
        }
        return false;
    }
}

class SymbolTableItem{
    public String name;
    public SymbolType type;
    public int depth;
    public SymbolType returnType;
    private List<SymbolTableItem> paramList;

    public SymbolTableItem(String name, SymbolType type, int depth){
        this.name = name;
        this.type = type;
        this.depth = depth;
        if(this.type == SymbolType.FUNCTION){
            this.paramList = new ArrayList<SymbolTableItem>();
        }
    }

    public SymbolTableItem(String name, SymbolType type, int depth, SymbolType returnType){
        this(name, type, depth);
        this.returnType = returnType;
    }

    public SymbolTableItem appendParam(SymbolTableItem param) throws SemanticError {
        if(param == null){
            throw new SemanticError("Error: param is null.");
        }
        else if(param.type==SymbolType.VOID){
            throw new SemanticError("Error: param type can not be VOID.");
        }
        else if(param.type==SymbolType.FUNCTION){
            throw new SemanticError("Error: param type can not be FUNCTION.");
        }
        for(SymbolTableItem s : this.paramList){
            if(s.name.equals(param.name)){
                throw new SemanticError("Error: param name can not be same.");
            }
        }
        this.paramList.add(param);
        return this;
    }

    public List<SymbolTableItem> getParamList(){
        return this.paramList;
    }
}

enum SymbolType{
    VOID,
    INT,
    DOUBLE,
    FUNCTION,
}
