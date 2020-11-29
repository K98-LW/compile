package Syntacticor;

import Tokenlizer.Token;

import java.util.List;

public class Syntacticor {
    private static Syntacticor syntacticor;
    private List<Token> tokenList;
    private int location;

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

    public void init(List list){
        this.tokenList = list;
        this.location = 0;
    }
}
