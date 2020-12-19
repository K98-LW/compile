package Semanticlizer;

final class CodeBuilder {
    private CodeBuilder() {}

    final public static String nop = "00";
    final public static String push(int num){
        return "push(" + num + ")";
    }
    final public static String push(long num){
        return "push(" + num + ")";
    }
    final public static String pop = "pop";
    final public static String dup = "dup";
    final public static String loca(int num){
        return "loca(" + num + ")";
    }
    final public static String arga(int num){
        return "arga(" + num + ")";
    }
    final public static String globa(int num){
        return "globa(" + num + ")";
    }
    final public static String load8 = "load8";
    final public static String load16 = "11";
    final public static String load32 = "12";
    final public static String load64 = "load64";
    final public static String store8 = "14";
    final public static String store16 = "15";
    final public static String store32 = "16";
    final public static String store64 = "store64";
    final public static String alloc = "18";
    final public static String free = "19";
    final public static String stackalloc(int num){
        return "stackalloc(" + num + ")";
    }
    final public static String addi = "addi";
    final public static String subi = "subi";
    final public static String muli = "muli";
    final public static String divi = "divi";
    final public static String addf = "addf";
    final public static String subf = "subf";
    final public static String mulf = "mulf";
    final public static String divf = "divf";
    final public static String divu = "28";
    final public static String shl = "29";
    final public static String shr = "2a";
    final public static String and = "2b";
    final public static String or = "2c";
    final public static String xor = "2d";
    final public static String not = "not";
    final public static String cmpi = "cmpi";
    final public static String cmpu = "31";
    final public static String cmpf = "cmpf";
    final public static String negi = "negi";
    final public static String negf = "negf";
    final public static String itof = "itof";
    final public static String ftoi = "ftoi";
    final public static String shrl = "38";
    final public static String setlt = "setlt";
    final public static String setgt = "setgt";
    final public static String br(int num){
        return "br(" + num + ")";
    }
    final private static String br(String string){
        return "41" + string;
    }
    final public static String brfalse(int num){
        return "42" + String.format("%08x", num);
    }
    final public static String brtrue(int num){
        return "brtrue(" + num + ")";
    }
    final public static String call(int num){
        return "call(" + num + ")";
    }
    final public static String ret = "ret";
    final public static String callname(int num){
        return "4a" + String.format("%08x", num);
    }
    final public static String scani = "scani";
    final public static String scanc = "scanc";
    final public static String scanf = "scanf";
    final public static String printi = "printi";
    final public static String printc = "printc";
    final public static String printf = "printf";
    final public static String prints = "prints";
    final public static String println = "println";
    final public static String panic = "fe";
    final public static String breakFlag = "<break>";
    final public static String continueFlag = "<continue>";
    final public static String whileStartFlag = "<while start>";
    final public static String whileEndFlag = "<while end>";
}
