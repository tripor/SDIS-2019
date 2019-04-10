import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Udp implements Runnable {

    private Thread udp_thread;

    private InetAddress group;
    private String address_name;
    private int port;
    private String socket_name;

    private MulticastSocket socket;

    private static final int MAX_SIZE_PACKET = 65000;
    public static final char CR = 0xD;
    public static final char LF = 0xA;
    public static final String CRLF = "" + Udp.CR + Udp.LF;

    private String[] message_received;
    private byte[] body_received;

    private String message_sent = "";

    /**
     * Constructor for the Udp
     * 
     * @param address The ip address of the connection
     * @param port    The port of the connection
     */
    public Udp(String address, int port, String type) {
        this.address_name = address;
        this.socket_name = type;
        try {
            this.group = InetAddress.getByName(address);
        } catch (Exception e) {
            System.out.println("An error has ocurred while trying to set up the address of the network.");
            System.exit(1);
        }
        this.port = port;
        try {
            this.socket = new MulticastSocket(port);
            // this.socket.setInterface(this.group);
            this.socket.joinGroup(this.group);
        } catch (Exception e) {
            System.out.println(
                    "An error has ocurred while trying to set up the multi cast socket. Port number or address incorrect.");
            System.exit(1);
        }

        this.udp_thread = new Thread(this);
        this.udp_thread.start();
    }

    /**
     * Leaves the group
     */
    public void leave() {
        try {
            this.socket.leaveGroup(this.group);
        } catch (IOException e) {
            System.out.println("A error as ocurred while trying to leave the group");
            System.exit(1);
        }
    }

    /**
     * Send a datagram packet to the socket
     * 
     * @param message Message to send
     */
    public void sendMessage(String message) {
        this.message_sent = message;
        message += Udp.CRLF + Udp.CRLF;
        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(), this.group, this.port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            System.out.println("A error as ocurred while trying to send a message to the multi cast socket.");
            System.exit(2);
        }
    }

    /**
     * Sends PutChunk datagram packet to the socket
     * 
     * @param message PutChunk message to send
     * @param body    PutChunk body to send
     */
    public void sendMessageBody(String message, byte[] body) {
        this.message_sent = message;
        message += Udp.CRLF + Udp.CRLF;

        byte[] mandar = new byte[message.getBytes().length + body.length];
        System.arraycopy(message.getBytes(), 0, mandar, 0, message.getBytes().length);
        System.arraycopy(body, 0, mandar, message.getBytes().length, body.length);

        DatagramPacket packet = new DatagramPacket(mandar, mandar.length, this.group, this.port);

        try {
            this.socket.send(packet);
        } catch (IOException e) {
            System.out.println("A error as ocurred while trying to send a message with body to the multi cast socket.");
            System.exit(2);
        }
    }

    public String[] getMessage() {
        return this.message_received;
    }

    public byte[] getBody() {
        return this.body_received;
    }
    public void setMessage(String[] message) {
        this.message_received=message;
    }

    public void setBody(byte[] body) {
        this.body_received=body;
    }

    @Override
    public void run() {
        while (true) {
            DatagramPacket receber = new DatagramPacket(new byte[Udp.MAX_SIZE_PACKET], Udp.MAX_SIZE_PACKET);
            try {
                this.socket.receive(receber);
            } catch (Exception e) {
                System.out
                        .println("A error as ocurred while trying to receive the message from the multi cast socket.");
                System.exit(2);

            }
            ExecutorService service =  Executors.newSingleThreadExecutor();
            Task task = new Task(this.socket_name,receber);
            Future<Integer> future = service.submit(task);

        }
    }
}


class Task implements Callable<Integer> {
    private String socket_name;
    private DatagramPacket receber;
        
    public Task(String socket,DatagramPacket receber)
    {
        this.socket_name=socket;
        this.receber=receber;
    }
	@Override
	public Integer call() throws Exception {
        
        String message = new String(receber.getData());
        String[] splited = message.split(Udp.CRLF);
        String[] message_splited = splited[0].split(" ");

        byte[] body_receber = new byte[receber.getLength() - (splited[0].length() + 4)];

        System.arraycopy(receber.getData(), splited[0].length() + 4, body_receber, 0,
                receber.getLength() - (splited[0].length() + 4));

        Message test;
        try {
            test = new Message(message_splited);
            if (Server.singleton.getServerNumber().equals(test.getSenderId())) {
                return 0;
            }
        } catch (Exception e) {
            System.out.println("Message received with wrong format");
            return 0;
        }
        if (this.socket_name.equals("MDB")) {
            Server.singleton.MDBmessageReceived(message_splited,body_receber);
        } else if (this.socket_name.equals("MC")) {
            Server.singleton.MCmessageReceived(message_splited,body_receber);
        } else if (this.socket_name.equals("MDR")) {
            Server.singleton.MDRmessageReceived(message_splited,body_receber);
        }
		return 1;
	}
} 