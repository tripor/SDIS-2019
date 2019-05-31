package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) {// aceitar ip de inicio peer e verificar se é o primeiro
        int port = 0;
        int option = 1;
        String ip = "";
        int port2 = 0;
        if (args.length == 4) {
            option = Integer.parseInt(args[0]);
            port = Integer.parseInt(args[1]);
            ip = args[2];
            port2 = Integer.parseInt(args[3]);
        } else {
            System.err.println("\nArguments must be: <option> <server_port> <ip_adress of another server> <port of another server>\n");
            System.exit(1);
        }
        Server server = new Server(option, port, ip,port2);
        server.run();
        server.close();
    }

    public static ThreadPoolExecutor executor;
    public static ScheduledThreadPoolExecutor scheduledExecutor;
    public static Server singleton;

    public Boolean serverRunning;

    private int port;
    private int option;
    private String anotherAddress;
    private int anotherPort;
    private Node node;
    private Storage storage;


    private TcpServer tcpServer;

    public Server(int option, int port, String address, int anotherPort) {
        Server.executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        Server.scheduledExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(2);
        Server.singleton = this;
        this.port = port;
        this.option = option;
        this.anotherAddress = address;
        this.anotherPort = anotherPort;
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
        System.out.println("Setting up server...");
        try {
            this.node = new Node(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), this.tcpServer.getPort()));
            if(!this.isFirstServer())
            {
                System.out.println("Trying to join chord ring");
                this.node.join(new InetSocketAddress(this.anotherAddress,this.anotherPort));
                System.out.println("Chord ring joined");
            }
        } catch (Exception e) {
            Colours.printRed("A error has ocurred while trying to create this node");
            System.exit(1);
        }

        if(this.option == 3)
        {
            try {
                TcpMessage message = new TcpMessage(this.anotherAddress,this.anotherPort);
                message.receiveData();
            } catch (Exception e) {
                //TODO: handle exception
            }
            return;
        }

        Runnable ringMaintain = new RingMaintenance(this.node);
        Server.scheduledExecutor.scheduleAtFixedRate(ringMaintain, 0, 10, TimeUnit.SECONDS);
        //Runnable fingerTableMaintain = new FingerTableMaintenance(this.node);
        //Server.scheduledExecutor.scheduleAtFixedRate(fingerTableMaintain, 0, 2, TimeUnit.SECONDS);
        this.storage=new Storage(this.node.getSelfAddressInteger());

        System.out.println("Server is running");
        this.serverRunning=true;
        try {
            while(this.serverRunning)
            {
                Colours.printCyan("Listening...\n");
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

    public Boolean isFirstServer()
    {
        if(this.option == 2)
            return true;
        else
            return false;
    }

    public Node getNode()
    {
        return this.node;
    }

    public Storage getStorage()
    {
        return this.storage;
    }

}