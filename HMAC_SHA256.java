import java.util.Arrays;

public class HMAC_SHA256 {
    private static final int BLOCK_BITS = 512;
    private static final int BLOCK_BYTES = BLOCK_BITS / 8;
    
    private static final int OUTPUT_BITS = 256;
    private static final int OUTPUT_BYTES = OUTPUT_BITS / 8;    
    
    public static String generateMAC(String key, String msg) {
        return SHA256.bytesToHex(generateMAC(msg.getBytes(), key.getBytes()));
    }
    
    public static byte[] generateMAC(byte[] key, byte[] msg) {
        //preprocess key to desired length
        if (key.length > BLOCK_BYTES) {
            key = SHA256.hash(key);
        } else if (key.length < BLOCK_BYTES) {
            key = pad(key);
        }
        
        //initialize 0x5c and 0x36 byte block
        byte[] o_key_pad = new byte[BLOCK_BYTES];
        Arrays.fill(o_key_pad, (byte) 0x5c);
        
        byte[] i_key_pad = new byte[BLOCK_BYTES];
        Arrays.fill(i_key_pad, (byte) 0x36);
        
        //initialize o_key_pad & i_key_pad
        for (int i = 0, n = BLOCK_BYTES; i < n; i++) {
            o_key_pad[i] = (byte) (o_key_pad[i] ^ key[i]);
            i_key_pad[i] = (byte) (i_key_pad[i] ^ key[i]);
        }
        
        //compute mac
        return SHA256.hash(concat(o_key_pad, SHA256.hash(concat(i_key_pad, msg)))); 
    }
    
    private static byte[] concat(byte[] b1, byte[] b2){
        byte[] c = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, c, 0, b1.length);
        System.arraycopy(b2, 0, c, b1.length, b2.length);
        
        return c;
    }
    
    private static byte[] pad(byte[] bytes) {
        byte[] c = new byte[BLOCK_BYTES];
        System.arraycopy(bytes, 0, c, 0, bytes.length);
        
        return c;
    }   
}