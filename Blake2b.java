//QUESTION: RADD or ADD? (Endian question)

import java.util.Arrays;

public class Blake2b implements Blake2{
    private static int[][] SIGMA = 
    {{ 0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15},
     {14, 10,  4,  8,  9, 15, 13,  6,  1, 12,  0,  2, 11,  7,  5,  3},
     {11,  8, 12,  0,  5,  2, 15, 13, 10, 14,  3,  6,  7,  1,  9,  4},
     { 7,  9,  3,  1, 13, 12, 11, 14,  2,  6,  5, 10,  4,  0, 15,  8},
     { 9,  0,  5,  7,  2,  4, 10, 15, 14,  1, 11, 12,  6,  8,  3, 13},
     { 2, 12,  6, 10,  0, 11,  8,  3,  4, 13,  7,  5, 15, 14,  1,  9},
     {12,  5,  1, 15, 14, 13,  4, 10,  0,  7,  6,  3,  9,  2,  8, 11},
     {13, 11,  7, 14, 12,  1,  3,  9,  5,  0, 15,  4,  8,  6,  2, 10},
     { 6, 15, 14,  9, 11,  3,  0,  8, 12,  2, 13,  7,  1,  4, 10,  5},
     {10,  2,  8,  4,  7,  6,  1,  5, 15, 11,  9, 14,  3, 12, 13,  0}};
    
    private static final long IV[] = 
    { 0X6A09E667F3BCC908L, 0XBB67AE8584CAA73BL, 
      0X3C6EF372FE94F82BL, 0XA54FF53A5F1D36F1L, 
      0X510E527FADE682D1L, 0X9B05688C2B3E6C1FL,
      0X1F83D9ABFB41BD6BL, 0X5BE0CD19137E2179L};
      
    private static final int BLOCK_SIZE = 128;
    
    private final int digestLength;
    private final byte[] buffer;
    private byte[] key;
    private long[] h;
    private long t0;
    private long t1;
    private int count;
    
    public Blake2b(int digestLength) {
        this(digestLength, new byte[0]);
    }
    
    public Blake2b(int digestLength, byte[] key) {
        if (digestLength > 64 | digestLength < 1) {
            throw new IllegalArgumentException("digest Length out of range");
        } else if (key.length > 64) {
            throw new IllegalArgumentException("key Length out of range");
        }
        this.buffer = new byte[BLOCK_SIZE];
        this.key = Arrays.copyOf(key, key.length);
        this.digestLength = digestLength;
        reset();
    }
    
    private Blake2b(Blake2b digest) {
        this.digestLength = digest.digestLength;
        this.buffer = Arrays.copyOf(digest.buffer, digest.buffer.length);
        this.key = Arrays.copyOf(digest.key, digest.key.length);
        this.h = Arrays.copyOf(digest.h, digest.h.length);
        this.t0 = digest.t0;
        this.t1 = digest.t0;
        this.count = digest.count;
    }
    
    public String algorithm() {
        return "Blake2b";
    }
    
    public int length() {
        return digestLength;
    }
    
    public Blake2b copy() {
        return new Blake2b(this);
    }
    
    public Blake2b reset() {
        t0 = 0L;
        t1 = 0L;
        h = Arrays.copyOf(IV,IV.length);
        h[0] ^= 0x01010000 | (key.length << 8) | digestLength;
        if (key.length > 0) {
            System.arraycopy(key, 0, buffer, 0, key.length);
            Arrays.fill(buffer, key.length, BLOCK_SIZE, (byte) 0);
            count = BLOCK_SIZE;
        } else {
            count = 0;
        }
        return this;
    }
    
    public Blake2b burn() {
        Arrays.fill(buffer, (byte) 0);
        Arrays.fill(key, (byte) 0);
        key = new byte[0];
        reset();
        return this;
    }
    
    public Blake2b update(byte input) {
        if (count == BLOCK_SIZE) {
            processBuffer(false);
        }
        buffer[count++] = input;
        return this;
    }
    
    public Blake2b update(byte... input) {
        return update(input, 0, input.length);
    }
    
    public Blake2b update(byte[] input, int off, int len) {
        if (off < 0 || off > len || len < 0 || len > input.length) {
            throw new IndexOutOfBoundsException();
        }
        int index = off;
        int remaining = len;
        while (remaining > 0) {
            if (count == BLOCK_SIZE) {
                processBuffer(false);
            }
            int cpLen = Math.min(BLOCK_SIZE - count, remaining);
            System.arraycopy(input, index, buffer, count, cpLen);
            remaining -= cpLen;
            index += cpLen;
            count += cpLen;
        }
        return this;
    }
    
    public byte[] digest() {
        Arrays.fill(buffer, count, BLOCK_SIZE, (byte) 0);
        processBuffer(true);
        byte[] out = new byte[digestLength];
        System.arraycopy(Byte_Manip.rLongsToBytes(h), 0, out, 0, digestLength);
        reset();
        return out;
    }

    private void processBuffer(boolean isLastBlock) {
        t0 += count;
        if (t0 == 0L && count != 0) {
            t1++;
            if (t1 == 0) {
                throw new IllegalStateException();
            }
        }
        count = 0;
        compress(buffer, isLastBlock);
    }
    
    private void compress(byte[] chunk, boolean isLastBlock) {
        //Setup local work vector V
        long[] V = new long[16];
        System.arraycopy(h, 0, V, 0, h.length);
        System.arraycopy(IV, 0, V, h.length, IV.length);
        
        //Mix 128-bit counter t into V12:V13
        V[12] ^= t0;
        V[13] ^= t1;
        
        //if this is last block then invert all bits in V14
        if (isLastBlock) {
            V[14] = ~V[14];
        }
        
        //split chunk into 16 8-byte words m (little endianess)
        long[] m = new long[16];
        for (int i = 0; i < 16; i++) {
            m[i] = Byte_Manip.rBytesToLong(chunk, i*8, i*8+8);
        }
        
        
        //12 rounds of mixing
        for (int i = 0; i < 12; i++) {
            int[] S = SIGMA[i % 10];
            
            mix(V,  0,  4,  8, 12, m[S[ 0]], m[S[ 1]]);
            mix(V,  1,  5,  9, 13, m[S[ 2]], m[S[ 3]]);
            mix(V,  2,  6, 10, 14, m[S[ 4]], m[S[ 5]]);
            mix(V,  3,  7, 11, 15, m[S[ 6]], m[S[ 7]]);
            
            mix(V,  0,  5, 10, 15, m[S[ 8]], m[S[ 9]]);
            mix(V,  1,  6, 11, 12, m[S[10]], m[S[11]]);
            mix(V,  2,  7,  8, 13, m[S[12]], m[S[13]]);
            mix(V,  3,  4,  9, 14, m[S[14]], m[S[15]]);
        }
        
        //mix upper and lower halves of V into h
        for (int i = 0; i < 8; i++) {
            h[i] ^= V[i];
            h[i] ^= V[i+8];
        }
    }
     
    private void mix(long[] v, int a, int b, int c, int d, long x, long y) {
        v[a] += v[b] + x;
        v[d] = Long.rotateRight(v[d] ^ v[a], 32);
        
        v[c] += v[d];
        v[b] = Long.rotateRight(v[b] ^ v[c], 24);
        
        v[a] += v[b] + y;
        v[d] = Long.rotateRight(v[d] ^ v[a], 16);
        
        v[c] += v[d];
        v[b] = Long.rotateRight(v[b] ^ v[c], 63);
    }
}