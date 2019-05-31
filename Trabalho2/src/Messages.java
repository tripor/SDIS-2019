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

    public String SendGetTable()throws Exception
    {
        this.printScreenMessage("GETTABLE");
        this.message.sendData(MessageHandler.subst(MessageHandler.GETTABLE));
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        this.message.close();
        if (response.startsWith("TABLE")) {
            Colours.printGreen("Response to GETTABLE received -->");
            System.out.print(response.trim());
            Colours.printGreen("<--\n");
            if(splitedMessage.length==1)
                return new String();
            else
                return splitedMessage[1];
        } else {
            throw new Exception();
        }
    }
    
    public Boolean SendBackup(String senderId,String fileName,int rep,byte[] info)throws Exception
    {
        this.printScreenMessage("BACKUP");
        String header = MessageHandler.subst(MessageHandler.BACKUP,senderId,fileName,Integer.toString(rep));
        byte[] toSend = new byte[header.length()+info.length];
        System.arraycopy(header.getBytes(), 0, toSend, 0, header.length());
        System.arraycopy(info, 0, toSend, header.length(), info.length);
        this.message.sendData(toSend);
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        this.message.close();
        if (!response.startsWith("OK")) {
            return false;
        }
        Colours.printGreen("Confirmation message to ");
        System.out.print("BACKUP");
        Colours.printGreen(" received successfully\n");
        return true;
    }
    public Boolean SendStore(String senderId,String fileName,int rep,byte[] info,InetSocketAddress self )throws Exception
    {
        this.printScreenMessage("STORE");
        String header = MessageHandler.subst(MessageHandler.STORE,senderId,fileName,Integer.toString(rep),self.getHostName(),Integer.toString(self.getPort()) );
        byte[] toSend = new byte[header.length()+info.length];
        System.arraycopy(header.getBytes(), 0, toSend, 0, header.length());
        System.arraycopy(info, 0, toSend, header.length(), info.length);
        this.message.sendData(toSend);
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        this.message.close();
        if (!response.startsWith("OK")) {
            return false;
        }
        Colours.printGreen("Confirmation message to ");
        System.out.print("STORE");
        Colours.printGreen(" received successfully\n");
        return true;
    }
    public byte[] SendRestore(String senderId,String fileName)throws Exception
    {
        this.printScreenMessage("RESTORE");
        String header = MessageHandler.subst(MessageHandler.RESTORE,senderId,fileName);
        this.message.sendData(header);
        
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        this.message.close();
        if (!response.startsWith("OK")) {
            return null;
        }
        if(splitedMessage.length==1)
            return null;

        
        int length = responseBytes.length - (header.length()+MessageHandler.CRLF.length());
        byte[] info = new byte[responseBytes.length - (header.length()+MessageHandler.CRLF.length())];
        System.arraycopy(responseBytes, header.length()+MessageHandler.CRLF.length(), info, 0,length);


        Colours.printGreen("Confirmation message to ");
        System.out.print("RESTORE");
        Colours.printGreen(" received successfully\n");
        return info;
    }
    public byte[] SendGet(String senderId,String fileName,InetSocketAddress self)throws Exception
    {
        this.printScreenMessage("GET");
        String header = MessageHandler.subst(MessageHandler.GET,senderId,fileName,self.getHostName(),Integer.toString(self.getPort()));
        this.message.sendData(header);
        
        byte[] responseBytes = this.message.receiveData();
        String response = new String(responseBytes);
        String[] splitedMessage = response.split(MessageHandler.CRLF);
        this.message.close();
        if (!response.startsWith("OK")) {
            return null;
        }
        if(splitedMessage.length==1)
            return null;

        
        int length = responseBytes.length - (header.length()+MessageHandler.CRLF.length());
        byte[] info = new byte[responseBytes.length - (header.length()+MessageHandler.CRLF.length())];
        System.arraycopy(responseBytes, header.length()+MessageHandler.CRLF.length(), info, 0,length);


        Colours.printGreen("Confirmation message to ");
        System.out.print("GET");
        Colours.printGreen(" received successfully\n");
        return info;
    }

    public Boolean SendDelete(String senderId,String fileName) {
        this.printScreenMessage("DELETE");
        try {
            this.message.sendData(MessageHandler.subst(MessageHandler.DELETE, senderId,fileName));
            String response = new String(this.message.receiveData());
            this.message.close();
            if (!response.startsWith("OK")) {
                return false;
            }
            Colours.printGreen("Confirmation message to ");
            System.out.print("DELETE" );
            Colours.printGreen(" received successfully\n");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}