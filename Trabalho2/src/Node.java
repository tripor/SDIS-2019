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
        System.out.println(this.selfId);

    }
}