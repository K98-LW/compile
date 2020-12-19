package Writer;

import Semanticlizer.CodeSaver;

import java.io.*;

public class Writer {
//    private static String path = "E:\\compile\\src\\code.o0";
    private static Writer writer;

    private Writer() {}

    public static Writer getInstance(){
        if(writer == null) {
            writer = new Writer();
        }
        return writer;
    }

    public void write(CodeSaver codeSaver, String path) throws IOException {
        File file = new File(path);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        String code = codeSaver.toString();
        for(int i=0; i<code.length(); i+=2){
            System.out.println(Integer.valueOf(code.substring(i, i+2), 16));
            fileOutputStream.write(Integer.valueOf(code.substring(i, i+2), 16));
        }
        fileOutputStream.close();
    }
}
