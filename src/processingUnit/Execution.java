package processingUnit;

public class Execution {

	public int finalPc;
	public int count;
	private boolean endFlag;
	
	Data_box data;

	
	public Execution(Data_box Data){
		data = Data;
        
        endFlag = false;
        
        count = 0;
	}
	
    // 定数定義
    public static final long MSTATUS = 0x300;
    public static final long MEDELEG = 0x302;
    public static final long MIDELEG = 0x303;
    public static final long MTVEC   = 0x305;
    public static final long MEPC    = 0x341;
    public static final long MCAUSE  = 0x342;
    public static final long MTVAL   = 0x343;
    public static final long SSTATUS = 0x100;
    public static final long SEDELEG = 0x102;
    public static final long SIDELEG = 0x103;
    public static final long STVEC   = 0x105;
    public static final long SEPC    = 0x141;
    public static final long SCAUSE  = 0x142;
    public static final long STVAL   = 0x143;
    public static final long USTATUS = 0x000;
    public static final long UTVEC   = 0x005;
    public static final long UEPC    = 0x041;
    public static final long UCAUSE  = 0x042;
    public static final long UTVAL   = 0x043;
    public static final long FFLAGS  = 0x001;
    public static final long FRM     = 0x002;
    public static final long FCSR    = 0x003;

	
	public int exe(int pc , int type) {
		
	    int IR;         					// 関数内呼び出し用

		int opcode;							// オペレーションコード

	    int funct7;							// function 7bit
	    int funct3;							// function 3bit

	    int shamt;							// シフト定数

	    int rs3;							// 第3レジスタ
	    int rs2;							// 第2レジスタ
	    int rs1;							// 第1レジスタ
	    int rd;								// 第3レジスタ

	    int imm;        					// 即値
		
		IR  = data.MEM[pc]<<24 | data.MEM[pc+1]<<16 | data.MEM[pc+2]<<8 | data.MEM[pc+3];   //opcodeに4byte合成
		opcode	= cob(IR,6,0);		// opcode の抽出
        funct3  = cob(IR,14,12);	// funct3 の抽出
        funct7  = cob(IR,31,25);	// funct7 の抽出
        
        
        
        // IRが0であるのはエラー
        if(IR == 0) {
        	finalPc = pc;
        	return pc;
        }
        
        //ステップ数が想定外の大きさ、要は無限ループしているときにやめさせる処理
        if(count == 10000000) {
        	data.err.add("error E2 : ステップ数が10,000,000回に到達しました。");
        	finalPc = pc;
        	return pc;
        }
        
        // System.out.printf("\nopcode : %x\n\n",opcode);

        
        switch (opcode) // opcodeの値で分岐
		{
        
        
		case 0b0110011:	// add sub sll slt sltu xor srl sra or and [M拡張] mul mulh mulhsu mulhu div divu rem remu

			rs2	= cob(IR,24,20);
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);
			if (funct7 == 0b0000001) { // [M拡張] mul mulh mulhsu mulhu div divu rem remu
		        switch (funct3) {
		            case 0b000: // mul
		                data.x[rd] = (int) ((long) data.x[rs1] * data.x[rs2]);
		                pc += 4;
		                break;

		            case 0b001: // mulh
		                data.x[rd] = (int) (((long) data.x[rs1] * data.x[rs2]) >> 32);
		                pc += 4;
		                break;

		            case 0b010: // mulhsu
		                data.x[rd] = (int) (((long) data.x[rs1] * Integer.toUnsignedLong(data.x[rs2])) >> 32);
		                pc += 4;
		                break;

		            case 0b011: // mulhu
		            	data.x[rd] = (int) ((((long) Integer.toUnsignedLong(data.x[rs1]) * (long) Integer.toUnsignedLong(data.x[rs2])) >>> 32) & 0xFFFFFFFFL);
		                pc += 4;
		                break;

		            case 0b100: // div
		                if (data.x[rs2] == 0) {
		                    // System.out.println("Error: Division by zero");
		                    return -1; // エラー終了
		                }
		                data.x[rd] = data.x[rs1] / data.x[rs2];
		                pc += 4;
		                break;

		            case 0b101: // divu
		                if (data.x[rs2] == 0) {
		                    // System.out.println("Error: Division by zero");
		                    return -1; // エラー終了
		                }
		                data.x[rd] = (int) (Integer.toUnsignedLong(data.x[rs1]) / Integer.toUnsignedLong(data.x[rs2]));
		                pc += 4;
		                break;

		            case 0b110: // rem
		                if (data.x[rs2] == 0) {
		                    // System.out.println("Error: Division by zero");
		                    return -1; // エラー終了
		                }
		                data.x[rd] = data.x[rs1] % data.x[rs2];
		                pc += 4;
		                break;

		            case 0b111: // remu
		                if (data.x[rs2] == 0) {
		                    // System.out.println("Error: Division by zero");
		                    return -1; // エラー終了
		                }
		                data.x[rd] = (int) (Integer.toUnsignedLong(data.x[rs1]) % Integer.toUnsignedLong(data.x[rs2]));
		                pc += 4;
		                break;

		            default:
		                // 不明なfunct3の場合、エラーを処理するか無視する
		                // System.out.println("Error: Unsupported funct3 value");
		                return -1;
		        }
		        
		    } else { // add sub sll slt sltu xor srl sra or and
		    	
		    	switch (funct3)	// funct3の値で分岐
				{
				case 0b000:	// add or sub
					if(funct7 == 0b0000000)	// add : Add
					{
						data.x[rd] = data.x[rs1] + data.x[rs2];
					}
					else if(funct7 == 0b0100000) //sub : Subtraction
					{
						data.x[rd] = data.x[rs1] - data.x[rs2];
					} else 	// error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					pc+=4;
					break;
				
				case 0b001:	// sll
					if(funct7 == 0b0000000)	// sll : Shift Left Logical
					{
						data.x[rd] = data.x[rs1] << cob(data.x[rs2],4,0);
					}
					else // error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}

					pc+=4;
					break;
				
				case 0b010:	// slt
					if(funct7 == 0b0000000)	// slt : Set on Less Than
					{
						if(data.x[rs1] > data.x[rs2])
						{
							data.x[rd] = 0;
						}
						else
						{
							data.x[rd] = 1;
						}
					}
					else // error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					
					pc+=4;
					break;
				
				case 0b011:	// sltu
					if(funct7 == 0b0000000)	// slt : Set on Less Than Unsign
					{
						if(Integer.toUnsignedLong(data.x[rs1]) > Integer.toUnsignedLong(data.x[rs2]))
						{
							data.x[rd] = 0;
						}
						else
						{
							data.x[rd] = 1;
						}
					}
					else // error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					
					pc+=4;
					break;
				
				case 0b100:	// xor
					if(funct7 == 0b0000000)	// xor : Exclusive Or
					{
						data.x[rd] = data.x[rs1] ^ data.x[rs2];
					}
					else // error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					data.x[rd] = data.x[rs1] ^ data.x[rs2];
					pc+=4;
					break;
				
				case 0b101:	// srl or sra
					if(funct7 == 0b0000000)	// srl : Shift Right Logical
					{
						data.x[rd] = data.x[rs1] >> cob(data.x[rs2],4,0);
					}
					else if(funct7 == 0b0100000)// sra : Shift Right Arithmetic
					{
						data.x[rd] = (data.x[rs1] >> cob(data.x[rs2],4,0)) | cob(data.x[rs1],31,31-cob(data.x[rs2],4,0)) << 31;
					}
					else // error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					
					pc+=4;
					break;
				
				case 0b110:	// or
					if(funct7 == 0b0000000)	// or : Or
					{
						data.x[rd] = data.x[rs1] | data.x[rs2];
					}
					else 							// error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					
					pc+=4;
					break;
				
				case 0b111:	// and
					if(funct7 == 0b0000000)	// and : And
					{
						data.x[rd] = data.x[rs1] & data.x[rs2];
					}
					else 							// error
					{
						// System.out.printf("execution error(%04x) : op:0110111\n",pc);
						return -1;
					}
					
					pc+=4;
					break;

				default:	// 例外処理
					// System.out.printf("\n%x : error(%x) ",pc,IR);
					return -1;
				}
		    }

			break;
			
		case 0b1010011:	// [F拡張] 

			 
			rs2	= cob(IR,24,20);
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);
			
			switch(funct7) 
			{
			case 0b0000000: // fadd.s
				data.fx[rd] = Converter.floatToLong(Converter.longToFloat(data.fx[rs1]) + Converter.longToFloat(data.fx[rs2]));
				break;
				
			case 0b0000100: // fsub.s
				data.fx[rd] = Converter.floatToLong(Converter.longToFloat(data.fx[rs1]) - Converter.longToFloat(data.fx[rs2]));
				break;
			
			case 0b0001000: // fmul.s
				data.fx[rd] = Converter.floatToLong(Converter.longToFloat(data.fx[rs1]) * Converter.longToFloat(data.fx[rs2]));
				break;
			
			case 0b0001100: // fdiv.s
				data.fx[rd] = Converter.floatToLong(Converter.longToFloat(data.fx[rs1]) / Converter.longToFloat(data.fx[rs2]));
				break;
				
			case 0b0101100: // fsqrt.s
				data.fx[rd] = Converter.floatToLong( (float) Math.sqrt(Converter.longToFloat(data.fx[rs1])) );
				break;
			
			case 0b0010000: // fsgnj.s fsgnj.s fsgnjn.s
				switch(funct3)
				{
				case 0b000: // fsgnj.s
					data.fx[rd] = Converter.floatToLong((data.fx[rs2] & 0x80000000) | (data.fx[rs1] & 0x7FFFFFFF));
					break;
					
				case 0b001: // fsgnj.s 
					data.fx[rd] = Converter.floatToLong((~data.fx[rs2] & 0x80000000) | (data.fx[rs1] & 0x7FFFFFFF));
					break;
				
				case 0b010: // fsgnjn.s
					data.fx[rd] = Converter.floatToLong(((data.fx[rs2] ^data.fx[rs1])& 0x80000000) | (data.fx[rs1] & 0x7FFFFFFF));
					break;
				
				default:
					break;
			
				}
				
				break;
			
			case 0b0010100: //fmin.s fmax.s
				switch(funct3)
				{
				case 0b000: // fmin.s
					if(Converter.longToFloat(data.fx[rs1]) > Converter.longToFloat(data.fx[rs2])) {
						data.fx[rd] = data.fx[rs2];
						
					} else {
						data.fx[rd] = data.fx[rs1];
						
					}
					
					break;
					
				case 0b001: // fmin.s fmax.s 
					if(Converter.longToFloat(data.fx[rs1]) < Converter.longToFloat(data.fx[rs2])) {
						data.fx[rd] = data.fx[rs2];
						
					} else {
						data.fx[rd] = data.fx[rs1];
						
					}
					
					break;
				
				default:
					break;
			
				}
				
				break;
				
			case 0b1100000: // fcvt.w.s fcvt.wu.s 
				switch(rs2)
				{
				case 0b000: // fcvt.w.s
					data.fx[rd] = Converter.floatToLong((float)data.x[rs1]); 
					break;
					
				case 0b001: // fcvt.wu.s
					data.fx[rd] = Converter.floatToLong((float)data.x[rs1]); 
					break;
				
				default:
					break;
			
				}
				break;
			
			case 0b1110000: // fmv.x.w fclass.s
				switch(funct3)
				{
				case 0b000: // fmv.x.w
					data.fx[rd] = data.x[rs1] & 0xFFFFFFFF;
					break;
					
				case 0b001: // fclass.s
					
					break;
					
				default:
					break;
				}
				
				break;
			
			case 0b1010000: // feq.s flt.s fle.s
				switch(funct3)
				{
				case 0b000: // fle.s
					if(Converter.longToFloat(data.fx[rs1]) <= Converter.longToFloat(data.fx[rs2])) {
						data.x[rd] = 1;
						
					} else {
						data.x[rd] = 0;
						
					}
					break;
					
				case 0b001: // flt.s
					if(Converter.longToFloat(data.fx[rs1]) < Converter.longToFloat(data.fx[rs2])) {
						data.x[rd] = 1;
						
					} else {
						data.x[rd] = 0;
						
					}
					break;
				
				case 0b010: // feq.s
					if(Converter.longToFloat(data.fx[rs1]) == Converter.longToFloat(data.fx[rs2])) {
						data.x[rd] = 1;
						
					} else {
						data.x[rd] = 0;
						
					}
					break;
				
				default:
					break;
			
				}
				
				break;
				
			case 0b1101000: // fcvt.s.w fcvt.s.wu
				switch(rs2)
				{
				case 0b000: // fcvt.s.w
					data.fx[rd] = Converter.floatToLong((float)data.x[rs1]); 
					break;
					
				case 0b001: // fcvt.s.wu
					data.fx[rd] = Converter.floatToLong((float)data.x[rs1]); 
					break;
				
				default:
					break;
			
				}
				
				break;
			
			case 0b1111000: // fmv.w.x
				data.fx[rd] = data.x[rs1] & 0xFFFFFFFF;
				break;
				
				
				
				
				
				
				
			case 0b0000001: // fadd.d
			    data.fx[rd] = Converter.doubleToLong(Converter.longToDouble(data.fx[rs1]) + Converter.longToDouble(data.fx[rs2]));
			    break;

			case 0b0000101: // fsub.d
			    data.fx[rd] = Converter.doubleToLong(Converter.longToDouble(data.fx[rs1]) - Converter.longToDouble(data.fx[rs2]));
			    break;

			case 0b0001001: // fmul.d
			    data.fx[rd] = Converter.doubleToLong(Converter.longToDouble(data.fx[rs1]) * Converter.longToDouble(data.fx[rs2]));
			    break;

			case 0b0001101: // fdiv.d
			    data.fx[rd] = Converter.doubleToLong(Converter.longToDouble(data.fx[rs1]) / Converter.longToDouble(data.fx[rs2]));
			    break;

			case 0b0101101: // fsqrt.d
			    data.fx[rd] = Converter.doubleToLong(Math.sqrt(Converter.longToDouble(data.fx[rs1])));
			    break;

			case 0b0010001: // fsgnj.d, fsgnjn.d, fsgnjx.d
			    switch (funct3) {
			        case 0b000: // fsgnj.d
			            data.fx[rd] = (data.fx[rs2] & 0x8000000000000000L) | (data.fx[rs1] & 0x7FFFFFFFFFFFFFFFL);
			            break;
			        case 0b001: // fsgnjn.d
			            data.fx[rd] = (~data.fx[rs2] & 0x8000000000000000L) | (data.fx[rs1] & 0x7FFFFFFFFFFFFFFFL);
			            break;
			        case 0b010: // fsgnjx.d
			            data.fx[rd] = ((data.fx[rs2] ^ data.fx[rs1]) & 0x8000000000000000L) | (data.fx[rs1] & 0x7FFFFFFFFFFFFFFFL);
			            break;
			    }
			    break;

			case 0b0010101: // fmin.d, fmax.d
			    switch (funct3) {
			        case 0b000: // fmin.d
			            data.fx[rd] = Converter.doubleToLong(
			                Math.min(Converter.longToDouble(data.fx[rs1]), Converter.longToDouble(data.fx[rs2])));
			            break;
			        case 0b001: // fmax.d
			            data.fx[rd] = Converter.doubleToLong(
			                Math.max(Converter.longToDouble(data.fx[rs1]), Converter.longToDouble(data.fx[rs2])));
			            break;
			    }
			    break;

			case 0b1010001: // feq.d, flt.d, fle.d
			    switch (funct3) {
			        case 0b000: // fle.d
			            data.x[rd] = (Converter.longToDouble(data.fx[rs1]) <= Converter.longToDouble(data.fx[rs2])) ? 1 : 0;
			            break;
			        case 0b001: // flt.d
			            data.x[rd] = (Converter.longToDouble(data.fx[rs1]) < Converter.longToDouble(data.fx[rs2])) ? 1 : 0;
			            break;
			        case 0b010: // feq.d
			            data.x[rd] = (Converter.longToDouble(data.fx[rs1]) == Converter.longToDouble(data.fx[rs2])) ? 1 : 0;
			            break;
			    }
			    break;

			case 0b1100001: // fcvt.w.d, fcvt.wu.d
			    switch (rs2) {
			        case 0b000: // fcvt.w.d
			            data.x[rd] = (int) Converter.longToDouble(data.fx[rs1]);
			            break;
			        case 0b001: // fcvt.wu.d
			            data.x[rd] = (int) Converter.longToDouble(data.fx[rs1]);
			            break;
			    }
			    break;

			case 0b1101001: // fcvt.d.w, fcvt.d.wu
			    switch (rs2) {
			        case 0b000: // fcvt.d.w
			            data.fx[rd] = Converter.doubleToLong((double) data.x[rs1]);
			            break;
			        case 0b001: // fcvt.d.wu
			            data.fx[rd] = Converter.doubleToLong((double) data.x[rs1]);
			            break;
			    }
			    break;

			case 0b1111001: // fmv.d.x
			    data.fx[rd] = data.x[rs1];
			    break;

			case 0b1110001: // fmv.x.d
			    data.x[rd] = (int) (data.fx[rs1] & 0xFFFFFFFFL);
			    break;

			default:
				break;
			
			}
			
			pc+=4;
			break;
		    	
		case 0b0010011: // slli srli srai addi slti sltiu xori ori andi
			
			// H-type <slli srli srai>
			shamt	= cob(IR,24,20);

			// I-Type <addi slti sltiu xori ori andi>
			imm		= cob(IR,31,20);
			
			// H and I
			rs1		= cob(IR,19,15);
			rd		= cob(IR,11,7);


			switch (funct3) // funct3の値で分岐
			{
			case 0b001:	// slli : Shift Left Logical Immediate
				data.x[rd] = data.x[rs1] << shamt;
								
				pc+=4;
				break;
			
			case 0b101:	// srli or srai
				if(funct7 == 0b0000000)	// srli : Shift Right Logical Immediate
				{
					data.x[rd] = data.x[rs1] >> shamt;
				}
				else if(funct7 == 0b0100000)    // srai : Shift Right Arithmetic Immediate
				{
					data.x[rd] = (data.x[rs1] >> shamt) | (cob(data.x[rs1],31,31) << 31);
				}
				pc+=4;
				break;
			
			case 0b000:	// addi : Add Immediate
				data.x[rd] = data.x[rs1] + cc(imm,12);
				pc+=4;
				break;
			
			case 0b010:	// slti : Set if Less Than Immediate
				if(data.x[rs1] > cc(imm,12))
				{
					data.x[rd] = 0;
				}
				else
				{
					data.x[rd] = 1;
				}

				pc+=4;
				break;
			
			case 0b011:	// sltiu : Set if Less Than Immediate Unsigned
				if(data.x[rs1] > imm)
				{
					data.x[rd] = 0;
				}
				else
				{
					data.x[rd] = 1;
				}
				pc+=4;
				break;

			case 0b100:	// xori : Exclusiive Or Immediate
				data.x[rd] = data.x[rs1] ^ cc(imm,12);
				pc+=4;
				break;
			
			case 0b110:	// ori : Or Immediate
				data.x[rd] = data.x[rs1] | cc(imm,12);
				pc+=4;
				break;
			
			case 0b111:	// andi : And Immediate
				data.x[rd] = data.x[rs1] & cc(imm,12);				
				pc+=4;
				break;
			
			default:
				break;
			}

			break;

		case 0b1100111: // jalr

			// I-Type <jalr>
			imm	= cob(IR,31,20);
			rs1	= cob(IR,19,15);	
			rd	= cob(IR,11,7);

			switch (funct3)	// funct3の値で分岐
			{
			case 0b000:	// jalr : Jump and Link Register
				data.x[rd] = pc+4;
				pc	  = (data.x[rs1] + cc(imm,21)) & (0xfffffff0 | 0b100);
				break;
			
			default:
				break;
			}

			break;
		
		case 0b0000011: // lb lh lw lbu lhu

			// I-Type <lw>
			imm	= cob(IR,31,20);
			rs1	= cob(IR,19,15);	
			rd	= cob(IR,11,7);

			switch (funct3)	// funct3の値で分岐
			{
			case 0b000:	// lb : Load Byte
				data.x[rd] = data.MEM[data.x[rs1] + cc(imm,12)] << 24 | data.MEM[data.x[rs1] + cc(imm,12)+1] << 16 | data.MEM[data.x[rs1] + cc(imm,12)+2] << 8 | data.MEM[data.x[rs1] + cc(imm,12)+3] ;
				data.x[rd] = (byte)(data.x[rd] & 0xFF);
				
				pc+=4;
				break;
	
			case 0b001:	// lh : Load Halfword
				data.x[rd] = data.MEM[data.x[rs1] + cc(imm,12)] << 24 | data.MEM[data.x[rs1] + cc(imm,12)+1] << 16 | data.MEM[data.x[rs1] + cc(imm,12)+2] << 8 | data.MEM[data.x[rs1] + cc(imm,12)+3] ;
				data.x[rd] = (short)(data.x[rd] & 0xFFFF);
				
				pc+=4;
				break;

			case 0b010:	// lw : Load Word
				data.x[rd] = data.MEM[data.x[rs1] + cc(imm,12)] << 24 | data.MEM[data.x[rs1] + cc(imm,12)+1] << 16 | data.MEM[data.x[rs1] + cc(imm,12)+2] << 8 | data.MEM[data.x[rs1] + cc(imm,12)+3] ;
				
				pc+=4;
				break;

			case 0b100:	// lbu : Load Byte Unsigned
				data.x[rd] = cob(data.MEM[data.x[rs1] + cc(imm,12)],7,0);
				pc+=4;
				break;

			case 0b101:	// lhu : Load Halfword Unsinged
				data.x[rd] = cob(data.MEM[data.x[rs1] + cc(imm,12)],15,0);
				pc+=4;
				break;

			default:
				break;
			}

			break;
			
		case 0b0000111: // [F拡張] flw

			// I-Type <lw>
			imm	= cob(IR,31,20);
			rs1	= cob(IR,19,15);	
			rd	= cob(IR,11,7);

			switch (funct3)	// funct3の値で分岐
			{
			case 0b010:	// flw : float Load Word
				data.fx[rd] = data.MEM[data.x[rs1] + cc(imm,12)] << 24 | data.MEM[data.x[rs1] + cc(imm,12)+1] << 16 | data.MEM[data.x[rs1] + cc(imm,12)+2] << 8 | data.MEM[data.x[rs1] + cc(imm,12)+3] ;
				pc+=4;
				break;
				
			case 0b011:	// fld : float Load Double
				data.fx[rd] = ((long) data.MEM[data.x[rs1] + cc(imm, 12)] & 0xFF) << 56 |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 1] & 0xFF) << 48 |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 2] & 0xFF) << 40 |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 3] & 0xFF) << 32 |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 4] & 0xFF) << 24 |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 5] & 0xFF) << 16 |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 6] & 0xFF) << 8  |
	                      ((long) data.MEM[data.x[rs1] + cc(imm, 12) + 7] & 0xFF);
				
				pc+=4;
				break;

			default:
				break;
			}

			break;
		
		case 0b0100011: // sb sh sw

			// S-Type <lw>
			imm	= (cob(IR,31,25) << 5) | cob(IR,11,7);
			rs2 = cob(IR,24,20);
			rs1	= cob(IR,19,15);	
			rd	= cob(IR,11,7);

			switch (funct3)	// funct3の値で分岐
			{
			case 0b000:	// sb : Store Byte
				data.MEM[data.x[rs1] + cc(imm,12)] = data.x[rs2] & 0xff;
				pc+=4;
				break;
			
			case 0b001:	// sh : Store Halfword
				data.MEM[data.x[rs1] + cc(imm,12)] = (data.x[rs2] & 0xff00) >>> 8;
				data.MEM[data.x[rs1] + cc(imm,12) + 1] = data.x[rs2] & 0x00ff;
				pc+=4;
				break;
			
			case 0b010:	// sw : Store Word
				data.MEM[data.x[rs1] + cc(imm,12)] = (data.x[rs2] & 0xFF000000) >>> 24;
				data.MEM[data.x[rs1] + cc(imm,12) + 1] = (data.x[rs2] & 0x00FF0000) >>> 16;
				data.MEM[data.x[rs1] + cc(imm,12) + 2] = (data.x[rs2] & 0x0000FF00) >>> 8;
				data.MEM[data.x[rs1] + cc(imm,12) + 3] = data.x[rs2] & 0x000000FF;
				pc+=4;
				break;
			
			default:
				break;
			}

			break;
			
		case 0b0100111: // [F拡張] fsw

			// S-Type <lw>
			imm	= (cob(IR,31,25) << 4) + cob(IR,11,7);
			rs2 = cob(IR,24,20);
			rs1	= cob(IR,19,15);	
			rd	= cob(IR,11,7);

			switch (funct3)	// funct3の値で分岐
			{
			case 0b010:							// fsw : float Store Word
				// アドレス計算
				int baseAddress = (int)(data.x[rs1] + cc(imm, 12)); // メモリアドレス計算
				long lower32Bits = data.fx[rs2] & 0xFFFFFFFFL;      // 下位32ビットを抽出

			    // 下位32ビットを分割して格納
			    data.MEM[baseAddress]     = (int)((lower32Bits >> 24) & 0xFF); // 最上位8ビット
			    data.MEM[baseAddress + 1] = (int)((lower32Bits >> 16) & 0xFF); // 次の8ビット
			    data.MEM[baseAddress + 2] = (int)((lower32Bits >> 8) & 0xFF);  // 次の8ビット
			    data.MEM[baseAddress + 3] = (int)(lower32Bits & 0xFF);         // 最下位8ビット
			
			
				pc+=4;
				// System.out.printf("\n\ndataは%x\nrs1は%x\nrs2は%x\n\n",data.MEM[data.x[rs1] + cc(imm,12)],data.x[rs1],data.x[rs2]);
				break;
			
			case 0b011: // fsd : Double Store Word
			    // アドレス計算
			    int baseAddressD = (int) (data.x[rs1] + cc(imm, 12)); // メモリアドレス計算
			    long doubleBits = data.fx[rs2]; // fx[rs2] には double のビット列 (64ビット) が格納されている

			    // 64ビットを分割してメモリに格納
			    data.MEM[baseAddressD    ] = (int) ((doubleBits >> 56) & 0xFF); // 最上位8ビット
			    data.MEM[baseAddressD + 1] = (int) ((doubleBits >> 48) & 0xFF); // 次の8ビット
			    data.MEM[baseAddressD + 2] = (int) ((doubleBits >> 40) & 0xFF); // 次の8ビット
			    data.MEM[baseAddressD + 3] = (int) ((doubleBits >> 32) & 0xFF); // 次の8ビット
			    data.MEM[baseAddressD + 4] = (int) ((doubleBits >> 24) & 0xFF); // 次の8ビット
			    data.MEM[baseAddressD + 5] = (int) ((doubleBits >> 16) & 0xFF); // 次の8ビット
			    data.MEM[baseAddressD + 6] = (int) ((doubleBits >> 8) & 0xFF);  // 次の8ビット
			    data.MEM[baseAddressD + 7] = (int) (doubleBits & 0xFF);         // 最下位8ビット

			    pc += 4;
			    break;	
			default:
				break;
			}

			break;
		case 0b1100011: // beq bne blt bge bltu bgeu

			// B-Type <beq bne>
			imm	= (cob(IR,31,31) << 12) | (cob(IR,7,7) << 11) | (cob(IR,30,25) << 5) | (cob(IR,11,8)<<1);
			rs2 = cob(IR,24,20);
			rs1	= cob(IR,19,15);	

			switch (funct3)	// funct3の値で分岐
			{
			case 0b000:							// beq : Branch on Equal
				// System.out.println("x[rs1] : "+data.x[rs1]+"\nx[rs2] : "+ data.x[rs2]);
				if(data.x[rs1] == data.x[rs2])
				{
					pc = pc + cc(imm,13);
					
				}
				else
				{
					pc+=4;
				}
				break;
			
			case 0b001:	// bne : Branch on Equal
				if(data.x[rs1] != data.x[rs2])
				{
					pc = pc + cc(imm,13);

				}
				else
				{
					pc+=4;

				}

				break;
			
			case 0b100:	// blt : Branch if Less Than
				if(data.x[rs1] < data.x[rs2])
				{
					pc = pc + cc(imm,13);

				}
				else
				{
					pc+=4;

				}

				break;
			
			case 0b101:	// bge : Branch if Greater Than or Equal
				if(data.x[rs1] >= data.x[rs2])
				{
					pc = pc + cc(imm,13);

				}
				else
				{
					pc+=4;

				}

				break;
			
			case 0b110:	// bltu : Branch if Less Than, Unsinged
				if(Integer.toUnsignedLong(data.x[rs1]) < Integer.toUnsignedLong(data.x[rs2]))
				{
					pc = pc + cc(imm,13);

				}
				else
				{
					pc+=4;

				}

				break;
			
			case 0b111:// bgeu : Branch if Greater Than or Equal, Unsinged
				if(Integer.toUnsignedLong(data.x[rs1]) >= Integer.toUnsignedLong(data.x[rs2]))
				{
					pc = pc + cc(imm,13);

				}
				else
				{
					pc+=4;

				}

				break;
			
			default:
				break;
			}

			break;

		case 0b1101111: // jal : Jump And Link

			// J-Type <lw>
			imm	= (cob(IR,31,31) << 20) | (cob(IR,19,12) << 12) | (cob(IR,20,20) << 11) | (cob(IR,30,21) << 1);	
			rd	= cob(IR,11,7);
			
			data.x[rd] = pc + 4;
			pc 	  = pc + cc(imm,20);

			break;
			

		case 0b0110111: // lui : Load Upper Immediate

			// U-Type <lw>
			imm	= (cob(IR,31,12) << 12);	
			rd	= cob(IR,11,7);

			data.x[rd] = imm;
			pc+=4;
			break;
		
		case 0b0010111: // AUIpc : Add Upper Immediate to pc

			// U-Type <lw>
			imm	= (cob(IR,31,12) << 12);	
			rd	= cob(IR,11,7);

			data.x[rd] = pc + imm;
			pc+=4;
			break;

		case 0b0001111: // fence fence.i

			// U-Type <lw>
			imm	= (cob(IR,31,12) << 11);	
			rd	= cob(IR,11,7);

			switch (funct3)
			{
			case 0b000:
				pc+=4;
				break;
			
			case 0b001:
				pc+=4;
				break;
			
			default:
				break;
			}

			break;

		case 0b1000011: // fmadd.s
			
			rs3	= cob(IR,31,27);
			rs2	= cob(IR,24,20);
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);

			data.fx[rd] = Converter.floatToLong(Converter.longToFloat(data.fx[rs1]) * Converter.longToFloat(data.fx[rs2]) + Converter.longToFloat(data.fx[rs3]));

			break;
		
		case 0b1000111: // fmsub.s
						
			rs3	= cob(IR,31,27);
			rs2	= cob(IR,24,20);
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);

			data.fx[rd] = Converter.floatToLong(Converter.longToFloat(data.fx[rs1]) * Converter.longToFloat(data.fx[rs2]) - Converter.longToFloat(data.fx[rs3]));

			break;
		
		case 0b1001011: // fnmsub.s
			
			rs3	= cob(IR,31,27);
			rs2	= cob(IR,24,20);
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);
		
			data.fx[rd] = Converter.floatToLong(-Converter.longToFloat(data.fx[rs1]) * Converter.longToFloat(data.fx[rs2]) + Converter.longToFloat(data.fx[rs3]));
		
			break;
		
		case 0b1001111: // fnmadd.s
			
			rs3	= cob(IR,31,27);
			rs2	= cob(IR,24,20);
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);
		
			data.fx[rd] = Converter.floatToLong(-Converter.longToFloat(data.fx[rs1]) * Converter.longToFloat(data.fx[rs2]) - Converter.longToFloat(data.fx[rs3]));
		
			break;
		
		case 0b1110011: // ecall ebreak csrrw csrrs csrrc csrrwi csrrsi csrrci
			rs1	= cob(IR,19,15);
			rd	= cob(IR,11,7);
			
			switch (funct3)
			{
			case 0b000: // ecall or ebreak
				
				switch (data.x[10]) 
				{
				case 1: // print_int
					// a1 レジスタに格納された整数を出力します。
					data.print_int = true;
					
					break;
					
				case 4: // print_string
					// a1 レジスタに格納されたヌル終端文字列のアドレスを出力します.
					data.print_string = true;
					
					break;
					
				case 9: // sbrk 
					// ヒープ領域を a1 バイト分割り当て、ヒープの先頭アドレスを a0 に返します。これは動的メモリの確保を行います.
				
					break;
				
				case 10: // exit
					// プログラムの終了.
					// System.out.println("プログラムの終了");
					endFlag = true;
					break;
				
				case 11: // print_character
					// a1 レジスタに格納されたASCII文字を出力します.
					data.print_char = true;
					
					break;
				
				case 13: // openFile
					// 仮想ファイルシステム (VFS) 内のファイルを開きます.
					// a1 はファイルパスのアドレス、a2 はアクセスモードを示すビットです.
					// ファイルディスクリプタを a0 に返します.
					
					break;
				
				case 14: // readFile
					// 説明: a1 にファイルディスクリプタ、a2 に読み込んだデータを格納する配列のアドレス、a3 に読み取りバイト数を指定します.
					// a0 に実際に読み取ったバイト数を返します。
					
					break;
					
				case 15: // writeFile
					// a1 にファイルディスクリプタ、a2 に書き込むデータのバッファ、a3 に書き込むバイト数、a4 に各項目のサイズを指定します.
					// a0 に実際に書き込んだバイト数を返します.
					
					break;
				
				case 16: // closeFile
					// a1 にファイルディスクリプタを指定し、ファイルを閉じます.
					// 成功時には 0 を、失敗時には EOF (-1) を返します.
	
					break;
				
				case 17: // exit2
					// a1 に終了コードを指定してプログラムを終了します.
					endFlag = true;
					data.print_int = true;
					break;
					
				case 18: // fflush
					// a1 にファイルディスクリプタを指定してバッファをフラッシュします.
					
					break;
				
				case 19: // feof
					// a1 にファイルディスクリプタを指定し、ファイルの終わりに達したかを判定します。
					// 終端に達している場合は非ゼロ値を返します。
					
					
					break;
				
				case 20: // ferror
					// a1 にファイルディスクリプタを指定し、エラーが発生しているかを判定します.
					// エラーがあれば非ゼロ値を返します.
					
					break;
					
				case 34: // printHex
					// a1 レジスタに格納された値を16進数として出力します.
					data.print_hex = true;
					
					break;
				
				case 0x3CC: // vlib
					// 仮想ライブラリに関連する関数群で、詳細はvlibのページを参照する必要があります.
					// これにより、より高度な機能を利用することができます。
					
					break;
					
				default:
					break;
				}
				
				pc+=4;
				break;
			
			case 0b001:							// csrrw : Control and Status Register Read and Write
				long t=data.CSR.read(MSTATUS);
				data.CSR.write(MSTATUS, data.x[rs1]);
				data.x[rd]=(int) t;
				pc+=4;
				break;
			
			case 0b010:							// csrrs : Control and Status Register Read and Set
				pc+=4;
				break;
			
			case 0b011:							// csrrc : Control and Status Register Read and Clear
				pc+=4;
				break;
			
			case 0b101:							// csrrwi : Control and Status Register Read and Write Immediate
				pc+=4;
				break;
			
			case 0b110:							// csrrsi : Control and Status Register Read and Set Immediate
				pc+=4;
				break;
			
			case 0b111:							// csrrci : Control and Status Register Read and Clear Immediate
				pc+=4;
				break;
			
			default:
				break;
			}
			
			break;
		
		}
       
        count++;
        
        if(endFlag == true) {
        	finalPc = pc;
        	return pc;
        }
        
        if(data.x[0] != 0) data.x[0]=0;
		
		return pc;
	}
	
	public static int cob(int str, int num1, int num2) { // Cap Out Bitの略
        int n = 0;

        // Num1がNum2より小さい場合のエラー処理
        if (num1 < num2) {
            // System.out.println("error1");
            return -1;
        }
        
        if(num1 > 31 || num2 > 31) {
        	// System.out.println("error2");
            return -1;
        }

        // 指定されたビット幅を切り取る
        for (int m = num1; m >= num2; m--) {
            n += 1 << m;
        }

        return (str & n) >> num2;
    }
	
	public static int cc(int a, int num) { // change complement
        switch (num) {
            case 20:
                if (a > 0x0fffff && a <= 0x1fffff) {
                    return -(a ^ 0x1fffff) - 1;
                } else {
                    return a;
                }

            case 13:
                if (a > 0x0fff && a <= 0x1fff) {
                    return -(a ^ 0x1fff) - 1;
                } else {
                    return a;
                }

            case 12:
                if (a > 0x7ff && a <= 0xfff) {
                    return -(a ^ 0xfff) - 1;
                } else {
                    return a;
                }

            default:
                // System.out.printf("a:%d num:%d error3",a,num);
                
                return -1;
        }
    }
	
	public void reset() {
		count=0;
		endFlag=false;
	}
}
