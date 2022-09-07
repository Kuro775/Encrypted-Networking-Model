
import java.util.Random;
import java.util.Arrays;

public class Test3 {
    private static final Random ran = new Random();
    
    public static void main() {
        SHA3 sha = new SHA3.SHA512();
        
        String plain = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmno";
        byte[] p = plain.getBytes();
        
        for (int i = 0; i < 16777216; i++) {
            sha.update(p);
        }
        byte[] c = sha.digest();
        System.out.println(Byte_Manip.bytesToHex(c));
    }
    
    public static long getRandom() {
        long x = ran.nextLong();
        return ran.nextBoolean()? x : -x;
    }
}