import java.net.*;
import java.io.*;
 
public class Tcp {

    private int port;

    public Tcp(int portNumber) { this.port = portNumber; } 
    
    public void run()
    {
         
        try (
            ServerSocket serverSocket = new ServerSocket(this.port);
            Socket clientSocket = serverSocket.accept(); 

            //recebe                  
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

            DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
        ) {
            String inputLine = in.readLine();
            String[] message_splited = inputLine.split(" ");

            int length = dIn.readInt();// read length of incoming message
            if(length>0) {
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length); // read the message
                //send
                Server.singleton.MDRmessageReceived(message_splited,message);
            }

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}