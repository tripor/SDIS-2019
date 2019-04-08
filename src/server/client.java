import java.io.*;
import java.util.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class Client
{
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println(
                    "\nNo arguments provided. Use make client arguments=\"<peer_ap> <sub_protocol> <opnd_1> <opnd_2>\"\n");
            System.exit(1);
        }

        try {
            Registry registry = LocateRegistry.getRegistry();
            ClientInterface stub = (ClientInterface) registry.lookup(args[0]);
            //String response = stub.request(algo);
            switch(args[1])
            {
                case "BACKUP": case "backup":
                if(args.length != 4)
                {
                    System.err.println("The BACKUP protocol must have 4 arguments: \"<peer_ap> <sub_protocol> <path> <replication degree>\"");
                    System.exit(1);
                }
                stub.backup(args[2],args[3]);
                break;

                case "RESTORE": case "restore":
                if(args.length != 3)
                {
                    System.err.println("The RESTORE protocol must have 3 arguments: \"<peer_ap> <sub_protocol> <path>\"");
                    System.exit(1);
                }
                stub.restore(args[2]);
                break;

                case "DELETE": case "delete":
                if(args.length != 3)
                {
                    System.err.println("The DELETE protocol must have 3 arguments: \"<peer_ap> <sub_protocol> <path>\"");
                    System.exit(1);
                }
                stub.delete(args[2]);
                break;

                case "RECLAIM": case "reclaim":
                if(args.length != 3)
                {
                    System.err.println("The RECLAIM protocol must have 3 arguments: \"<peer_ap> <sub_protocol> <new_space_limit>\"");
                    System.exit(1);
                }
                stub.reclaim(args[2]);
                break;

                case "STATE": case "state":
                if(args.length != 2)
                {
                    System.err.println("The STATE protocol must have 2 arguments: \"<peer_ap> <sub_protocol>\"");
                    System.exit(1);
                }
                stub.state();
                break;

                default:
                    System.err.println("The protocol entered : " + args[1] + " doesn't correspond to a valid protocol!");
                    System.exit(1);
            }
        } catch (Exception e) {

            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
     
}