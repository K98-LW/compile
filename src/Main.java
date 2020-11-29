import Tokenlizer.Token;
import Tokenlizer.Tokenlizer;
import Tokenlizer.TokenlizerError;

import java.util.List;

//import Tokenlizer.Tokenlizer;

public class Main {
    public static void main(String[] args) {
        Tokenlizer tokenlizer = Tokenlizer.getInstance();
        try{
            tokenlizer.init();
        } catch (Exception e){
            System.out.println("Init Tokenlizer error.");
        }

        List<Token> tokenList;
        try {
            tokenList = tokenlizer.analyze();
        } catch (TokenlizerError tokenlizerError) {
            return;
        }

        for(Token t : tokenList){
            System.out.println("Type: " + t.getTokenType() + "\t\tValue: " + t.getValue());
        }

        return;
    }
}
