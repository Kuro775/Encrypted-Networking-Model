import java.math.BigInteger;
import java.util.*;

public class RSA{
    private static final Base64.Encoder encoder = Base64.getEncoder();
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static final BigInteger e = BigInteger.valueOf(65537);
    private BigInteger privateKey;
    private BigInteger publicKey;
    
    public RSA() {
        this(128);
    }
        
    public RSA(int bit) {
        BigInteger p = PrimeGenerator.generatePrime(bit);
        BigInteger q = PrimeGenerator.generatePrime(bit);
        publicKey = p.multiply(q); // n
        BigInteger c = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        privateKey = modInverse(e,c);
    }
    
    public String getPublicKey() {
        return BigIntegerToBase64(publicKey);
    }
    
    public static String encrypt(String str, String key) {
        BigInteger m = StringToBigInteger(str);
        BigInteger k = Base64ToBigInteger(key);
        BigInteger c = m.modPow(e,k);
        return BigIntegerToBase64(c);
    }
    
    public String decrypt(String str) {
        BigInteger c = Base64ToBigInteger(str);
        BigInteger m = c.modPow(privateKey,publicKey);
        return BigIntegerToString(m);
    }
    
    private BigInteger lcm(BigInteger x, BigInteger y) {
        BigInteger gcd = x.gcd(y);
        BigInteger absProd = x.multiply(y).abs();
        return absProd.divide(gcd);
    }
    
    private static String BigIntegerToString(BigInteger x) {
        String info = x.toString(2);
        StringBuilder res = new StringBuilder();
        int offSet = info.length() % 8;
        res.append((char) Integer.parseInt(info.substring(0,offSet),2));
        for (int i = offSet; i < info.length(); i += 8) {
            String temp = info.substring(i, i + 8);
            res.append((char) Integer.parseInt(temp,2));
        }
        return res.toString();
    }
    
    private static BigInteger StringToBigInteger(String x) {
        StringBuilder bin = new StringBuilder();
        char[] chars = x.toCharArray();
        for (char ch : chars) {
            bin.append(String.format("%8s", Integer.toBinaryString(ch)).replaceAll(" ", "0"));
        }
        return new BigInteger(bin.toString(), 2);
    }
    
    private static String BigIntegerToBase64(BigInteger x) {
        byte[] bytes = x.toByteArray();
        return encoder.encodeToString(bytes);
    }
    
    private static BigInteger Base64ToBigInteger(String x) {
        byte[] bytes = decoder.decode(x);
        return new BigInteger(bytes);
    }
    
    private BigInteger modInverse(BigInteger a, BigInteger m) {
        BigInteger m0 = m;
        BigInteger x = BigInteger.ONE;
        BigInteger y = BigInteger.ZERO;
        if (m.equals(BigInteger.ONE)) {
            return BigInteger.ZERO;
        }
        
        while (a.compareTo(BigInteger.ONE) > 0) {
            BigInteger q = a.divide(m);
            
            BigInteger t = m;
            m = a.mod(m);
            a = t;
            
            t = y;
            y = x.subtract(q.multiply(y));
            x = t;
        }
        
        if (x.compareTo(BigInteger.ZERO) < 0) {
            x = x.add(m0);
        }
        return x;
    }
}