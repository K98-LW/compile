package Writer;

import Semanticlizer.CodeSaver;

import java.io.*;

public class Writer {
    private static String path = "E:\\compile\\src\\code.oO";
    private static Writer writer;

    private Writer() {}

    public static Writer getInstance(){
        if(writer == null) {
            writer = new Writer();
        }
        return writer;
    }

    public void write(CodeSaver codeSaver) throws IOException {
        File file = new File(path);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(codeSaver.toString());
        fileWriter.close();
    }
}
