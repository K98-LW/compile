package Semanticlizer;

public class CodeSaver {
    private StringBuilder code;

    public CodeSaver(){
        this.code = new StringBuilder();
    }

    public String getCode(){
        return this.code.toString();
    }

    public CodeSaver append(String string){
        this.code.append(string);
        return this;
    }

    public CodeSaver appendFront(String string){
        this.code.insert(0, string);
        return this;
    }
}
