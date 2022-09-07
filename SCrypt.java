import java.util.Arrays;

public class SCrypt {
    public static void main() {
        byte[] res = scrypt ("", "", 16, 1, 1, 64);
        
        System.out.println(Byte_Manip.bytesToHex(res));
    }
    
    public static byte[] scrypt (String msg, String salt, int n, int r, int p ,int dkLen) {
        return scrypt(msg.getBytes(), salt.getBytes(), n, r, p, dkLen);
    }
    
    public static byte[] scrypt(String msg, byte[] salt, int n, int r, int p, int dkLen) {
        return scrypt(msg.getBytes(), salt, n, r, p , dkLen);
    }
    
    public static byte[] scrypt(byte[] msg, byte[] salt, int n, int r, int p, int dkLen) {
        int blockSize = 128 * r;
        
        byte[][] B = Byte_Manip.split(PBKDF2_HMAC_SHA256.PBKDF2(msg, salt, 1, blockSize * p), blockSize);
        
        for (int i = 0; i < p; i++) {
            B[i] = ROMix(B[i], n);
        }
        
        byte[] expensiveSalt = Byte_Manip.merge(B);
        
        return PBKDF2_HMAC_SHA256.PBKDF2(msg, expensiveSalt, 1, dkLen);
    }
    
    private static byte[] ROMix(byte[] bytes, int iterations) {
        //allocate large memory
        int n = binLog(iterations);
        byte[] X = bytes;
        byte[][] V = new byte[iterations][bytes.length];
        
        //create iterations copies of X
        for (int i = 0; i < iterations; i++) {
            V[i] = X;
            X = blockMix(X);
        }
        
        //mixing with the iteration
        for (int i = 0; i < iterations; i++) {
            int j = integerify(X,n);
            X = blockMix(Byte_Manip.xor(X,V[j]));
        }
        
        return X;
    }
    
    // returns logbase2 of n
    // returns 0 if n = 0
    private static int binLog(int n) {
        int log = 0;
        if((n & 0xffff0000 ) != 0 ) { n >>>= 16; log = 16; }
        if( n >= 256 )              { n >>>=  8; log += 8; }
        if( n >= 16  )              { n >>>=  4; log += 4; }
        if( n >= 4   )              { n >>>=  2; log += 2; }
        return log + (n >>> 1);
    }
    
    //return last n bit as little endian integer
    private static int integerify(byte[] bytes, int n) {
        if (n > 31) {
            throw new IllegalArgumentException();
        }
        int res = Byte_Manip.rBytesToInt(bytes, bytes.length - 64, bytes.length-60);

        int mask = ~(~0 << n);
        return res & mask;
    }
    
    private static byte[] blockMix(byte[] bytes) {
        //break up input into 2r 64-byte chunk
        int r = bytes.length / 128;
        byte[][] B = new byte[2 * r][64];
        int start = 0;
        for (int i = 0; i < 2 * r; i++) {
            System.arraycopy(bytes, start, B[i], 0, 64);
            start += 64;
        }
        
        //applied block mix
        byte[][] Y = new byte [2 * r][64];
        byte[] X = B[2 * r - 1]; // is this safe?
        for (int i = 0; i < 2 * r; i++) {
            X = salsa20_8(Byte_Manip.xor(X, B[i]));
            System.arraycopy(X,0,Y[i],0,64);
        }
        
        //concat Y and return
        byte[] res = new byte[bytes.length];
        start = 0;
        for (int i = 0; i < 2 * r; i+=2) {
            System.arraycopy(Y[i],0,res,start,64);
            start += 64;
        }
        for (int i = 1; i < 2 * r; i+=2) {
            System.arraycopy(Y[i],0,res,start,64);
            start += 64;
        }
        return res;
    }
    
    //rotate left bitwise
    private static int ROTL(int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }
    
    //function for salsa mix
    private static void QR(int[] arr, int a, int b, int c, int d) {
        arr[b] ^= ROTL(arr[a] + arr[d], 7);
        arr[c] ^= ROTL(arr[b] + arr[a], 9); 
        arr[d] ^= ROTL(arr[c] + arr[b],13); 
        arr[a] ^= ROTL(arr[d] + arr[c],18);
    }
    
    //Salsa mix
    //input of 64 bytes
    private static byte[] salsa20_8(byte[] bytes) {
        int[] b = Byte_Manip.rBytesToInts(bytes);
        int[] x = new int[16];
        System.arraycopy(b,0,x,0,16);
        for (int i = 0; i < 8; i += 2) {
            //Odd rounds
            QR (x, 0, 4, 8,12);
            QR (x, 5, 9,13, 1);
            QR (x,10,14, 2, 6);
            QR (x,15, 3, 7,11);
            //Even rounds
            QR (x, 0, 1, 2, 3);
            QR (x, 5, 6, 7, 4);
            QR (x,10,11, 8, 9);
            QR (x,15,12,13,14);
        }
        for (int i = 0; i < 16; i++) {
            x[i] += b[i];
        }
        return Byte_Manip.rIntsToBytes(x);
    }
}