
public interface Blake2 {
    String algorithm();
    int length();
    Blake2 copy();
    Blake2 reset();
    Blake2 burn();
    Blake2 update(byte... input);
    Blake2 update(byte[] input, int off, int len);
    
    default byte[] digest(byte... input) {
        return digest(input, 0, input.length);
    }
    
    default byte[] digest(byte[] input, int off, int len) {
        return update(input, off, len).digest();
    }
    
    byte[] digest();
}