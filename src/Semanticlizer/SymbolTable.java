package Semanticlizer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

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

    public void pop(int depth){
        for(int i=this.list.size()-1; i>=0; i--){
            if(this.list.get(i).depth < depth){
                break;
            }
            this.list.pollLast();
        }
}

    public boolean hasSameName(String name, int depth){
        int i = this.list.size() - 1;
        SymbolTableItem item;
        while(i>=0 && (item=this.list.get(i)).depth==depth){
            if(item.name.equals(name)){
                return true;
            }
            i--;
        }
//        if(i>=0 && (item=this.list.get(i)).type==SymbolType.FUNCTION){
//            for(SymbolTableItem s : item.getParamList()){
//                if(s.name.equals(name)){
//                    return true;
//                }
//            }
//        }
        return false;
    }

    public SymbolTableItem find(String name){
        ListIterator<SymbolTableItem> it = this.list.listIterator(this.list.size());
        while(it.hasPrevious()){
            SymbolTableItem symbolTableItem = it.previous();
            if(symbolTableItem.name.equals(name)){
                return symbolTableItem;
            }
        }
        return null;
    }

    public SymbolTableItem get(int index){
        return this.list.get(index);
    }

    public int size(){
        return this.list.size();
    }

    public void print(){
        System.out.println("==============================");
        for(SymbolTableItem sti : this.list){
            System.out.println("("+sti.type+")\t"+sti.name+"\t:"+sti.depth);
        }
        System.out.println("==============================");
    }
}

class SymbolTableItem{
    public String name;
    public SymbolType type;
    public int depth;
    public SymbolType returnType;
    private List<SymbolType> paramList;
    public boolean isConst = false;
    public int location;
    public boolean isParam = false;

    public SymbolTableItem(String name, SymbolType type, int depth){
        this.name = name;
        this.type = type;
        this.depth = depth;
        if(this.type == SymbolType.FUNCTION){
            this.paramList = new ArrayList<SymbolType>();
        }
    }

    public SymbolTableItem(String name, SymbolType type, int depth, SymbolType returnType){
        this(name, type, depth);
        this.returnType = returnType;
    }

    public SymbolTableItem setIsConst(){
        this.isConst = true;
        return this;
    }

    public SymbolTableItem setIsParam(){
        this.isParam = true;
        return this;
    }

    public SymbolTableItem appendParam(SymbolType symbolType) throws SemanticError {
        if(symbolType == null){
            throw new SemanticError("Error: param is null.");
        }
        else if(symbolType == SymbolType.VOID){
            throw new SemanticError("Error: param type can not be VOID.");
        }
        else if(symbolType == SymbolType.FUNCTION){
            throw new SemanticError("Error: param type can not be FUNCTION.");
        }
//        for(SymbolTableItem s : this.paramList){
//            if(s.name.equals(param.name)){
//                throw new SemanticError("Error: param name can not be same.");
//            }
//        }
        this.paramList.add(symbolType);
        return this;
    }

    public List<SymbolType> getParamList(){
        return this.paramList;
    }
}

enum SymbolType{
    VOID,
    INT,
    DOUBLE,
    FUNCTION,
}
