package Semanticlizer;

import java.util.LinkedList;
import java.util.List;

public class CodeSaver {
    private StringBuilder code;
    private LinkedList<String> codeList;
    private int count;

    public CodeSaver(){
        this.code = new StringBuilder();
        this.codeList = new LinkedList();
        this.count = 0;
    }

    public String getCode(){
        StringBuilder code = new StringBuilder();
        for(String s : this.codeList){
            code.append(s);
        }
        return code.toString();
    }

    public CodeSaver append(String string){
        this.codeList.add(string);
        return this;
    }

    public CodeSaver append(int num, int bytes){
        this.codeList.add(String.format("%0" + (bytes*2) + "x", num));
        return this;
    }

    public CodeSaver append(CodeSaver codeSaver){
        for(String s : codeSaver.codeList){
            this.codeList.add(s);
        }
        return this;
    }

    public CodeSaver appendFront(String string){
        this.codeList.addFirst(string);
        return this;
    }

    public CodeSaver appendFront(int num, int bytes){
        this.codeList.addFirst(String.format("%0" + (bytes*2) + "x", num));
        return this;
    }

    public CodeSaver appendFront(CodeSaver codeSaver){
        List<String> temp = this.codeList;
        this.codeList = new LinkedList(codeSaver.codeList);
        for(String s : temp){
            this.codeList.add(s);
        }
        return this;
    }

    public int size(){
        return this.codeList.size();
    }

    public CodeSaver replaceLabel(){
        // break
        for(int i=0; i<this.codeList.size(); i++){
            if(this.codeList.get(i).equals(CodeBuilder.breakFlag)){
                for(int j=i+1; j<this.codeList.size(); j++){
                    if(this.codeList.get(j).equals(CodeBuilder.whileEndFlag)){
                        this.codeList.set(i, CodeBuilder.br(j-i));
                        break;
                    }
                }
            }
        }

        // continue
        for(int i=0; i<this.codeList.size(); i++){
            if(this.codeList.get(i).equals(CodeBuilder.continueFlag)){
                for(int j=i-1; j>=0; j--){
                    if(this.codeList.get(j).equals(CodeBuilder.whileStartFlag)){
                        this.codeList.set(i, CodeBuilder.br(j-i));
                        break;
                    }
                }
            }
        }

        // while
        for(int i=0; i<this.codeList.size(); i++){
            if(this.codeList.get(i).equals(CodeBuilder.whileStartFlag)){
                this.codeList.set(i, CodeBuilder.br(0));
            }
            else if(this.codeList.get(i).equals(CodeBuilder.whileEndFlag)){
                this.codeList.set(i, CodeBuilder.br(0));
            }
        }

        return this;
    }

    public void print(){
        for(String s : this.codeList){
            System.out.println(s);
        }
    }
}
