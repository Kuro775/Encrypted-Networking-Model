import java.util.Arrays;

public class SHA3{
    private static final int DM = 5;
    private static final int NR = 24;
    private static final int WIDTH = 200;
    
    //precomputed round constant in step map Iota
    private static final long[] RC = 
    {0x01L, 0x8082L, 0x800000000000808aL,
     0x8000000080008000L, 0x808bL, 0x80000001L,
     0x8000000080008081L, 0x8000000000008009L, 0x8aL,
     0x88L, 0x80008009L, 0x8000000aL,
     0x8000808bL, 0x800000000000008bL, 0x8000000000008089L,
     0x8000000000008003L, 0x8000000000008002L, 0x8000000000000080L,
     0x800aL, 0x800000008000000aL, 0x8000000080008081L,
     0x8000000000008080L, 0x80000001L, 0x8000000080008008L};
    
    private final int digestLength;
    private final int blockSize;
    private final String algorithm;
    
    private final byte[] buffer;
    private byte[] state = new byte[WIDTH];
    private final long[] lanes = new long[DM*DM];
    private int count;
    
    private SHA3(String algorithm, int digestLength) {
        this.digestLength = digestLength;
        this.blockSize = WIDTH - (2 * digestLength);
        this.algorithm = algorithm;
        this.buffer = new byte[blockSize];
        reset();
    }
    
    private SHA3(SHA3 digest) {
        this.digestLength = digest.digestLength;
        this.blockSize = digest.blockSize;
        this.algorithm = digest.algorithm;
        this.buffer = new byte[blockSize];
        this.state = Arrays.copyOf(digest.state, digest.state.length);
        this.count = digest.count;
    }
    
    public String algorithm() {
        return algorithm;
    }
    
    public int length() {
        return digestLength;
    }
    
    public SHA3 copy() {
        return new SHA3(this);
    }
    
    private SHA3 reset() {
        Arrays.fill(state, (byte) 0);
        Arrays.fill(lanes, 0L);
        count = 0;
        return this;
    }
    
    public SHA3 update(byte input) {
        if (count == blockSize) {
            processBuffer();
        }
        buffer[count++] = input;
        return this;
    }
    
    public SHA3 update(byte... input) {
        return update(input, 0, input.length);
    }
    
    public SHA3 update(byte[] input, int off, int len) {
        if (off < 0 || off > len || len < 0 || len > input.length) {
            throw new IndexOutOfBoundsException();
        }
        int index = off;
        int remaining = len;
        while (remaining > 0) {
            if (count == blockSize) {
                processBuffer();
            }
            int cpLen = Math.min(blockSize - count, remaining);
            System.arraycopy(input, index, buffer, count, cpLen);
            remaining -= cpLen;
            index += cpLen;
            count += cpLen;
        }
        return this;
    }
    
    public byte[] digest(byte... input) {
        update(input);
        return digest();
    }
    
    public byte[] digest() {
        int p = pad();
        processBuffer();
        if (p == 0) {
            Arrays.fill(buffer, (byte) 0);
            buffer[buffer.length-1] = (byte) 0x80;
            processBuffer();
        }
        byte[] out = new byte[digestLength];
        System.arraycopy(state, 0, out, 0, digestLength);
        reset();
        return out;
    }
    
    public int pad() {
        buffer[count++] = 0x06;
        if (count == blockSize) {
            return 0;
        }
        Arrays.fill(buffer, count, blockSize, (byte) 0);
        buffer[blockSize-1] = (byte) 0x80;
        return 1;
    }
    
    private void processBuffer() {
        count = 0;
        for (int i = 0; i < buffer.length; i++) {
            state[i] ^= buffer[i];
        }
        keccak();
    }
    
    private long[] theta(long[] a) {
        long c0 = a[0] ^ a[5] ^ a[10] ^ a[15] ^ a[20];
        long c1 = a[1] ^ a[6] ^ a[11] ^ a[16] ^ a[21];
        long c2 = a[2] ^ a[7] ^ a[12] ^ a[17] ^ a[22];
        long c3 = a[3] ^ a[8] ^ a[13] ^ a[18] ^ a[23];
        long c4 = a[4] ^ a[9] ^ a[14] ^ a[19] ^ a[24];
        
        long d0 = c4 ^ Long.rotateLeft(c1, 1);
        long d1 = c0 ^ Long.rotateLeft(c2, 1);
        long d2 = c1 ^ Long.rotateLeft(c3, 1);
        long d3 = c2 ^ Long.rotateLeft(c4, 1);
        long d4 = c3 ^ Long.rotateLeft(c0, 1);
        for (int i = 0; i < a.length; i += DM) {
            a[ i ] ^= d0;
            a[i+1] ^= d1;
            a[i+2] ^= d2;
            a[i+3] ^= d3;
            a[i+4] ^= d4;
        }
        return a;
    }
    
    private long[] rho(long[] a) {
        for (int i = 0, x = 1, y = 0; i < 24; i++) {
            int shift = ((i+1) * (i+2) / 2) % 64;
            a[y*5+x] = Long.rotateLeft(a[y*5+x], shift);
            int temp = x;
            x = y;
            y = (3*y + 2*temp) % DM;
        }
        return a;
    }
    
    private long[] pi(long[] a) {
        long tmp = a[10];
        int temp = 10;
        for (int i = 0, x = 0, y = 2; i < 23; i++) {
            int nextTemp = (temp % 5 + 3 * (temp / 5)) % 5 + temp % 5 * 5;
            a[temp] = a[nextTemp];
            temp = nextTemp;
        }
        a[temp] = tmp;
        return a;
    }
    
    //pre calculated
    private long[] piRho(long[] a) {
        long tmp = Long.rotateLeft(a[10], 3);
        a[10] = Long.rotateLeft(a[1], 1);
        a[1] = Long.rotateLeft(a[6], 44);
        a[6] = Long.rotateLeft(a[9], 20);
        a[9] = Long.rotateLeft(a[22], 61);
        a[22] = Long.rotateLeft(a[14], 39);
        a[14] = Long.rotateLeft(a[20], 18);
        a[20] = Long.rotateLeft(a[2], 62);
        a[2] = Long.rotateLeft(a[12], 43);
        a[12] = Long.rotateLeft(a[13], 25);
        a[13] = Long.rotateLeft(a[19], 8);
        a[19] = Long.rotateLeft(a[23], 56);
        a[23] = Long.rotateLeft(a[15], 41);
        a[15] = Long.rotateLeft(a[4], 27);
        a[4] = Long.rotateLeft(a[24], 14);
        a[24] = Long.rotateLeft(a[21], 2);
        a[21] = Long.rotateLeft(a[8], 55);
        a[8] = Long.rotateLeft(a[16], 45);
        a[16] = Long.rotateLeft(a[5], 36);
        a[5] = Long.rotateLeft(a[3], 28);
        a[3] = Long.rotateLeft(a[18], 21);
        a[18] = Long.rotateLeft(a[17], 15);
        a[17] = Long.rotateLeft(a[11], 10);
        a[11] = Long.rotateLeft(a[7], 6);
        a[7] = tmp;
        return a;
    }
    
    private long[] chi(long[] a) {
        for (int i = 0; i < a.length; i += DM) {
            long a0 = a[i];
            long a1 = a[i+1];
            long a2 = a[i+2];
            long a3 = a[i+3];
            long a4 = a[i+4];
            a[i] = a0 ^ ((~a1) & a2);
            a[i+1] = a1 ^ ((~a2) & a3);
            a[i+2] = a2 ^ ((~a3) & a4);
            a[i+3] = a3 ^ ((~a4) & a0);
            a[i+4] = a4 ^ ((~a0) & a1);
        }
        return a;
    }
    
    private long[] iota(long[] a, int i) {
        a[0] ^= RC[i];
        return a;
    }
    
    private void bytesToLanes(byte[] s, long[] m) {
        for (int i = 0; i < DM * DM; i++) {
            m[i] = Byte_Manip.rBytesToLong(s, i*8, (i+1)*8);
        }
    }
    
    private byte[] lanesToBytes(long[] m) {
        return Byte_Manip.rLongsToBytes(m);
    }
    
    private void keccak() {
        bytesToLanes(state, lanes);
        for (int i = 0; i < NR; i++) {
            iota(chi(piRho(theta(lanes))), i);
        }
        state = lanesToBytes(lanes);
    }
    
    public static final class SHA224 extends SHA3 {
        public SHA224() {
            super("SHA3-224", 28);
        }
    }
    
    public static final class SHA256 extends SHA3 {
        public SHA256() {
            super("SHA3-256", 32);
        }
    }
    
    public static final class SHA384 extends SHA3 {
        public SHA384() {
            super("SHA3-384", 48);
        }
    }
    
    public static final class SHA512 extends SHA3 {
        public SHA512() {
            super("SHA3-512", 64);
        }
    }
}