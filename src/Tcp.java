import java.net.*;
import java.io.*;
 
public class Tcp implements Runnable {

    private Thread tcp_thread;

    public Tcp() {
        this.tcp_thread = new Thread(this);
        this.tcp_thread.start();
     } 
    
    @Override
    public void run()
    {
         
        try (
            ServerSocket serverSocket = new ServerSocket(Server.singleton.getPort());
            Socket clientSocket = serverSocket.accept(); 

            //recebe                  
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));

            DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
        ) {
            /*
            String inputLine = in.readLine();
            String[] message_splited = inputLine.split(" ");
            System.out.println(inputLine);
            
            int length = dIn.readInt();// read length of incoming message
            if(length>0) {
                byte[] message = new byte[length];
                dIn.readFully(message, 0, message.length); // read the message
                //send
                Server.singleton.MDRmessageReceived(message_splited,message);
            }*/

        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                + Server.singleton.getPort() + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}