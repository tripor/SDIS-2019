package src;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * FingerTable
 */
public class FingerTable {

    private HashMap<Integer, InetSocketAddress> fingerTable;
    private final int m=64;

    public FingerTable() {
        this.fingerTable = new HashMap<Integer, InetSocketAddress>();
        for (int i = 1; i <= this.m; i++) {
            this.fingerTable.put(i, null);
        }
    }

    public synchronized InetSocketAddress getSuccessor() {
        //TODO check is alive
        InetSocketAddress succ =this.fingerTable.get(1);
        if(succ==null)
        {
            for (int i = 1; i <= this.m; i++) {
                succ = this.fingerTable.get(i);
                if (succ != null) {
                    this.fingerTable.put(1,succ);
                    this.fingerTable.put(i,null);
                    return succ;
                }
            }
        }
        return succ;
    }

    public synchronized void overridePosition(int position, InetSocketAddress address)
    {
        this.fingerTable.put(position, address);
    }

    public synchronized void setPosition(int position, InetSocketAddress address) {
        InetSocketAddress onTable = this.fingerTable.get(position);
        if(onTable != null)
        {
            this.fingerTable.put(position, address);
            for(int i= position+1; i <= this.m;i++)
            {
                InetSocketAddress temp = this.fingerTable.get(i);
                this.fingerTable.put(i, onTable);
                onTable = temp;
            }
        }
        else
        {
            this.fingerTable.put(position, address);
        }
    }

    public synchronized InetSocketAddress closestPrecedingNode(long id)
    {
        for(int i=this.m;i>=1;i--)
        {
            InetSocketAddress position = this.fingerTable.get(i);
            if(position==null)continue;
            long tableId = Hash.hashBytesInteger(position.hashCode()+position.getPort());
            System.out.println(Server.singleton.getNode().getSelfAddressInteger());
            System.out.println(tableId);
            System.out.println(id);
            if(Hash.isBetween(Server.singleton.getNode().getSelfAddressInteger(), tableId, id))
            {
                return position;
            }
        }
        return Server.singleton.getNode().getSelfAddress();
    }

    public int getM()
    {
        return this.m;
    }

    public InetSocketAddress getPosition(int i)
    {
        return this.fingerTable.get(i);
    }

    @Override
    public synchronized String toString()
    {
        String devolver = new String();
        for(int i=1;i<=this.m;i++)
        {
            InetSocketAddress ad = this.fingerTable.get(i);
            if(ad!=null)
                devolver+= "number: "+ i +" | ip: "+ ad.getHostName() + " | port: " + ad.getPort() + " | id: " + Hash.hashBytesInteger(ad.hashCode()+ad.getPort())+"\n";
        }
        return devolver;
    }
}

