package processingUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Data_box {

    // データを保持するためのクラス.
    private static class Variable {

        int mem_location; // 変数のビットサイズ.
        long value;       // 変数に格納される値.

        public Variable(int mem_location, long value) {
            this.mem_location = mem_location; //アドレスを格納.
            this.value = value; //アドレスに対応した値を格納.
        }
    }
    
    private boolean selectA = false;
    private int Anum;
    
    public boolean selectM = true;
    public boolean selectF = true;
    public boolean selectD = true;
    
    public boolean print_int = false;
    public boolean print_string = false;
    public boolean print_char = false;
    public boolean print_hex = false;
    
    public int MEM[] = new int[0xffff]; //メモリ配列.
    public int x[] = new int[32]; //レジスタを格納する配列.
    public long fx[] = new long[32]; //レジスタを格納する配列.
    public ControlAndStatusRegister CSR;
    
    

    public int data_count = 0; //.data内での行数のカウント用の変数.
    public int text_count = 0; //.text内での行数のカウント用の変数.
    public int tentative_count = 0; //

    public ArrayList<String> err = new ArrayList<>(); //エラーメッセージを格納.
    public ArrayList<String> BIS = new ArrayList<>(); //Base Instructionを格納.
    public ArrayList<String> OIS = new ArrayList<>(); //Original Instructionを格納.
    
    
    
    private Map<String, Variable> data_map; //データマップを設定. 
    public Map<Integer, String> label_map; //ラベルマップを設定
    
    
    
    public void start_Data_box() { //コンストラクタみたいなメソッド. レジスタの初期化とマップの生成.
        x[2] = 0x7fff; // spを設定
        
        data_map = new HashMap<>(); //マップの生成.
        label_map = new HashMap<>();
        
        CSR = new ControlAndStatusRegister();
    }

    // data_mapに変数名, ビット幅のタイプ, 整数値を設定するメソッド.
    public void addData(String word, String type, int imm, long dimm, boolean overlap) {

        //ここでマップにデータを入力.
    	//重複したらマップにデータを登録しない
        //0x9000+なのはdata域が0x9000から0xAFFFまでにしているから.
    	if(!overlap) {
    		data_map.put(word, new Variable(0x9000 + data_count, imm)); 

    	}
        
        
        if (type.matches(".word")) { //.wordの時は4btye確保する
            MEM[0x9000 + data_count] = (imm & 0xFF000000) >>> 24;
            MEM[0x9001 + data_count] = (imm & 0x00FF0000) >>> 16;
            MEM[0x9002 + data_count] = (imm & 0x0000FF00) >>> 8;;
            MEM[0x9003 + data_count] = imm & 0x000000ff;

            data_count += 4;

        } else if (type.matches(".half")) { //.halfの時は2byte確保する
            MEM[0x9000 + data_count] = (imm & 0xff00) >>> 8;
            MEM[0x9001 + data_count] = imm & 0x000000ff;

            data_count += 2;

        } else if (type.matches(".byte")) { //byteでは1byte確保する
            MEM[0x9000 + data_count] = imm;

            data_count += 1;

        } else if (type.matches(".space")) { //spaceでは1byte確保する
        	for(int i=0; i<imm; i++) {
                data_count += 1;
                
        	}

        } else if (type.matches(".double")) { //spaceでは1byte確保する
        	MEM[0x9000 + data_count] = (int)(dimm >>> 56) & 0xFF;
        	MEM[0x9001 + data_count] = (int)(dimm >>> 48) & 0xFF;
        	MEM[0x9002 + data_count] = (int)(dimm >>> 40) & 0xFF;
        	MEM[0x9003 + data_count] = (int)(dimm >>> 32) & 0xFF;
        	MEM[0x9004 + data_count] = (int)(dimm >>> 24) & 0xFF;
        	MEM[0x9005 + data_count] = (int)(dimm >>> 16) & 0xFF;
        	MEM[0x9006 + data_count] = (int)(dimm >>> 8) & 0xFF;
        	MEM[0x9007 + data_count] = (int)dimm & 0xFF;
        
            data_count += 8;

        } else if (type.matches(".float")) { //spaceでは1byte確保する
        	MEM[0x9000 + data_count] = imm >> 24;
    		MEM[0x9001 + data_count] = (imm >> 16) & 0x00ff;
        	MEM[0x9002 + data_count] = (imm >> 8) & 0x0000ff;
        	MEM[0x9003 + data_count] = imm & 0x000000ff;
        
            data_count += 4;

        } else if (type.matches(".zero")) { //zeroでは1byte確保する
        	for(int i=0; i<imm; i++) {
                data_count += 1;
                
        	}

        } else {
            // System.out.println("Data_box: wordが対象外のタイプです" + word);
            System.exit(0); // 終了
        }
        
        if(selectA) {
        	if(Anum != 0) {
        		while(data_count % Anum != 0) {
            		data_count++;
            	}
        	}
        	
        }
    }
    
    // data_mapに変数名, ビット幅のタイプ, 整数値を設定するメソッド.
    public void addStringData(String word, String type, String string) {

        // マップにデータを入力
        data_map.put(word, new Variable(0x9000 + data_count, 0));

        // 先頭と末尾のダブルクォートを削除
        String newString = string.replaceAll("^\"|\"$", "");

        try {
            // Shift_JISエンコードされたバイト列を取得
            byte[] encodedBytes = newString.getBytes("Shift_JIS");

            // 終端が0で区切られた文字列をメモリに格納
            if (type.matches(".asciiz") || type.matches(".string")) {
                for (byte b : encodedBytes) {
                    // バイトデータを符号なしで格納
                    MEM[0x9000 + data_count] = b & 0xFF;
                    System.out.printf("MEM[0x%04X] = %d%n", 0x9000 + data_count, MEM[0x9000 + data_count]);
                    data_count++; // インクリメントは必ず次の位置のために行う
                }

                // 終端文字を追加（通常0）
                MEM[0x9000 + data_count] = 0;
                System.out.printf("MEM[0x%04X] = %d%n", 0x9000 + data_count, MEM[0x9000 + data_count]);
                data_count++;
            }
        } catch (Exception e) {
            System.err.println("エンコードエラー: " + e.getMessage());
        }
        
        if(selectA) {
        	if(Anum != 0) {
        		while(data_count % Anum != 0) {
            		data_count++;
            	}
        	}
        	
        }
    }

    
    // aling用のメソッド.
    public void setAling(boolean type, int num){
    	selectA = type;
    	Anum =  (int) Math.pow(2, num);
    	System.out.print("setAlingでは"+Anum);
    }



    //ラベル
    public void addLabel(String word) {
        data_map.put(Converter.convertString(word), new Variable(tentative_count, 0));
        label_map.put(tentative_count, word);
        printMap();
    }

    public void MEMput(int all) {
        MEM[text_count] = (all & 0xff000000) >>> 24;
        MEM[text_count + 1] = (all & 0x00ff0000) >>> 16;
        MEM[text_count + 2] = (all & 0x0000ff00) >>> 8;
        MEM[text_count + 3] = all & 0x000000ff;

        text_count += 4;
    }

    public int getData(String word) {

        //data_mapにwordに格納された文字列が登録されていたら, その文字列に対応したアドレスを返す.
        //登録されていなかったらnullを返す.
        Variable var = data_map.get(word);

        if (var != null) { //varがnullじゃなかったらアドレスを返す
            return var.mem_location;

        } else { //nullなら-1を返す
            return -1;

        }
    }

    public void printMap() {
        for (Map.Entry<String, Variable> entry : data_map.entrySet()) {
            String name = entry.getKey();
            Variable var = entry.getValue();
            System.out.println("Variable name: " + name + ", mem_location = " + var.mem_location + ", value = " + var.value);
        }
    }
    
    public void resetDataBox() {
        // レジスタの初期化
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
        }
        x[2] = 0x7fff; // spを初期設定
        
        for (int i = 0; i < fx.length; i++) {
            fx[i] = 0;
        }

        // メモリの初期化
        for (int i = 0; i < MEM.length; i++) {
            MEM[i] = 0;
        }

        // カウント変数の初期化
        data_count = 0;
        text_count = 0;
        tentative_count = 0;

        // エラーメッセージリストのクリア
        err.clear();

        // Base Instruction と Original Instruction のリストをクリア
        BIS.clear();
        OIS.clear();

        // データマップのクリア
        if (data_map != null) {
            data_map.clear();
        } else {
            data_map = new HashMap<>(); // マップがnullなら新たに生成
        }
        
        print_int = false;
        print_string = false;
        print_char = false;
        print_hex = false;

        // モードの初期化
        /*
        selectM = false;
        selectF = false;
        selectD = false;
        */

        // System.out.println("Data_box has been reset to initial state.");
    }
    
    public void resetFlag() {  
    	for (int i = 0; i < x.length; i++) {
            x[i] = 0;
        }
        x[2] = 0x7fff; // spを初期設定
        
        for (int i = 0; i < fx.length; i++) {
            fx[i] = 0;
        }
        
        // 各フラグの処理を初期化
        print_int = false;
        print_string = false;
        print_char = false;
        print_hex = false;
        
        // エラーメッセージのクリア
        err.clear();

        // モードの初期化
        /*
        selectM = false;
        selectF = false;
        selectD = false;
        */

        // System.out.println("Data_box has been reset to initial state.");
    }

}
