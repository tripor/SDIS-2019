package src;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * RingMaintenance
 */
public class RingMaintenance implements Runnable {

    private Node belongs;

    public RingMaintenance(Node belongs)
    {
        this.belongs = belongs;
    }

    @Override
    public void run()
    {
        System.out.println("\n-->Starting ring maintenance");
        InetSocketAddress succ = this.belongs.getSuccessor();
        if(succ==null)
        {
            System.out.println("This server is alone in the ring");
        }
        else
        {
            TcpMessage channel = null;
            do {
                try {
                    //Verificar se o successor esta vivo
                    channel = new TcpMessage(succ);
                    channel.sendData(MessageHandler.ALIVE);
                    String response = new String(channel.receiveData());
                    if(!response.startsWith("OK"))
                    {
                        throw new Exception();
                    }
                    try {
                        channel.close();
                    } catch (Exception e) {
                        System.out.println("A error has ocurred while trying to close a tcp connection");
                    }
                } catch (Exception e) {
                    //Caso o sucessor não esteja vivo ir buscar a finger table um sucessor
                    System.out.println("Successor is no longer alive. Getting next successor");
                    this.belongs.getFingerTable().overridePosition(1,null);
                    this.belongs.getFingerTable().fixPositions();
                    succ = this.belongs.getSuccessor();
                    if(succ == null)
                    {
                        //Caso não haja mais sucessores
                        System.out.println("There is no new successor available");
                        break;
                    }
                    System.out.println("New successor was set up. Details: ");
                    System.out.println("\tIp address:- " + succ.getHostName());
                    System.out.println("\tPort:- " + succ.getPort());
                }
            } while (channel == null);
            if(channel == null)
            {
                //Caso de não haver mais sucessores
                System.out.println("-->Ring maintenance has ended");
                return;
            }
            try {
                //Perguntar ao successor qual é o seu predecedor
                channel = new TcpMessage(succ);
                System.out.println("Asking what the predecessor of the successor is");
                channel.sendData(MessageHandler.YOURPRE);
                byte[] responseBytes = channel.receiveData();
                String response = new String(responseBytes);
                System.out.println("\tResponse:-->" + response.trim() + "<--");
                String[] splitedMessage = response.split(MessageHandler.CRLF);
                String header = splitedMessage[0];
                String[] splitedHeader = header.split(" ");
                channel.close();
                if(response.startsWith("MYPRE"))
                {
                    String ip = splitedHeader[1];
                    int port = Integer.parseInt(splitedHeader[2]);
                    if(ip.equals("null"))
                    {
                        //Notificar que eu sou o predecessor dele
                        System.out.println("Notifying that I'm the predecessor of the successor");
                        try {
                            InetSocketAddress self = this.belongs.getSelfAddress();
                            channel = new TcpMessage(succ);
                            channel.sendData(MessageHandler.subst(MessageHandler.IAMPREDECESSOR,self.getHostName(),Integer.toString(self.getPort())));
                            String responsePre = new String(channel.receiveData());
                            if(!responsePre.startsWith("OK"))
                            {
                                throw new Exception();
                            }
                            channel.close();
                        } catch (Exception e) {
                            System.err.println("A error has ocurred while trying to notify the successor that I'm his predecessor");
                        }
                    }
                    else
                    {
                        InetSocketAddress address = new InetSocketAddress(ip, port);
                        if(!address.equals(this.belongs.getSelfAddress()))
                        {
                            long addressValue = Hash.hashBytesInteger(address.hashCode()+address.getPort());
                            long succValue = Hash.hashBytesInteger(succ.hashCode()+succ.getPort());
                            if(Hash.isBetween(this.belongs.getSelfAddressInteger(),addressValue,succValue))
                            {
                                //Se o predecedor do successor tiver um id mais baixo, este passa a ser o meu successor
                                try {
                                    InetSocketAddress self = this.belongs.getSelfAddress();
                                    channel = new TcpMessage(ip,port);
                                    channel.sendData(MessageHandler.subst(MessageHandler.IAMPREDECESSOR,self.getHostName(),Integer.toString(self.getPort())));
                                    String responsePre = new String(channel.receiveData());
                                    if(!responsePre.startsWith("OK"))
                                    {
                                        throw new Exception();
                                    }
                                    this.belongs.getFingerTable().setPosition(1,address);
                                    System.out.println("A new successor was set up. Details: ");
                                    System.out.println("\tIp address:- " + ip);
                                    System.out.println("\tPort:- " + port);
                                    channel.close();

                                } catch (Exception e) {
                                    System.err.println("A error has ocurred while trying to set up a new successor.");
                                }

                            }
                        }
                    }
                }
                else
                {
                    throw new Exception();
                }
                
            } catch (Exception e) {
                System.err.println("A error has ocurred while trying to be the successor predecessor");
            }
        }
        System.out.println("-->Ring maintenance has ended");
    }

}