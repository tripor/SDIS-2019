import java.io.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

public interface ClientInterface extends Remote {
    String request(String opr) throws RemoteException;
}

/* 
* Class where the client interface will be implemented
* DBS-distributed backup service
*/
/*public class DBS implements ClientInterface {
    public DBS() {}

    public String request(String opr) {

        System.out.println("Server processing message: " + opr + "...");
    }

}*/