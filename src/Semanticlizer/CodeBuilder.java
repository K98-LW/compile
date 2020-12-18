package Semanticlizer;

final class CodeBuilder {
    private CodeBuilder() {}

    final public static String nop = "00";
    final public static String push(int num){
        return "01" + String.format("%016x", num);
    }
    final public static String push(long num){
        return "01" + String.format("%016lx", num);
    }
    final public static String pop = "03";
    final public static String dup = "04";
    final public static String loca(int num){
        return "0a" + String.format("%08x", num);
    }
    final public static String arga(int num){
        return "0b" + String.format("%08x", num);
    }
    final public static String globa(int num){
        return "0c" + String.format("%08x", num);
    }
    final public static String load8 = "10";
    final public static String load16 = "11";
    final public static String load32 = "12";
    final public static String load64 = "13";
    final public static String store8 = "14";
    final public static String store16 = "15";
    final public static String store32 = "16";
    final public static String store64 = "17";
    final public static String alloc = "18";
    final public static String free = "19";
    final public static String stackalloc(int num){
        return "1a" + String.format("%08x", num);
    }
    final public static String addi = "20";
    final public static String subi = "21";
    final public static String muli = "22";
    final public static String divi = "23";
    final public static String addf = "24";
    final public static String subf = "25";
    final public static String mulf = "26";
    final public static String divf = "27";
    final public static String divu = "28";
    final public static String shl = "29";
    final public static String shr = "2a";
    final public static String and = "2b";
    final public static String or = "2c";
    final public static String xor = "2d";
    final public static String not = "2e";
    final public static String cmpi = "30";
    final public static String cmpu = "31";
    final public static String cmpf = "32";
    final public static String negi = "34";
    final public static String negf = "35";
    final public static String itof = "36";
    final public static String ftoi = "37";
    final public static String shrl = "38";
    final public static String setlt = "39";
    final public static String setgt = "3a";
    final public static String br(int num){
        if(num >= 0){
            return "41" + String.format("%08x", num);
        }
        else{
            int a = Integer.MAX_VALUE + num;
            a += 1;
            return "41" + String.format("1%07x", a);
        }
    }
    final private static String br(String string){
        return "41" + string;
    }
    final public static String brfalse(int num){
        return "42" + String.format("%08x", num);
    }
    final public static String brtrue(int num){
        return "43" + String.format("%08x", num);
    }
    final public static String call(int num){
        return "48" + String.format("%08x", num);
    }
    final public static String ret = "49";
    final public static String callname(int num){
        return "4a" + String.format("%08x", num);
    }
    final public static String scani = "50";
    final public static String scanc = "51";
    final public static String scanf = "52";
    final public static String printi = "54";
    final public static String printc = "55";
    final public static String printf = "56";
    final public static String prints = "57";
    final public static String println = "58";
    final public static String panic = "fe";
    final public static String breakFlag = "<break>";
    final public static String continueFlag = "<continue>";
    final public static String whileStartFlag = "<while start>";
    final public static String whileEndFlag = "<while end>";
}
