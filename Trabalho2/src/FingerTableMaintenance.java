package src;

import java.net.InetSocketAddress;

/**
 * FingerTableMaintenance
 */
public class FingerTableMaintenance implements Runnable {

    private int index=2;
    private Node belongs;
    private FingerTable fingerTable;

    public FingerTableMaintenance(Node belongs)
    {
        this.belongs=belongs;
        this.fingerTable = this.belongs.getFingerTable();
    }

    @Override
    public void run()
    {
        Colours.printPurple("-->Starting Finger Table Maintenance for index "+index +"\n");

        Colours.printPurple("Index " + index + " on the finger table is null. Trying to find the successor\n");
        long power = (long)(Math.pow(2, index-1));
        long max = (long)(Math.pow(2, this.fingerTable.getM()));
        long newValue =this.belongs.getSelfAddressInteger() + power;
        if(newValue>=max||newValue<=0)
        {
            newValue = power-(max-this.belongs.getSelfAddressInteger());
        }
        InetSocketAddress succ = this.belongs.findSuccessor(newValue);
        if(succ != null && !succ.equals(this.belongs.getSelfAddress()))//diferente de null e de mim
        {
            Colours.printPurple("The new successor of index " + index + " on the finger table is ip: "+succ.getAddress().getHostAddress() +" port: "+succ.getPort() +"\n");   
            this.fingerTable.replacePosition(index, succ);
        }
        else
        {
            Colours.printPurple("The successor of index " + index + " is this node or an error has ocurred\n");   
        }
        
        Colours.printPurple("-->Finger Table Maintenance for index "+index+" has ended\n\n");

        this.index+=1;
        if(this.index>=this.fingerTable.getM())
        {
            this.index=2;
        }
    }
    
}