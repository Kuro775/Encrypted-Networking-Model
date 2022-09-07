import java.util.Arrays;

public class SHA1 {
    private static final int[] H0 =
    {0x67452301, 0xefcdab89, 0x98badcfe, 0x10325476, 0xc3d2e1f0};
    
    private static final int[] K = 
    {0x5a827999, 0x6ed9eba1, 0x8f1bbcdc, 0xca62c1d6};
    
    private static final int BLOCK_SIZE = 64;
    private static final int DIGEST_LENGTH = 20;
    private static final int MAX_SIZE = 8;
    
    private final byte[] buffer;
    private int[] h;
    private long t;
    private int count;
    
    public SHA1() {
        this.buffer = new byte[BLOCK_SIZE];
        reset();
    }
    
    private SHA1(SHA1 digest) {
        this.buffer = Arrays.copyOf(digest.buffer, digest.buffer.length);
        this.h = Arrays.copyOf(digest.h, digest.h.length);
        this.t = digest.t;
        this.count = digest.count;
    }
    
    public String algorithm() {
        return "SHA1";
    }
    
    public int length() {
        return DIGEST_LENGTH;
    }
    
    public SHA1 copy() {
        return new SHA1(this);
    }
    
    public SHA1 reset() {
        t = 0;
        h = Arrays.copyOf(H0, H0.length);
        count = 0;
        return this;
    }
    
    public SHA1 update(byte input) {
        if (count == BLOCK_SIZE) {
            processBuffer();
        }
        buffer[count++] = input;
        return this;
    }
    
    public SHA1 update(byte... input) {
        return update(input, 0, input.length);
    }
    
    public SHA1 update(byte[] input, int off, int len) {
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
        int[] W = new int[80];
        System.arraycopy(chunk, 0, W, 0, 16);
        for (int i = 16; i < 80; i++) {
            W[i] = Integer.rotateLeft(W[i-3] ^ W[i-8] ^ W[i-14] ^ W[i-16], 1);
        }
        
        //initialize working variables
        int[] temp = Arrays.copyOf(h, h.length);
        
        //compress function
        int f = 0;
        int k = 0;
        for (int i = 0; i < 80; i++) {
            if (i < 20) {
                f = (temp[1] & temp[2]) | ((~temp[1]) & temp[3]);
                k = 0x5a827999;
            } else if (i < 40) {
                f = temp[1] ^ temp[2] ^ temp[3];
                k = 0x6ed9eba1;
            } else if (i < 60) {
                f = (temp[1] & temp[2]) | (temp[1] & temp[3]) | (temp[2] & temp[3]);
                k = 0x8f1bbcdc;
            } else {
                f = temp[1] ^ temp[2] ^ temp[3];
                k = 0xca62c1d6;
            }
            
            int x = Integer.rotateLeft(temp[0],5) + f + temp[4] + k + W[i];
            System.arraycopy(temp, 0, temp, 1, temp.length-1);
            temp[2] = Integer.rotateLeft(temp[2], 30);
            temp[0] = x;
        }
        
        //add temp to H
        for (int i = 0; i < h.length; i++) {
            h[i] += temp[i];
        }
    }
    
    private static int smallSig0(int x) {
        return Integer.rotateRight(x, 7) ^ Integer.rotateRight(x,18) ^ (x >>>  3);
    }
}
    
