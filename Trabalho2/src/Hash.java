package src;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash
 */
public class Hash {

    //https://www.geeksforgeeks.org/sha-1-hash-in-java/
    public static String hashBytes(byte[] bytes)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] messageDigest = md.digest(bytes);

            BigInteger no = new BigInteger(1,messageDigest);

            String hashtext = no.toString(16);

            while(hashtext.length() < 64)
            {
                hashtext = "0" + hashtext;
            }

            return hashtext;
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Something went wrong will trying to hash");
            return null;
        }
    }

    public static String bytesToString(byte[] bytes)
    {
        return new String(bytes);
    }

    public static String hashBytes(int toHash)
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(toHash);
        return Hash.hashBytes(b.array());
    }
}