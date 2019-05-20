package src;
import java.nio.channels.SocketChannel;

/**
 * MessageHandler
 */
public class MessageHandler implements Runnable {

    private SocketChannel socketChannel;

    public MessageHandler(SocketChannel socketChannel)
    {
        this.socketChannel=socketChannel;
    }

    @Override
    public void run()
    {
        System.out.println("ola");
    }
}


        /*
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
        return devolver;*/