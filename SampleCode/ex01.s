.section .data
s: .space 4

# 1から10の総和を計算するプログラム
    .section .text

_start:
    li t0, 0           # t0 に 0 をロード (合計値を保持)
    li t1, 1           # t1 に 1 をロード (カウンタ)
    li t2, 10          # t2 に 10 をロード (上限値)

loop:
    add  t0, t0, t1     # t0 = t0 + t1 (合計にカウンタの値を加える)
    addi  t1, t1, 1     # t1 = t1 + 1 (カウンタをインクリメント)

    bge t1, t2, done   # x6 >= 10 の場合は done ラベルへジャンプ

    j loop             # それ以外は loop ラベルへジャンプして繰り返し

done:
    la t3, s
    sw t2, 0(t3)
    # 結果を出力 (a7 に 10 をロードして ecall)
    la a7, 10
    ecall


