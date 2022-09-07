import java.util.Arrays;
import java.util.Base64;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Byte_Manip {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 == 1) {
            throw new IllegalArgumentException("The binary key cannot have odd number digits");
        }
        
        byte[] res = new byte[hex.length() >> 1];
        for (int i = 0, n = hex.length() >> 1; i < n; i++) {
            res[i] = (byte) ((getHexVal(hex.charAt(i << 1)) << 4) 
                           + (getHexVal(hex.charAt((i << 1) + 1))));
        }
        
        return res;
    }
    
    private static int getHexVal(char hex) {
        int val = (int)hex;
        return val - (val < 58 ? 48 : (val < 97 ? 55 : 87));
    }
    
    public static String bytesToBase64(byte[] bytes) {
        return encoder.encodeToString(bytes);
    }
    
    public static byte[] base64ToBytes(String encoded) {
        return decoder.decode(encoded);
    }
    
    public static String bytesToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replaceAll(" ", "0"));
        }
        return sb.toString();
    }
    
    public static byte[] binaryToBytes(String str) {
        if (str.length() % 8 != 0) {
            throw new IllegalArgumentException("string length must be multiple of 8");
        }
        String[] arr = splitEqually(str, 8);
        byte[] res = new byte[arr.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) Integer.valueOf(arr[i], 2).intValue();
        }
        return res;
    }
    
    private static String[] splitEqually(String str, int size) {
        String[] res = new String[str.length()/ size];
        for (int start = 0; start < str.length(); start += size) {
            res[start/size] = str.substring(start, Math.min(str.length(), start + size));
        }
        return res;
    }
    
    public static byte[] xor(byte[] arr1, byte[] arr2) {
        int max = Math.max(arr1.length, arr2.length);
        int min = Math.min(arr1.length, arr2.length);
        byte[] res = new byte[max];
        for (int i = 0; i < min; i++) {
            res[i] = (byte) (arr1[i] ^ arr2[i]);
        }
        
        if (arr1.length > arr2.length) {
            System.arraycopy(arr1, min, res, min, max - min);
        } else if (arr2.length < arr1.length) {
            System.arraycopy(arr2, min, res, min , max - min);
        }
        return res;
    }
    
    public static byte[] add(byte[] arr1, byte[] arr2) {
        int max = Math.max(arr1.length, arr2.length);
        int c = 0;
        byte[] res = new byte[max];
        for (int i = 1; i <= max; i++) {
            int x = i <= arr1.length? arr1[arr1.length - i] : 0;
            int y = i <= arr2.length? arr2[arr2.length - i] : 0;
            x = x < 0? x + 256 : x;
            y = y < 0? y + 256 : y;
            c += x + y;
            res[max - i] = (byte) c;
            c >>= 8;
        }
        return res;
    }
    
    public static byte[] radd(byte[] arr1, byte[] arr2) {
        int max = Math.max(arr1.length, arr2.length);
        int c = 0;
        byte[] res = new byte[max];
        for (int i = 0; i < max; i++) {
            int x = i < arr1.length? arr1[i] : 0;
            int y = i < arr2.length? arr2[i] : 0;
            x = x < 0? x + 256 : x;
            y = y < 0? y + 256 : y;
            c += x + y;
            res[i] = (byte) c;
            c >>= 8;
        }
        return res;
    }
    
    public static byte[] subtract(byte[] arr1, byte[] arr2) {
        return add(arr1, changeSign(arr2));
    }
    
    public static byte[] rsubtract(byte[] arr1, byte[] arr2) {
        return radd(arr1, rChangeSign(arr2));
    }
    
    public static int compare(byte[] arr1, byte[] arr2) {
        byte[] longer = arr1;
        byte[] shorter = arr2;
        int res = 1;
        if (arr1.length < arr2.length) {
            shorter = longer;
            longer = arr2;
            res = -res;
        }
        int diff = Math.abs(arr1.length - arr2.length);
        
        for (int i = 0; i < diff; i++) {
            if (longer[i] != 0) {
                return res;
            }
        }
        
        int len = longer.length - diff;
        if (longer[diff] >= 0 && shorter[diff] < 0) {
            return res;
        } else if (longer[diff] < 0 && shorter[diff] >= 0) {
            return -res;
        }
        
        for (int i = 0; i < len; i++) {
            int c = (longer[i+diff] & 0xFF) - (shorter[i] & 0xFF);
            if (c != 0) {
                return c * res;
            }
        }
        return 0;
    }
    
    public static int rCompare(byte[] arr1, byte[] arr2) {
        byte[] longer = arr1;
        byte[] shorter = arr2;
        int res = 1;
        if (arr1.length < arr2.length) {
            shorter = longer;
            longer = arr2;
            res = -res;
        }
        int diff = Math.abs(arr1.length - arr2.length);
        
        for (int i = 1; i <= diff; i++) {
            if (longer[longer.length - i] != 0) {
                return res;
            }
        }
        
        int len = longer.length - diff - 1;
        if (longer[len] >= 0 && shorter[len] < 0) {
            return res;
        } else if (longer[len] < 0 && shorter[len] >= 0) {
            return -res;
        }
        
        for (int i = len; i >= 0; i--) {
            int c = Integer.compare((longer[i] & 0xFF), (shorter[i] & 0xFF));
            System.out.println(c);
            if (c != 0) {
                return c * res;
            }
        }
        return 0;
    }
    
    public static byte[] pad(byte[] bytes, int len) {
        byte[] res = new byte[len];
        System.arraycopy(bytes, 0, res, 0, bytes.length);
        return res;
    }
    
    
    //byte to int and reverse
    public static int bytesToInt(byte[] b) {
        return bytesToInt(b,0,b.length);
    }
    
    public static int bytesToInt(byte[] b, int start, int end) {
        if (end - start != 4) {
            throw new IllegalArgumentException("interval must be 4");
        } else if (end  > b.length) {
            throw new IllegalArgumentException("end must be smaller or equal to array length");
        }
        return ((b[start + 0] & 0xFF) << 24) | 
               ((b[start + 1] & 0xFF) << 16) | 
               ((b[start + 2] & 0xFF) << 8 ) | 
               ((b[start + 3] & 0xFF) << 0 );
    }
    
    public static int[] bytesToInts(byte[] b) {
        if (b.length % 4 != 0) {
            throw new IllegalArgumentException("byte array must be divisible by 4");
        }
        int[] res = new int[b.length/4];
        int i;
        for (i = 0; i < res.length; i++) {
            res[i] = bytesToInt(b,i * 4, i * 4 + 4);
        }
        return res;
    }
    
    public static byte[] intToBytes(int x) {
        return new byte[] {
               (byte)(x >>> 24),
               (byte)(x >>> 16),
               (byte)(x >>> 8),
               (byte) x};
    }
    
    public static byte[] intsToBytes(int[] x) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i : x) {
                baos.write(intToBytes(i));
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    public static int rBytesToInt(byte[] b) {
        return rBytesToInt(b,0,b.length);
    }
    
    public static int rBytesToInt(byte[] b, int start, int end) {
        if (end - start != 4) {
            throw new IllegalArgumentException("interval must be 4");
        } else if (end  > b.length) {
            throw new IllegalArgumentException("end must be smaller or equal to array length");
        }
        return ((b[start + 3] & 0xFF) << 24) | 
               ((b[start + 2] & 0xFF) << 16) | 
               ((b[start + 1] & 0xFF) << 8 ) | 
               ((b[start + 0] & 0xFF) << 0 );
    }
    
    public static int[] rBytesToInts(byte[] b) {
        if (b.length % 4 != 0) {
            throw new IllegalArgumentException("byte array must be divisible by 4");
        }
        int[] res = new int[b.length/4];
        int i;
        for (i = 0; i < res.length; i++) {
            res[i] = rBytesToInt(b,i * 4, i * 4 + 4);
        }
        return res;
    }
    
    public static byte[] rIntToBytes(int x) {
        return new byte[] {
               (byte)  x,
               (byte) (x >>>  8),
               (byte) (x >>> 16),
               (byte) (x >>> 24)};
    }
    
    public static byte[] rIntsToBytes(int[] x) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i : x) {
                baos.write(rIntToBytes(i));
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    //byte to long and reverse
    public static long bytesToLong(byte[] b) {
        return bytesToLong(b,0,b.length);
    }
    
    public static long bytesToLong(byte[] b, int start, int end) {
        if (end - start != 8) {
            throw new IllegalArgumentException("interval must be 8");
        } else if (end > b.length) {
            throw new IllegalArgumentException("end must be smaller or equal to array length");
        }
        long res = 0;
        for (int i = start; i < end; i++) {
            res = (res << 8) | (b[i] & 0xFF);
        }
        return res;
    }
    
    public static long[] bytesToLongs(byte[] b) {
        if (b.length % 8 != 0) {
            throw new IllegalArgumentException("byte array must be divisible by 4");
        }
        long[] res = new long[b.length/8];
        for (int i = 0; i < res.length; i++) {
            res[i] = bytesToLong(b,i * 8, i * 8 + 8);
        }
        return res;
    }
    
    public static byte[] longToBytes(long x) {
        return new byte[] {
               (byte)(x >>> 56),
               (byte)(x >>> 48),
               (byte)(x >>> 40),
               (byte)(x >>> 32),
               (byte)(x >>> 24),
               (byte)(x >>> 16),
               (byte)(x >>>  8),
               (byte) x};
    }
    
    public static byte[] longsToBytes(long[] x) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (long i : x) {
                baos.write(longToBytes(i));
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    public static long rBytesToLong(byte[] b) {
        return rBytesToLong(b,0,b.length);
    }
    
    public static long rBytesToLong(byte[] b, int start, int end) {
        if (end - start != 8) {
            throw new IllegalArgumentException("interval must be 8");
        } else if (end  > b.length) {
            throw new IllegalArgumentException("end must be smaller or equal to array length");
        }
        long res = 0;
        for (int i = end - 1; i >= start; i--) {
            res = (res << 8) | (b[i] & 0xFF);
        }
        return res;
    }
    
    public static long[] rBytesToLongs(byte[] b) {
        if (b.length % 8 != 0) {
            throw new IllegalArgumentException("byte array must be divisible by 4");
        }
        long[] res = new long[b.length/8];
        for (int i = 0; i < res.length; i++) {
            res[i] = rBytesToLong(b,i * 8, i * 8 + 8);
        }
        return res;
    }
    
    public static byte[] rLongToBytes(long x) {
        return new byte[] {
               (byte)  x,
               (byte) (x >>>  8),
               (byte) (x >>> 16),
               (byte) (x >>> 24),
               (byte) (x >>> 32),
               (byte) (x >>> 40),
               (byte) (x >>> 48),
               (byte) (x >>> 56)};
    }
    
    public static byte[] rLongsToBytes(long[] x) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (long i : x) {
                baos.write(rLongToBytes(i));
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    public static byte[][] split(byte[] bytes, int size) {
        byte[][] res = new byte[(int) Math.ceil(bytes.length / size)][size];
        
        int start = 0;
        for (int i = 0; i < res.length; i++) {
            if (start + size <= bytes.length) {
                System.arraycopy(bytes, start, res[i], 0, size);
            } else {
                System.arraycopy(bytes, start, res[i], 0, bytes.length - start);
            }
            start += size;
        }
        return res;
    }
    
    public static byte[] merge(byte[][] bytes) {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < bytes.length; i++) {
                baos.write(bytes[i]);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    public static byte[] join(byte[]... bs) {
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i < bs.length; i++) {
                baos.write(bs[i]);
            }
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    public static void increment (byte[] bytes) {
        int i = bytes.length - 1;
        int c = 1;
        while (i >= 0 && c != 0) {
            int x = bytes[i];
            x = x < 0? x + 256 : x;
            c += x;
            bytes[i] = (byte) c;
            c >>= 8;
            i--;
        }
    }
    
    public static void rincrement(byte[] bytes) {
        int i = 0;
        int c = 1;
        while (i < bytes.length && c != 0) {
            int x = bytes[i];
            x = x < 0? x + 256 : x;
            c += x;
            bytes[i] = (byte) c;
            c >>= 8;
            i++;
        }
    }
    
    public static byte[] changeSign(byte[] bytes) {
        byte[] res = invert(bytes);
        increment(res);
        return res;
    }
    
    public static byte[] rChangeSign(byte[] bytes) {
        byte[] res = invert(bytes);
        rincrement(res);
        return res;
    }
    
    public static byte[] changeEndian(byte[] bytes) {
        int n = bytes.length;
        byte[] res = new byte[n--];
        for (byte b : bytes) {
            res[n--] = b;
        }
        return res;
    }
    
    public static void setBit(byte[] bytes, int pos, int val) {
        if (val != 0 && val != 1) {
            throw new IllegalArgumentException("val must be 0 or 1");
        } else if (bytes.length * 8 <= pos) {
            throw new IndexOutOfBoundsException("pos is outside of byte array range");
        }
        int posByte = pos/8;
        int posBit = pos%8;
        byte oldByte = bytes[posByte];
        oldByte = (byte) (((0xFF7F >> posBit) & oldByte) & 0x00FF);
        byte newByte = (byte) ((val << (8 - (posBit + 1))) | oldByte);
        bytes[posByte] = newByte;
    }
    
    public static int getBit(byte[] bytes, int pos) {
        if (bytes.length * 8 <= pos) {
            throw new IndexOutOfBoundsException("pos is outside of byte array range");
        }
        int posByte = pos/8;
        int posBit = pos%8;
        byte val = bytes[posByte];
        return val >>(8 - (posBit+1)) & 0x0001;
    }
    
    public static byte[] rotateLeft(byte[] bytes, int step) {
        return rotateLeft(bytes, bytes.length, 0, step);
    }
    
    public static byte[] rotateLeft(byte[] bytes, int len, int step) {
        return rotateLeft(bytes, len, 0, step);
    }
    
    public static byte[] rotateLeft(byte[] bytes, int len, int off, int step) {
        if (len > bytes.length * 8 - off) {
            throw new IllegalArgumentException("rotate length must be smaller than original byte array length - offset");
        }
        int numOfBytes = (len - 1) / 8 + 1;
        byte[] res = new byte[numOfBytes];
        for (int i = 0; i < len; i++) {
            int val = getBit(bytes, (i+step) % len + off);
            setBit(res,i,val);
        }
        return res;
    }
    
    public static byte[] rotateRight(byte[] bytes, int step) {
        return rotateRight(bytes, bytes.length, 0, bytes.length - step);
    }
    
    public static byte[] rotateRight(byte[] bytes, int len, int step) {
        return rotateRight(bytes, len, 0, len - step);
    }
    
    public static byte[] rotateRight(byte[] bytes, int len, int off, int step) {
        return rotateLeft(bytes, len, off, len - step);
    }
    
    public static byte[] invert(byte[] bytes) {
        byte[] res = new byte[bytes.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) (bytes[i] ^ -1);
        }
        return res;
    }
}