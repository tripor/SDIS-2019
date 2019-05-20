package src;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * MessageHandler
 */
public class MessageHandler implements Runnable {

    private SocketChannel socketChannel;

    public MessageHandler(SocketChannel socketChannel) throws IOException
    {
        this.socketChannel=socketChannel;
        this.socketChannel.configureBlocking(false);
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

    @Override
    public void run()
    {
        try {
            System.out.println(this.receiveData());
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}