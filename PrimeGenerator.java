import java.util.*;
import java.math.BigInteger;

public class PrimeGenerator {
    private static final Random RANDOM = new Random (System.currentTimeMillis());
    private static final boolean[] SIEVE = sieve(1000);
    private static final int NUMBERS_OF_TRIAL = 64;
    
    public static BigInteger generatePrime(int n) {
        while (true) {
            BigInteger primeCandidate = generateLowLevelPrime(n);
            if (!isMillerPrime(primeCandidate,NUMBERS_OF_TRIAL)) {
                continue;
            } else {
                return primeCandidate;
            }
        }
    }
    
    private static BigInteger generateLowLevelPrime(int n) {
        while (true) {
            BigInteger primeCandidate = nBitOddRandom(n);
            boolean isPrime = true;
            for (int i = 2; i < 1000 && BigInteger.valueOf(i * i).compareTo(primeCandidate) < 0; i++) {
                if (SIEVE[i] && primeCandidate.mod(BigInteger.valueOf(i)).equals(BigInteger.ZERO)){
                    isPrime = false;
                    break;
                }
            }
            if (isPrime) {
                return primeCandidate;
            }
        }
    }
    
    private static BigInteger nBitOddRandom(int n) {
        BigInteger x = new BigInteger(n-1, RANDOM);
        x = x.add(BigInteger.TWO.pow(n-1).add(BigInteger.ONE));
   
        return x.mod(BigInteger.TWO).equals(BigInteger.ZERO) ? x.subtract(BigInteger.ONE) : x;
    }
    
    private static boolean isMillerPrime (BigInteger n, int k) {
        if (n.compareTo(BigInteger.ONE) <= 0 || n.equals(BigInteger.valueOf(4))) {
            return false;
        } else if (n.compareTo(BigInteger.valueOf(3)) <= 0) {
            return true;
        }
        
        BigInteger d = n.subtract(BigInteger.ONE);
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
        }
        
        for (int i = 0; i < k; i++) {
            if (!millerTest(n,d)) {
                return false;
            }
        }
        
        return true;
    }
    
    private static boolean millerTest(BigInteger n, BigInteger d) {
        BigInteger a = random(n.subtract(BigInteger.valueOf(3))).add(BigInteger.TWO);
        BigInteger x = power (a,d,n);
        if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) {
            return true;
        }
        
        while (!d.equals(n.subtract(BigInteger.ONE))) {
            x = x.multiply(x).mod(n);
            d = d.multiply(BigInteger.TWO);
            if (x.equals(BigInteger.ONE)) {
                return false;
            } else if (x.equals(n.subtract(BigInteger.ONE))) {
                return true;
            }
        }
        return false;
    }
    
    private static BigInteger power (BigInteger x, BigInteger y, BigInteger p) {
        BigInteger res = BigInteger.ONE;
        x = x.mod(p);
        
        while (y.compareTo(BigInteger.ZERO) > 0) {
            if (y.mod(BigInteger.TWO).equals(BigInteger.ONE)) {
                res = res.multiply(x).mod(p);
            }
            y = y.divide(BigInteger.TWO);
            x = x.multiply(x).mod(p);
        }
        
        return res;
    }
    
    private static BigInteger random (BigInteger n) {
        BigInteger randomNum;
        do {
            randomNum = new BigInteger (n.bitLength(), RANDOM);
        } while (randomNum.compareTo(n) >= 0);
        return randomNum;
    }
    
    private static boolean[] sieve (int n) {
        boolean[] sieve = new boolean[n+1];
        Arrays.fill(sieve, true);
        sieve[0] = false;
        sieve[1] = false;
        
        for (int i = 2; i * i < n; i++) {
            if (sieve[i]) {
                for (int j = i * i; j <= n; j += i) {
                    sieve [j] = false;
                }
            }
        }
        
        return sieve;
    }
}
