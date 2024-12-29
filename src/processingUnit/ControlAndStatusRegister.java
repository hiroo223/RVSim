package processingUnit;

import java.util.HashMap;
import java.util.Map;

public class ControlAndStatusRegister {

    private final Map<Long, Long> csr;

    // コンストラクタ（デフォルト）
    public ControlAndStatusRegister() {
        this.csr = new HashMap<>();
        long[] addresses = {
                MSTATUS, MEDELEG, MIDELEG, MTVEC, MEPC, MCAUSE, MTVAL,
                SSTATUS, SEDELEG, SIDELEG, STVEC, SEPC, SCAUSE, STVAL,
                USTATUS, UTVEC, UEPC, UCAUSE, UTVAL, FFLAGS, FRM, FCSR
        };
        for (long address : addresses) {
            csr.put(address, 0L); // 初期値を0に設定
        }
    }

    // CSRに指定されたアドレスが存在するかを確認するメソッド
    public boolean contains(long address) {
        return csr.containsKey(address);
    }

    // CSRから値を読み取るメソッド
    public long read(long address) {
        if (contains(address)) {
            return csr.get(address);
        }
        throw new IllegalArgumentException(String.format("Address not found: %x", address));
    }

    // CSRに値を書き込むメソッド
    public void write(long address, long value) {
        if (contains(address)) {
            csr.put(address, value);
        } else {
            throw new IllegalArgumentException(String.format("Address not found: %x", address));
        }
    }

    // CSR Read and Write (CSRRW) メソッド
    public long csrrw(long address, long value) {
        long t = read(address);
        write(address, value);
        return t;
    }

    // CSR Read and Set (CSRRS) メソッド
    public long csrrs(long address, long value) {
        long t = read(address);
        write(address, csr.get(address) | value);
        return t;
    }

    // CSR Read and Clear (CSRRC) メソッド
    public long csrrc(long address, long value) {
        long t = read(address);
        write(address, csr.get(address) & ~value);
        return t;
    }

    // ステータスレジスタ内の特定フィールドを更新するメソッド
    public void updateStatusField(long address, long fieldMask, long value) {
        if (contains(address)) {
            long currentValue = read(address);
            // 指定したフィールドのみを更新するための処理（ビットのクリア後セット）
            long updatedValue = (currentValue & ~fieldMask) | (value & fieldMask);
            write(address, updatedValue);
        } else {
            throw new IllegalArgumentException(String.format("Address not found: %x", address));
        }
    }

    // ステータスレジスタ内の特定フィールドを読み取るメソッド
    public long readStatusField(long address, long fieldMask) {
        if (contains(address)) {
            long currentValue = read(address);
            return currentValue & fieldMask;
        } else {
            throw new IllegalArgumentException(String.format("Address not found: %x", address));
        }
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

    // 使用例
    public static void main(String[] args) {
        ControlAndStatusRegister csr = new ControlAndStatusRegister();

        // CSRの値を読み取る
        long value = csr.read(MSTATUS);
        System.out.printf("Initial value of MSTATUS: %x%n", value);

        // CSRに新しい値を書き込む
        csr.write(MSTATUS, 0x1);
        System.out.printf("New value of MSTATUS: %x%n", csr.read(MSTATUS));

        // CSRの値をセットする
        long oldValue = csr.csrrs(MSTATUS, 0x4);
        System.out.printf("Old value of MSTATUS before setting: %x%n", oldValue);
        System.out.printf("New value of MSTATUS after setting: %x%n", csr.read(MSTATUS));

        // CSRの値をクリアする
        oldValue = csr.csrrc(MSTATUS, 0x1);
        System.out.printf("Old value of MSTATUS before clearing: %x%n", oldValue);
        System.out.printf("New value of MSTATUS after clearing: %x%n", csr.read(MSTATUS));

        // ステータスレジスタのフィールド更新のテスト
        csr.updateStatusField(MSTATUS, 0x8, 0x8); // MIEビットをセット
        System.out.printf("MSTATUS after setting MIE bit: %x%n", csr.read(MSTATUS));

        // ステータスレジスタのフィールド読み取りのテスト
        long mieValue = csr.readStatusField(MSTATUS, 0x8);
        System.out.printf("MSTATUS MIE bit value: %x%n", mieValue);
    }
}
