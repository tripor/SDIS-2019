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

    public DBS() {}

    public void backup(String path,String repDeg) {

        try {
            Server.singleton.sendPutChunkMessage(path , Integer.parseInt(repDeg));

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
        }

        //System.out.println("Server processing message: " + opr + "...");
    }

    public void restore(String path) {

        try {
            Server.singleton.saveFile(path, Server.singleton.sendGetChunkMessage(path));

        } catch (Exception e) {
            System.out.println("Message format wrong");
            //System.exit(3);
        }

    }

    public void delete(String path) {

        try {
            Server.singleton.sendDeletemessage(path);

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
        }

    }

    public void reclaim(String newSize) {

        System.out.println("Being build...");

    }

    public String state() {

        String state = Server.singleton.retrieve_info_file_data() + Server.singleton.retrieve_info_data() + Server.singleton.retrieve_storage_data();

        return state;
    }

}