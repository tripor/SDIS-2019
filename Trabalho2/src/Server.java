package src;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server
{
    public static void main(String[] args) {//aceitar ip de inicio peer e verificar se é o primeiro
        int port = 0;
        if(args.length == 1)
        {
            port= Integer.parseInt(args[0]);
        }
        else if(args.length > 1)
        {
            System.err.println("Arguments must be: <server_port>");
        }
        else
        {
            System.out.println("Server port number is automatically allocated");
        }
        Server server = new Server(port);
        server.run();
        server.close();
    }




    private int port;

    private TcpServer tcpServer;




    public Server(int port)
    {
        this.port=port;
        try {
            this.tcpServer = new TcpServer(port);
        } catch (IOException e) {
            System.err.println("Couldn't make the server socket. Exiting...");
            System.exit(1);
        }
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            System.out.println("IP Address:- " + inetAddress.getHostAddress());
            System.out.println("Host Name:- " + inetAddress.getHostName());
        } catch (UnknownHostException e) {
            System.err.println("Couldn't get local ip address");
            System.exit(1);
        }
    }

    public void run()
    {
        System.out.println("Server is running");
        try {
            System.out.println(this.tcpServer.acceptConnection());
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    public void close()
    {
        try {
            this.tcpServer.close();
        } catch (IOException e) {
            System.err.println("Error while closing the Server Socket.");
            System.exit(1);
        }
        System.out.println("Server has been closed");
    }


}