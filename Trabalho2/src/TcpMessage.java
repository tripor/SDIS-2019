package src;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * TcpMessage
 */
public class TcpMessage {

    private SocketChannel socketChannel;

    public TcpMessage(String address,int port) throws IOException
    {
        this.socketChannel = SocketChannel.open();
        this.socketChannel.connect(new InetSocketAddress(address, port));
    }


    public void sendData(byte[] data) throws InterruptedException,IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.clear();
        buf.put(data);
        buf.flip();
        while(buf.hasRemaining())
        {
            wait();

            this.socketChannel.write(buf);

            notify();
        }
    }
    public void sendData(String data) throws InterruptedException,IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(data.length());
        buf.clear();
        buf.put(data.getBytes());
        buf.flip();
        while(buf.hasRemaining())
        {
            wait();

            this.socketChannel.write(buf);

            notify();
        }
    }


    public void close() throws IOException
    {
        this.socketChannel.close();
    }
}