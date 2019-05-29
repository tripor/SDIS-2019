package src;

import java.net.InetSocketAddress;
import java.util.ArrayList;

/**
 * Node
 */
public class Node {

    private ArrayList<Long> finding;

    private InetSocketAddress self;
    private String selfId;
    private long selfIdInteger;
    private FingerTable fingerTable;

    private InetSocketAddress predecessor;

    public Node(InetSocketAddress self)
    {
        this.predecessor=null;
        this.finding = new ArrayList<Long>();
        this.self=self;
        this.selfId = Hash.hashBytes(self.hashCode()+self.getPort());
        this.selfIdInteger = Hash.hashBytesInteger(self.hashCode()+self.getPort());
        Colours.printCyan("Node created with id: " + this.selfId + " length:" + this.selfId.length()+"\n");
        Colours.printCyan("Corresponding value of id is: " + this.selfIdInteger+"\n");
        this.fingerTable = new FingerTable();
    }

    public void join(InetSocketAddress anotherServer)
    {
        assert anotherServer != null;
        try {
            TcpMessage message = new TcpMessage(anotherServer);
            message.sendData(MessageHandler.subst(MessageHandler.IAMPREDECESSOR,this.self.getHostString(),Integer.toString(this.self.getPort())));
            String response = new String(message.receiveData());
            if(response.startsWith("ERROR"))
            {
                message.close();
                throw new Exception();
            }
            this.fingerTable.setPosition(1,anotherServer);
            message.close();
        } catch (Exception e) {
            Colours.printRed("A error has ocurred while trying to join the ring\n");
            e.printStackTrace();
            System.exit(2);
        }
    } 

    public void hasPredecessor(InetSocketAddress pre)
    {
        assert pre!=null;
        if(this.predecessor==null)
        {
            this.predecessor=pre;
            Colours.printCyan("A new predecessor was set up. Details: \n");
            Colours.printCyan("\tIp: ");
            System.out.println(pre.getHostName());
            Colours.printCyan("\tPort: ");
            System.out.println(pre.getPort());
        }
        else
        {
            long predecessorId = Hash.hashBytesInteger(this.predecessor.hashCode()+this.predecessor.getPort());
            long preId = Hash.hashBytesInteger(pre.hashCode()+pre.getPort());
            if(Hash.isBetween(predecessorId, preId, this.selfIdInteger))
            {
                this.predecessor=pre;
                Colours.printCyan("A new predecessor was set up. Details: \n");
                Colours.printCyan("\tIp: ");
                System.out.println(pre.getHostName());
                Colours.printCyan("\tPort: ");
                System.out.println(pre.getPort());
            }
        }
    }

    public InetSocketAddress findSuccessor(long id)
    {
        Colours.printCyan("Finding successor of id: " + id + "\n");
        InetSocketAddress succ = this.getSuccessor();
        if(succ==null)
        {
            Colours.printCyan("This node is alone in the ring so the successor of id " + id + " is this node\n");
            return this.self;
        }
        long succID = Hash.hashBytesInteger(succ.hashCode()+succ.getPort());
        if(this.predecessor!=null && Hash.isBetween(Hash.hashBytesInteger(this.predecessor.hashCode()+this.predecessor.getPort()), id, this.selfIdInteger))
        {
            Colours.printCyan("Successor of id: " + id + " is this node with id : " + this.selfIdInteger + "\n");
            return this.self;
        }
        else if(Hash.isBetween(this.selfIdInteger, id, succID))
        {
            Colours.printCyan("Successor of id: " + id + " is the successor of this node with id: " + succID + "\n");
            return succ;
        }
        else
        {
            InetSocketAddress closestPrec = this.closestPrecedingNode(id);
            if(closestPrec.equals(this.self))
            {
                Colours.printCyan("Successor of id: " + id + " is this node\n");
                return this.self;
            }
            else
            {
                Colours.printCyan("Asking the closest predecessor of " + id +" to find the successor\n");
                try {
                    this.finding.add(id);
                    TcpMessage findSucc = new TcpMessage(closestPrec);
                    findSucc.sendData(MessageHandler.subst(MessageHandler.FINDSUCCESSOR, Long.toString(id)));
                    String response = new String(findSucc.receiveData());
                    Colours.printCyan("\tMessage:-->");
                    System.out.print(response.trim());
                    Colours.printCyan("<--\n");
                    String[] splitedMessage = response.split(MessageHandler.CRLF);
                    String header = splitedMessage[0];
                    String[] splitedHeader = header.split(" ");
                    InetSocketAddress devolver = null;
                    if(!splitedHeader[0].equals("SUCC"))
                    {
                        Colours.printCyan("The closest predecessor didn't find the successor of "+id+"\n");
                    }
                    else
                    {
                        String ip = splitedHeader[1];
                        int port = Integer.parseInt(splitedHeader[2]);
                        devolver= new InetSocketAddress(ip, port);
                    }
                    findSucc.close();
                    this.finding.remove(id);
                    return devolver;
                } catch (Exception e) {
                    e.printStackTrace();//TODO acontece um erro aqui
                    Colours.printRed("A error has ocurred while trying ask the successor of "+ id + "\n");
                }
            }
        }

        return null;
    }

    public InetSocketAddress closestPrecedingNode(long id)
    {
        return this.fingerTable.closestPrecedingNode(id);
    }

    public InetSocketAddress getSuccessor()
    {
        InetSocketAddress succ = this.fingerTable.getSuccessor();
        if(succ != null)
            return this.fingerTable.getSuccessor();
        else if(this.predecessor!=null)
        {
            try {
                //Verificar se o predecedor estar vivo
                TcpMessage test = new TcpMessage(this.predecessor);
                test.sendData(MessageHandler.ALIVE);
                String response = new String(test.receiveData());
                if(!response.startsWith("OK"))
                {
                    throw new Exception();
                }
                test.close();
            } catch (Exception e) {
                this.predecessor=null;
                return null;
            }
            this.fingerTable.setPosition(1,this.predecessor);
            return this.fingerTable.getSuccessor();
        }
        return null;
    }

    public FingerTable getFingerTable()
    {
        return this.fingerTable;
    }

    public InetSocketAddress getSelfAddress()
    {
        return this.self;
    }
    public long getSelfAddressInteger()
    {
        return this.selfIdInteger;
    }

    public InetSocketAddress getPreAddress()
    {
        try {
            //Verificar se o predecedor estar vivo
            TcpMessage test = new TcpMessage(this.predecessor);
            test.sendData(MessageHandler.ALIVE);
            String response = new String(test.receiveData());
            if(!response.startsWith("OK"))
            {
                throw new Exception();
            }
            test.close();
        } catch (Exception e) {
            this.predecessor=null;
            return null;
        }
        return this.predecessor;
    }
    public void deletePre()
    {
        this.predecessor=null;
    }
    public Boolean isSearchingId(long id)
    {
        return this.finding.contains(id);
    }



}