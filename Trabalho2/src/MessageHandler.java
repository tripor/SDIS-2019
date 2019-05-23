package src;
import java.io.IOException;
import java.net.InetSocketAddress;
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
    public static final String IAMPREDECESSOR = "IAMPRE ? ?" + MessageHandler.CRLF;
    public static final String OK = "OK" + MessageHandler.CRLF;
    public static final String ERROR = "ERROR" + MessageHandler.CRLF;

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

    public Boolean sendResponse(String address,int port,String message)
    {
        try {
            TcpMessage toSend = new TcpMessage(this.socketChannel);
            toSend.sendData(message);
            System.out.println("Response message " + message.trim() + " was send with success");
        } catch (Exception e) {
            System.err.println("Response message " + message.trim() + " couldn't be sent");
            try {
                TcpMessage toSend = new TcpMessage(this.socketChannel);
                toSend.sendData(MessageHandler.ERROR);
            } catch (Exception ee) {
                return false;
            }
            return false;
        }
        return true;
    }

    @Override
    public void run()
    {
        System.out.println("Received a message");
        try {
            byte[] bytesReceived = this.receiveData();
            String stringReceived = Hash.bytesToString(bytesReceived);
            System.out.println("\tMessage:-->" + stringReceived.trim() + "<--");
            String[] splitedMessage = stringReceived.split(MessageHandler.CRLF);
            String header = splitedMessage[0];
            String[] splitedHeader = header.split(" ");
            if(splitedHeader[0].equals("FINDSUCCESSOR"))
            {
                String id = splitedHeader[1];
                Server.singleton.getNode().findSuccessor(id);
            }
            else if(splitedHeader[0].equals("IAMPRE"))
            {
                String ip = splitedHeader[1];
                int port = Integer.parseInt(splitedHeader[2]);
                this.sendResponse(ip,port,MessageHandler.OK);
                Server.singleton.getNode().hasPredecessor(new InetSocketAddress(ip, port));
            }
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
}