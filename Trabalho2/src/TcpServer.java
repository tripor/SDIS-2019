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


    private ServerSocketChannel serverSocketChannel;

    public TcpServer(int port) throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
    }

    public SocketChannel acceptConnection() throws IOException {
        SocketChannel socket = this.serverSocketChannel.accept();
        assert socket != null;
        return socket;
    }

    public int getPort()
    {
        return this.serverSocketChannel.socket().getLocalPort();
    }

    public void close() throws IOException {
        this.serverSocketChannel.close();
    }
}