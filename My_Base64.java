import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FilterInputStream;
import java.io.IOException;

public class My_Base64 {
    private static final String base64chars = "ABCDEFGHIJKLMNOPQRSTUBWXYZabcdefghijklmnopqrstuvwxyz0123456789+//";
    
    public static void main() {
        byte[] b = {-1, -1,-1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1-1, -1,-1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1,-1, -1, -1, -1, -1};
        byte[] lineSeparator = {13,10,10};
        System.out.println(b.length);
        Base64.Encoder encoder = Base64.getMimeEncoder(16, lineSeparator);
        byte[] res = encoder.encode(b);
        System.out.println(Arrays.toString(res));
        System.out.println(encoder.encodeToString(b));
        
        Base64.Encoder noPad = encoder.withoutPadding();
        byte[] res11 = noPad.encode(b);
        System.out.println(Arrays.toString(res));
        System.out.println(noPad.encodeToString(b));
        
        Base64.Decoder decoder = Base64.getMimeDecoder();
        byte[] p = decoder.decode(res);
        System.out.println(p.length);
        System.out.println(Arrays.toString(p));
        
        Decoder decoders = getDecoder();
        byte[] p2 = decoders.decode(res11);
        System.out.println(Arrays.toString(p2));
        
        Encoder encoders = getMimeEncoder();
        byte[] res2 = encoders.encode(b);
        String str2 = encoder.encodeToString(b);
        System.out.println(Arrays.toString(res2));
        System.out.println(encoders.encodeToString(b));
        
        Encoder noPads = encoders.withoutPadding();
        byte[] res22 = noPads.encode(b);
        System.out.println(Arrays.toString(res22));
        System.out.println(noPads.encodeToString(b));
        
        ByteBuffer bb = ByteBuffer.wrap(b);
        ByteBuffer bb2 = encoders.encode(bb);
        byte[] res3 = bb2.array();
        System.out.println(Arrays.toString(res3));
    }
    
    public static Encoder getMimeEncoder() {
        return new Encoder (true);
    }
    
    public static String encode(String s) {
        //padding, result and pad count respectively
        String p = "";
        StringBuilder r = new StringBuilder();
        int c = s.length() % 3;
        
        //add right zero pad to make s.length() multiple of 3
        if (c > 0) {
            for (; c < 3; c++) {
                p += "=";
                s += "\0";
            }
        }
        
        //increment over s, 3 characters at a time.
        for (c = 0; c < s.length(); c += 3) {
            
            // there 8-bit (ASCII) characters become one 24-bit integer
            int n = (s.charAt(c + 0) << 16) +
                    (s.charAt(c + 1) <<  8) +
                    (s.charAt(c + 2) <<  0);
            
            //24-bit integer become four 6-bit numbers
            int n1 = (n >> 18) & 63, n2 = (n >> 12) & 63,
                n3 = (n >>  6) & 63, n4 = (n >>  0) & 63;
            
            //four 6-bit number turned into Base64 according to the alphabet
            //and appended to the resulted string
            r.append(base64chars.charAt(n1));
            r.append(base64chars.charAt(n2));
            r.append(base64chars.charAt(n3));
            r.append(base64chars.charAt(n4));
        }
        return r.substring(0, r.length() - p.length()) + p;
    } 
    
    public static String decode (String s) {
        //remove all non base64 characters.
        s.replaceAll("[^" + base64chars + "]", "");
        
        //replace padding with zero pad ('=' to 'A')
        String p = (s.charAt(s.length() - 1) == '=' ?
                   (s.charAt(s.length() - 2) == '=' ? "AA" : "A") : "");
        StringBuilder r = new StringBuilder();
        s = s.substring(0, s.length() - p.length()) + p;
        
        //traverse over s, four character at a time
        for (int c = 0; c < s.length(); c += 4) {
            //concat four 6-bit base64 character to 24-bit integer
            int n =   (base64chars.indexOf(s.charAt(c + 0)) << 18)
            + (base64chars.indexOf(s.charAt(c + 1)) << 12)
            + (base64chars.indexOf(s.charAt(c + 2)) <<  6)
            + (base64chars.indexOf(s.charAt(c + 3)) <<  0);
            
            //turn 24-bit integer to 3 8-bit character
            r.append((char) ((n >>> 16) & 0xFF));
            r.append((char) ((n >>>  8) & 0xFF));
            r.append((char) ((n >>>  0) & 0xFF));
        }
    
        //remove zero pad
        return r.substring(0, r.length() - p.length());
    }
    
    public static Decoder getDecoder() {
        return new Decoder();
    }
    
    public static Encoder getEncoder() {
        return new Encoder();
    }
    public static class Encoder {
        private final String base64chars;
        private final boolean mime;
        private final byte[] lineSeparator;
        private final int lineLength;
        private final boolean pad;
        
        private Encoder() {
            this("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", false, true);
        }
        
        private Encoder(String alphabet) {
            this(alphabet, false, true);
        }
        
        private Encoder(boolean m) {
            this("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", m, true);
        }
        
        private Encoder(String alphabet, boolean m) {
            this(alphabet, false, true);
        }
        
        private Encoder(String alphabet, boolean m, boolean p) {
            if (alphabet.length() != 64) {
                System.out.println(alphabet.length());
                throw new IllegalArgumentException("alphabet must be 64 letter");
            } else if (!isUnique(alphabet)) {
                throw new IllegalArgumentException("letter in alphabet must be unique");
            }
            base64chars = alphabet;
            mime = m;
            pad = p;
            byte[] t = {10,13};
            lineSeparator = t;
            lineLength = 76;
        }
        
        private Encoder(String alphabet, boolean m, byte[] ls, int ll, boolean p) {
            if (alphabet.length() != 64) {
                System.out.println(alphabet.length());
                throw new IllegalArgumentException("alphabet must be 64 letter");
            } else if (!isUnique(alphabet)) {
                throw new IllegalArgumentException("letter in alphabet must be unique");
            }
            base64chars = alphabet;
            mime = m;
            pad = p;
            lineSeparator = ls;
            lineLength = ll/4*4;
        }
        
        public byte[] encode(byte[] bytes) {
            //find resulted length
            int resLength = (bytes.length + 2) / 3 * 4;
            if (mime) {
                resLength += (bytes.length - 1) / (lineLength/4*3) * lineSeparator.length;
            }
            byte[] res = new byte[resLength];
            
            int i = 0;
            try {
                for (int c = 0; c < bytes.length; c += 3, i += 4) {
                    //fill result array
                    if (c > 0 && (c / 3 * 4) % lineLength == 0 && mime) {
                        System.arraycopy(lineSeparator,0, res, i, lineSeparator.length);
                        i += lineSeparator.length;
                    }
                    
                    int n = (bytes[c + 0] << 16) 
                          | (bytes[c + 1] <<  8)
                          | (bytes[c + 2] <<  0);
                     
                    int n1 = (n >> 18) & 63, n2 = (n >> 12) & 63,
                        n3 = (n >>  6) & 63, n4 = (n >>  0) & 63;
                
                    res[i+0] = (byte) base64chars.charAt(n1);
                    res[i+1] = (byte) base64chars.charAt(n2);
                    res[i+2] = (byte) base64chars.charAt(n3);
                    res[i+3] = (byte) base64chars.charAt(n4);
                }
            } catch(IndexOutOfBoundsException ex) {
                //handle last block and padding
                int m = bytes.length % 3;
                byte[] temp = new byte[3];
                System.arraycopy(bytes, bytes.length - m, temp, 0, m);
                
                int n = (temp[0] << 16) 
                      | (temp[1] <<  8)
                      | (temp[2] <<  0);
                      
                int n1 = (n >> 18) & 63, n2 = (n >> 12) & 63,
                    n3 = (n >>  6) & 63;
                    
                res[i+0] = (byte) base64chars.charAt(n1);
                res[i+1] = (byte) base64chars.charAt(n2);
                res[i+2] = (byte) base64chars.charAt(n3);
                res[i+3] = (byte) '=';
                if (m == 1) {
                    res[i+2] = (byte) '=';
                }
            }
            if (!pad) {
                int l = res.length;
                int off = bytes.length % 3;
                if (off > 0) {
                    l -= (3-off);
                }
                byte[] result = new byte[l];
                System.arraycopy(res, 0, result, 0, l);
                return result;
            }
            return res;
        }
        
        public int encode (byte[] src, byte[] dst) {
            int encodedLength = (src.length + 2) / 3 * 4;
            if (mime) {
                encodedLength += (src.length - 1) / 57 * 2;
            }
            if (encodedLength > dst.length) {
                throw new IllegalArgumentException("destination array not long enough");
            }
            System.arraycopy(encode(src), 0, dst, 0, encodedLength);
            return encodedLength;
        }
        
        public ByteBuffer encode (ByteBuffer bb) {
            byte[] src = new byte[bb.remaining()];
            bb.get(src);
            return ByteBuffer.wrap(encode(src));
        }
        
        public String encodeToString(byte[] src) {
            return new String(encode(src), StandardCharsets.ISO_8859_1);
        }
        
        public Encoder withoutPadding() {
            return new Encoder(base64chars, mime, false);
        }
        
        public OutputStream wrap(OutputStream os) {
            return new Base64EncoderStream(os);
        }
        
        private class Base64EncoderStream extends FilterOutputStream {
            private byte[] buffer;
            private int c;
            
            private Base64EncoderStream(OutputStream out){
                super(out);
                buffer = new byte[3];
                c = 0;
            }
            
            public void close() throws IOException {
                if (c != 0) {
                    byte[] temp  = c == 1? new byte[1] : new byte[2];
                    System.arraycopy(buffer, 0, temp, 0, c);
                    out.write(encode(temp));
                }
                super.close();
            }
            
            public void flush() throws IOException {
                if (c == 3) {
                    out.write(encode(buffer));
                    c = 0;
                    Arrays.fill(buffer, (byte) 0);
                }
                super.flush();
            }
            
            public void write (byte[] b) throws IOException {
                write(b, 0, b.length);
            }
            
            public void write (byte[] b, int off, int len) throws IOException {
                int i = off;
                while (i < len) {
                    int n = Math.min(3-c, len-i);
                    System.arraycopy(b, i, buffer, c, n);
                    c = n;
                    i += n;
                    flush();
                }
            }
            
            public void write (int b) throws IOException {
                buffer[c] = (byte) b;
                c++;
                flush();
            }
        }
        
        private boolean isUnique(String x) {
            HashSet<Character> set = new HashSet();
            for (int i = 0; i < x.length(); i++) {
                if (set.contains(x.charAt(i))) {
                    return false;
                }
                set.add(x.charAt(i));
            }
            return true;
        }
    }
    
    public static class Decoder {
        private final String base64chars;
        private final boolean mime;
        
        private Decoder() {
            this("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", false);
        }
        
        private Decoder(String alphabet) {
            this(alphabet, false);
        }
        
        private Decoder(boolean m) {
            this("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/", m);
        }
        
        private Decoder(String alphabet, boolean m) {
            if (alphabet.length() != 64) {
                System.out.println(alphabet.length());
                throw new IllegalArgumentException("alphabet must be 64 letter");
            } else if (!isUnique(alphabet)) {
                throw new IllegalArgumentException("letter in alphabet must be unique");
            }
            base64chars = alphabet;
            mime = m;
        }
        
        public byte[] decode (byte[] bytes) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int l = bytes.length;
            int c = 0;

            for (; c < l - 4; c += 4) {
                while (mime && base64chars.indexOf(bytes[c]) == -1 && bytes[c] != '=') {
                    c++;
                }
                if (c >= l - 4) {
                    break;
                }
                
                int n = (base64chars.indexOf(bytes[c+0]) << 18)
                      + (base64chars.indexOf(bytes[c+1]) << 12)
                      + ((base64chars.indexOf(bytes[c+2]) & 0xFF) <<  6)
                      + ((base64chars.indexOf(bytes[c+3]) & 0xFF) <<  0);
                
                if (base64chars.indexOf(bytes[c+0]) == -1 || base64chars.indexOf(bytes[c+1]) == -1
                  ||base64chars.indexOf(bytes[c+2]) == -1 || base64chars.indexOf(bytes[c+3]) == -1) {
                      throw new IllegalArgumentException("byte is not in Base64 scheme");
                }
                
                baos.write((n >>> 16) & 0xFF);
                baos.write((n >>>  8) & 0xFF);
                baos.write((n >>>  0) & 0xFF);
            }
           
            
            
            if (base64chars.indexOf(bytes[c+0]) == -1 || base64chars.indexOf(bytes[c+1]) == -1) {       
                throw new IllegalArgumentException("byte is not in Base64 scheme");
            }
            int n =  (base64chars.indexOf(bytes[c+0]) << 18) + (base64chars.indexOf(bytes[c+1]) << 12);
            
            baos.write((n >>> 16) & 0xFF);
            if (c+2 != l && bytes[c+2] != '=') {
                if (base64chars.indexOf(bytes[c+2]) == -1) {
                    throw new IllegalArgumentException("byte is not in Base64 scheme");
                }
                n += (base64chars.indexOf(bytes[c+2]) <<  6);
                
                baos.write((n >>>  8) & 0xFF);
                if (c+3 != l && bytes[c+3] != '=') {
                    if (base64chars.indexOf(bytes[c+3]) == -1) {
                        throw new IllegalArgumentException("byte is not in Base64 scheme");
                    }
                    n += (base64chars.indexOf(bytes[c+3]) <<  0);
                    baos.write((n >>>  0) & 0xFF);
                }
            }
            
            return baos.toByteArray();
        }
        
        public int decode (byte[] src, byte[] dst) {
            byte[] decoded = decode(src);
            if (decoded.length > dst.length) {
                throw new IllegalArgumentException("destination array not long enough");
            }
            System.arraycopy(decode(src), 0, dst, 0, decoded.length);
            return decoded.length;
        }
        
        public ByteBuffer decode (ByteBuffer bb) {
            byte[] src = new byte[bb.remaining()];
            bb.get(src);
            return ByteBuffer.wrap(decode(src));
        }
        
        public byte[] decode (String str) {
            return decode(str.getBytes(StandardCharsets.ISO_8859_1));
        }
        
        public InputStream wrap(InputStream is) {
            return new Base64DecoderStream(is);
        }
        
        private class Base64DecoderStream extends FilterInputStream {
            private byte[] input;
            private byte[] decoded;
            private int pos;
            private int cur;
            
            private Base64DecoderStream(InputStream in) {
                this(in, 20, 15);
            }
            
            private Base64DecoderStream(InputStream in, int inputSize, int decodedSize){
                super(in);
                input = new byte[inputSize];
                decoded = new byte[decodedSize];
                pos = 0;
                cur = 0;
            }
            
            private boolean update() throws IOException{
                if (cur == 0) {
                    pos = 0;
                    int n = in.read(input);
                    if (n == -1) {
                        return false;
                    } else if (n != 20) {
                        byte[] temp = new byte[n];
                        System.arraycopy(input, 0, temp, 0, n);
                        cur = decode(temp, decoded);
                    } else {
                        cur = decode(input, decoded);
                    }
                }
                return cur != 0;
            }
            
            public int read() throws IOException {
                update();
                pos++;
                cur--;
                return decoded[pos-1];
            }
            
            public int read(byte[] b) throws IOException {
                return read(b, 0, b.length);
            }
            
            public int read(byte[] b, int off, int length) throws IOException {
                int count = 0;
                while (update() && count < length) {
                    int l = Math.min(cur, length - count);
                    System.arraycopy(decoded, pos, b, off + count, l);
                    count += l;
                    pos += l;
                    cur -= l;
                }
                if (!update()) {
                    return -1;
                }
                return count;
            }
            
            public long skip(long n) throws IOException {
                int count = 0;
                while (count < n && update()) {
                    int l = (int) Math.min(n - count, cur);
                    count += l;
                    cur -= l;
                    pos += l;
                }
                return count;
            }
        }
        
        private boolean isUnique(String x) {
            HashSet<Character> set = new HashSet();
            for (int i = 0; i < x.length(); i++) {
                if (set.contains(x.charAt(i))) {
                    return false;
                }
                set.add(x.charAt(i));
            }
            return true;
        }
    }
}