package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
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
            Colours.printRed("\nArguments must be: <option> <server_port> <ip_adress of another server> <port of another server>\n");
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
    private String ip;


    private TcpServer tcpServer;
    
    private ArrayList<Long> storeCycle;
    private ArrayList<Long> getCycle;
    private ArrayList<Long> removeCycle;
    private ArrayList<Long> storeSpecialCycle;

    public Server(int option, int port, String address, int anotherPort) {
        Server.executor = (ThreadPoolExecutor)Executors.newCachedThreadPool();
        Server.scheduledExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(2);
        Server.singleton = this;
        this.port = port;
        this.option = option;
        this.anotherAddress = address;
        this.anotherPort = anotherPort;
        this.storeCycle = new ArrayList<Long>();
        this.getCycle = new ArrayList<Long>();
        this.removeCycle = new ArrayList<Long>();
        this.storeSpecialCycle = new ArrayList<Long>();
        try {
            this.tcpServer = new TcpServer(this.port);
            System.out.println("Port used:- " + this.tcpServer.getPort());
        } catch (IOException e) {
            Colours.printRed("Couldn't make the server socket. Exiting...\n");
            System.exit(1);
        }
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();

            // https://stackoverflow.com/questions/2939218/getting-the-external-ip-address-in-java
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine(); // you get the IP as a String
            if(this.option==0)
            {
                this.ip=ip;
            }
            else
            {
                this.ip=inetAddress.getHostAddress();
            }
            System.out.println("\tServer setup in:");
            System.out.println("\tLocal IP Address:- " + inetAddress.getHostAddress());
            System.out.println("\tExternal IP Address:- " + ip);
            System.out.println("\tHost Name:- " + inetAddress.getHostName());
        } catch (UnknownHostException e) {
            Colours.printRed("Couldn't get INetAddress\n");
            System.exit(1);
        } catch (Exception e) {
            Colours.printRed("Error while trying to get the external ip address\n");
            System.exit(1);
        }
    }

    public void run() {
        System.out.println("Setting up server...");
        try {
            this.node = new Node(new InetSocketAddress(this.ip, this.tcpServer.getPort()));
            if(!this.isFirstServer())
            {
                System.out.println("Trying to join chord ring");
                this.node.join(new InetSocketAddress(this.anotherAddress,this.anotherPort));
                System.out.println("Chord ring joined");
            }
        } catch (Exception e) {
            Colours.printRed("A error has ocurred while trying to create this node\n");
            System.exit(1);
        }

        Runnable ringMaintain = new RingMaintenance(this.node);
        Server.scheduledExecutor.scheduleAtFixedRate(ringMaintain, 0, 4, TimeUnit.SECONDS);
        Runnable fingerTableMaintain = new FingerTableMaintenance(this.node);
        Server.scheduledExecutor.scheduleAtFixedRate(fingerTableMaintain, 0, 2, TimeUnit.SECONDS);
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
            Colours.printRed("Error while closing the Server Socket.\n");
            System.exit(1);
        }
        System.out.println("Server has been closed");
    }

    public Boolean isFirstServer()
    {
        if(this.anotherAddress.equals("0") && this.anotherPort==0)
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
    public ArrayList<Long> getStoreCycle()
    {
        return this.storeCycle;
    }
    public ArrayList<Long> getGetCycle()
    {
        return this.getCycle;
    }
    public ArrayList<Long> getRemoveCycle()
    {
        return this.removeCycle;
    }

    public ArrayList<Long> getStoreSpecialCycle()
    {
        return this.storeSpecialCycle;
    }

}