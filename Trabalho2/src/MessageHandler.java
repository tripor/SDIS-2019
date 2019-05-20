package src;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

/**
 * MessageHandler
 */
public class MessageHandler implements Runnable {

    public static final char CR = 0xD;
    public static final char LF = 0xA;
    public static final String CRLF = "" + MessageHandler.CR + MessageHandler.LF;
    public static final String FINDSUCCESSOR = "FINDSUCCESSOR ?" + MessageHandler.CRLF;

    public static String subst(String regex, String... replaces)
    {
        for(String replace: replaces)
        {
            regex = regex.replaceFirst("\\?", replace);
        }
        return regex;
    }

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
        ArrayList<Integer> listSize = new ArrayList<Integer>();
        while ((bytesRead = this.socketChannel.read(buf)) > 0 || list.size()==0) {
            if(bytesRead==0)continue;
            totalBytesRead += bytesRead;
            list.add(buf);
            listSize.add(bytesRead);
        }
        byte[] devolver = new byte[totalBytesRead];
        int position=0;
        for(int i=0;i<list.size();i++)
        {
            System.arraycopy(list.get(i).array(), 0, devolver, position, listSize.get(i));
            position+=listSize.get(i);
        }
        return devolver;
    }

    @Override
    public void run()
    {
        System.out.println("Received a message");
        try {
            byte[] bytesReceived = this.receiveData();
            String stringReceived = Hash.bytesToString(bytesReceived);
            System.out.println("\tMessage:-->" + stringReceived + "<--");
            String[] splitedMessage = stringReceived.split(MessageHandler.CRLF);
            String header = splitedMessage[0];
            String[] splitedHeader = header.split(" ");
            if(splitedHeader[0].equals("FINDSUCCESSOR"))
            {
                String id = splitedHeader[1];
                Server.singleton.getNode().findSuccessor(id);
            }
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}