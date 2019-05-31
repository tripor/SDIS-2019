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
    public static final String SUCC = "SUCC ? ?" + MessageHandler.CRLF;
    public static final String IAMPREDECESSOR = "IAMPRE ? ?" + MessageHandler.CRLF;
    public static final String OK = "OK" + MessageHandler.CRLF;
    public static final String ERROR = "ERROR" + MessageHandler.CRLF;
    public static final String ALIVE = "ALIVE" + MessageHandler.CRLF;
    public static final String YOURPRE = "YOURPRE" + MessageHandler.CRLF;
    public static final String MYPRE = "MYPRE ? ?" + MessageHandler.CRLF;
    public static final String GETTABLE = "GETTABLE" + MessageHandler.CRLF;
    public static final String TABLE = "TABLE" + MessageHandler.CRLF +"?";
    public static final String BACKUP = "BACKUP ? ? ?" + MessageHandler.CRLF;
    public static final String STORE = "STORE ? ? ?" + MessageHandler.CRLF;
    public static final String RESTORE = "RESTORE ? ?" + MessageHandler.CRLF;
    public static final String GET = "GET ? ?" + MessageHandler.CRLF;
    public static final String DELETE = "DELETE ? ?" + MessageHandler.CRLF;
    public static final String REMOVE = "REMOVE ? ?" + MessageHandler.CRLF;
    public static final String RECLAIM = "RECLAIM ?" + MessageHandler.CRLF;
    public static final String STORESPECIAL = "STORESPECIAL ?" + MessageHandler.CRLF;




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
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        ArrayList<Integer> listSize = new ArrayList<Integer>();
        while ((bytesRead = this.socketChannel.read(buf)) > 0 || list.size()==0) {
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

    public Boolean sendResponse(String address,int port,String message)
    {
        try {
            TcpMessage toSend = new TcpMessage(address,port);
            toSend.sendData(message);
        } catch (Exception e) {
            try {
                TcpMessage toSend = new TcpMessage(address,port);
                toSend.sendData(MessageHandler.ERROR);
            } catch (Exception ee) {
                return false;
            }
            return false;
        }
        return true;
    }

    public Boolean sendResponse(String message)
    {
        try {
            TcpMessage toSend = new TcpMessage(this.socketChannel);
            toSend.sendData(message);
        } catch (Exception e) {
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
    public Boolean sendResponse(byte[] message)
    {
        try {
            TcpMessage toSend = new TcpMessage(this.socketChannel);
            toSend.sendData(message);
        } catch (Exception e) {
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

    public void close() throws IOException
    {
        this.socketChannel.close();
    }

    @Override
    public void run()
    {
        Colours.printGreen("Received a message\n");
        try {
            byte[] bytesReceived = this.receiveData();
            String stringReceived = Hash.bytesToString(bytesReceived);
            String[] splitedMessage = stringReceived.split(MessageHandler.CRLF);
            String header = splitedMessage[0];
            String[] splitedHeader = header.split(" ");
            if(splitedHeader[0].equals("FINDSUCCESSOR"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                long id = Long.parseLong(splitedHeader[1]);
                if(Server.singleton.getNode().isSearchingId(id))
                {
                    this.sendResponse(MessageHandler.ERROR);
                }
                else
                {
                    InetSocketAddress succ = Server.singleton.getNode().findSuccessor(id);
                    this.sendResponse(MessageHandler.subst(MessageHandler.SUCC, succ.getAddress().getHostAddress(),Integer.toString(succ.getPort())));
                }
            }
            else if(splitedHeader[0].equals("IAMPRE"))
            {
                String ip = splitedHeader[1];
                int port = Integer.parseInt(splitedHeader[2]);
                this.sendResponse(MessageHandler.OK);
                Server.singleton.getNode().hasPredecessor(new InetSocketAddress(ip, port));
            }
            else if(splitedHeader[0].equals("ALIVE"))
            {
                this.sendResponse(MessageHandler.OK);
            }
            else if(splitedHeader[0].equals("YOURPRE"))
            {
                InetSocketAddress pre= Server.singleton.getNode().getPreAddress();
                if(pre!=null)
                    this.sendResponse(MessageHandler.subst(MessageHandler.MYPRE,pre.getAddress().getHostAddress(),Integer.toString(pre.getPort())));
                else
                    this.sendResponse(MessageHandler.subst(MessageHandler.MYPRE,"null","0"));
            }
            else if(splitedHeader[0].equals("GETTABLE"))
            {  
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                String info = Server.singleton.getNode().toString()+"\n";
                info+=Server.singleton.getNode().getFingerTable().toString() + "\n";
                info+=Server.singleton.getStorage().toString() + "\n";
                this.sendResponse(MessageHandler.subst(MessageHandler.TABLE,info));
            }
            else if(splitedHeader[0].equals("BACKUP"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                int length = bytesReceived.length - (header.length()+MessageHandler.CRLF.length());
                byte[] info = new byte[bytesReceived.length - (header.length()+MessageHandler.CRLF.length())];
                System.arraycopy(bytesReceived, header.length()+MessageHandler.CRLF.length(), info, 0,length);
                String senderId = splitedHeader[1];
                String fileName = splitedHeader[2];
                int rep = Integer.parseInt(splitedHeader[3]);
                long hashedId=Hash.hashBytesInteger(senderId + fileName);
                InetSocketAddress succId = Server.singleton.getNode().findSuccessor(hashedId);
                int newRep = rep;
                if(Server.singleton.getStorage().hasSpace(info.length))
                    newRep--;
                if(rep==0)
                {
                    Colours.printRed("Replication degree can't be 0\n");
                    throw new Exception();
                }
                if(succId==null)
                {
                    Colours.printRed("Something went wrong while trying to find the successor of this file\n");
                    throw new Exception();
                }
                if(Server.singleton.getNode().getSelfAddress().equals(succId))
                {
                    Server.singleton.getStoreCycle().add(hashedId);
                    InetSocketAddress succ = Server.singleton.getNode().getSuccessor();
                    if(succ==null )
                    {
                        if(rep>1)
                        {
                            Colours.printRed("Replication degree can't be achieved\n");
                            Server.singleton.getStoreCycle().remove(hashedId);
                            throw new Exception();
                        }
                        else
                        {
                            if(newRep!=rep)
                                Server.singleton.getStorage().store(hashedId, info);
                            else
                            {
                                Colours.printRed("No space available\n");
                                Server.singleton.getStoreCycle().remove(hashedId);
                                throw new Exception();
                            }
                        }
                    }
                    else
                    {
                        if(rep>1 || rep==newRep)
                        {
                            Messages message = new Messages(succ);
                            if(!message.SendStore(senderId, fileName, newRep, info))
                            {
                                Colours.printRed("Replication degree can't be achieved\n");
                                Server.singleton.getStoreCycle().remove(hashedId);
                                throw new Exception();
                            }
                        }
                        if(rep!=newRep)
                            Server.singleton.getStorage().store(hashedId, info);
                    }
                }
                else
                {
                    Messages message = new Messages(succId);
                    if(!message.SendStore(senderId, fileName, rep, info))
                    {
                        Colours.printRed("Replication degree can't be achieved\n");
                        Server.singleton.getStoreCycle().remove(hashedId);
                        throw new Exception();
                    }
                }
                Server.singleton.getStoreCycle().remove(hashedId);
                this.sendResponse(MessageHandler.OK);
            }
            else if(splitedHeader[0].equals("STORE"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                int length = bytesReceived.length - (header.length()+MessageHandler.CRLF.length());
                byte[] info = new byte[bytesReceived.length - (header.length()+MessageHandler.CRLF.length())];
                System.arraycopy(bytesReceived, header.length()+MessageHandler.CRLF.length(), info, 0,length);
                String senderId = splitedHeader[1];
                String fileName = splitedHeader[2];
                int rep = Integer.parseInt(splitedHeader[3]);
                int newRep = rep;
                long hashedId=Hash.hashBytesInteger(senderId + fileName);
                if(Server.singleton.getStorage().hasSpace(info.length))
                    newRep--;
                if(Server.singleton.getStoreCycle().contains(hashedId))
                {
                    Colours.printRed("Replication degree can't be achieved\n");
                    Server.singleton.getStoreCycle().remove(hashedId);
                    throw new Exception();
                }
                Server.singleton.getStoreCycle().add(hashedId);
                InetSocketAddress succ = Server.singleton.getNode().getSuccessor();
                if(succ==null)
                {
                    if(rep>1)
                    {
                        Colours.printRed("Replication degree can't be achieved\n");
                        Server.singleton.getStoreCycle().remove(hashedId);
                        throw new Exception();
                    }
                    else
                    {
                        if(newRep!=rep)
                            Server.singleton.getStorage().store(hashedId, info);
                        else
                        {
                            Colours.printRed("No space available\n");
                            Server.singleton.getStoreCycle().remove(hashedId);
                            throw new Exception();
                        }
                    }
                }
                else
                {
                    if(rep>1 || rep==newRep)
                    {
                        Messages message = new Messages(succ);
                        if(!message.SendStore(senderId, fileName, newRep, info))
                        {
                            Colours.printRed("Replication degree can't be achieved\n");
                            Server.singleton.getStoreCycle().remove(hashedId);
                            throw new Exception();
                        }
                    }
                    if(rep!=newRep)
                        Server.singleton.getStorage().store(hashedId, info);
                }
                Server.singleton.getStoreCycle().remove(hashedId);
                this.sendResponse(MessageHandler.OK);
            }
            else if(splitedHeader[0].equals("RESTORE"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                String senderId = splitedHeader[1];
                String fileName = splitedHeader[2];
                long hashedId=Hash.hashBytesInteger(senderId + fileName);
                InetSocketAddress succId = Server.singleton.getNode().findSuccessor(hashedId);
                byte[] info=null;
                if(succId==null)
                {
                    Colours.printRed("Something went wrong while trying to find the successor of this file\n");
                    throw new Exception();
                }
                if(Server.singleton.getNode().getSelfAddress().equals(succId))
                {
                    Server.singleton.getGetCycle().add(hashedId);
                    if(Server.singleton.getStorage().contains(hashedId))
                    {
                        info=Server.singleton.getStorage().read(hashedId);
                    }
                    else
                    {
                        InetSocketAddress succ = Server.singleton.getNode().getSuccessor();
                        if(succ==null)
                        {
                            Colours.printRed("Restored file was not found\n");
                            Server.singleton.getGetCycle().remove(hashedId);
                            throw new Exception();
                        }
                        if(succ!=null)
                        {
                            Messages message = new Messages(succ);
                            info = message.SendGet(senderId, fileName);
                            if(info==null)
                            {
                                Colours.printRed("Restored file was not found\n");
                                Server.singleton.getGetCycle().remove(hashedId);
                                throw new Exception();
                            }
                        }
                    }
                }
                else
                {
                    Messages message = new Messages(succId);
                    info = message.SendGet(senderId, fileName);
                    if(info==null)
                    {
                        Colours.printRed("Restored file was not found\n");
                        Server.singleton.getGetCycle().remove(hashedId);
                        throw new Exception();
                    }
                }
                if(info==null)
                {
                    Colours.printRed("Restored file was not found\n");
                    Server.singleton.getGetCycle().remove(hashedId);
                    throw new Exception();
                }
                String toSendHeader = MessageHandler.OK;
                byte[] toSend = new byte[toSendHeader.length()+info.length];
                System.arraycopy(toSendHeader.getBytes(), 0, toSend, 0, toSendHeader.length());
                System.arraycopy(info, 0, toSend, toSendHeader.length(), info.length);
                Server.singleton.getGetCycle().remove(hashedId);
                this.sendResponse(toSend);
            }
            else if(splitedHeader[0].equals("GET"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                String senderId = splitedHeader[1];
                String fileName = splitedHeader[2];
                long hashedId=Hash.hashBytesInteger(senderId + fileName);
                if(Server.singleton.getGetCycle().contains(hashedId))
                {
                    Colours.printRed("Restored file was not found\n");
                    throw new Exception();
                }
                Server.singleton.getGetCycle().add(hashedId);
                byte[] info=null;
                if(Server.singleton.getStorage().contains(hashedId))
                {
                    info=Server.singleton.getStorage().read(hashedId);
                }
                else
                {
                    InetSocketAddress succ = Server.singleton.getNode().getSuccessor();
                    if(succ==null)
                    {
                        Colours.printRed("Restored file was not found\n");
                        Server.singleton.getGetCycle().remove(hashedId);
                        throw new Exception();
                    }
                    if(succ!=null)
                    {
                        Messages message = new Messages(succ);
                        info = message.SendGet(senderId, fileName);
                        if(info==null)
                        {
                            Colours.printRed("Restored file was not found\n");
                            Server.singleton.getGetCycle().remove(hashedId);
                            throw new Exception();
                        }
                    }
                }
                if(info==null)
                {
                    Colours.printRed("Restored file was not found\n");
                    Server.singleton.getGetCycle().remove(hashedId);
                    throw new Exception();
                }
                String toSendHeader = MessageHandler.OK;
                byte[] toSend = new byte[toSendHeader.length()+info.length];
                System.arraycopy(toSendHeader.getBytes(), 0, toSend, 0, toSendHeader.length());
                System.arraycopy(info, 0, toSend, toSendHeader.length(), info.length);
                Server.singleton.getGetCycle().remove(hashedId);
                this.sendResponse(toSend);
            }
            else if(splitedHeader[0].equals("DELETE"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                String senderId = splitedHeader[1];
                String fileName = splitedHeader[2];
                long hashedId=Hash.hashBytesInteger(senderId + fileName);
                Server.singleton.getRemoveCycle().add(hashedId);
                InetSocketAddress succ = Server.singleton.getNode().getSuccessor();
                if(succ!=null)
                {
                    Messages message = new Messages(succ);
                    if(!message.SendRemove(senderId, fileName))
                    {
                        Colours.printRed("Delete file was not possible\n");
                        Server.singleton.getRemoveCycle().remove(hashedId);
                        throw new Exception();
                    }
                }

                try {
                    Server.singleton.getStorage().delete(Hash.hashBytesInteger(senderId + fileName));
                } catch (Exception e) {
                }
                Server.singleton.getRemoveCycle().remove(hashedId);
                this.sendResponse(MessageHandler.OK);
            }
            else if(splitedHeader[0].equals("REMOVE"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                String senderId = splitedHeader[1];
                String fileName = splitedHeader[2];
                long hashedId=Hash.hashBytesInteger(senderId + fileName);
                if(!Server.singleton.getRemoveCycle().contains(hashedId))
                {
                    Server.singleton.getRemoveCycle().add(hashedId);
                    InetSocketAddress succ = Server.singleton.getNode().getSuccessor();
                    if(succ!=null)
                    {
                        Messages message = new Messages(succ);
                        if(!message.SendRemove(senderId, fileName))
                        {
                            Colours.printRed("Delete file was not possible\n");
                            Server.singleton.getRemoveCycle().remove(hashedId);
                            throw new Exception();
                        }
                    }

                    try {
                        Server.singleton.getStorage().delete(Hash.hashBytesInteger(senderId + fileName));
                    } catch (Exception e) {
                    }
                }
                Server.singleton.getRemoveCycle().remove(hashedId);
                this.sendResponse(MessageHandler.OK);
            }
            else if(splitedHeader[0].equals("RECLAIM"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                long space = Long.parseLong(splitedHeader[1]);
                if(Server.singleton.getStorage().reclaim(space))
                    this.sendResponse(MessageHandler.OK);
                else
                    throw new Exception();
            }
            else if(splitedHeader[0].equals("STORESPECIAL"))
            {
                Colours.printGreen("\tMessage:-->");
                System.out.print(header.trim());
                Colours.printGreen("<--\n");
                int length = bytesReceived.length - (header.length()+MessageHandler.CRLF.length());
                byte[] info = new byte[bytesReceived.length - (header.length()+MessageHandler.CRLF.length())];
                System.arraycopy(bytesReceived, header.length()+MessageHandler.CRLF.length(), info, 0,length);
                long id = Long.parseLong(splitedHeader[1]);
                if(Server.singleton.getStoreSpecialCycle().contains(id))
                {
                    Colours.printRed("File couldn't be store on another server. File will be lost\n");
                    Server.singleton.getStoreSpecialCycle().remove(id);
                    throw new Exception();
                }
                Server.singleton.getStoreSpecialCycle().add(id);
                if(!Server.singleton.getStorage().contains(id) && Server.singleton.getStorage().hasSpace(info.length))
                    Server.singleton.getStorage().store(id, info);
                else
                {
                    InetSocketAddress succ = Server.singleton.getNode().getSuccessor();  
                    if(succ==null)
                    {
                        Colours.printRed("File couldn't be stored\n");
                        Server.singleton.getStoreSpecialCycle().remove(id);
                        throw new Exception();
                    }
                    if(succ!=null)
                    {
                        Messages message = new Messages(succ);
                        if(!message.SendStoreSpecial(id, info))
                        {
                            Colours.printRed("File couldn't be stored\n");
                            Server.singleton.getStoreSpecialCycle().remove(id);
                            throw new Exception();
                        }
                    }
                }
                
                Server.singleton.getStoreSpecialCycle().remove(id);
                this.sendResponse(MessageHandler.OK);
            }
            else
            {
                Colours.printRed("Message format no supported\n");
                throw new Exception();
            }
        } catch (Exception e) {
            this.sendResponse(MessageHandler.ERROR);
        }
        try {
            this.socketChannel.close();
        } catch (Exception e) {
            Colours.printRed("A error has ocurred while trying close a tcp connection\n");
        }
    }
}