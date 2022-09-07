import java.util.Arrays;

public class SHA224 {
    private static final int[] H0 =
    {0xc1059ed8, 0x367cd507, 0x3070dd17, 0xf70e5939, 
     0xffc00b31, 0x68581511, 0x64f98fa7, 0xbefa4fa4};
     
    private static final int[] k = 
    {0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
     0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
     0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
     0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
     0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
     0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
     0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
     0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};
    
    private static final int BLOCK_SIZE = 64;
    private static final int DIGEST_LENGTH = 28;
    private static final int MAX_SIZE = 8;
    
    private final byte[] buffer;
    private int[] h;
    private long t;
    private int count;
    
    public SHA224() {
        this.buffer = new byte[BLOCK_SIZE];
        reset();
    }
    
    private SHA224(SHA224 digest) {
        this.buffer = Arrays.copyOf(digest.buffer, digest.buffer.length);
        this.h = Arrays.copyOf(digest.h, digest.h.length);
        this.t = digest.t;
        this.count = digest.count;
    }
    
    public String algorithm() {
        return "SHA224";
    }
    
    public int length() {
        return DIGEST_LENGTH;
    }
    
    public SHA224 copy() {
        return new SHA224(this);
    }
    
    public SHA224 reset() {
        t = 0;
        h = Arrays.copyOf(H0, H0.length);
        count = 0;
        return this;
    }
    
    public SHA224 update(byte input) {
        if (count == BLOCK_SIZE) {
            processBuffer();
        }
        buffer[count++] = input;
        return this;
    }
    
    public SHA224 update(byte... input) {
        return update(input, 0, input.length);
    }
    
    public SHA224 update(byte[] input, int off, int len) {
        if (off < 0 || off > len || len < 0 || len > input.length) {
            throw new IndexOutOfBoundsException();
        }
        int index = off;
        int remaining = len;
        while (remaining > 0) {
            if (count == BLOCK_SIZE) {
                processBuffer();
            }
            int cpLen = Math.min(BLOCK_SIZE - count, remaining);
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
        if (count == BLOCK_SIZE) {
            processBuffer();
        }
        buffer[count] = (byte) 0b10000000;
        if (count > BLOCK_SIZE - MAX_SIZE - 1) {
            Arrays.fill(buffer, count + 1, BLOCK_SIZE, (byte) 0);
            processBuffer();
            buffer[0] = 0;
        }
        Arrays.fill(buffer, count + 1, BLOCK_SIZE, (byte) 0);
        System.arraycopy(Byte_Manip.longToBytes(t + count * 8), 0, buffer, BLOCK_SIZE - 8, 8);
        processBuffer();
        byte[] out = new byte[DIGEST_LENGTH];
        System.arraycopy(Byte_Manip.intsToBytes(h), 0, out, 0, DIGEST_LENGTH);
        reset();
        return out;
    }
    
    private void processBuffer() {
        t += count * 8;
        if (t == 0 && count != 0) {
            throw new IllegalStateException();
        }
        count = 0;
        
        //turn buffer into int array
        int[] chunk = Byte_Manip.bytesToInts(buffer);
                
        //prep message schedule
        int[] W = new int[64];
        System.arraycopy(chunk, 0, W, 0, 16);
        for (int i = 16; i < 64; i++) {
            W[i] = W[i-16] + smallSig0(W[i-15]) + W[i-7] + smallSig1(W[i-2]);
        }
        
        //initialize working variables
        int[] temp = Arrays.copyOf(h, h.length);
        
        //compress function
        for (int i = 0; i < 64; i++) {
            int t1 = temp[7] + bigSig1(temp[4]) + ch(temp[4],temp[5],temp[6]) + k[i] + W[i];
            int t2 = bigSig0(temp[0]) + maj(temp[0],temp[1],temp[2]);
            System.arraycopy(temp,0,temp,1,temp.length-1);
            temp[4] += t1;
            temp[0] = t1 + t2;
        }
        
        //add temp to H
        for (int i = 0; i < h.length; i++) {
            h[i] += temp[i];
        }
    }
    
    private static int smallSig0(int x) {
        return Integer.rotateRight(x, 7) ^ Integer.rotateRight(x,18) ^ (x >>>  3);
    }
    
    private static int smallSig1(int x) {
        return Integer.rotateRight(x,17) ^ Integer.rotateRight(x,19) ^ (x >>> 10);
    }
    
    private static int bigSig1(int x) {
        return Integer.rotateRight(x, 6) ^ Integer.rotateRight(x,11) ^ Integer.rotateRight(x,25);
    }
    
    private static int bigSig0(int x) {
        return Integer.rotateRight(x, 2) ^ Integer.rotateRight(x,13) ^ Integer.rotateRight(x,22);
    }
    
    private static int ch(int x, int y, int z) {
        return (x & y) ^ (~x & z);
    }
    
    private static int maj(int x, int y, int z) {
        return (x & y) ^ (x & z) ^ (y & z);
    }
}
    
