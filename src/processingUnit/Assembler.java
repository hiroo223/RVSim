package processingUnit;

public class Assembler {

	public Data_box Assemble (String text,Data_box data)  {
	
		
	
		String delim = " ,\t\r\n()"; //単語の区切り記号（ここではスペース，タブ，改行とした）.
    
		data.start_Data_box(); //コンストラクタみたいなメソッドの実行.
    

		Context ct = new Context(text, delim); //Contextオブジェクトの作成.
		
		
		Prog p = new Prog(); //Progのオブジェクト作成.
		p.parse(ct,data); //構文解析.
		
		if(data.err.isEmpty() != true) {
			return data;
		}
		
		p.exe(data); //実行.
		
		//data.printMap();			
		
		/*
    
		// System.out.println("Displaying range 0x0 to 0x30:");
		for (int i = 0x0; i <= 0x64; i += 4) {
			// System.out.printf("MEM[0x%04X ~ 0x%04X] : %02X%02X%02X%02X\n",
					i, i + 3, data.MEM[i], data.MEM[i + 1], data.MEM[i + 2], data.MEM[i + 3]);
		}

		// 0x9000 から 0x9003 までの範囲を表示.
		// System.out.println("\nDisplaying range 0x9000 to 0x9003:");
		for (int i = 0x9000; i <= 0x9003; i += 4) {
			// System.out.printf("MEM[0x%04X ~ 0x%04X] : %02X%02X%02X%02X\n",
					i, i + 3, data.MEM[i], data.MEM[i + 1], data.MEM[i + 2], data.MEM[i + 3]);
		}
		*/
		
		return data;
    
    
	}
	
	public void setStatus(Data_box data, boolean M, boolean F, boolean D) {
		if(M == true) data.selectM = true;
		else data.selectM = false;
		
		if(F == true) data.selectF = true;
		else data.selectF = false;
		
		if(D == true) data.selectD = true;
		else data.selectD = false;
		
	}
}

/* 
今回下記に示す生成規則に則って構文解析を行う.

(1)  Prog         => [.section]?[Data_section | Text_section | Bss_section] Prog? [EOF]
(2)  Data_section => [a-zA-Z]+ [.ward | .byte | .half | .string | .double | .space] [0-9]+ Data_section?
(3)  Bss_sectiong => [a-zA-Z]+ [.space | .zero] [0-9]+ Bss_section?
(3)  Text_section => [.globl label]? Main_section
(3)  Main_section => label? [R_type I_type_0 I_type_1 S_type B_type U_type J_type] Comment? Main_section? 
(4)  R_type       => [or and xor add sub srl sra sll slt sltu] Reg Reg Reg
(5)  I_type_0     => [ori andi xori addi subi slti sltiu ] Reg Reg Imm
(6)  I_type_1     => [jalr lb lh lw lbu lhu] Reg Imm [(] Reg [)] 
(7)  S_type       => [sb sh aw] Reg Imm [(] Reg [)]
(8)  B_type       => [beq bne blt bge bltu bgeu] Reg Reg [a-zA-Z]+
(9)  U_type       => [lui auipc] Reg [0-9]+ | [a-zA-Z]+
(10) J_type       => [jal]  Reg Imm
(11) G_type       => [li mv nop j jalr call ret la bgt bge bgtu bgeu] 
(11) Reg          => x0 | ra | sp| t[0-6] | a[0-6] | s([0-9] | 1[0-1])
(12) Imm          => [0-9]+ | [a-zA-Z]+
(13) Comment      => #[a-zA-Z0-9ぁ-ん]+
(14) label        => ([a-zA-Z]+)\\s*: 

コメントアウトは(13)に用意しているが利用することはなく, 基本的にスキップする予定
I typeはReg Reg ImmとReg Imm [(] Reg [)] の形で二種類用意している.
疑似命令G typeとして実装.



*/

//(1)  Prog => [Data_box_section Text_section Bss_section] Prog? [EOF]
class Prog{
	
	String op;
	
	private Data_section d_sec = null;
	private Bss_section b_sec = null;
	private Main_section m_sec = null;
	private Prog prog = null;
	
	public void parse(Context ct, Data_box data)   {
		
		if(ct.match(".section")) ct.toNext();
		
		if (ct.match(".data")) {  // .dataがあるか調べる.
            ct.toNext(); //次に進める.
			
            d_sec = new Data_section(); // Data_sectionに進める.
            d_sec.parse(ct,data);
            
            data.setAling(false, 0);
            
        } else if (ct.match(".text")) { // .textがあるか調べる.
            ct.toNext(); //次に進める.

            m_sec = new Main_section(); // Main_sectionに進める.
            m_sec.parse(ct,data);
          
        } else if (ct.match(".bss")) { // .bssがあるか調べる.
            ct.toNext(); //次に進める.

            b_sec = new Bss_section(); // Bss_sectionに進める.
            b_sec.parse(ct,data);
            
            data.setAling(false, 0);
          
        } else { //それ以外の時はエラー.
        	data.err.add("(" + ct.currentLine() + "行目): error C1 : 認識されていないディレクティブが宣言されています。 : "+ ct.currentToken());
        	ct.toNextLine();
        	
        }
		
		// System.out.printf("いまのトークンは%s\n",ct.currentToken());
		
		 //各セクションの処理が終わったあとに次のセクションがあるか調べる.
		if(ct.match(".data|.text|.section")) {
			data.setAling(false,0); //リセット
				
			prog = new Prog(); //あったらインスタンスを生成して実行.
			prog.parse(ct,data);
		}

	}
	
	public void exe(Data_box data) {
		if(m_sec != null) { //Mainがあったら実行.
			m_sec.exe(data);
		}
		
		if(prog != null) { //次のセクションがあれば実行.
			prog.exe(data);	
		}
	}
}



//(2)  Data_section => [a-zA-Z]+ [.ward | .byte | .half | .space | .string | .asciiz | .double | .float | .align] [0-9]+ Data_section?
class Data_section{
	
	String word; //変数の名前を格納.
	String lestWord;
	String type; //ビット幅のタイプを格納.
	String string;
	Imm imm; //数字を格納.
	int fimm;
	long dimm;
	
	Data_section d_sec;
	
	public void parse(Context ct,Data_box data)   {
		
		// .alingの場合の処理
		if(ct.match(".aling")){
			ct.toNext();
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ; //格納する数字をintに直して格納.
			data.setAling(true,imm.imm);
			
		// それ以外の処理
		} else {
			lestWord = ct.currentToken(); //変数の名前を保存.
			word = Converter.convertString(lestWord); //wordのみにする. ex) n: => n
			
			//n => nの場合.
			if(word.equals(lestWord)) {
				data.err.add("(" + ct.currentLine() + "行目): error C7 : ラベルに : がついていません。 : "+ ct.currentToken());
				ct.toNextLine();
			
			} else {
				ct.toNext(); //次に進める
				
				type = ct.currentToken(); //タイプを保存.
				
				if(type.matches(".word|.half|.byte|.space|.string|.asciiz|.float|.double") != true) {
					data.err.add("(" + ct.currentLine() + "行目): error C2 : .dataセクション内で認識されていない変数の大きさが宣言されています。 : "+ ct.currentToken());
		        	ct.toNextLine();
					
				} else {
					ct.toNext(); //次に進める.
					
					//"～～～"で表される文字列かどうかの判定.
					if (ct.currentToken().startsWith("\"") && ct.currentToken().endsWith("\"")) {
			            string = ct.currentToken();
			            ct.toNext();
			            
			        } else { //stirng以外のディレクティブの判定.
			    		
			        	imm = new Imm();
			        	if(imm.parse(ct, data) != true) return ; //格納する数字をintに直して格納.
			        }
						
					if(type.matches(".word|.half|.byte") && imm != null) { //タイプがこれらなら実行.
						data.addData(word, type, imm.imm, 0, false);
						
					} else if(type.matches(".float") && imm != null){
						fimm = imm.fimm;
						data.addData(word, type, fimm, 0, false);
						
					} else if(type.matches(".double") && imm != null){
						dimm = imm.dimm;
						data.addData(word, type, 0, dimm, false);
						
					}  else if(type.matches(".space") && imm != null){
						data.addData(word, type, imm.imm, 0, false);
						
					} else if(type.matches(".string") && imm == null){
						data.addStringData(word, type, string);
						
					} else if(type.matches(".asciiz") && imm == null){
						data.addStringData(word, type, string);
						
					}
				}
				
				while(ct.match("[+-]?[0-9]+(.[0-9]+)?")) {
					Imm immSeries = new Imm();
					immSeries.parse(ct, data);
					
					if(type.matches(".word|.half|.byte")) { //タイプがこれらなら実行.
						data.addData(word, type, immSeries.imm, 0, true);
						
					} else if(type.matches(".float")){
						fimm = immSeries.fimm;
						data.addData(word, type, fimm, 0, true);
						
					} else if(type.matches(".double") && imm != null){
						
						dimm = immSeries.dimm;
						data.addData(word, type, 0, dimm, true);
						
					}  else if(type.matches(".space") || type.matches(".string") || type.matches(".asciiz")){
						data.err.add("(" + ct.currentLine() + "行目): error C8 : 整数型以外のディレクティブで配列として宣言されています。 : "+ ct.currentToken());
						ct.toNextLine();
						break;
						
					} 
				}
				
				while(ct.match(".word|.half|.byte|.float|.double")) {
					type = ct.currentToken(); //タイプを保存.
					ct.toNext();
					System.out.println(" カレント:"+ct.currentToken());
					
					Imm remoteImm = new Imm();
		        	if(remoteImm.parse(ct, data) != true) return ; //格納する数字をintに直して格納.
		        	
		        	if(type.matches(".word|.half|.byte")) { //タイプがこれらなら実行.
						data.addData(word, type, remoteImm.imm, 0, true);
						System.out.println("ここにこれた"+remoteImm.imm);
						
					} else if(type.matches(".float")){
						fimm = remoteImm.fimm;
						data.addData(word, type, fimm, 0, true);
						
					} else if(type.matches(".double") && imm != null){
						
						dimm = remoteImm.dimm;
						data.addData(word, type, 0, dimm, true);
						
					}
					
				}
			}
		}
		
		
		// System.out.printf("いまのトークンは%s\n\n",ct.currentToken());
		
		if (ct.match("([a-zA-Z0-9_]+)\\s*:")) { //n+1個目の変数があれば再帰呼び出し.
			d_sec = new Data_section();
			d_sec.parse(ct, data);
		}
		
	}
	
	//exe()は存在しない. なぜなら, 変数の保存することがこのセクションでの役割のためparseした時点で役割を果たしている.
}

// (3)  Bss_sectiong => [a-zA-Z]+ [.space | .zero] [0-9]+ Bss_section?
class Bss_section{
	
	String word; //変数の名前を格納.
	String lestWord;
	String type; //ビット幅のタイプを格納.
	String string;
	Imm imm; //数字を格納.
	
	Data_section d_sec;
	
	public void parse(Context ct,Data_box data)   {

		lestWord = ct.currentToken(); //変数の名前を保存.
		word = Converter.convertString(lestWord); //wordのみにする. ex) n: => n
		
		if(word.matches(lestWord)) {
			data.err.add("(" + ct.currentLine() + "行目): error C7 : ラベルに : がついていません。 : "+ ct.currentToken());
			ct.toNextLine();
		
		} else {
			ct.toNext(); //次に進める
			
			type = ct.currentToken(); //タイプを保存.
			
			// .space、.zeroじゃなければ
			if(type.matches(".space|.zero") != true) {
				data.err.add("(" + ct.currentLine() + "行目): error C2 : .bssセクション内で認識されていない変数の大きさが宣言されています。 : "+ ct.currentToken());
	        	ct.toNextLine();
				
			} else {
				ct.toNext(); //次に進める.
				
				//"～～～"で表される文字列かどうかの判定.
				if (ct.currentToken().startsWith("\"") && ct.currentToken().endsWith("\"")) {
		            string = ct.currentToken();
		            ct.toNext();
		            
		        } else { //stirng以外のディレクティブの判定.
		    		
		        	imm = new Imm();
		        	if(imm.parse(ct, data) != true) return ; //格納する数字をintに直して格納.
		        }

					
				if(type.matches(".space") && imm != null){
					data.addData(word, type, imm.imm, 0, false);
					
				} else if(type.matches(".zero") && imm == null){
					data.addData(word, type, imm.imm, 0, false);
					
				}
			}
		}
		
		
		// System.out.printf("いまのトークンは%s\n\n",ct.currentToken());
		
		if (ct.match("([a-zA-Z0-9_]+)\\s*:")) { //n+1個目の変数があれば再帰呼び出し.
			d_sec = new Data_section();
			d_sec.parse(ct, data);
		}
		
	}
	
	//exe()は存在しない. なぜなら, 変数の保存することがこのセクションでの役割のためparseした時点で役割を果たしている.
}



//(3)  Main_section => Label? [R_type I_type S_type B_type U_type J_type] Main_section?
class Main_section{
	
	//それぞれ各クラスのオブジェクト.
	//どのクラスのインスタンスが生成されたかわかるように初期値はnull.
	private R_type_0 r_type_0 = null;
	private R_type_1 r_type_1 =null;
	private R4_type r4_type = null;
	private I_type_0 i_type_0 = null;
	private I_type_1 i_type_1 = null;
	private I_type_2 i_type_2 = null;
	private S_type s_type = null;
	private B_type b_type = null;
	private U_type u_type = null;
	private J_type j_type = null;
	private G_type g_type = null;
	private M_type m_type = null;
	
	private Main_section m_sec  = null;
	
	private Label label  = null;
	
	public void parse(Context ct,Data_box data)   {
		
	
		
		//文字列に":"が含まれている場合, 用はラベルがある場合はLabelのオブジェクトを生成.
		if(ct.currentToken().contains(":")) {
			label = new Label();
			label.parce(ct, data);
			
		}
		
		// System.out.printf("command : %s gyou : %2X\n", ct.currentToken(),data.tentative_count);
		
		//R typeの命令.
		if (ct.match("or|and|xor|add|sub|srl|sra|sll|slt|sltu")) {
			data.OIS.add(ct.getLineString());
		    r_type_0 = new R_type_0();
		    r_type_0.parse(ct, data);

		//I type 0の命令
		} else if (ct.match("ori|andi|xori|addi|subi|slti|sltiu")) {
			data.OIS.add(ct.getLineString());
		    i_type_0 = new I_type_0();
		    i_type_0.parse(ct, data);

		//I type 1の命令
		} else if (ct.match("jalr|lb|lh|lw|lbu|lhu")) {
			data.OIS.add(ct.getLineString());
		    i_type_1 = new I_type_1();
		    i_type_1.parse(ct, data);

		//I type 2の命令
		} else if (ct.match("ecall")) {
			data.OIS.add(ct.getLineString());
		    i_type_2 = new I_type_2();
		    i_type_2.parse(ct, data);

		//S typeの命令
		} else if (ct.match("sb|sh|sw")) { 
			data.OIS.add(ct.getLineString());
		    s_type = new S_type();
		    s_type.parse(ct, data);

		// B typeの命令
		} else if (ct.match("beq|bne|blt|bge|bltu|bgeu")) {
			data.OIS.add(ct.getLineString());
		    b_type = new B_type();
		    b_type.parse(ct, data);

		// U typeの命令
		} else if (ct.match("lui|auipc")) {
			data.OIS.add(ct.getLineString());
		    u_type = new U_type();
		    u_type.parse(ct, data);

		// J typeの命令
		} else if (ct.match("jal")) {
			data.OIS.add(ct.getLineString());
		    j_type = new J_type();
		    j_type.parse(ct, data);

		// G typeの命令(疑似命令)
		} else if (ct.match("la|nop|li|mv|not|neg|seqz|snez|sltz|sgtz|beqz|bnez|blez|bgez|bltz|bgtz|bgt|ble|bgtu|bleu|j|jr|ret|call|tail|fence")){
			data.OIS.add(ct.getLineString());
			g_type = new G_type();
		    g_type.parse(ct, data);

		    
		// M拡張命令
		} else if (ct.match("mul|mulh|mulhsu|mulhu|div|divu|rem|remu") && data.selectM == true){
			data.OIS.add(ct.getLineString());
			m_type = new M_type();
		    m_type.parse(ct, data);

		    
		// F拡張命令 <I type>
		} else if (ct.match("flw") && data.selectF == true){
			data.OIS.add(ct.getLineString());
			i_type_1 = new I_type_1();
		    i_type_1.parse(ct, data);

		    
		// F拡張命令 <S type>
		} else if (ct.match("fsw") && data.selectF == true){
			data.OIS.add(ct.getLineString());
			s_type = new S_type();
		    s_type.parse(ct, data);

		    
		// F拡張命令 <R4 type>
		} else if (ct.match("fmadd.s|fmsub.s|fmmsub.s|fmmadd.s") && data.selectF == true){
			data.OIS.add(ct.getLineString());
			r4_type = new R4_type();
		    r4_type.parse(ct, data);

		    
		// F拡張命令 <R type 0>
		} else if (ct.match("fadd.s|fsub.s|fmul.s|fdiv.s|fsgnj.s|fsgnjn.s|fsgnjx.s|fmin.s|fmax.s|feq.s|flt.s|fle.s") && data.selectF == true){
			data.OIS.add(ct.getLineString());
			r_type_0 = new R_type_0();
		    r_type_0.parse(ct, data);

		    
		// F拡張命令 <R type 1>
		} else if (ct.match("fsqrt.s|fcvt.w.s|fcvt.wu.s|fmv.w.s|fclass.s|fcvt.s.w|fcvt.s.wu|fmv.w.x") && data.selectF == true){
			data.OIS.add(ct.getLineString());
			r_type_1 = new R_type_1();
		    r_type_1.parse(ct, data);

		    
		// D拡張命令 <I type>
		} else if (ct.match("fld") && data.selectD == true) { // D拡張命令 (I type)
		    data.OIS.add(ct.getLineString());
		    i_type_1 = new I_type_1();
		    i_type_1.parse(ct, data);

		// D拡張命令 <S type>
		} else if (ct.match("fsd") && data.selectD == true) { // D拡張命令 (S type)
		    data.OIS.add(ct.getLineString());
		    s_type = new S_type();
		    s_type.parse(ct, data);

		// D拡張命令 <R4 type>
		} else if (ct.match("fmadd.d|fmsub.d|fmmsub.d|fmmadd.d") && data.selectD == true){
			data.OIS.add(ct.getLineString());
			r4_type = new R4_type();
		    r4_type.parse(ct, data);

		    
		// D拡張命令 <R type 0>
		} else if (ct.match("fadd.d|fsub.d|fmul.d|fdiv.d|fsgnj.d|fsgnjn.d|fsgnjx.d|fmin.d|fmax.d|feq.d|flt.d|fle.d") && data.selectD == true){
			data.OIS.add(ct.getLineString());
			r_type_0 = new R_type_0();
		    r_type_0.parse(ct, data);

		    
		// D拡張命令 <R type 1>
		} else if (ct.match("fsqrt.d|fcvt.w.d|fcvt.wu.d|fmv.w.d|fclass.d|fcvt.d.w|fcvt.d.wu|fmv.d.x") && data.selectF == true){
			data.OIS.add(ct.getLineString());
			r_type_1 = new R_type_1();
		    r_type_1.parse(ct, data);

		    
		// それ以外のタイプは存在しないためエラー
		} else { 
        	data.err.add("(" + ct.currentLine() + "行目): error C3 : RV32I以外の命令が含まれています。 : "+ ct.currentToken());
        	ct.toNextLine();
        	
        }
		
		System.out.println("ここまではきてるよ" + ct.currentToken());
				
		data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
		
		if(ct.match("^[a-zA-Z_][a-zA-Z0-9_]*:$")) { //n+1行目でラベルがあればオブジェクトを生成.
			label = new Label();
			label.parce(ct, data);
			
		}
		// n+1行目にRV32Iの命令が来るか判定
		if (ct.match("or|and|xor|add|sub|srl|sra|sll|slt|sltu|ori|andi|xori|addi|subi|slti|sltiu|jalr|ecall|lb|lh|lw|lbu|lhu|sb|sh|sw|beq|bne|blt|bge|bltu|bgeu|lui|auipc|jal|la|nop|li|mv|not|neg|seqz|snez|sltz|sgtz|beqz|bnez|blez|bgez|bltz|bgtz|bgt|ble|bgtu|bleu|j|jr|ret|call|tail|fence")) {
		    m_sec = new Main_section();
		    m_sec.parse(ct, data);

		// n+1行目にRV32Mの命令が来るか判定
		} else if (ct.match("mul|mulh|mulhsu|mulhu|div|divu|rem|remu") && data.selectM == true){
			m_sec = new Main_section();
		    m_sec.parse(ct, data);
			
		// n+1行目にRV32Fの命令が来るか判定
		} else if (ct.match("flw|fsw|fmadd.s|fmsub.s|fnmsub.s|fnmadd.s|fadd.s|fsub.s|fmul.s|fdiv.s|fsqrt.s|fsgnj.s|fsgnjn.s|fsgnjx.s|fmin.s|fmax.s|fcvt.w.s|fcvt.wu.s|fcvt.s.w|fcvt.s.wu|fclass.s|fmv.x.w|fmv.w.x") && data.selectF == true) {
			m_sec = new Main_section();
		    m_sec.parse(ct, data);
			
		// n+1行目にRV32Dの命令が来るか判定
		} else if (ct.match("fld|fsd|fmadd.d|fmsub.d|fnmsub.d|fnmadd.d|fadd.d|fsub.d|fmul.d|fdiv.d|fsgnj.d|fsgnjn.d|fsgnjx.d|fmin.d|fmax.d|fsqrt.d|fmv.x.d|fmv.d.x|fcvt.d.w|fcvt.d.wu|fcvt.w.d|fcvt.wu.d|fclass.d") && data.selectD == true) {
		    m_sec = new Main_section();
		    m_sec.parse(ct, data);
		    
		 // セクションが来ないこととnullではないことが確認できたら、変な命令が来ているためエラー    
		} else if (!ct.match(".data") && !ct.match(".text") && !ct.match(".bss") && !ct.match(".section") && ct.currentToken() != null) {
        	data.err.add("(" + ct.currentLine() + "行目): error C3 : RV32I以外の命令が含まれています。 : "+ ct.currentToken());
        	ct.toNextLine();
        	
		}
		
	}
	
	public void exe(Data_box data) {
		if(r_type_0 != null) {
			r_type_0.exe(data);
			
		} else if(i_type_0 != null) {
			i_type_0.exe(data);
			
		} else if(i_type_1 != null) {
			i_type_1.exe(data);
			
		} else if(i_type_2 != null) {
			i_type_2.exe(data);
			
		} else if(s_type != null) {
			s_type.exe(data);
			
		} else if(b_type != null) {
			b_type.exe(data);
			
		} else if(u_type != null) {
			u_type.exe(data);
			
		} else if(j_type != null) {
			j_type.exe(data);
			
		} else if(g_type != null) {
			g_type.exe(data);
			
		} else if(m_type != null) {
			m_type.exe(data);
			
		} 
		
		
		if(m_sec != null) {
			m_sec.exe(data);
			
		}
	}
	
}


//(5)  R_type => [or and xor add sub srl sra sll slt sltu] | [F拡張]Reg Reg Reg
class R_type_0{

	private byte opcode; //オペコードを格納する変数.
	private byte func7; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1,rs2; //それぞれrd, rs1, rs2を保持するオブジェクト.
	
	private int nowLine;
	
	public void parse(Context ct,Data_box data)   {
		
		//命令によって各変数に値を格納.
		if (ct.match("add")) {
			opcode = 0b0110011;
			func7  = 0b0000000;
			func3  = 0b000;
			
		} else if (ct.match("sub")) {
			opcode = 0b0110011;
			func7  = 0b0100000;
			func3  = 0b000;
			
		}  else if (ct.match("sll")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b001;
		    
		} else if (ct.match("slt")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b010;
		    
		} else if (ct.match("sltu")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b011;
		    
		} else if (ct.match("xor")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b100;
		    
		} else if (ct.match("srl")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b101;
		    
		} else if (ct.match("sra")) {  
		    opcode = 0b0110011;
		    func7  = 0b0100000;
		    func3  = 0b101;
		    
		} else if (ct.match("or")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b110;
		    
		} else if (ct.match("and")) {  
		    opcode = 0b0110011;
		    func7  = 0b0000000;
		    func3  = 0b111;
		    
		}
		
		if(data.selectF) {
			if (ct.match("fadd.s")) {
				opcode = 0b1010011;
			    func7  = 0b0000000;
			    func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
			    
			} else if (ct.match("fsub.s")) {
				opcode = 0b1010011;
			    func7  = 0b0000100;
			    func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
			    
			} else if (ct.match("fmul.s")) {
				opcode = 0b1010011;
			    func7  = 0b0001000;
			    func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
			    
			} else if (ct.match("fdiv.s")) {
				opcode = 0b1010011;
			    func7  = 0b0001100;
			    func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
			    
			} else if (ct.match("fsgnj.s")) {
				opcode = 0b1010011;
			    func7  = 0b0010000;
			    func3  = 0b000;
			    
			} else if (ct.match("fsgnjn.s")) {
				opcode = 0b1010011;
			    func7  = 0b0010000;
			    func3  = 0b001;
			    
			} else if (ct.match("fsgnjx.s")) {
				opcode = 0b1010011;
			    func7  = 0b0010000;
			    func3  = 0b010;
			    
			} else if (ct.match("fmin.s")) {
				opcode = 0b1010011;
			    func7  = 0b0010100;
			    func3  = 0b000;
			    
			} else if (ct.match("fmax.s")) {
				opcode = 0b1010011;
			    func7  = 0b0010100;
			    func3  = 0b001;
			    
			} else if (ct.match("feq.s")) {
				opcode = 0b1010011;
			    func7  = 0b1010000;
			    func3  = 0b010;
			    
			} else if (ct.match("flt.s")) {
				opcode = 0b1010011;
			    func7  = 0b1010000;
			    func3  = 0b001;
			    
			}  else if (ct.match("fle.s")) {
				opcode = 0b1010011;
			    func7  = 0b1010000;
			    func3  = 0b000;
			    
			} 
				
		} 
		
		if (data.selectD) { // D拡張命令 (倍精度浮動小数点命令) 
		    if (ct.match("fadd.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0000001; 
		        func3 = (byte) data.CSR.read(0x002);

		    } else if (ct.match("fsub.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0000101;
		        func3 = (byte) data.CSR.read(0x002);

		    } else if (ct.match("fmul.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0001001;
		        func3 = (byte) data.CSR.read(0x002);

		    } else if (ct.match("fdiv.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0001101;
		        func3 = (byte) data.CSR.read(0x002);

		    } else if (ct.match("fsgnj.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0010001;
		        func3 = 0b000;

		    } else if (ct.match("fsgnjn.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0010001;
		        func3 = 0b001;

		    } else if (ct.match("fsgnjx.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0010001;
		        func3 = 0b010;

		    } else if (ct.match("fmin.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0010101;
		        func3 = 0b000;

		    } else if (ct.match("fmax.d")) {
		        opcode = 0b1010011;
		        func7 = 0b0010101;
		        func3 = 0b001;

		    } else if (ct.match("feq.d")) {
		        opcode = 0b1010011;
		        func7 = 0b1010001;
		        func3 = 0b010;

		    } else if (ct.match("flt.d")) {
		        opcode = 0b1010011;
		        func7 = 0b1010001;
		        func3 = 0b001;

		    } else if (ct.match("fle.d")) {
		        opcode = 0b1010011;
		        func7 = 0b1010001;
		        func3 = 0b000;
		    }
		}

		
		nowLine = ct.lineNumber;
		
		ct.toNext(); //次に進める.
		
		//rd, rs1, rs2に各値を保持.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;		
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		rs2 = new Reg();
		if(rs2.parse(ct, data) != true) return ;
		
	}
	
	public void exe(Data_box data) {

		//論理演算を利用してallに機械語を代入.
		//ConverterクラスのconbertToXメソッドはt0やa0などの形式からx10やx7などのx表記に変換する.
		int rdVal = Converter.convertToX(rd.reg) << 7;
		int rs1Val = Converter.convertToX(rs1.reg) << 15;
		int rs2Val = Converter.convertToX(rs2.reg) << 20;
		int func7Val = func7 << 25;
		int func3Val = func3 << 12;

		all = rdVal | rs1Val | rs2Val | func7Val | func3Val | opcode;

		data.MEMput(all);
	}
}


//(5)  R_type_1 => [fsqrt.s fmv.w.x] Reg Reg
class R_type_1{

	private byte opcode; //オペコードを格納する変数.
	private byte func7; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1; //それぞれrd, rs1, rs2を保持するオブジェクト.
	
	int rs2Val=0;
	
	private int nowLine;
	
	public void parse(Context ct,Data_box data)   {
		
		//命令によって各変数に値を格納.
		if(data.selectF) {
			if (ct.match("fsqrt.s")) {
				opcode = 0b1010011;
				func7  = 0b0101100;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
			} else if (ct.match("fcvt.w.s")) {
				opcode = 0b1010011;
				func7  = 0b1111000;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
			} else if (ct.match("fcvt.wu.s")) {
				opcode = 0b1010011;
				func7  = 0b1100000;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
				rs2Val = 1 << 20;
				
			} else if (ct.match("fmv.x.w")) {
				opcode = 0b1010011;
				func7  = 0b1100000;
				func3  = 0b000;
				
			} else if (ct.match("fclass.s")) {
				opcode = 0b1010011;
			    func7  = 0b1110000;
			    func3  = 0b001;
			    
			} else if (ct.match("fcvt.s.w")) {
				opcode = 0b1010011;
			    func7  = 0b1101000;
			    func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
			    
			} else if (ct.match("fcvt.s.wu")) {
				opcode = 0b1010011;
			    func7  = 0b1101000;
			    func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
			    
			    rs2Val = 1 << 20;
			    
			}  else if (ct.match("fmv.w.x")) {
				opcode = 0b1010011;
			    func7  = 0b1111000;
			    func3  = 0b000;
			    
			} 
		}
		
		if(data.selectD) {
			
			if (ct.match("fsqrt.d")) {
			    opcode = 0b1010011;
			    func7  = 0b0101101; // fsqrt.d の func7
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm

			} else if (ct.match("fcvt.l.d")) {
			    opcode = 0b1010011;
			    func7  = 0b1111001; // fcvt.l.d の func7
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm

			} else if (ct.match("fcvt.lu.d")) {
			    opcode = 0b1010011;
			    func7  = 0b1100001; // fcvt.lu.d の func7
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm
			    rs2Val = 1 << 20;

			} else if (ct.match("fmv.x.d")) {
			    opcode = 0b1010011;
			    func7  = 0b1100001; // fmv.x.d の func7
			    func3  = 0b000;

			} else if (ct.match("fclass.d")) {
			    opcode = 0b1010011;
			    func7  = 0b1110001; // fclass.d の func7
			    func3  = 0b001;

			} else if (ct.match("fcvt.d.l")) {
			    opcode = 0b1010011;
			    func7  = 0b1101001; // fcvt.d.l の func7
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm

			} else if (ct.match("fcvt.d.lu")) {
			    opcode = 0b1010011;
			    func7  = 0b1101001; // fcvt.d.lu の func7
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm
			    rs2Val = 1 << 20;

			} else if (ct.match("fmv.d.x")) {
			    opcode = 0b1010011;
			    func7  = 0b1111001; // fmv.d.x の func7
			    func3  = 0b000;
			}

		}
		
		
		nowLine = ct.lineNumber;
		
		ct.toNext(); //次に進める.
		
		//rd, rs1に各値を保持.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
	
		
	}
	
	public void exe(Data_box data) {

		//論理演算を利用してallに機械語を代入.
		//ConverterクラスのconbertToXメソッドはt0やa0などの形式からx10やx7などのx表記に変換する.
		int rdVal = Converter.convertToX(rd.reg) << 7;
		int rs1Val = Converter.convertToX(rs1.reg) << 15;
		int func7Val = func7 << 25;
		int func3Val = func3 << 12;

		all = rdVal | rs1Val | rs2Val | func7Val | func3Val | opcode;

		data.MEMput(all);
	}
}


//(5)  R4_type => [fmadd.s fmsub.s fnmsub.s fnmadd.s] Reg Reg Reg Reg
class R4_type{

	private byte opcode; //オペコードを格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1,rs2,rs3; //それぞれrd, rs1, rs2を保持するオブジェクト.
	
	private int nowLine;
	
	public void parse(Context ct,Data_box data)   {
		
		if(data.selectF) {
			//命令によって各変数に値を格納.
			if (ct.match("fmadd.s")) {
				opcode = 0b1000011;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
			} else if (ct.match("fmsub.s")) {
				opcode = 0b1000111;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
			} else if (ct.match("fnmsub.s")) {
				opcode = 0b1001011;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
			} else if (ct.match("fnmadd.s")) {
				opcode = 0b1001111;
				func3  = (byte) data.CSR.read(0x002); //0x002はcsrのfrm
				
			}
			
		}
		
		if(data.selectD) {
			// 命令によって各変数に値を格納
			if (ct.match("fmadd.d")) {
			    opcode = 0b1000011; // fmadd.d の opcode
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm

			} else if (ct.match("fmsub.d")) {
			    opcode = 0b1000111; // fmsub.d の opcode
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm

			} else if (ct.match("fnmsub.d")) {
			    opcode = 0b1001011; // fnmsub.d の opcode
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm

			} else if (ct.match("fnmadd.d")) {
			    opcode = 0b1001111; // fnmadd.d の opcode
			    func3  = (byte) data.CSR.read(0x002); // 0x002はcsrのfrm
			}

		}
		
		nowLine = ct.lineNumber;
		
		ct.toNext(); //次に進める.
		
		//rd, rs1, rs2に各値を保持.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		if(nowLine != ct.lineNumber) {
			return ;
		}
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		rs2 = new Reg();
		if(rs2.parse(ct, data) != true) return ;
		
		rs3 = new Reg();
		if(rs3.parse(ct, data) != true) return ;
		
	}
	
	public void exe(Data_box data) {

		//論理演算を利用してallに機械語を代入.
		//ConverterクラスのconbertToXメソッドはt0やa0などの形式からx10やx7などのx表記に変換する.
		int rdVal = Converter.convertToX(rd.reg) << 7;
		int rs1Val = Converter.convertToX(rs1.reg) << 15;
		int rs2Val = Converter.convertToX(rs2.reg) << 20;
		int rs3Val = Converter.convertToX(rs3.reg) << 27;
		int func3Val = func3 << 12;

		all = rdVal | rs1Val | rs2Val | rs3Val | func3Val |opcode;

		data.MEMput(all);
	}
}


//(6)  I_type => [ori andi xori addi subi slti sltiu] Reg Reg Imm 
class I_type_0{
	
	private byte opcode; //オペコードを格納する変数.
	private byte func7; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1; //それぞれrd, rs1を保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.
	
	public void parse(Context ct,Data_box data)   {

		//命令によって各変数に値を格納.
		if (ct.match("slli")) {  
		    opcode = 0b0010011;
		    func7  = 0b0000000;
		    func3  = 0b001;
		    
		} else if (ct.match("srli")) {  
		    opcode = 0b0010011;
		    func7  = 0b0000000;
		    func3  = 0b101;
		    
		} else if (ct.match("srai")) {  
		    opcode = 0b0010011;
		    func7  = 0b0100000;
		    func3  = 0b101;
		    
		} else if (ct.match("addi")) {  
		    opcode = 0b0010011;
		    func3  = 0b000;
		    
		} else if (ct.match("slti")) {  
		    opcode = 0b0010011;
		    func3  = 0b010;
		    
		} else if (ct.match("sltiu")) {  
		    opcode = 0b0010011;
		    func3  = 0b011;
		    
		} else if (ct.match("xori")) {  
		    opcode = 0b0010011;
		    func3  = 0b100;
		    
		} else if (ct.match("ori")) {  
		    opcode = 0b0010011;
		    func3  = 0b110;
		    
		} else if (ct.match("andi")) {  
		    opcode = 0b0010011;
		    func3  = 0b111;
		    
		}
		
		ct.toNext(); //次に進める.

		//rd, rs1, immの順番で各値を格納
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		imm = new Imm();
		if(imm.parse(ct, data) != true) return ;
		
		//immが12bitを超えている場合.
		if(imm.imm > 0xFFF) {
			// System.out.println("I_type_0: immが12bitを超えています : " + ct.currentToken());
			
			data.err.add("(" + ct.currentLine() + "行目): error C6 : 即値が12bitを超えています。 : "+ ct.currentToken());
        	ct.toNextLine();
            
		}
		
		
	}
	
	public void exe(Data_box data) {
		int rdVal = Converter.convertToX(rd.reg) << 7;
		int rs1Val = Converter.convertToX(rs1.reg) << 15;
		int immVal = imm.imm << 20;
		int func7Val = func7 << 25;
		int func3Val = func3 << 12;
		
		if(imm.letter != null) //エラーメッセージ
		
		all = rdVal | rs1Val | immVal | func7Val | func3Val | opcode;
		
		data.MEMput(all);
	}
}


//(7)  I_type_1  => [jalr lb lh lw lbu lhu] | [flw]Reg Imm ( Reg ) 
class I_type_1{
	
	private byte opcode; //オペコードを格納する変数.
	private byte func7=0; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1; //それぞれrd, rs1を保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.
	
	public void parse(Context ct,Data_box data)   {

		//命令によって各変数に値を格納.
		if (ct.match("jalr")) {  
		    opcode = 0b1100111;
		    func3  = 0b000;
		    
		} else if (ct.match("lb")) {  
		    opcode = 0b0000011;
		    func3  = 0b000;
		    
		} else if (ct.match("lh")) {  
		    opcode = 0b0000011;
		    func3  = 0b001;
		    
		} else if (ct.match("lw")) {  
		    opcode = 0b0000011;
		    func3  = 0b010;
		    
		} else if (ct.match("lbu")) {  
			opcode = 0b0000011;
			func3  = 0b100;
		    
		} else if (ct.match("lhu")) {  
		    opcode = 0b0000011;
		    func3  = 0b101;
		    
		} else if(data.selectF && ct.match("flw")) {
			opcode = 0b0000111;
		    func3  = 0b010;
		    
		} else if (data.selectD && ct.match("fld")) {
		    opcode = 0b0000111; 
		    func3 = 0b011;      
		}
		
		ct.toNext(); //次に進める.

		//rd, imm(rs1)の順番で各値を格納.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		imm = new Imm();
		if(imm.parse(ct, data) != true) return ;
		
		//即値が12bitを超える場合
		if( imm.imm > 0xFFF ) { 
			//擬似命令でない場合
			if(ct.match("zero|ra|sp|t[0-6]|a[0-7]|s[0-9]|s1[0-1]|x([0-9]|1[0-9]|2[0-9]|3[0-1])") ) {
				// System.out.println("I_type_1: immが12bitを超えています : " + ct.currentToken());
				
				data.err.add("(" + ct.currentLine() + "行目): error C6 : 即値が12bitを超えています。 : "+ ct.currentToken());
	        	ct.toNextLine();
	        	
	        	return;
	        //擬似命令の場合はreturnでメソッドを終了する
	        //	=>lw rd, Symbolという形が擬似命令の形となり、rd, imm(rs1)という形にならないため
			} else { 
				data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
				data.OIS.add(" ");
				return;
			}
			
		}
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		
	}
	
	public void exe(Data_box data) {
		
		if(data.getData(imm.letter) != -1) {
			imm.imm = data.getData(imm.letter);
			
		}
		
		if(rs1 != null) {
			int rdVal = Converter.convertToX(rd.reg) << 7;  // rdフィールドは7ビットシフト
			int rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフト
			int immVal = imm.imm << 20;  // 即値(imm)は20ビットシフト
			int func7Val = func7 << 25;  // func7フィールドは25ビットシフト
			int func3Val = func3 << 12;  // func3フィールドは12ビットシフト
	
			all = rdVal | rs1Val | immVal | func7Val | func3Val | opcode;  // 各フィールドをビットOR
			
		} else { //immが12bitを超えた時
			
			//auipcとBase命令で処理
			int rdVal;
			int rs1Val;
			int immVal;
			int func7Val;
			int func3Val;
			
			rdVal = Converter.convertToX(rd.reg) << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
			immVal = imm.imm & 0xFFFFF000;                 // 32ビットの即値の上位20bitを取って配置 (12-31ビット目)

			// opcodeとビットORで結合して最終的な32ビットの命令を生成
			all = rdVal | immVal | 0b0010111;

			data.MEMput(all);
			
			//Base命令の処理
			rdVal = Converter.convertToX(rd.reg) << 7;
			rs1Val = Converter.convertToX(rd.reg) << 15;
			immVal = ((imm.imm & 0xFFF) - (data.text_count - 4)) << 20;
			func7Val = func7 << 25;
			func3Val = func3 << 12;
			
			all = rdVal | rs1Val | immVal | func7Val | func3Val | opcode;
			
		}
		
		data.MEMput(all);
	}
}

//(7)  I_type_2  => [ecall] 
class I_type_2{
	
	private byte opcode; //オペコードを格納する変数.
	private byte func7=0; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1; //それぞれrd, rs1を保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.
	
	public void parse(Context ct,Data_box data)   {

		//命令によって各変数に値を格納.
		if (ct.match("ecall")) {  
		    opcode = 0b1110011;
		    func3  = 0b000;
		    
		}
		
		ct.toNext(); //次に進める.		
		
	}
	
	public void exe(Data_box data) {
		
		all = opcode;
		data.MEMput(all);
	}
}


//(8)  S_type => [sb sh sw] Reg Imm [(] Reg [)]
class S_type{
	private byte opcode; //オペコードを格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rs1,rs2; //それぞれrs1, rs2を保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.
	
	public void parse(Context ct, Data_box data)   {

		//命令によって各変数に値を格納.
		if (ct.match("sb")) {  
			 opcode = 0b0100011;
			 func3  = 0b000;
			 
		} else if (ct.match("sh")) {  
			 opcode = 0b0100011;
			 func3  = 0b001;
			 
		} else if (ct.match("sw")) {  
			 opcode = 0b0100011;
			 func3  = 0b010;
			 
		} else if(data.selectF && ct.match("fsw")) {
			opcode = 0b0100111;
		    func3  = 0b010;
		}
		
		ct.toNext(); //次に進める.
		
		//rs2, imm(rs1)の順番で各値を格納.
		rs2 = new Reg();
		if(rs2.parse(ct, data) != true) return ;
		
		imm = new Imm();
		if(imm.parse(ct, data) != true) return ;
		
		if( imm.imm > 0xFFF ) { 
			//擬似命令でない場合
			if(ct.match("zero|ra|sp|t[0-6]|a[0-7]|s[0-9]|s1[0-1]|x([0-9]|1[0-9]|2[0-9]|3[0-1])") ) {
				// // System.out.println("S_type: immが12bitを超えています : " + ct.currentToken());
				
	        	data.err.add("(" + ct.currentLine() + "行目): error C6 : 即値が12bitを超えています。 : "+ ct.currentToken());
	        	ct.toNextLine();
	        	
	        	return;
	            
	        //擬似命令の場合はreturnでメソッドを終了する
	        //	=>lw rd, Symbolという形が擬似命令の形となり、rd, imm(rs1)という形にならないため
			} else { 
				data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
				return;
			}
			
		}
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		
	}
	
	public void exe(Data_box data) {
		if(imm.imm <= 0xFFF) {
			int immLowVal = (imm.imm & 0x1F) << 7;  // 即値の下位5ビットを7ビットシフトして配置 (imm[4:0])
			int rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			int rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			int immHighVal = (imm.imm >> 5) << 25;  // 即値の上位ビットを25ビットシフトして配置 (imm[11:5])
			int func3Val = func3 << 12;  // func3フィールドを12ビットシフトして配置
		
			all = immLowVal | rs1Val | rs2Val | immHighVal | func3Val | opcode;  // 全てのフィールドをビットORで結合

			//// System.out.printf("\nopcode : %x\n\n",opcode);
			//// System.out.printf("\nall : %x\n\n",all);
		} else {
			int PC = data.text_count;
		
			//auipcとBase命令で処理
			int rdVal;
			int rs1Val;
			int immVal;
			int func3Val;
			int immLowVal;
			int rs2Val;
			int immHighVal;
		
			rdVal = Converter.convertToX(rs2.reg) << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
			immVal = imm.imm & 0xFFFFF000;                 // 32ビットの即値の上位20bitを取って配置 (12-31ビット目)

			// opcodeとビットORで結合して最終的な32ビットの命令を生成
			all = rdVal | immVal | 0b0010111;
			data.MEMput(all);
			
			imm.imm = (imm.imm & 0xFFF) - PC;
			
			immLowVal = (imm.imm & 0x1F) << 7;  // 即値の下位5ビットを7ビットシフトして配置 (imm[4:0])
			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			immHighVal = (imm.imm >> 5) << 25;  // 即値の上位ビットを25ビットシフトして配置 (imm[11:5])
			func3Val = func3 << 12;  // func3フィールドを12ビットシフトして配置
		
			all = immLowVal | rs1Val | rs2Val | immHighVal | func3Val | opcode;  // 全てのフィールドをビットORで結合
		}
		
		data.MEMput(all);
	}
}


//(9)  B_type       => [beq bne blt bge bltu bgeu] Reg Reg [a-zA-Z]+
class B_type{

	private byte opcode; //オペコードを格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private int relative; //相対的なアドレスを格納する変数. 現在のアドレスから分岐するアドレスの差を格納する.
	
	private int now_location; //現在のアドレスを格納する変数. 
	
	private Reg rs1,rs2; //それぞれrs1, rs2を保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.

	public void parse(Context ct,Data_box data)   {

		//命令によって各変数に値を格納.
		if (ct.match("beq")) {  
			    opcode = 0b1100011;
			    func3  = 0b000;
			    
		} else if (ct.match("bne")) {  
			    opcode = 0b1100011;
			    func3  = 0b001;
			    
		} else if (ct.match("blt")) {  
			    opcode = 0b1100011;
			    func3  = 0b100;
			    
		} else if (ct.match("bge")) {  
			    opcode = 0b1100011;
			    func3  = 0b101;
			    
		} else if (ct.match("bltu")) {  
			    opcode = 0b1100011;
			    func3  = 0b110;
			    
		} else if (ct.match("bgeu")) {  
			    opcode = 0b1100011;
			    func3  = 0b111;
			    
		}
		
		ct.toNext(); //次に進める.
		now_location = data.tentative_count; //現在のアドレスを格納.
		
		
		//rs1, rs2, immの順番で各値を格納する.
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		rs2 = new Reg();
		if(rs2.parse(ct, data) != true) return ;
		
		imm = new Imm();
		if(imm.parse(ct, data) != true) return ;
			
	}

	public void exe(Data_box data) {
	    if (data.getData(imm.letter) != -1) {
	        relative = (data.getData(imm.letter) - now_location) / 2; // 4バイト単位でエンコード
	    } else {
	        relative = imm.imm / 2; // 即値も4バイト単位に
	    }

	    int immBit31 = (relative >> 12) & 0x1;
	    int immBit11 = (relative >> 11) & 0x1;
	    int immBit10to5 = (relative >> 5) & 0x3F;
	    int immBit4to1 = (relative & 0xF);

	    immBit31 <<= 31;
	    immBit11 <<= 7;
	    immBit10to5 <<= 25;
	    immBit4to1 <<= 8;

	    int rs1Val = Converter.convertToX(rs1.reg) << 15;
	    int rs2Val = Converter.convertToX(rs2.reg) << 20;
	    int func3Val = func3 << 12;

	    all = immBit31 | immBit11 | immBit10to5 | rs2Val | rs1Val | func3Val | immBit4to1 | opcode;

	    data.MEMput(all);
	}

}



//(10) U_type => [lui auipc] Reg ( [0-9]+ | [a-zA-Z]+ )
class U_type{

	private byte opcode; //オペコードを格納する変数.
	
	private Reg rd; //rdを保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.
	
	private int all; //4byteの機械語を格納する変数.
	
	public void parse(Context ct,Data_box data)   {

		//命令によって各変数に値を格納.
		if (ct.match("lui")) {  
		    opcode = 0b0110111;
		    
		} else if (ct.match("auipc")) {  
		    opcode = 0b0010111;
		    
		}

		ct.toNext(); //次に進める.
		
		//rd, immの順番で各値を格納する.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		imm = new Imm();
		if(imm.parse(ct, data) != true) return ;
		
	}
	
	public void exe(Data_box data) {
		int rdVal = Converter.convertToX(rd.reg) << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
		int immVal = imm.imm << 12;                       // 20ビットの即値を12ビットシフトして配置 (12-31ビット目)

		// opcodeとビットORで結合して最終的な32ビットの命令を生成
		all = rdVal | immVal | opcode;

		data.MEMput(all);
	}
}



//(11) J_type => [jal]  Reg Imm
class J_type{
	private byte opcode; //オペコードを格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private int relative; //相対的なアドレスを格納する変数. 分岐するアドレスと現在のアドレスの差を格納する.
	
	private int now_location; //現在のアドレスを格納する変数. 
	
	private Reg rd; //rdを保持するオブジェクト.
	private Imm imm; //イミディエイト（整数）を保持するオブジェクト.
	
	public void parse(Context ct,Data_box data)   {
		if (ct.match("jal")) {  
		    opcode = 0b1101111;
		    
		}
		
		ct.toNext(); //次に進める.
		
		//rd, immの順番で各値を格納する.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		imm = new Imm();
		if(imm.parse(ct, data) != true) return ;
		
		now_location = data.tentative_count; //現在のアドレスを格納.
		
	}
	
	public void exe(Data_box data) {

		//Data_boxクラスのgetDataメソッドではdata_mapにimm.letterが登録されているか調べる.
		//登録されていた場合はそのアドレスを返して, 登録されていなかったら-1を返す.
		//data_mapはMapとなっていて(String, Variable)というセットになっており, VariableはData_boxの内部クラス.
		//Variableクラスではアドレスと整数値を保持している.
		if(data.getData(imm.letter) != -1) { 
			relative = data.getData(imm.letter) - now_location; //relativeに登録されたアドレスから現在のアドレスを引いた値を格納する.

		} else {
			relative = imm.imm; //登録されていなかった場合はimmにアドレスが登録されているため, 現在のアドレスを引いた値を格納する.
		}
		
		// System.out.println("getData     : " + data.getData(imm.letter));
		// System.out.println("tenta_count : " + now_location);
		// System.out.println("relative    : " + relative);
		
		
		int rdVal = Converter.convertToX(rd.reg) << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
		int imm10to1 = (relative & 0x7FE) << 20;         // 即値の[10:1]ビットを抽出して20ビットシフト
		int imm11 = (relative & 0x800) << 9;             // 即値の[11]ビットを抽出して20ビット目に配置
		int imm19to12 = (relative & 0xFF000);            // 即値の[19:12]ビットをそのまま配置 (12-19ビット目)
		int imm20 = (relative & 0x80000000);             // 即値の[31]ビットを最上位ビット (31) に配置 (符号ビット)

		all = rdVal | imm10to1 | imm11 | imm19to12 | imm20 | opcode;  // 各ビットフィールドをビットORで結合


		data.MEMput(all);
	}
}



class G_type{
	private byte opcode; //オペコードを格納する変数.
	private byte func7; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private int relative; //相対的なアドレスを格納する変数. 分岐するアドレスと現在のアドレスの差を格納する.
	
	private int now_location; //現在のアドレスを格納する変数. 
	
	private Reg rd,rs1,rs2; //それぞれrd, rs1, rs2を保持するオブジェクト.
	private Imm imm; //即値を保持するオブジェクト
	
	private boolean la   = false;
	
	private boolean nop   = false;
	private boolean li    = false;
	private boolean mv    = false;
	private boolean not   = false;
	private boolean neg   = false;
	private boolean seqz  = false;
	private boolean snez  = false;
	private boolean sltz  = false;
	private boolean sgtz  = false;
	
	private boolean beqz  = false;
	private boolean bnez  = false;
	private boolean blez  = false;
	private boolean bgez  = false;
	private boolean bltz  = false;
	private boolean bgtz  = false;
	
	private boolean bgt   = false;
	private boolean ble   = false;
	private boolean bgtu  = false;
	private boolean bleu  = false;
	
	private boolean j     = false;
	private boolean jr    = false;
	
	private boolean ret   = false;
	private boolean call  = false;
	private boolean tail  = false;
	
	private boolean fence = false;
	
	
	/*
	 * l {b, h, w, d} rd, symbol
	 * はBase命令の即値拡張版であるため、G Typeでは処理しない
	 * 
	 * 
	 * s {b, h, w, d} rd, symbol, rt
	 * も同様
	 * 
	 * 
	 * flfw|dg rd, symbol, rt
	 * fsfw|dg rd, symbol, rt
	 * 
	 * negw rd, rs
	 * sext.w rd, rs
	 * 
	 * fmv.s rd, rs
	 * fabs.s rd, rs
	 * fneg.s rd, rs
	 * fmv.d rd, rs
	 * fabs.d rd, rs
	 * fneg.d rd, rs
	 * 
	 * については浮動小数点命令であるため今回は不採用
	 * 
	 * 
	 * jal offset
	 * についてもBase命令の短縮形であるため、ここで処理しない
	 * 
	 * */

	
	public void parse(Context ct,Data_box data)   {
		if(ct.match("la")) { //la rd, symbol
			// laはauipcとaddiに展開される
			// ex) la t0, label => auipc t0, offset_hi ,addi t0, t0, offset_lo
			// auipcを使う場合はpcの分addiで引き算をする必要がある
			
			la = true;

			ct.toNext();
			
			rd = new Reg();
			if(rd.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			if(data.getData(imm.letter) != -1) {
				imm.imm = data.getData(imm.letter);
				
				
			}
			
			if(imm.imm > 0xfff) data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
			data.OIS.add(" ");
			
		} else if(ct.match("nop")) { //nop
			// nopはaddiに展開される
			// ex) nop => addi x0, x0, 0
			
			nop = true;
			
		} else if(ct.match("li")) { //li rd, imm
			// liはaddiかauipc + addiに展開される
			// ex1) li t0, 10 => addi t0, x0, 10
			// ex2) li t0, 0xFFFF0000 => auipc t0, 0xFFFF0 addi t0, x0, -PC;
			
			li = true;
			
			ct.toNext();
			
			rd = new Reg();
			if(rd.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			if(imm.imm > 0xfff) {
				data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
				data.OIS.add(" ");
			}
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("mv")) { //mv rd, rs1
			// mvはaddiに展開される
			// ex) mv t0, t1 => addi t0, t1, 0
			
			mv = true;
			
			ct.toNext();
			
			rd = new Reg();
			if(rd.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
		} else if(ct.match("not")) { //not rd, rs1
			// notはxoriに展開される
			// ex) not t0, t1 => xori t0, t1, -1
			
			not = true;
			
			ct.toNext();
			
			rd = new Reg();
			if(rd.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
		} else if(ct.match("neg")) { //neg rd, rs1
			// negはsub rd, x0, rs1
			// ex) neg t0, t1 => sub t0, x0, t1
			
			neg = true;
			
			ct.toNext();
			
			rd = new Reg();
			if(rd.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
		} else if(ct.match("seqz")) { //seqz rd, rs1
			// seqzはsltiu rd, rs1 ,1
			// ex) seqz t0, t1 => sltiu t0, t1, 1
			
			seqz = true;

			ct.toNext();
			
			rd = new Reg();
			if(rd.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
		} else if(ct.match("beqz")) { //beqz rs1, offset
			// beqzはbeqに展開される
			// ex) beqz t0, offset => beq t0, x0, offset
			
			beqz = true;

			ct.toNext();
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bnez")) { //bnez rs1, offset
			// bnezはbneに展開される
			// ex) bnez t0, offset => bne t0, x0, offset
			
			bnez = true;

			ct.toNext();
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("blez")) { //blez rs1, offset
			// beqzはbeqに展開される
			// ex) blez t0, offset => bge x0, t0, offset
			
			blez = true;

			ct.toNext();
			
			rs2 = new Reg();
			if(rs2.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bgez")) { //bgez rs1, offset
			// bgezはbeqに展開される
			// ex) bgez t0, offset => bge t0, x0, offset
			
			bgez = true;

			ct.toNext();
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bltz")) { //bltz rs1, offset
			// beqzはbeqに展開される
			// ex) bltz t0, offset => blt t0, x0, offset
			
			bltz = true;

			ct.toNext();
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bgtz")) { //bgtz rs1, offset
			// beqzはbeqに展開される
			// ex) bgtz t0, offset => blt x0, t0, offset
			
			bgtz = true;

			ct.toNext();
			
			rs2 = new Reg();
			if(rs2.parse(ct, data) != true) return ;
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bgt")) { //bgt rs2, rs1, offset
			// bgtはbltに展開される
			// ex) bgt t0, t1, offset => blt t1, t0, offset
			
			bgt = true;

			ct.toNext();
			
			rs2 = new Reg();
			if(rs2.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("ble")) { //ble rs2, rs1, offset
			// beqzはbeqに展開される
			// ex) ble t0, t1, offset => bge t1, t0, offset
			
			ble = true;

			ct.toNext();
			
			rs2 = new Reg();
			if(rs2.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bgtu")) { //bgtu rs2, rs1, offset
			// bgtuはbeqに展開される
			// ex) bgtu t0, t1, offset => bltu t1, t0, offset
			
			bgtu = true;

			ct.toNext();
			
			rs2 = new Reg();
			if(rs2.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("bleu")) { //bleu rs2, rs1, offset
			// bleuはbeqに展開される
			// ex) bleu t0, t1, offset => bgeu t1, t0, offset
			
			bgtu = true;

			ct.toNext();
			
			rs2 = new Reg();
			if(rs2.parse(ct, data) != true) return ;
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("j")) { //j offset
			// jはjalに展開される
			// ex) j label => jal x0, label
			
			j = true;
			
			ct.toNext();

			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("jr")) { //jr rs
			// jrはjalrに展開される
			// ex) jr t0 => jalr x0, t0, 0
			
			jr = true;
			
			ct.toNext();
			
			rs1 = new Reg();
			if(rs1.parse(ct, data) != true) return ;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		} else if(ct.match("ret")) { //ret
			//retはjalrに展開される
			// ex) ret => jalr x0, x1, 0
			
			ret = true;
			
			now_location = data.tentative_count; //現在のアドレスを格納.
			
		}  else if(ct.match("call")) { //call offset
			// callはauipcとjalに展開される
			// ex) call func => auipc x6, offset[31:12] jalr x1, x6, offset[11:0]
			
			call = true;
			
			ct.toNext();
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			if(imm.imm > 0xfff) data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
			
			now_location = data.tentative_count; //現在のアドレスを格納.
		} else if(ct.match("tail")) { //call offset
			// tailはauipcとjalに展開される
			// ex) call func => auipc x6, offset[31:12] jalr x0, x6, offset[11:0]
			
			tail = true;
			
			ct.toNext();
			
			imm = new Imm();
			if(imm.parse(ct, data) != true) return ;
			
			if(imm.imm > 0xfff) {
				data.tentative_count += 4; //メモリのアドレスをカウント. 仮のカウント.
				data.OIS.add(" ");
			}
			
			now_location = data.tentative_count; //現在のアドレスを格納.
		}
	}
	
	public void exe(Data_box data) {
		
		int rdVal;
		int rs1Val;
		int immVal;
		int func7Val;
		int func3Val;
		int rs2Val;
		int imm10to1;
		int imm11;
		int imm19to12;
		int imm20;
		int immBit31;
		int immBit11;
		int immBit10to5;
		int immBit4to1;
		
		if(la) {
			
			if(data.getData(imm.letter) != -1) {
				imm.imm = data.getData(imm.letter);
				
			} 
			
			if(imm.imm <= 0xFFF) {
				//addiの処理
				rdVal = Converter.convertToX(rd.reg) << 7;
				rs1Val = 0 << 15;
				immVal = (imm.imm & 0xFFF) << 20;
				func7Val = 0b0000000 << 25;
				func3Val = 0b000 << 12;
			
				all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
				
			
				data.MEMput(all);
			} else {
				//auipcの処理
				rdVal = Converter.convertToX(rd.reg) << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
				immVal = imm.imm & 0xFFFFF000;                 // 32ビットの即値の上位20bitを取って配置 (12-31ビット目)

				all = rdVal | immVal | 0b0010111;

				data.MEMput(all);
				
				
				
				
			
				//addiの処理
				rdVal = Converter.convertToX(rd.reg) << 7;
				rs1Val = Converter.convertToX(rd.reg) << 15;
				immVal = ((imm.imm & 0xFFF) - now_location + 4) << 20;
				func7Val = 0b0000000 << 25;
				func3Val = 0b000 << 12;
			
				all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
			
				data.MEMput(all);
			}
			
		} else if(nop) {
			//addiの処理
			rdVal = 0 << 7;
			rs1Val = 0 << 15;
			immVal = 0 << 20;
			func7Val = 0b0000000 << 25;
			func3Val = 0b000 << 12;
			
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
			
			data.MEMput(all);
			
		} else if(li) {
			
			if(imm.imm <= 0xFFF) {
				//addiの処理
				rdVal = Converter.convertToX(rd.reg) << 7;
				rs1Val = 0 << 15;
				immVal = (imm.imm & 0xFFF) << 20;
				func7Val = 0b0000000 << 25;
				func3Val = 0b000 << 12;
			
				all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
				
			
				data.MEMput(all);
			} else {
				//auipcの処理
				rdVal = Converter.convertToX(rd.reg) << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
				immVal = imm.imm & 0xFFFFF000;                 // 32ビットの即値の上位20bitを取って配置 (12-31ビット目)

				all = rdVal | immVal | 0b0010111;

				data.MEMput(all);
			
				//addiの処理
				rdVal = Converter.convertToX(rd.reg) << 7;
				rs1Val = Converter.convertToX(rd.reg) << 15;
				immVal = ((imm.imm & 0xFFF) - now_location + 4) << 20;
				func7Val = 0b0000000 << 25;
				func3Val = 0b000 << 12;
			
				all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
			
				data.MEMput(all);
			}
		} else if(mv) {
			//addiの処理
			rdVal = Converter.convertToX(rd.reg) << 7;
			rs1Val = Converter.convertToX(rs1.reg) << 15;
			immVal = ((0 & 0xFFF) - now_location) << 20;
			func7Val = 0b0000000 << 25;
			func3Val = 0b000 << 12;
			
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
			
			data.MEMput(all);
			
		} else if(not) {
			//xoriの処理
			rdVal = Converter.convertToX(rd.reg) << 7;
			rs1Val = 0 << 15;
			immVal = (-1 & 0xFFF) << 20;
			func7Val = 0b0000000 << 25;
			func3Val = 0b100 << 12;
			
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b0010011;
			
			data.MEMput(all);
			
		} else if(neg) {
			//subの処理
			rdVal = Converter.convertToX(rd.reg) << 7;
			rs1Val = 0 << 15;
			rs2Val = Converter.convertToX(rs2.reg) << 20;
			func7Val = 0b0100000 << 25;
			func3Val = 0b000 << 12;
			
			all = rdVal | rs1Val | rs2Val | func7Val | func3Val | 0b0110011;

			data.MEMput(all);
			
		} else if(beqz){
			//beqの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = 0 << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 000 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = (immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011) & 0xFFFF8FFF;
			
			data.MEMput(all);
			
		} else if(bnez){
			//bneの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = 0 << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b001 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(blez){
			//bgeの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = 0 << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b101 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(bgez){
			//bgeの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = 0 << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b101 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(bltz){
			//bltの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = 0 << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b100 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(bgtz){
			//bltの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = 0 << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b100 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(bgt){
			//bltの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b100 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(ble){
			//bgeの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b101 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(bgtu){
			//bltuの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b110 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);                       // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);
			
		} else if(bleu){
			//bgeuの処理
			if(data.getData(imm.letter) != -1) {
				relative = data.getData(imm.letter) - now_location;
				
			} else {
				relative = imm.imm;

			}
			
			immBit31 = (relative >> 31) << 31;        // imm[12] -> 最上位ビット (31) に格納 (符号ビット)
			immBit11 = (relative & 0x1) << 7;         // imm[11] -> 7ビット目に格納
			immBit10to5 = ((relative >> 1) & 0x3F) << 8; // imm[10:5] -> 8-13ビット目に格納 (6ビット)
			immBit4to1 = ((relative >> 5) & 0xF) << 25;  // imm[4:1] -> 25-28ビット目に格納 (4ビット)

			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフトして配置
			rs2Val = Converter.convertToX(rs2.reg) << 20;  // rs2フィールドは20ビットシフトして配置
			func3Val = 0b111 << 12;                        // func3フィールドは12ビットシフトして配置

			// 各ビットフィールドをビットORで結合
			all = immBit31 | immBit11 | immBit10to5 | rs1Val | rs2Val | immBit4to1 | func3Val | 0b1100011;

			data.MEMput(all);                       // func3フィールドは12ビットシフトして配置
			
		} else if(j) {
			
			if(data.getData(imm.letter) != -1) { 
				relative = data.getData(imm.letter) - now_location; //relativeに登録されたアドレスから現在のアドレスを引いた値を格納する.

			} else {
				relative = imm.imm  - now_location; //登録されていなかった場合はimmにアドレスが登録されているため, 現在のアドレスを引いた値を格納する.
			}
			
			rdVal = 0 << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
			imm10to1 = (relative & 0x7FE) << 20;         // 即値の[10:1]ビットを抽出して20ビットシフト
			imm11 = (relative & 0x800) << 9;             // 即値の[11]ビットを抽出して20ビット目に配置
			imm19to12 = (relative & 0xFF000);            // 即値の[19:12]ビットをそのまま配置 (12-19ビット目)
			imm20 = (relative & 0x80000000);             // 即値の[31]ビットを最上位ビット (31) に配置 (符号ビット)

			all = rdVal | imm10to1 | imm11 | imm19to12 | imm20 | 0b1101111;  // 各ビットフィールドをビットORで結合


			data.MEMput(all);
			
		} else if(jr) {
			//jalr
			rdVal = 0 << 7;  // rdフィールドは7ビットシフト
			rs1Val = Converter.convertToX(rs1.reg) << 15;  // rs1フィールドは15ビットシフト
			immVal = imm.imm << 20;  // 即値(imm)は20ビットシフト
			func7Val = 0b0000000 << 25;  // func7フィールドは25ビットシフト
			func3Val = 0b000 << 12;  // func3フィールドは12ビットシフト
	
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b1100111;  // 各フィールドをビットOR
			
			data.MEMput(all);
			
		} else if(ret) {
			//jalr
			rdVal = 0 << 7;  // rdフィールドは7ビットシフト
			rs1Val = 1 << 15;  // rs1フィールドは15ビットシフト
			immVal = 0 << 20;  // 即値(imm)は20ビットシフト
			func7Val = 0b0000000 << 25;  // func7フィールドは25ビットシフト
			func3Val = 0b000 << 12;  // func3フィールドは12ビットシフト
	
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b1100111;  // 各フィールドをビットOR
			
			data.MEMput(all);
			
		} else if(call) {		
			//auipcの処理
			rdVal = 6 << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
			immVal = imm.imm & 0xFFFFF000;                 // 32ビットの即値の上位20bitを取って配置 (12-31ビット目)

			all = rdVal | immVal | 0b0010111;

			data.MEMput(all);
			
			//jalr
			rdVal = 1 << 7;  // rdフィールドは7ビットシフト
			rs1Val = 6 << 15;  // rs1フィールドは15ビットシフト
			immVal = (imm.imm & 0xFFF) << 20;  // 即値(imm)は20ビットシフト
			func7Val = 0b0000000 << 25;  // func7フィールドは25ビットシフト
			func3Val = 0b000 << 12;  // func3フィールドは12ビットシフト
	
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b1100111;  // 各フィールドをビットOR
			
			data.MEMput(all);
		} else if(tail) {		
			//auipcの処理
			rdVal = 6 << 7;    // rdフィールドを7ビットシフトして配置 (7-11ビット目)
			immVal = imm.imm & 0xFFFFF000;                 // 32ビットの即値の上位20bitを取って配置 (12-31ビット目)

			all = rdVal | immVal | 0b0010111;

			data.MEMput(all);
			
			//jalr
			rdVal = 0 << 7;  // rdフィールドは7ビットシフト
			rs1Val = 6 << 15;  // rs1フィールドは15ビットシフト
			immVal = (imm.imm & 0xFFF) << 20;  // 即値(imm)は20ビットシフト
			func7Val = 0b0000000 << 25;  // func7フィールドは25ビットシフト
			func3Val = 0b000 << 12;  // func3フィールドは12ビットシフト
	
			all = rdVal | rs1Val | immVal | func7Val | func3Val | 0b1100111;  // 各フィールドをビットOR
			
			data.MEMput(all);
		}
	}
}


//(5)  R_type => [or and xor add sub srl sra sll slt sltu] Reg Reg Reg
class M_type{

	private byte opcode; //オペコードを格納する変数.
	private byte func7; //Func7を格納する変数.
	private byte func3; //Func3を格納する変数.
	
	private int all; //4byteの機械語を格納する変数.
	
	private Reg rd,rs1,rs2; //それぞれrd, rs1, rs2を保持するオブジェクト.
	
	private int nowLine;
	
	public void parse(Context ct,Data_box data)   {
		
		//命令によって各変数に値を格納.
		if (ct.match("mul")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b000;
		    
		} else if (ct.match("mulh")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b001;
		    
		} else if (ct.match("mulhsu")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b010;
		    
		} else if (ct.match("mulhu")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b011;
		    
		} else if (ct.match("div")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b100;
		    
		} else if (ct.match("divu")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b101;
		    
		} else if (ct.match("rem")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b110;
		    
		} else if (ct.match("remu")) {
		    opcode = 0b0110011;
		    func7  = 0b0000001;  // Updated func7
		    func3  = 0b111;
		}
		
		nowLine = ct.lineNumber;
		
		ct.toNext(); //次に進める.
		
		//rd, rs1, rs2に各値を保持.
		rd = new Reg();
		if(rd.parse(ct, data) != true) return ;
		
		if(nowLine != ct.lineNumber) {
			return ;
		}
		
		rs1 = new Reg();
		if(rs1.parse(ct, data) != true) return ;
		
		rs2 = new Reg();
		if(rs2.parse(ct, data) != true) return ;
		
	}
	
	public void exe(Data_box data) {

		//論理演算を利用してallに機械語を代入.
		//ConverterクラスのconbertToXメソッドはt0やa0などの形式からx10やx7などのx表記に変換する.
		int rdVal = Converter.convertToX(rd.reg) << 7;
		int rs1Val = Converter.convertToX(rs1.reg) << 15;
		int rs2Val = Converter.convertToX(rs2.reg) << 20;
		int func7Val = func7 << 25;
		int func3Val = func3 << 12;

		all = rdVal | rs1Val | rs2Val | func7Val | func3Val | opcode;

		data.MEMput(all);
	}
}


//(12) Imm => [0-9]+ | [a-zA-Z]+
class Imm{
	int imm; //数字を格納する変数.
	float f;
	double d;
	int fimm;
	long dimm;
	String letter = null; //ラベルを格納する変数.
	
	public boolean parse(Context ct,Data_box data) {
		
		// 整数値ならimmに格納.
		if (ct.match("[+-]?[0-9]+")) {
			imm =Integer.parseInt(ct.currentToken());
			
		// 少数なら
		} else if (ct.match("[+-]?[0-9]*\\.[0-9]+")) {
		    d = Double.parseDouble(ct.currentToken());
		    f = Float.parseFloat(ct.currentToken());
		    
		    fimm=Float.floatToIntBits(f);
		    dimm=Double.doubleToLongBits(f);

		// 0xからはじまる16進数なら10進数に変換してimmに代入.
		} else if (ct.match("0x[0-9A-Fa-f]+")) {
			imm = (int) Long.parseLong(ct.currentToken().substring(2), 16); // 符号付きの範囲にキャスト

			
		// 0bからはじまる2進数なら10進数に変換してimmに代入.
		} else if (ct.match("0b[0-1]+")) {
			imm =Integer.parseInt(ct.currentToken().substring(2), 2);
			
		// アルファベットならラベルなのでletterに代入
		} else if(ct.match("^[A-Za-z0-9_]+$")) {
			letter = ct.currentToken();
			
			
			
		// アルファベットならラベルなのでletterに代入
		} else { 
      	data.err.add("(" + ct.currentLine() + "行目): error C5 : 数字もしくはラベルが来る場所に数字もしくはラベルがありません。 : "+ ct.currentToken());
      	ct.toNextLine();
      	return false;
      	
      }
		
		ct.toNext(); //次に進める.
		
		return true;
		
	}
}


//(13) Reg => x0 | ra | sp| t[0-6] | a[0-6] | s([0-9] | 1[0-1]) | x([0-9] | 1[0-9] | 2[0-9] | 3[0-1])
class Reg{
	String reg;
	
	public boolean parse(Context ct,Data_box data)   {
		
		if (ct.match("zero|ra|sp|t[0-6]|a[0-7]|s[0-9]|s1[0-1]")) {
		    reg = ct.currentToken();
		    
		    // x0-x31のレジスタ範囲を判定
		} else if (ct.match("x([0-9]|1[0-9]|2[0-9]|3[0-1])")) {
		    reg = ct.currentToken();
		    
		    // エラー処理
		} else if (ct.match("f([0-9]|1[0-9]|2[0-9]|3[0-1])")) {
		    reg = ct.currentToken();
		    
		    // エラー処理
		} else if (ct.match("ft[0-9]|fa[0-7]|fs[0-9]|fs1[0-1]|ft1[0-1]")) {
		    reg = ct.currentToken();
		    
		    // エラー処理
		} else { 
      	data.err.add("(" + ct.currentLine() + "行目): error C4 : レジスタが来る場所にレジスタがありません。 : "+ ct.currentToken());
      	ct.toNextLine();
      	System.out.println("errの時だよ　"+ct.currentToken());
      	return false;
      }
		
		ct.toNext(); //次に進める.
		return true;
	}

	//Regクラスはレジスタを保存するためのクラスなのでexe()は存在しない.
}


//(14) label => ([a-zA-Z]+)\\s*:
class Label{
	public void parce(Context ct,Data_box data){
		if(data.getData(ct.currentToken()) == -1) {
			data.addLabel(ct.currentToken());
				
		} else {
			data.err.add("(" + ct.currentLine() + "行目): error C9 : ラベルが重複しています。 : "+ ct.currentToken());
      	
		}
		ct.toNext();
	}
}
