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
    public static void main(String[] args) throws SyntacticorError, SemanticError, IOException, TokenlizerError {
        Tokenlizer tokenlizer = Tokenlizer.getInstance();
        System.out.println("path: " + args[0]);
        tokenlizer.init(args[0]);

        List<Token> tokenList;
        tokenList = tokenlizer.analyze();

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
        writer.write(codeSaver, args[2]);

        return;
    }
}
