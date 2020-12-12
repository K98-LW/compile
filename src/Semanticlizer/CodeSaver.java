package Semanticlizer;

public class CodeSaver {
    private StringBuilder code;
    private int count;

    public CodeSaver(){
        this.code = new StringBuilder();
        this.count = 0;
    }

    public String getCode(){
        return this.code.toString();
    }

    public CodeSaver append(String string){
        this.code.append(string);
        this.count++;
        return this;
    }

    public CodeSaver append(int num, int bytes){
        this.code.append(String.format("%0" + (bytes*2) + "x", num));
        this.count++;
        return this;
    }

    public CodeSaver append(CodeSaver codeSaver){
        this.code.append(codeSaver.code);
        return this;
    }

    public CodeSaver appendFront(String string){
        this.code.insert(0, string);
        this.count++;
        return this;
    }

    public CodeSaver appendFront(int num, int bytes){
        this.code.insert(0, String.format("%0" + (bytes*2) + "x", num));
        this.count++;
        return this;
    }

    public CodeSaver appendFront(CodeSaver codeSaver){
        this.code.insert(0, codeSaver.code);
        return this;
    }

    public int size(){
        return this.count;
    }
}
