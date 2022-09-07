import java.util.Scanner;
import java.io.*;

public class Test {
    public static void main() throws FileNotFoundException{
        String plain = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000000; i++) {
            sb.append("a");
        }
        plain = sb.toString(); 
        
        SHA3 sha1 = new SHA3.SHA224();
        byte[] b1 = sha1.digest(plain.getBytes());
        String hex1 = Byte_Manip.bytesToHex(b1);
        
        SHA3 sha2 = new SHA3.SHA256();
        byte[] b2 = sha2.digest(plain.getBytes());
        String hex2 = Byte_Manip.bytesToHex(b2);
        
        SHA3 sha3 = new SHA3.SHA384();
        byte[] b3 = sha3.digest(plain.getBytes());
        String hex3 = Byte_Manip.bytesToHex(b3);
        
        SHA3 sha4 = new SHA3.SHA512();
        byte[] b4 = sha4.digest(plain.getBytes());
        String hex4 = Byte_Manip.bytesToHex(b4);
        
        System.out.println(hex1);
        System.out.println(hex2);
        System.out.println(hex3);
        System.out.println(hex4);
    }
}