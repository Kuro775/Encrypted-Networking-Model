import java.util.Arrays;

public class SHA384 {
    private static final long[] H0 =
    {0xcbbb9d5dc1059ed8L, 0x629a292a367cd507L, 
     0x9159015a3070dd17L, 0x152fecd8f70e5939L, 
     0x67332667ffc00b31L, 0x8eb44a8768581511L, 
     0xdb0c2e0d64f98fa7L, 0x47b5481dbefa4fa4L};
     
    private static final long[] k = 
    {0x428a2f98d728ae22L, 0x7137449123ef65cdL, 0xb5c0fbcfec4d3b2fL, 0xe9b5dba58189dbbcL, 0x3956c25bf348b538L, 
     0x59f111f1b605d019L, 0x923f82a4af194f9bL, 0xab1c5ed5da6d8118L, 0xd807aa98a3030242L, 0x12835b0145706fbeL, 
     0x243185be4ee4b28cL, 0x550c7dc3d5ffb4e2L, 0x72be5d74f27b896fL, 0x80deb1fe3b1696b1L, 0x9bdc06a725c71235L, 
     0xc19bf174cf692694L, 0xe49b69c19ef14ad2L, 0xefbe4786384f25e3L, 0x0fc19dc68b8cd5b5L, 0x240ca1cc77ac9c65L, 
     0x2de92c6f592b0275L, 0x4a7484aa6ea6e483L, 0x5cb0a9dcbd41fbd4L, 0x76f988da831153b5L, 0x983e5152ee66dfabL, 
     0xa831c66d2db43210L, 0xb00327c898fb213fL, 0xbf597fc7beef0ee4L, 0xc6e00bf33da88fc2L, 0xd5a79147930aa725L, 
     0x06ca6351e003826fL, 0x142929670a0e6e70L, 0x27b70a8546d22ffcL, 0x2e1b21385c26c926L, 0x4d2c6dfc5ac42aedL, 
     0x53380d139d95b3dfL, 0x650a73548baf63deL, 0x766a0abb3c77b2a8L, 0x81c2c92e47edaee6L, 0x92722c851482353bL, 
     0xa2bfe8a14cf10364L, 0xa81a664bbc423001L, 0xc24b8b70d0f89791L, 0xc76c51a30654be30L, 0xd192e819d6ef5218L, 
     0xd69906245565a910L, 0xf40e35855771202aL, 0x106aa07032bbd1b8L, 0x19a4c116b8d2d0c8L, 0x1e376c085141ab53L, 
     0x2748774cdf8eeb99L, 0x34b0bcb5e19b48a8L, 0x391c0cb3c5c95a63L, 0x4ed8aa4ae3418acbL, 0x5b9cca4f7763e373L, 
     0x682e6ff3d6b2b8a3L, 0x748f82ee5defb2fcL, 0x78a5636f43172f60L, 0x84c87814a1f0ab72L, 0x8cc702081a6439ecL, 
     0x90befffa23631e28L, 0xa4506cebde82bde9L, 0xbef9a3f7b2c67915L, 0xc67178f2e372532bL, 0xca273eceea26619cL, 
     0xd186b8c721c0c207L, 0xeada7dd6cde0eb1eL, 0xf57d4f7fee6ed178L, 0x06f067aa72176fbaL, 0x0a637dc5a2c898a6L, 
     0x113f9804bef90daeL, 0x1b710b35131c471bL, 0x28db77f523047d84L, 0x32caab7b40c72493L, 0x3c9ebe0a15c9bebcL, 
     0x431d67c49c100d4cL, 0x4cc5d4becb3e42b6L, 0x597f299cfc657e2aL, 0x5fcb6fab3ad6faecL, 0x6c44198c4a475817L};
    
    private static final int BLOCK_SIZE = 128;
    private static final int DIGEST_LENGTH = 48;
    private static final int MAX_SIZE = 16;
    
    private final byte[] buffer;
    private long[] h;
    private long t0;
    private long t1;
    private int count;
    
    public SHA384() {
        this.buffer = new byte[BLOCK_SIZE];
        reset();
    }
    
    private SHA384(SHA384 digest) {
        this.buffer = Arrays.copyOf(digest.buffer, digest.buffer.length);
        this.h = Arrays.copyOf(digest.h, digest.h.length);
        this.t0 = digest.t0;
        this.t1 = digest.t1;
        this.count = digest.count;
    }
    
    public String algorithm() {
        return "SHA384";
    }
    
    public int length() {
        return DIGEST_LENGTH;
    }
    
    public SHA384 copy() {
        return new SHA384(this);
    }
    
    public SHA384 reset() {
        t0 = 0L;
        t1 = 0L;
        h = Arrays.copyOf(H0, H0.length);
        count = 0;
        return this;
    }
    
    public SHA384 update(byte input) {
        if (count == BLOCK_SIZE) {
            processBuffer();
        }
        buffer[count++] = input;
        return this;
    }
    
    public SHA384 update(byte... input) {
        return update(input, 0, input.length);
    }
    
    public SHA384 update(byte[] input, int off, int len) {
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
        
        t0 += count * 8;
        if (t0 == 0 && count != 0) {
            t1++;
            if (t1 == 0) {
                throw new IllegalStateException();
            }
        }
        
        System.arraycopy(Byte_Manip.longToBytes(t1), 0, buffer, BLOCK_SIZE - 16, 8);
        System.arraycopy(Byte_Manip.longToBytes(t0), 0, buffer, BLOCK_SIZE -  8, 8);
        processBuffer();
        byte[] out = new byte[DIGEST_LENGTH];
        System.arraycopy(Byte_Manip.longsToBytes(h), 0, out, 0, DIGEST_LENGTH);
        reset();
        return out;
    }
    
    private void processBuffer() {
        //add count to size
        t0 += count * 8;
        if (t0 == 0 && count != 0) {
            t1++;
            if (t1 == 0) {
                throw new IllegalStateException();
            }
        }
        count = 0;
        
        //turn buffer into int array
        long[] chunk = Byte_Manip.bytesToLongs(buffer);
        
        //prep message schedule
        long[] W = new long[80];
        System.arraycopy(chunk, 0, W, 0, 16);
        for (int i = 16; i < 80; i++) {
            W[i] = W[i-16] + smallSig0(W[i-15]) + W[i-7] + smallSig1(W[i-2]);
        }
        
        //initialize working variables
        long[] temp = Arrays.copyOf(h, h.length);
        
        //compress function
        for (int i = 0; i < 80; i++) {
            long t1 = temp[7] + bigSig1(temp[4]) + ch(temp[4],temp[5],temp[6]) + k[i] + W[i];
            long t2 = bigSig0(temp[0]) + maj(temp[0],temp[1],temp[2]);
            System.arraycopy(temp,0,temp,1,temp.length-1);
            temp[4] += t1;
            temp[0] = t1 + t2;
        }
        
        //add temp to H
        for (int i = 0; i < h.length; i++) {
            h[i] += temp[i];
        }
    }
    
    private static long smallSig0(long x) {
        return Long.rotateRight(x, 1) ^ Long.rotateRight(x, 8) ^ (x >>>  7);
    }
    
    private static long smallSig1(long x) {
        return Long.rotateRight(x,19) ^ Long.rotateRight(x,61) ^ (x >>>  6);
    }
    
    private static long bigSig1(long x) {
        return Long.rotateRight(x,14) ^ Long.rotateRight(x,18) ^ Long.rotateRight(x,41);
    }
    
    private static long bigSig0(long x) {
        return Long.rotateRight(x,28) ^ Long.rotateRight(x,34) ^ Long.rotateRight(x,39);
    }
    
    private static long ch(long x, long y, long z) {
        return (x & y) ^ (~x & z);
    }
    
    private static long maj(long x,long y, long z) {
        return (x & y) ^ (x & z) ^ (y & z);
    }
}
    
