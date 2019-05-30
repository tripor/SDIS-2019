package src;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Messages
 */
public class Messages {

    private TcpMessage message;

    public Messages(String address, int port) throws IOException {
        this.message = new TcpMessage(address, port);
    }

    public Messages(InetSocketAddress address) throws IOException {
        this.message = new TcpMessage(address);
    }

    public Messages(SocketChannel socket) throws IOException {
        this.message = new TcpMessage(socket);
    }

    private void printScreenMessage(String message) {
        Colours.printGreen("Sending ");
        System.out.print(message);
        Colours.printGreen(" message\n");
    }

    public Boolean SendAlive() {
        this.printScreenMessage("ALIVE");
        try {
            this.message.sendData(MessageHandler.ALIVE);
            String response = new String(this.message.receiveData());
            this.message.close();
            if (!response.startsWith("OK")) {
                return false;
            }
            Colours.printGreen("Confirmation message to ");
            System.out.print("ALIVE" );
            Colours.printGreen(" received successfully\n");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String[] SendYourPre() throws Exception {
        this.printScreenMessage("YOURPRE");
        this.message.sendData(MessageHandler.YOURPRE);
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        String header = splitedMessage[0];
        String[] splitedHeader = header.split(" ");
        this.message.close();
        if (response.startsWith("MYPRE")) {
            Colours.printGreen("Response to YOURPRE received -->");
            System.out.print(response.trim());
            Colours.printGreen("<--\n");
            return splitedHeader;
        } else {
            throw new Exception();
        }
    }

    /**
     * Send the message IAMPREDECESSOR
     * 
     * @param strings 2 parameters to be sent. Ip and Port
     * @return true if everything went right or false if everything went wrong
     */
    public Boolean SendIamPredecessor(String... strings) {
        this.printScreenMessage("IAMPREDECESSOR");
        try {
            this.message.sendData(MessageHandler.subst(MessageHandler.IAMPREDECESSOR, strings));
            String responsePre = new String(this.message.receiveData());
            this.message.close();
            if (!responsePre.startsWith("OK")) {
                return false;
            }
            Colours.printGreen("Confirmation message to ");
            System.out.print("IAMPREDECESSOR");
            Colours.printGreen(" received successfully\n");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String[] SendFindSsuccessor(String... strings) throws Exception {
        this.printScreenMessage("FINDSUCCESSOR");
        this.message.sendData(MessageHandler.subst(MessageHandler.FINDSUCCESSOR, strings));
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        String header = splitedMessage[0];
        String[] splitedHeader = header.split(" ");
        this.message.close();
        if (response.startsWith("SUCC")) {
            Colours.printGreen("Response to FINDSUCCESSOR received -->");
            System.out.print(response.trim());
            Colours.printGreen("<--\n");
            return splitedHeader;
        } else {
            throw new Exception();
        }
    }
}