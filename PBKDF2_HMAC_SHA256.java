import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PBKDF2_HMAC_SHA256 {
    private static final int BLOCK_BITS = 256;
    private static final int BLOCK_BYTES = BLOCK_BITS / 8;
        
    public static byte[] PBKDF2(String password, byte[] salt, int c, int dkLen){
        return PBKDF2(password.getBytes(), salt, c, dkLen);
    }
    
    public static byte[] PBKDF2(byte[] password, byte[] salt, int c, int dkLen){
        try {
            int block = dkLen / BLOCK_BYTES + 1;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i = 1;
            for (; i < block; i++) {
                baos.write(F(password,salt,c,i));
            }
            baos.write(F(password,salt,c,i),0,dkLen % BLOCK_BYTES);
        
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
    }
    
    private static byte[] F(byte[] password, byte[] salt, int c, int i){
        byte[] u;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(salt);
            baos.write(Byte_Manip.intToBytes(i));
            u = baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException();
        }
        u = HMAC_SHA256.generateMAC(password,u);
        
        byte[] res = new byte[u.length];
        System.arraycopy(u, 0, res, 0, u.length);
        
        for (int j = 2; j <= c; j++) {
            u = HMAC_SHA256.generateMAC(password, u);
            res = Byte_Manip.xor(res, u);
        }
        
        return res;
    }
}