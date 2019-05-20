package src;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * FingerTable
 */
public class FingerTable {

    private HashMap<Integer, InetSocketAddress> fingerTable;

    public FingerTable() {
        this.fingerTable = new HashMap<Integer, InetSocketAddress>();
        for (int i = 1; i <= 32; i++) {
            this.fingerTable.put(i, null);
        }
    }
}