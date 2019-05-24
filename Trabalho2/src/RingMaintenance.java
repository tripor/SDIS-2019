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
        Colours.printBlue("\n-->Starting ring maintenance\n");
        InetSocketAddress succ = this.belongs.getSuccessor();
        if(succ==null)
        {
            Colours.printBlue("This server is alone in the ring\n");
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
                        Colours.printRed("A error has ocurred while trying to close a tcp connection\n");
                    }
                } catch (Exception e) {
                    //Caso o sucessor não esteja vivo ir buscar a finger table um sucessor
                    Colours.printBlue("Successor is no longer alive. Getting next successor\n");
                    this.belongs.getFingerTable().overridePosition(1,null);
                    this.belongs.getFingerTable().fixPositions();
                    succ = this.belongs.getSuccessor();
                    if(succ == null)
                    {
                        //Caso não haja mais sucessores

                        Colours.printBlue("There is no new successor available\n");
                        break;
                    }
                    Colours.printBlue("New successor was set up. Details: \n");
                    Colours.printBlue("\tIp address:- ");
                    System.out.println(succ.getHostName());
                    Colours.printBlue("\tPort:- ");
                    System.out.println(succ.getPort());
                }
            } while (channel == null);
            if(channel == null)
            {
                //Caso de não haver mais sucessores
                Colours.printBlue("-->Ring maintenance has ended\n");
                return;
            }
            try {
                //Perguntar ao successor qual é o seu predecedor
                channel = new TcpMessage(succ);
                Colours.printBlue("Asking what the predecessor of the successor is\n");
                channel.sendData(MessageHandler.YOURPRE);
                byte[] responseBytes = channel.receiveData();
                String response = new String(responseBytes);
                Colours.printBlue("\tResponse:-->");
                System.out.print(response.trim());
                Colours.printBlue("<--\n");
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
                        Colours.printBlue("Notifying that I'm the predecessor of the successor\n");
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
                            Colours.printRed("A error has ocurred while trying to notify the successor that I'm his predecessor\n");
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
                                    Colours.printBlue("A new successor was set up. Details: \n");
                                    Colours.printBlue("\tIp address:- ");
                                    System.out.println(ip);
                                    Colours.printBlue("\tPort:- ");
                                    System.out.println(port);
                                    channel.close();

                                } catch (Exception e) {
                                    Colours.printRed("A error has ocurred while trying to set up a new successor.\n");
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
                Colours.printRed("A error has ocurred while trying to be the successor predecessor\n");
            }
        }
        Colours.printBlue("-->Ring maintenance has ended\n");
    }

}