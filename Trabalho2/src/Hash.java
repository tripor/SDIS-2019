package src;

import java.math.BigInteger;
import java.net.InetSocketAddress;
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
    public static long hashBytesInteger(byte[] bytes)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] messageDigest = md.digest(bytes);

            BigInteger no = new BigInteger(1,messageDigest);

            return no.longValue();
            
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Something went wrong will trying to hash");
            return 0;
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
    public static long hashBytesInteger(int toHash)
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(toHash);
        return Math.abs(Hash.hashBytesInteger(b.array()));
    }
    public static long hashBytesInteger(InetSocketAddress toHash)
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(toHash.hashCode()+toHash.getPort());
        return Math.abs(Hash.hashBytesInteger(b.array()));
    }

    
    public static long difference(long a,long b)
    {
        long dif = a - b;
        if(dif < 0)
        {
            dif += Math.pow(2, 64);
        }
        return dif;
    }

    public static Boolean isBetween(long start,long test,long end)
    {
        if(start > end)
        {
            if(start < test)
                return true;
            else if( end > test)
                return true;
            else
                return false;
        }
        else
        {
            if(start < test  && test < end)
                return true;
            else
                return false;
        }
    }
}