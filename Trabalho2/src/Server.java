package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    public static void main(String[] args) {// aceitar ip de inicio peer e verificar se é o primeiro
        int port = 0;
        int option = 1;
        String ip = "";
        if (args.length == 3) {
            option = Integer.parseInt(args[0]);
            port = Integer.parseInt(args[1]);
            ip = args[2];
        } else {
            System.err.println("\nArguments must be: <option> <server_port> <ip_adress of another server>\n");
            System.exit(1);
        }
        Server server = new Server(option, port, ip);
        server.run();
        server.close();
    }

    public static ThreadPoolExecutor executor;

    public Boolean serverRunning;

    private int port;
    private int option;
    private String address;

    private TcpServer tcpServer;

    public Server(int option, int port, String address) {
        Server.executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        this.port = port;
        this.option = option;
        this.address = address;
        try {
            this.tcpServer = new TcpServer(port);
            System.out.println("Port used:- " + this.tcpServer.getPort());
        } catch (IOException e) {
            System.err.println("Couldn't make the server socket. Exiting...");
            System.exit(1);
        }
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();

            // https://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine(); // you get the IP as a String

            System.out.println("\tServer setup in:");
            System.out.println("\tLocal IP Address:- " + inetAddress.getHostAddress());
            System.out.println("\tExternal IP Address:- " + ip);
            System.out.println("\tHost Name:- " + inetAddress.getHostName());
        } catch (UnknownHostException e) {
            System.err.println("Couldn't get INetAddress");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error while trying to get the external ip address");
            System.exit(1);
        }
    }

    public void run() {
        System.out.println(this.option);
        if(this.option == 3)
        {
            try {
                TcpMessage message = new TcpMessage(this.address,this.port);
                message.receiveData();
            } catch (Exception e) {
                //TODO: handle exception
            }
            return;
        }
        System.out.println("Server is running");
        this.serverRunning=true;
        try {
            while(this.serverRunning)
            {
                System.out.println("Listening...");
                SocketChannel sc = this.tcpServer.acceptConnection();
                Runnable messageHandler = new MessageHandler(sc);
                Server.executor.submit(messageHandler);

            }
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            this.tcpServer.close();
        } catch (IOException e) {
            System.err.println("Error while closing the Server Socket.");
            System.exit(1);
        }
        System.out.println("Server has been closed");
    }

}