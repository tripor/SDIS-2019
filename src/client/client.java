import java.io.*;
import java.util.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import server.ClientInterface;

class Client
{
    public static void main(String[] args) {
        System.out.println("ola from client");

        try {
            Registry registry = LocateRegistry.getRegistry();
            ClientInterface stub = (ClientInterface) registry.lookup(args[0]);
            //String response = stub.request(algo);
            //System.out.println(fstr + " : " + response);
        } catch (Exception e) {
            //System.out.println("\n" + fstr + " : EXCEPTION_ERROR");
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
     
}