package processingUnit;
public class TrapHandler {

    public enum PrivilegeMode {
        Machine, Supervisor, User
    }

    public static class Cause {
        private final long causeValue;

        public Cause(long causeValue) {
            this.causeValue = causeValue;
        }

        public long toPrimitive() {
            return causeValue;
        }
    }

    // Constants for CSR addresses
    private static final long MCAUSE = 0x342;
    private static final long SCAUSE = 0x142;
    private static final long UCAUSE = 0x42;
    private static final long MEPC = 0x341;
    private static final long SEPC = 0x141;
    private static final long UEPC = 0x41;
    private static final long MTVAL = 0x343;
    private static final long STVAL = 0x143;
    private static final long UTVAL = 0x43;
    private static final long MSTATUS = 0x300;
    private static final long SSTATUS = 0x100;
    private static final long USTATUS = 0x0;
    private static final long MTVEC = 0x305;
    private static final long STVEC = 0x105;
    private static final long UTVEC = 0x5;

    private static final long STATUS_MPP = 0x1800;
    private static final long STATUS_SPP = 0x100;
    private static final long STATUS_MIE = 0x8;
    private static final long STATUS_SIE = 0x2;
    private static final long STATUS_UIE = 0x1;
    private static final long STATUS_MPIE = 0x80;
    private static final long STATUS_SPIE = 0x20;
    private static final long STATUS_UPIE = 0x10;

    // Helper functions to select CSR addresses based on privilege mode
    private static long selectAddress(PrivilegeMode mode, long machineAddress, long supervisorAddress, long userAddress) {
        switch (mode) {
            case Machine:
                return machineAddress;
            case Supervisor:
                return supervisorAddress;
            case User:
                return userAddress;
            default:
                throw new IllegalArgumentException("Unsupported privilege mode");
        }
    }

    private static long selectStatusField(PrivilegeMode mode, long mie, long sie, long uie) {
        switch (mode) {
            case Machine:
                return mie;
            case Supervisor:
                return sie;
            case User:
                return uie;
            default:
                throw new IllegalArgumentException("Unsupported privilege mode");
        }
    }

    private static long selectTval(Cause cause, long instruction) {
        // Implementation-specific: return the appropriate value based on the cause and instruction
        return instruction; // Simplified example
    }

    public static PrivilegeMode delegatedPrivilegeMode(ControlAndStatusRegister csr, Cause cause) {
        // Simplified example - determine the next privilege mode
        // This should typically check if the cause is delegable and select the correct privilege mode
        return PrivilegeMode.Machine; // Simplified to always use Machine mode
    }

    public static Tuple<PrivilegeMode, Long> handleTrap(Cause cause, long pcAddress, long instruction,
                                                        PrivilegeMode currentPrivilegeMode,
                                                        ControlAndStatusRegister csr) {
        // Determine the next privilege mode based on the cause and CSR settings
        PrivilegeMode nextPrivilegeMode = delegatedPrivilegeMode(csr, cause);

        // Set the cause register
        long causeAddress = selectAddress(nextPrivilegeMode, MCAUSE, SCAUSE, UCAUSE);
        csr.csrrw(causeAddress, cause.toPrimitive());

        // Set the exception program counter
        long epcAddress = selectAddress(nextPrivilegeMode, MEPC, SEPC, UEPC);
        csr.csrrw(epcAddress, pcAddress);

        // Set the trap value register
        long tvalAddress = selectAddress(nextPrivilegeMode, MTVAL, STVAL, UTVAL);
        long tval = selectTval(cause, instruction);
        csr.csrrw(tvalAddress, tval);

        // Set the previous privilege mode in the status register
        long statusAddress = selectAddress(nextPrivilegeMode, MSTATUS, SSTATUS, USTATUS);
        switch (nextPrivilegeMode) {
            case Machine:
                csr.updateStatusField(statusAddress, STATUS_MPP, currentPrivilegeMode.ordinal());
                break;
            case Supervisor:
                csr.updateStatusField(statusAddress, STATUS_SPP, currentPrivilegeMode.ordinal());
                break;
            case User:
                // User mode doesn't have a previous privilege field in the same way
                break;
        }

        // Set the previous interrupt enable field
        long ieField = selectStatusField(nextPrivilegeMode, STATUS_MIE, STATUS_SIE, STATUS_UIE);
        long ie = csr.readStatusField(statusAddress, ieField);
        long pieField = selectStatusField(nextPrivilegeMode, STATUS_MPIE, STATUS_SPIE, STATUS_UPIE);
        csr.updateStatusField(statusAddress, pieField, ie);

        // Disable the current interrupt enable bit
        csr.updateStatusField(statusAddress, ieField, 0);

        // Set the program counter to the trap-vector base address register
        long tvecAddress = selectAddress(nextPrivilegeMode, MTVEC, STVEC, UTVEC);
        long tvec = csr.csrrs(tvecAddress, 0);

        // Return the next privilege mode and the address to jump to
        return new Tuple<>(nextPrivilegeMode, tvec);
    }

    // Utility class to simulate a tuple (since Java doesn't have built-in tuples)
    public static class Tuple<X, Y> {
        public final X x;
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }
}
