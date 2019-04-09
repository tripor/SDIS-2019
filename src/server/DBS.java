import java.io.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

/*
* Class where the client interface will be implemented
* DBS-distributed backup service
*/
public class DBS implements ClientInterface {

    private Server belong;

    public DBS(Server s) { this.belong = s;}

    public void backup(String path,String repDeg) {

        try {
            this.belong.sendPutChunkMessage(this.belong.getVersion(), path , Integer.parseInt(repDeg));

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
        }

        //System.out.println("Server processing message: " + opr + "...");
    }

    public void restore(String path) {

        try {
            this.belong.saveFile(path, this.belong.sendGetChunkMessage(this.belong.getVersion(), path));

        } catch (Exception e) {
            System.out.println("Message format wrong");
            //System.exit(3);
        }

    }

    public void delete(String path) {

        try {
            this.belong.sendDeletemessage(this.belong.getVersion(), path);

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
        }

    }

    public void reclaim(String newSize) {

        System.out.println("Being build...");

    }

    public void state() {

        System.out.println("Being build...");

    }

}