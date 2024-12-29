package processingUnit;

public class ChangeBaseInstruction {
	
	Data_box data;
	
	public ChangeBaseInstruction(Data_box Data){
		data = Data;
	}
	
	public int exe() {
	    int IR;         					// 関数内呼び出し用

		int opcode;							// オペレーションコード

	    int funct7;							// function 7bit
	    int funct3;							// function 3bit

	    int shamt;							// シフト定数

	    int rs2;							// 第2レジスタ
	    int rs1;							// 第1レジスタ
	    int rd;								// 第3レジスタ

	    int imm;        					// 即値

		int ERR_F = 0;						// エラーフラグ
		
		int c = 0;
		int pc = 0;
		
		
		while(ERR_F == 0) {
			IR  = data.MEM[pc]<<24 | data.MEM[pc+1]<<16 | data.MEM[pc+2]<<8 | data.MEM[pc+3];   //opcodeに4byte合成
			opcode	= cob(IR,6,0);		// opcode の抽出
			funct3  = cob(IR,14,12);	// funct3 の抽出
			funct7  = cob(IR,31,25);	// funct7 の抽出
			
			if(IR == 0) break; //命令が0
			
			switch (opcode) // opcodeの値で分岐
			{
			case 0b0110011:	// add sub sll slt sltu xor srl sra or and 

				// R-type <add sub sll slt sltu xor srl sra or and>
				rs2	= cob(IR,24,20);
				rs1	= cob(IR,19,15);
				rd	= cob(IR,11,7);
				
				if (funct7 == 0b0000001) { // [M拡張] mul mulh mulhsu mulhu div divu rem remu
			        switch (funct3) {
			            case 0b000: // mul
			            	data.BIS.add("mul x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b001: // mulh
			            	data.BIS.add("mulh x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b010: // mulhsu
			            	data.BIS.add("mulhsu x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b011: // mulhu
			            	data.BIS.add("mulhu x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b100: // div
			            	data.BIS.add("div x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b101: // divu
			            	data.BIS.add("divu x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b110: // rem
			            	data.BIS.add("rem x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
			                break;

			            case 0b111: // remu
			            	data.BIS.add("remu x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
							pc += 4;
							// System.out.println("\n\nできた\n\n");
			                break;

			            default:
			                // 不明なfunct3の場合、エラーを処理するか無視する
			                // System.out.println("Error: Unsupported funct3 value");
			                return -1;
			        }
			        
			    } else {
			    	switch (funct3)	// funct3の値で分岐
					{
					case 0b000:							// add or sub
						if(funct7 == 0b0000000)			// add : Add
						{
							data.BIS.add("add x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else if(funct7 == 0b0100000) 	//sub : Subtraction
						{
							data.BIS.add("sub x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));

						} else 							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}
						pc+=4;
						break;
					
					case 0b001:							// sll
						if(funct7 == 0b0000000)			// sll : Shift Left Logical
						{
							data.BIS.add("sll x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else 							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}

						pc+=4;
						break;
					
					case 0b010:							// slt
						if(funct7 == 0b0000000)			// slt : Set on Less Than
						{
							data.BIS.add("slt x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else 							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}
						
						pc+=4;
						break;
					
					case 0b011:							// sltu
						if(funct7 == 0b0000000)			// slt : Set on Less Than Unsign
						{
							data.BIS.add("sltu x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else 							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}
						
						pc+=4;
						break;
					
					case 0b100:							// xor
						if(funct7 == 0b0000000)			// xor : Exclusive Or
						{
							data.BIS.add("xor x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else 							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}
						
						pc+=4;
						break;
					
					case 0b101:							// srl or sra
						if(funct7 == 0b0000000)			// srl : Shift Right Logical
						{
							data.BIS.add("srl x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else if(funct7 == 0b0100000)	// sra : Shift Right Arithmetic
						{
							data.BIS.add("sra x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}
						
						pc+=4;
						break;
					
					case 0b110:							// or
						if(funct7 == 0b0000000)			// or : Or
						{
							data.BIS.add("or x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
						}
						else 							// error
						{
							// System.out.printf("execution error(%04x) : op:0110111\n",pc);
							return -1;
						}
						
						pc+=4;
						break;
					
					case 0b111:							// and
						if(funct7 == 0b0000000)			// and : And
						{
							data.BIS.add("and x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(rs2));
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
			case 0b1010011:	// [F拡張] [D拡張]

				 
				rs2	= cob(IR,24,20);
				rs1	= cob(IR,19,15);
				rd	= cob(IR,11,7);
				
				switch(funct7) 
				{
				case 0b0000000: // fadd.s
					data.BIS.add("fadd.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
					break;
					
				case 0b0000100: // fsub.s
					data.BIS.add("fsub.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
					break;
				
				case 0b0001000: // fmul.s
					data.BIS.add("fmul.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
					break;
				
				case 0b0001100: // fdiv.s
					data.BIS.add("fdiv.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
					break;
					
				case 0b0101100: // fsqrt.s
					data.BIS.add("fsqrt.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1));
					break;
				
				case 0b0010000: // fsgnj.s fsgnj.s fsgnjn.s
					switch(funct3)
					{
					case 0b000: // fsgnj.s
						data.BIS.add("fsgnj.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						break;
						
					case 0b001: // fsgnjn.s 
						data.BIS.add("fsgnjn.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						break;
					
					case 0b010: // fsgnjnx.s
						data.BIS.add("fsgnjx.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						break;
					
					default:
						break;
				
					}
					
					break;
				
				case 0b0010100: //fmin.s fmax.s
					switch(funct3)
					{
					case 0b000: // fmin.s
						data.BIS.add("fmin.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						
						
						break;
						
					case 0b001: // fmin.s fmax.s 
						data.BIS.add("fmax.s f"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						
						
						break;
					
					default:
						break;
				
					}
					
					break;
					
				case 0b1100000: // fcvt.w.s fcvt.wu.s 
					switch(rs2)
					{
					case 0b000: // fcvt.w.s
						data.BIS.add("fcvt.w.s f"+String.valueOf(rd)+", x"+String.valueOf(rs1));
						break;
						
					case 0b001: // fcvt.wu.s
						data.BIS.add("fcvt.wu.s f"+String.valueOf(rd)+", x"+String.valueOf(rs1));
						break;
					
					default:
						break;
				
					}
					break;
				
				case 0b1110000: // fmv.x.w fclass.s
					switch(funct3)
					{
					case 0b000: // fmv.x.w
						data.BIS.add("fmv.x.w f"+String.valueOf(rd)+", x"+String.valueOf(rs1));
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
						data.BIS.add("fle.s x"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						break;
						
					case 0b001: // flt.s
						data.BIS.add("flt.s x"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						break;
					
					case 0b010: // feq.s
						data.BIS.add("feq.s x"+String.valueOf(rd)+", f"+String.valueOf(rs1)+", f"+String.valueOf(rs2));
						break;
					
					default:
						break;
				
					}
					
					break;
					
				case 0b1101000: // fcvt.s.w fcvt.s.wu
					switch(rs2)
					{
					case 0b000: // fcvt.s.w
						data.BIS.add("fcvt.s.w f"+String.valueOf(rd)+", x"+String.valueOf(rs1));
						break;
						
					case 0b001: // fcvt.s.wu
						data.BIS.add("fcvt.s.wu f"+String.valueOf(rd)+", x"+String.valueOf(rs1));
						break;
					
					default:
						break;
				
					}
					
					break;
				
				case 0b1111000: // fmv.w.x
				    data.BIS.add("fmv.w.x f" + String.valueOf(rd) + ", x" + String.valueOf(rs1));
				    break;

				    
				    
				    
				    
				    
				case 0b0000001: // fadd.d
				    data.BIS.add("fadd.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				    break;

				case 0b0000101: // fsub.d
				    data.BIS.add("fsub.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				    break;

				case 0b0001001: // fmul.d
				    data.BIS.add("fmul.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				    break;

				case 0b0001101: // fdiv.d
				    data.BIS.add("fdiv.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				    break;

				case 0b0101101: // fsqrt.d
				    data.BIS.add("fsqrt.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1));
				    break;

				case 0b0010001: // fsgnj.d fsgnjn.d fsgnjx.d
				    switch (funct3) {
				        case 0b000: // fsgnj.d
				            data.BIS.add("fsgnj.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        case 0b001: // fsgnjn.d
				            data.BIS.add("fsgnjn.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        case 0b010: // fsgnjx.d
				            data.BIS.add("fsgnjx.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        default:
				            break;
				    }
				    break;

				case 0b0010101: // fmin.d fmax.d
				    switch (funct3) {
				        case 0b000: // fmin.d
				            data.BIS.add("fmin.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        case 0b001: // fmax.d
				            data.BIS.add("fmax.d f" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        default:
				            break;
				    }
				    break;

				case 0b1100001: // fcvt.w.d fcvt.wu.d
				    switch (rs2) {
				        case 0b000: // fcvt.w.d
				            data.BIS.add("fcvt.w.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1));
				            break;

				        case 0b001: // fcvt.wu.d
				            data.BIS.add("fcvt.wu.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1));
				            break;

				        default:
				            break;
				    }
				    break;

				case 0b1110001: // fmv.x.d fclass.d
				    switch (funct3) {
				        case 0b000: // fmv.x.d
				            data.BIS.add("fmv.x.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1));
				            break;

				        case 0b001: // fclass.d
				            data.BIS.add("fclass.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1));
				            break;

				        default:
				            break;
				    }
				    break;

				case 0b1010001: // feq.d flt.d fle.d
				    switch (funct3) {
				        case 0b000: // fle.d
				            data.BIS.add("fle.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        case 0b001: // flt.d
				            data.BIS.add("flt.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        case 0b010: // feq.d
				            data.BIS.add("feq.d x" + String.valueOf(rd) + ", f" + String.valueOf(rs1) + ", f" + String.valueOf(rs2));
				            break;

				        default:
				            break;
				    }
				    break;

				case 0b1101001: // fcvt.d.w fcvt.d.wu
				    switch (rs2) {
				        case 0b000: // fcvt.d.w
				            data.BIS.add("fcvt.d.w f" + String.valueOf(rd) + ", x" + String.valueOf(rs1));
				            break;

				        case 0b001: // fcvt.d.wu
				            data.BIS.add("fcvt.d.wu f" + String.valueOf(rd) + ", x" + String.valueOf(rs1));
				            break;

				        default:
				            break;
				    }
				    break;

				case 0b1111001: // fmv.d.x
				    data.BIS.add("fmv.d.x f" + String.valueOf(rd) + ", x" + String.valueOf(rs1));
				    break;

				    
				default:
					System.out.println("nannde");
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
				case 0b001:							// slli : Shift Left Logical Immediate
					data.BIS.add("slli x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(shamt));
				
					pc+=4;
					break;
				
				case 0b101:							// srli or srai
					if(funct7 == 0b0000000)			// srli : Shift Right Logical Immediate
					{
						data.BIS.add("srli x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(shamt));
					}
					else if(funct7 == 0b0100000)    // srai : Shift Right Arithmetic Immediate
					{
						data.BIS.add("srai x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(shamt));
					}
					pc+=4;
					break;
				
				case 0b000:							// addi : Add Immediate
					data.BIS.add("addi x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(imm));
					
					pc+=4;
					break;
				
				case 0b010:							// slti : Set if Less Than Immediate
					data.BIS.add("slti x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(imm));
					
					pc+=4;
					break;
				
				case 0b011:							// sltiu : Set if Less Than Immediate Unsigned
					data.BIS.add("sltiu x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(imm));
					
					pc+=4;
					break;

				case 0b100:							// xori : Exclusiive Or Immediate
					data.BIS.add("xori x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", x"+String.valueOf(imm));
					
					pc+=4;
					break;
				
				case 0b110:							// ori : Or Immediate
					data.BIS.add("ori x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(imm));
					
					pc+=4;
					break;
				
				case 0b111:							// andi : And Immediate
					data.BIS.add("andi x"+String.valueOf(rd)+", x"+String.valueOf(rs1)+", "+String.valueOf(imm));
					
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
				case 0b000:							// jalr : Jump and Link Register
					data.BIS.add("jalr x"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc += 4;
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
				case 0b000:							// lb : Load Byte
					data.BIS.add("lb x"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc+=4;
					break;
		
				case 0b001:							// lh : Load Halfword
					data.BIS.add("lh x"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc+=4;
					break;

				case 0b010:							// lw : Load Word
					data.BIS.add("lw x"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc+=4;
					break;
					
				case 0b100:							// lbu : Load Byte Unsigned
					data.BIS.add("lbu x"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc+=4;
					break;
					
				case 0b101:							// lhu : Load Halfword Unsinged
					data.BIS.add("lhu x"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
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
					data.BIS.add("flw f"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					pc+=4;
					break;

				case 0b011:	// flw : float Load Word
					data.BIS.add("fld f"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
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
				case 0b000:							// sb : Store Byte
					data.BIS.add("sb x"+String.valueOf(rs2)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc+=4;
					break;
				
				case 0b001:							// sh : Store Halfword
					data.BIS.add("sh x"+String.valueOf(rs2)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
					pc+=4;
					break;
				
				case 0b010:							// sw : Store Word
					data.BIS.add("sw x"+String.valueOf(rs2)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					
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
				case 0b010:	// fsw : float Store Word
					data.BIS.add("fsw f"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					pc+=4;
					// System.out.printf("\n\ndataは%x\nrs1は%x\nrs2は%x\n\n",data.MEM[data.x[rs1] + cc(imm,12)],data.x[rs1],data.x[rs2]);
					break;
					
				case 0b011:	// fsw : float Store Word
					data.BIS.add("fsd f"+String.valueOf(rd)+", "+String.valueOf(imm)+"(x"+String.valueOf(rs1)+")");
					pc+=4;
					// System.out.printf("\n\ndataは%x\nrs1は%x\nrs2は%x\n\n",data.MEM[data.x[rs1] + cc(imm,12)],data.x[rs1],data.x[rs2]);
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
					data.BIS.add("beq x"+String.valueOf(rs1)+", x"+String.valueOf(rs2)+", "+String.valueOf(imm));
					
					pc+=4;
					break;
				
				case 0b001:							// bne : Branch on Equal
					data.BIS.add("bne x"+String.valueOf(rs1)+", x"+String.valueOf(rs2)+", "+String.valueOf(imm));
					
					pc += 4;
					break;
				
				case 0b100:							// blt : Branch if Less Than
					data.BIS.add("blt x"+String.valueOf(rs1)+", x"+String.valueOf(rs2)+", "+String.valueOf(imm));
					
					pc += 4;
					break;
				
				case 0b101:							// bge : Branch if Greater Than or Equal
					data.BIS.add("bge x"+String.valueOf(rs1)+", x"+String.valueOf(rs2)+", "+String.valueOf(imm));
					
					pc += 4;
					break;
				
				case 0b110:							// bltu : Branch if Less Than, Unsinged
					data.BIS.add("bltu x"+String.valueOf(rs1)+", x"+String.valueOf(rs2)+", "+String.valueOf(imm));
					pc += 4;
					
					break;
				
				case 0b111:							// bgeu : Branch if Greater Than or Equal, Unsinged
					data.BIS.add("bgeu x"+String.valueOf(rs1)+", x"+String.valueOf(rs2)+", "+String.valueOf(imm));
					
					pc += 4;
					break;
				
				default:
					break;
				}
				
				break;
				
			case 0b1101111: 						// jal : Jump And Link
				
				// J-Type <lw>
				imm	= (cob(IR,31,31) << 20) | (cob(IR,19,12) << 12) | (cob(IR,20,20) << 11) | (cob(IR,30,21) << 1);	
				rd	= cob(IR,11,7);
				
				data.BIS.add("jal x" + String.valueOf(rd) + ", " + String.valueOf(imm));
				pc += 4;
				
				break;
				
			case 0b0110111: 						// lui : Load Upper Immediate
				
				// U-Type <lw>
				imm	= (cob(IR,31,12) << 12);	
				rd	= cob(IR,11,7);
				
				data.BIS.add("lui x" + String.valueOf(rd) + ", " + String.valueOf(imm));
				pc += 4;
				
				break;
			
			case 0b0010111: 						// AUIpc : Add Upper Immediate to pc
				
				// U-Type <lw>
				imm	= (cob(IR,31,12) << 12);	
				rd	= cob(IR,11,7);
				
				data.BIS.add("auipc x" + String.valueOf(rd) + ", " + String.valueOf(imm));
				pc += 4;
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

			case 0b1110011: // ecall ebreak csrrw csrrs csrrc csrrwi csrrsi csrrci

				switch (funct3)
				{
				case 0b000: // ecall or ebreak
					
					data.BIS.add("ecall");

					break;
				
				case 0b001:							// csrrw : Control and Status Register Read and Write

					break;
				
				case 0b010:							// csrrs : Control and Status Register Read and Set

					break;
				
				case 0b011:							// csrrc : Control and Status Register Read and Clear

					break;
				
				case 0b101:							// csrrwi : Control and Status Register Read and Write Immediate

					break;
				
				case 0b110:							// csrrsi : Control and Status Register Read and Set Immediate

					break;
				
				case 0b111:							// csrrci : Control and Status Register Read and Clear Immediate

					break;
				
				default:
					break;
				}
				
				pc+=4;
				break;
			
			}
	        c++;
	        if(c==100000) ERR_F = 1;
		}
		
		return 0;
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
}
