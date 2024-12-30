    .section .data
a:  .float 1.5          # 浮動小数点数1
b:  .float 2.5          # 浮動小数点数2
result: .float 0.0      # 結果を格納する場所

    .section .text

    # レジスタの初期化
    la t0, a             # aのアドレスをt0にロード
    la t1, b             # bのアドレスをt1にロード
    la t2, result        # resultのアドレスをt2にロード

    # aをf0にロード
    flw f0, 0(t0)

    # bをf1にロード
    flw f1, 0(t1)

    # 加算 f2 = f0 + f1
    fadd.s f2, f0, f1
    fsw f2, 0(t2)        # 結果をメモリに格納

    # 減算 f3 = f0 - f1
    fsub.s f3, f0, f1
    fsw f3, 4(t2)        # 結果をメモリに格納

    # 乗算 f4 = f0 * f1
    fmul.s f4, f0, f1
    fsw f4, 8(t2)        # 結果をメモリに格納

    # 除算 f5 = f0 / f1
    fdiv.s f5, f0, f1
    fsw f5, 12(t2)       # 結果をメモリに格納

    # プログラム終了
    li a7, 10            # exitシステムコール
    ecall
