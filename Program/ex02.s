# レベル2 - 条件分岐で値の大小を比較
    .section .text

_start:
    li x5, 15         # x5 に 15 をロード
    li x6, 10         # x6 に 10 をロード

    bge x5, x6, greater  # x5 >= x6 なら greater へ分岐
    j less              # x5 < x6 なら less へ分岐

greater:
    li x7, 1           # 結果を 1 に設定
    j done

less:
    li x7, -1          # 結果を -1 に設定

done:
    la a7, 10
    ecall



