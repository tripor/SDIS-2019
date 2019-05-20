package src;
import java.net.InetSocketAddress;

/**
 * Node
 */
public class Node {

    private InetSocketAddress self;
    private String selfId;
    private FingerTable fingerTable;

    private InetSocketAddress predecessor;

    public Node(InetSocketAddress self)
    {
        this.self=self;
        this.selfId = Hash.hashBytes(self.hashCode());
        this.fingerTable = new FingerTable();
    }

    public void join(InetSocketAddress anotherServer)
    {
        assert anotherServer != null;
        try {
            TcpMessage message = new TcpMessage(anotherServer);
            message.sendData(MessageHandler.subst(MessageHandler.FINDSUCCESSOR,this.selfId));
            message.receiveData();
            System.out.println("ola");
        } catch (Exception e) {
            System.err.println("A error has ocurred while trying to join the ring");
            e.printStackTrace();
            System.exit(2);
        }
    } 

    public InetSocketAddress findSuccessor(String id)
    {

    }

    public InetSocketAddress getSuccessor()
    {
        return this.fingerTable.getSuccessor();
    }

    public FingerTable getFingerTable()
    {
        return this.fingerTable;
    }
}