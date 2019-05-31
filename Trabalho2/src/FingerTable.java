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
    /**
     * Garanties that the it returns a alive successor if possible or null
     * @return One alive successor or null
     */
    public synchronized InetSocketAddress getSuccessor() {
        InetSocketAddress succ =this.fingerTable.get(1);
        if(succ!=null)
        {   try {
                Messages message = new Messages(succ);
                if(!message.SendAlive())
                {
                    throw new Exception();
                }
                else
                {
                    Colours.printYellow("The current successor is alive\n");
                }
            } catch (Exception e) {
                Colours.printYellow("The current successor is not alive\n");
                this.fingerTable.put(1, null);
                succ=null;
            }
        }
        if(succ==null)
        {
            for (int i = 1; i <= this.m; i++) {
                succ = this.fingerTable.get(i);
                if (succ != null) {
                    this.fingerTable.put(i,null);
                    try {
                        Colours.printYellow("Checking if next successor in the finger table is alive\n");
                        Messages message = new Messages(succ);
                        if(!message.SendAlive())
                        {
                            Colours.printYellow("The next successor in the finger table is not alive\n");
                            succ=null;
                        }
                        else
                        {
                            this.fingerTable.clear();
                            this.fingerTable.put(1,succ);
                            Colours.printYellow("A new successor was set up. Details: \n");
                            Colours.printYellow("\tIp address:- ");
                            System.out.println(succ.getAddress().getHostAddress());
                            Colours.printYellow("\tPort:- ");
                            System.out.println(succ.getPort());
                            return succ;
                        }
                    } catch (Exception e) {
                        Colours.printYellow("The next successor in the finger table is not alive\n");
                        succ=null;
                    }
                }
            }
        }
        if(succ==null)
        {
            InetSocketAddress pre = Server.singleton.getNode().getPreAddress();
            if(pre!=null)
            {
                this.fingerTable.clear();
                this.fingerTable.put(1,pre);
                succ=pre;
                Colours.printYellow("A new successor was set up. Details: \n");
                Colours.printYellow("\tIp address:- ");
                System.out.println(succ.getAddress().getHostAddress());
                Colours.printYellow("\tPort:- ");
                System.out.println(succ.getPort());
            }
        }
        return succ;
    }

    public synchronized void fixPositions() {
        HashMap<Integer, InetSocketAddress> newTable = new HashMap<Integer, InetSocketAddress>();
        int j = 1;
        for (int i = 1; i <= this.m; i++) {
            InetSocketAddress value = this.fingerTable.get(1);
            if (value != null) {
                newTable.put(j, value);
                j++;
            }
        }
        for (; j <= this.m; j++) {
            newTable.put(j,null);
        }
        this.fingerTable=newTable;
    }

    public synchronized void clear()
    {
        this.fingerTable = new HashMap<Integer, InetSocketAddress>();
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
    public synchronized void replacePosition(int position, InetSocketAddress address) {
        for(int i=2;i<=this.m;i++)
        {
            if(this.fingerTable.get(i)==null)continue;
            if(i>position)
            {
                if(this.fingerTable.get(i).equals(address))
                {
                    return;
                }
            }
            else
            {
                if(this.fingerTable.get(i).equals(address))
                {
                    this.fingerTable.put(i, null);
                }
            }
        }
        this.fingerTable.put(position, address);
    }

    public synchronized InetSocketAddress closestPrecedingNode(long id)
    {
        for(int i=this.m;i>=1;i--)
        {
            InetSocketAddress position = this.fingerTable.get(i);
            if(position==null)continue;
            try {
                Messages message = new Messages(position);
                if(!message.SendAlive())
                {
                    throw new Exception();
                }
            } catch (Exception e) {
                Colours.printYellow("The node in the fingerTable is not alive. Discarting and finding a new preceding node\n");
                this.fingerTable.put(i, null);
                continue;
            }
            long tableId = Hash.hashBytesInteger(position);
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
        devolver+= "Finger table:\n";
        for(int i=1;i<=this.m;i++)
        {
            InetSocketAddress ad = this.fingerTable.get(i);
            if(ad!=null)
                devolver+= "number: "+ i +" | ip: "+ ad.getAddress().getHostAddress() + " | port: " + ad.getPort() + " | id: " + Hash.hashBytesInteger(ad.hashCode()+ad.getPort())+"\n";
        }
        return devolver;
    }
}

