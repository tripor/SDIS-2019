package src;
import java.io.IOException;
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
    public TcpMessage(InetSocketAddress address) throws IOException
    {
        this.socketChannel = SocketChannel.open();
        this.socketChannel.connect(address);
        this.socketChannel.configureBlocking(false);
    }
    public TcpMessage(SocketChannel socket) throws IOException
    {
        this.socketChannel = socket;
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
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        ArrayList<Integer> listSize = new ArrayList<Integer>();
        while ((bytesRead = this.socketChannel.read(buf)) != -1 ) {
            if(bytesRead==0)continue;
            totalBytesRead += bytesRead;
            buf.flip();
            byte[] add = new byte[bytesRead];
            System.arraycopy(buf.array(),0,add,0,bytesRead);
            list.add(add);
            buf.clear();
            listSize.add(bytesRead);
        }
        if(totalBytesRead < 0) totalBytesRead=0;
        byte[] devolver = new byte[totalBytesRead];
        int position=0;
        for(int i=0;i<list.size();i++)
        {
            System.arraycopy(list.get(i), 0, devolver, position, listSize.get(i));
            position+=listSize.get(i);
        }
        return devolver;
    }


    public void close() throws IOException
    {
        this.socketChannel.close();
    }
}