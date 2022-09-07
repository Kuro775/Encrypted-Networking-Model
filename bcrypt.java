
//This class is not tested and clearly wrong. 
//Use with caution.

public class bcrypt {
    public static void main() {
        String salt64 = "zVHmKQtGGQob.b/Nc7l9NO";
        byte[] salt = Byte_Manip.base64ToBytes(salt64);
        
        String plain = "correctbatteryhorsestapler";
        byte[] password = plain.getBytes();
        
        int cost = 4;
        
        encrypt(cost, salt , password);
    }
    
    public static byte[] encrypt(int cost, byte[] salt, byte[] password) {
        Blowfish.expensiveKeySetup(cost, salt, password);
        
        byte[] ctext = "OrpheanBeholderScryDoubt".getBytes();
        byte[][] cipher = Byte_Manip.split(ctext, 8);
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 3; j++) {
                cipher[j] = Blowfish.encrypt(cipher[j]);
            }
        }
        
        ctext = Byte_Manip.join(cipher);
        System.out.println(Byte_Manip.bytesToBase64(ctext));
        return Byte_Manip.join(salt, ctext);
    }
}