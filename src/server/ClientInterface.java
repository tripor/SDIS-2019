import java.io.*;
import java.util.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.rmi.server.UnicastRemoteObject;

public interface ClientInterface extends Remote {
    void backup(String path,String repDeg) throws RemoteException;

    void restore(String path) throws RemoteException;

    void delete(String path) throws RemoteException;

    void reclaim(String newSize) throws RemoteException;

    void state() throws RemoteException;
}