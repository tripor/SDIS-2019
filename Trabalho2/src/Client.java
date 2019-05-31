package src;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Client
 */
public class Client {

    public static void main(String[] args) {
        String option = new String();
        int port = 0;
        String ip = new String();
        if (args.length >= 3) {
            option = args[0];
            ip = args[1];
            port = Integer.parseInt(args[2]);
        } else {
            Colours.printRed("\nArguments must be: <option> <ip_adress of another server> <port of another server>\n");
            System.exit(1);
        }
        switch (option) {
        case "STATE":
            if (args.length == 3) {
                try {
                    Messages message = new Messages(ip, port);
                    String response = message.SendGetTable();
                    System.out.println("\n\n\n" + response);
                } catch (Exception e) {
                    Colours.printRed("A error has ocurred while trying to communicate with the server\n");
                    System.exit(2);
                }
            } else {
                Colours.printRed("\nArguments must be: STATE <ip_adress of another server> <port of another server>\n");
                System.exit(1);
            }

            break;
        case "BACKUP":
            if (args.length == 5) {
                String fileName = args[3];
                int rep = Integer.parseInt(args[4]);
                Storage localStorage = new Storage("./files/client/");
                byte[] info = null;
                try {
                    info = localStorage.read(fileName);
                } catch (IOException e) {
                    Colours.printRed("A error has ocurred while trying to read the file\n");
                    System.exit(1);
                }
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    Messages message = new Messages(ip, port);
                    if (message.SendBackup(inetAddress.getHostName(), fileName, rep, info)) {
                        Colours.printBlue("Backup was made successfully\n");
                    } else {
                        Colours.printRed("Backup couldn't be made\n");
                    }
                } catch (Exception e) {
                    Colours.printRed("A error has ocurred while trying to communicate with the server\n");
                    System.exit(2);
                }

            } else {
                Colours.printRed(
                        "\nArguments must be: BACKUP <ip_adress of another server> <port of another server> <file_name> <rep_degree>\n");
                System.exit(1);
            }

            break;
        case "RESTORE":
            if (args.length == 4) {
                String fileName = args[3];
                Storage localStorage = new Storage("./files/client/restored/");
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    Messages message = new Messages(ip, port);
                    byte[] info = message.SendRestore(inetAddress.getHostName(), fileName);
                    if (info != null) {
                        Colours.printBlue("Restore was made successfully\n");
                        localStorage.store(fileName, info);
                    } else {
                        Colours.printRed("Restore couldn't be made\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Colours.printRed("A error has ocurred while trying to communicate with the server\n");
                    System.exit(2);
                }

            } else {
                Colours.printRed(
                        "\nArguments must be: RESTORE <ip_adress of another server> <port of another server> <file_name>\n");
                System.exit(1);
            }

            break;
        case "DELETE":
            if (args.length == 4) {
                String fileName = args[3];
                try {
                    InetAddress inetAddress = InetAddress.getLocalHost();
                    Messages message = new Messages(ip, port);
                    if (message.SendDelete(inetAddress.getHostName(), fileName)) {
                        Colours.printBlue("Delete was made successfully\n");
                    } else {
                        Colours.printRed("Delete couldn't be made\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Colours.printRed("A error has ocurred while trying to communicate with the server\n");
                    System.exit(2);
                }

            } else {
                Colours.printRed(
                        "\nArguments must be: DELETE <ip_adress of another server> <port of another server> <file_name>\n");
                System.exit(1);
            }

            break;
        default:
            Colours.printRed("Option " + option + " doesn't exist\n");
            break;
        }
    }
}