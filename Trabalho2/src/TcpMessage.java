package src;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;


/**
 * TcpMessage
 */
public class TcpMessage {

    private SocketChannel socketChannel;

    public TcpMessage(String address,int port) throws IOException
    {
        this.socketChannel = SocketChannel.open();
        this.socketChannel.connect(new InetSocketAddress(address, port));
        this.socketChannel.configureBlocking(false);
    }


    public void sendData(byte[] data) throws InterruptedException,IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.clear();
        buf.put(data);
        buf.flip();
        while(buf.hasRemaining())
        {
            this.socketChannel.write(buf);
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
            this.socketChannel.write(buf);
        }
    }


    public byte[] receiveData() throws IOException
    {
        ByteBuffer buf = ByteBuffer.allocate(2048);
        int bytesRead;
        int totalBytesRead=0;
        ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
        while ((bytesRead = this.socketChannel.read(buf)) != -1) {
            totalBytesRead += bytesRead;
            list.add(buf);
        }
        byte[] devolver = new byte[totalBytesRead];
        int position=0;
        for(ByteBuffer bb : list)
        {
            System.arraycopy(bb.array(), 0, devolver, position, bb.array().length);
            position+=bb.array().length;
        }
        return devolver;
    }


    public void close() throws IOException
    {
        this.socketChannel.close();
    }
}