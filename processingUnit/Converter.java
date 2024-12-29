package processingUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converter {

    private static final Map<String, String> xToAliasMap = new HashMap<>();
    private static final Map<String, Integer> aliasToXMap = new HashMap<>();

    static {
        // xレジスタからエイリアスへの変換マップを初期化
        xToAliasMap.put("x0", "zero");
        xToAliasMap.put("x1", "ra");
        xToAliasMap.put("x2", "sp");

        // tレジスタ（テンポラリレジスタ）
        xToAliasMap.put("x5", "t0");
        xToAliasMap.put("x6", "t1");
        xToAliasMap.put("x7", "t2");
        xToAliasMap.put("x28", "t3");
        xToAliasMap.put("x29", "t4");
        xToAliasMap.put("x30", "t5");
        xToAliasMap.put("x31", "t6");

        // aレジスタ（引数/戻り値レジスタ）
        xToAliasMap.put("x10", "a0");
        xToAliasMap.put("x11", "a1");
        xToAliasMap.put("x12", "a2");
        xToAliasMap.put("x13", "a3");
        xToAliasMap.put("x14", "a4");
        xToAliasMap.put("x15", "a5");
        xToAliasMap.put("x16", "a6");
        xToAliasMap.put("x17", "a7");

        // sレジスタ（保存レジスタ）
        xToAliasMap.put("x8", "s0");
        xToAliasMap.put("x9", "s1");
        xToAliasMap.put("x18", "s2");
        xToAliasMap.put("x19", "s3");
        xToAliasMap.put("x20", "s4");
        xToAliasMap.put("x21", "s5");
        xToAliasMap.put("x22", "s6");
        xToAliasMap.put("x23", "s7");
        xToAliasMap.put("x24", "s8");
        xToAliasMap.put("x25", "s9");
        xToAliasMap.put("x26", "s10");
        xToAliasMap.put("x27", "s11");
        
        // tレジスタ（テンポラリレジスタ）
        xToAliasMap.put("f0", "ft0");
        xToAliasMap.put("f1", "ft1");
        xToAliasMap.put("f2", "ft2");
        xToAliasMap.put("f3", "ft3");
        xToAliasMap.put("f4", "ft4");
        xToAliasMap.put("f5", "ft5");
        xToAliasMap.put("f6", "ft6");
        xToAliasMap.put("f7", "ft7");

        // sレジスタ（保存レジスタ）
        xToAliasMap.put("f8", "fs0");
        xToAliasMap.put("f9", "fs1");
        
        // aレジスタ（引数・戻り値レジスタ）
        xToAliasMap.put("f10", "fa0");
        xToAliasMap.put("f11", "fa1");
        xToAliasMap.put("f12", "fa2");
        xToAliasMap.put("f13", "fa3");
        xToAliasMap.put("f14", "fa4");
        xToAliasMap.put("f15", "fa5");
        xToAliasMap.put("f16", "fa6");
        xToAliasMap.put("f17", "fa7");

        // sレジスタ（保存レジスタ）
        xToAliasMap.put("f18", "fs2");
        xToAliasMap.put("f19", "fs3");
        xToAliasMap.put("f20", "fs4");
        xToAliasMap.put("f21", "fs5");
        xToAliasMap.put("f22", "fs6");
        xToAliasMap.put("f23", "fs7");
        xToAliasMap.put("f24", "fs8");
        xToAliasMap.put("f25", "fs9");
        xToAliasMap.put("f26", "fs10");
        xToAliasMap.put("f27", "fs11");

        // 再度テンポラリレジスタ
        xToAliasMap.put("f28", "ft8");
        xToAliasMap.put("f29", "ft9");
        xToAliasMap.put("f30", "ft10");
        xToAliasMap.put("f31", "ft11");


        // 逆変換マップを初期化（エイリアスから整数へのマップ）
        for (Map.Entry<String, String> entry : xToAliasMap.entrySet()) {
            String key = entry.getKey();
            String alias = entry.getValue();
            // 正規表現で数値部分を抽出
            String numberPart = key.replaceAll("[^0-9]", "");
            int registerNumber = Integer.parseInt(numberPart); // 数字部分を整数に変換
            aliasToXMap.put(alias, registerNumber);
        }

    }

    // xレジスタからエイリアスに変換
    public static String convertToAlias(String xRegister) {
        return xToAliasMap.getOrDefault(xRegister, xRegister);
    }

    // エイリアスからxレジスタの整数値に変換
    public static int convertToX(String alias) {
        if (aliasToXMap.containsKey(alias)) {
            return aliasToXMap.get(alias);
            
        } else if (alias.startsWith("x")) {
            // エイリアスではなく x レジスタが直接渡された場合
            return Integer.parseInt(alias.substring(1));
            
        } else if (alias.startsWith("f")) {
            // エイリアスではなく f レジスタが直接渡された場合
            return Integer.parseInt(alias.substring(1));
        } else {
            throw new IllegalArgumentException("無効なエイリアスまたはレジスタ名: " + alias);
        }
    }

    public static String convertString(String input) {
        // 正規表現パターンの作成
        Pattern pattern = Pattern.compile("([a-zA-Z0-9_]+)\\s*:");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            // 正規表現にマッチした部分から変換後の文字列を返す
            return matcher.group(1);
        }
        // 正規表現にマッチしない場合は元の文字列を返す
        return input;
    }
    
    public static int numRegtonum(String input) {
        // 正規表現パターンを定義
        Pattern pattern = Pattern.compile("(\\d+)\\([a-zA-Z][a-zA-Z0-9]*\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 数字を変数に格納
            int number = Integer.parseInt(matcher.group(1));
            return number;
        } else {
            return -1;
        }
    }
    
    public static String numRegtoReg(String input) {
        // 正規表現パターンを定義
        Pattern pattern = Pattern.compile("\\d+\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // レジスター名を変数に格納
            String register = matcher.group(1);
            return register;
        } else {
            return null;
        }
    }
    
    public static boolean numRegFormat(String input) {
        // 正規表現パターンを定義
        Pattern pattern = Pattern.compile("^\\d+\\([a-zA-Z][a-zA-Z0-9]*\\)$");
        Matcher matcher = pattern.matcher(input);

        // パターンに一致するかどうかを返す
        return matcher.matches();
    }
    
    // long の下位32ビットを float に変換
    public static float longToFloat(long value) {
        int lower32Bits = (int) (value & 0xFFFFFFFFL); // 下位32ビットを取り出す
        return Float.intBitsToFloat(lower32Bits);     // ビットを float に解釈
    }

    // float を long の下位32ビットに変換
    public static long floatToLong(float value) {
        int floatBits = Float.floatToIntBits(value); // float をビット表現 (int) に変換
        return ((long) floatBits) & 0xFFFFFFFFL;     // 下位32ビットとして long に変換
    }
    
 // long を double に変換
    public static double longToDouble(long value) {
        return Double.longBitsToDouble(value); // long のビットを double に解釈
    }

    // double を long に変換
    public static long doubleToLong(double value) {
        return Double.doubleToLongBits(value); // double をビット表現 (long) に変換
    }

    
    public static void main(String[] args) {
        // テストする文字列
        String[] testStrings = {"a:", "b:", "hello world :", "foo : bar", "example: text"};


        // 各文字列を変換
        for (String str : testStrings) {
            String result = convertString(str);
            System.out.println("Original: " + str);
            System.out.println("Converted: " + result);
            System.out.println();
        }
    }
}
