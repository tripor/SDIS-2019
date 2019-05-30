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
            Messages message = new Messages(anotherServer);
            if(!message.SendIamPredecessor(this.self.getHostString(),Integer.toString(this.self.getPort())))
            {
                throw new Exception();
            }
            else
            {
                this.fingerTable.setPosition(1, anotherServer);
                Colours.printYellow("A new successor was set up. Details: \n");
                Colours.printYellow("\tIp address:- ");
                System.out.println(anotherServer.getHostName());
                Colours.printYellow("\tPort:- ");
                System.out.println(anotherServer.getPort());
            }
        } catch (Exception e) {
            Colours.printRed("A error has ocurred while trying to join the ring\n");
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
        long succID = Hash.hashBytesInteger(succ);
        if(this.predecessor!=null && Hash.isBetween(Hash.hashBytesInteger(this.predecessor), id, this.selfIdInteger))
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
            if(closestPrec.equals(this.self) || closestPrec == null)
            {
                Colours.printCyan("Successor of id: " + id + " is this node\n");
                return this.self;
            }
            else
            {
                Colours.printCyan("Asking the closest predecessor of " + id +" to find the successor\n");
                try {
                    this.finding.add(id);
                    Messages findSucc = new Messages(closestPrec);
                    String[] splitedHeader = findSucc.SendFindSsuccessor(Long.toString(id));
                    String ip = splitedHeader[1];
                    int port = Integer.parseInt(splitedHeader[2]);
                    this.finding.remove(id);
                    return new InetSocketAddress(ip, port);
                } catch (Exception e) {
                    this.finding.remove(id);
                    Colours.printCyan("A error has ocurred while trying ask the successor of "+ id + ", maybe it's already searching for this id or it is not alive\n");
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
        return this.fingerTable.getSuccessor();
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

    /**
     * Checks if the currente predecessor is alive and returns it
     * @return The predecessor if alive or null
     */
    public InetSocketAddress getPreAddress()
    {
        if(this.predecessor==null)
        {
            Colours.printCyan("Predeccessor is not set\n");
            return null;
        }
        try {
            //Verificar se o predecedor estar vivo
            Colours.printCyan("Checking if predeccessor is alive\n");
            Messages message = new Messages(this.predecessor);
            if(!message.SendAlive())
            {
                throw new Exception();
            }
            else
            {
                Colours.printCyan("Predeccessor is alive\n");
            }
        } catch (Exception e) {
            Colours.printCyan("Predeccessor is not alive\n");
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