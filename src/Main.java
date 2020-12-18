import Semanticlizer.*;
import Semanticlizer.Semanticlizer;
import Syntacticor.Syntacticor;
import Syntacticor.SyntacticorError;
import Syntacticor.SyntaxTreeNode;
import Tokenlizer.Token;
import Tokenlizer.Tokenlizer;
import Tokenlizer.TokenlizerError;
import Writer.Writer;

import java.io.IOException;
import java.util.List;

//import Tokenlizer.Tokenlizer;

public class Main {
    public static void main(String[] args) throws SyntacticorError, SemanticError, IOException {
//        System.out.println(String.format("%01x", 30));
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

        Syntacticor syntacticor = Syntacticor.getInstance();
        syntacticor.init(tokenList);
        SyntaxTreeNode program = syntacticor.analyze();

        System.out.println("\nSyntacticor complete.\n");

        syntacticor.printTree(program, 0);

        Semanticlizer semanticlizer = Semanticlizer.getInstance();
        semanticlizer.init(program);

        CodeSaver codeSaver = semanticlizer.analyze();
        codeSaver.print();

        Writer writer = Writer.getInstance();
        writer.write(codeSaver);

        return;
    }
}
