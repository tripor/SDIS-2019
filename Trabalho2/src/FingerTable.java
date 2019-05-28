package src;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * FingerTable
 */
public class FingerTable {

    private HashMap<Integer, InetSocketAddress> fingerTable;
    private final int m=32;

    public FingerTable() {
        this.fingerTable = new HashMap<Integer, InetSocketAddress>();
        for (int i = 1; i <= this.m; i++) {
            this.fingerTable.put(i, null);
        }
    }

    public synchronized InetSocketAddress getSuccessor() {
        this.fixPositions();
        return this.fingerTable.get(1);
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

    public int getM()
    {
        return this.m;
    }

    public InetSocketAddress getPosition(int i)
    {
        return this.fingerTable.get(i);
    }
}

