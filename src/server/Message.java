
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException; 

public class Message {

    private String type;
    private String version;
    private String Senderid;
    private String FileId;
    private String ChunkNo;
    private String ReplicationDeg;

    public Message(String[] message) throws Erros {
        if (message.length < 4) {
            System.out.println("A message must have at least 3 fields");
            throw new Erros();
        }
        if (message[0].equals("PUTCHUNK") && message.length == 6) {
            this.type = "PUTCHUNK";
            this.version = message[1];
            this.Senderid = message[2];
            this.FileId = message[3];
            this.ChunkNo = message[4];
            this.ReplicationDeg = message[5];
        } else if (message[0].equals("STORED") && message.length == 5) {
            this.type = "STORED";
            this.version = message[1];
            this.Senderid = message[2];
            this.FileId = message[3];
            this.ChunkNo = message[4];
        } else if (message[0].equals("GETCHUNK") && message.length == 5) {
            this.type = "GETCHUNK";
            this.version = message[1];
            this.Senderid = message[2];
            this.FileId = message[3];
            this.ChunkNo = message[4];
        } else if (message[0].equals("CHUNK") && message.length == 5) {
            this.type = "CHUNK";
            this.version = message[1];
            this.Senderid = message[2];
            this.FileId = message[3];
            this.ChunkNo = message[4];
        } else if (message[0].equals("DELETE") && message.length == 4) {
            this.type = "DELETE";
            this.version = message[1];
            this.Senderid = message[2];
            this.FileId = message[3];
        } else if (message[0].equals("REMOVED") && message.length == 5) {
            this.type = "REMOVED";
            this.version = message[1];
            this.Senderid = message[2];
            this.FileId = message[3];
            this.ChunkNo = message[4];
        } else {
            System.out.println("Invalid message");
            throw new Erros();
        }
    }

    public String getMessage() {
        String devolver = this.type + " " + this.version + " " + this.Senderid + " " + this.FileId + " ";
        if (this.type.equals("STORED") || this.type.equals("GETCHUNK") || this.type.equals("CHUNK")
                || this.type.equals("REMOVED")) {
            devolver += this.ChunkNo + " ";
        } else if (this.type.equals("PUTCHUNK")) {
            devolver += this.ChunkNo + " " + this.ReplicationDeg + " ";
        }
        return devolver;
    }

    public String getFileId()
    {
        return this.FileId;
    }
    /**
     * https://www.geeksforgeeks.org/sha-256-hash-in-java/
     */
    public void hashFileId() {

        try {

            // Static getInstance method is called with hashing SHA
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // digest() method called
            // to calculate message digest of an input
            // and return array of byte
            byte[] messageDigest = md.digest(this.FileId.getBytes());

            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);

            // Convert message digest into hex value
            String hashtext = no.toString(16);

            while (hashtext.length() < 64) {
                hashtext = "0" + hashtext;
            }

            this.FileId=hashtext;
        }

        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            System.out.println("Exception thrown" + " for incorrect algorithm: " + e);
        }
    }
    public static String getSHA(String input) 
    { 
        try { 
  
            // Static getInstance method is called with hashing SHA 
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
  
            // digest() method called 
            // to calculate message digest of an input 
            // and return array of byte 
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            String hashtext = no.toString(16); 
  
            while (hashtext.length() < 64) { 
                hashtext = "0" + hashtext; 
            } 
  
            return hashtext; 
        } 
  
        // For specifying wrong message digest algorithms 
        catch (NoSuchAlgorithmException e) { 
            System.out.println("Exception thrown"
                               + " for incorrect algorithm: " + e); 
  
            return null; 
        } 
    } 
}