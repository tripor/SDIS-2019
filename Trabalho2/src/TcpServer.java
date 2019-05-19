package src;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * Tcp
 */
public class TcpServer {

    private SocketChannel socket;

    private ServerSocketChannel serverSocketChannel;

    public TcpServer(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);
    }

    public byte[] acceptConnection() throws IOException {
        this.socket=null;
        this.socket = this.serverSocketChannel.accept();
        if (this.socket == null) {
            System.out.println("No new connection was made");
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocate(2048);
        int bytesRead;
        int totalBytesRead=0;
        ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
        while ((bytesRead = this.socket.read(buf)) > 0) {//mudar para threads
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

    public void close() throws IOException {
        this.serverSocketChannel.close();
    }
}