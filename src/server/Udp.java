import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Udp {

    private InetAddress group;
    private String address_name;
    private int port;

    private MulticastSocket socket;

    private static final int MAX_SIZE_PACKET=65536;

    /**
     * Constructor for the Udp
     * @param address The ip address of the connection
     * @param port The port of the connection
     */
    public Udp(String address, int port) {
        this.address_name = address;
        try {
            this.group = InetAddress.getByName(address);
        } catch (Exception e) {
            System.out.println("A error as ocurred while trying to set up the address of the network.");
            System.exit(1);
        }
        this.port = port;
        try {
            this.socket = new MulticastSocket(port);
            //this.socket.setInterface(this.group);
            this.socket.joinGroup(this.group);
        } catch (Exception e) {
            System.out.println(
                    "A error as ocurred while trying to set up the multi cast socket. Port number or address incorrect.");
            System.exit(1);
        }
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
     * @param message Message to send
     */
    public void sendMessage(String message)
    {
        message += 0xD + 0xA;
        DatagramPacket packet= new DatagramPacket(message.getBytes(),message.length(),this.group,this.port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            System.out.println("A error as ocurred while trying to send a message to the multi cast socket.");
            System.exit(2);
        }
    }
    /**
     * Sends PutChunk datagram packet to the socket
     * @param message PutChunk message to send
     * @param body PutChunk body to send
     */
    public void sendMessageBody(String message, String body)
    {
        message += 0xD + 0xA + body;
        DatagramPacket packet= new DatagramPacket(message.getBytes(),message.length(),this.group,this.port);
        
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            System.out.println("A error as ocurred while trying to send a message to the multi cast socket.");
            System.exit(2);
        }
    }

    public String receive()
    {
        while(true)
        {
            //DatagramPacket receber= new DatagramPacket(buf, length);
        }

        return "";
    }
}