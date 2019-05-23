package src;
import java.net.InetSocketAddress;

/**
 * Node
 */
public class Node {

    private InetSocketAddress self;
    private String selfId;
    private long selfIdInteger;
    private FingerTable fingerTable;

    private InetSocketAddress predecessor;

    public Node(InetSocketAddress self)
    {
        this.predecessor=null;
        this.self=self;
        this.selfId = Hash.hashBytes(self.hashCode()+self.getPort());
        this.selfIdInteger = Hash.hashBytesInteger(self.hashCode()+self.getPort());
        System.out.println("Node created with id: " + this.selfId + " length:" + this.selfId.length());
        System.out.println("Corresponding value of id is: " + this.selfIdInteger);
        this.fingerTable = new FingerTable();
    }

    public void join(InetSocketAddress anotherServer)
    {
        assert anotherServer != null;
        try {
            TcpMessage message = new TcpMessage(anotherServer);
            //find successor
            message.sendData(MessageHandler.subst(MessageHandler.IAMPREDECESSOR,this.self.getHostString(),Integer.toString(this.self.getPort())));
            String response = new String(message.receiveData());
            if(response.startsWith("ERROR"))
            {
                throw new Exception();
            }
        } catch (Exception e) {
            System.err.println("A error has ocurred while trying to join the ring");
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
        }
        else
        {
            long predecessorId = Hash.hashBytesInteger(this.predecessor.hashCode()+this.predecessor.getPort());
            long preId = Hash.hashBytesInteger(pre.hashCode()+pre.getPort());
            if(Hash.isBetween(predecessorId, preId, this.selfIdInteger))
            {
                this.predecessor=pre;
            }
        }
    }

    public InetSocketAddress findSuccessor(String id)
    {
        return null;
    }

    public InetSocketAddress getSuccessor()
    {
        InetSocketAddress succ = this.fingerTable.getSuccessor();
        if(succ != null)
            return this.fingerTable.getSuccessor();
        else if(this.predecessor!=null)
        {
            this.fingerTable.setPosition(1,this.predecessor);
            return this.fingerTable.getSuccessor();
        }
        return null;
    }

    public FingerTable getFingerTable()
    {
        return this.fingerTable;
    }
}