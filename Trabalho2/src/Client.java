package src;

/**
 * Client
 */
public class Client {

    public static void main(String[] args) {
        String option=new String();
        int port = 0;
        String ip=new String();
        if (args.length >= 3) {
            option = args[0];
            port = Integer.parseInt(args[2]);
            ip = args[1];
        } else {
            System.err.println("\nArguments must be: <option> <ip_adress of another server> <port of another server>\n");
            System.exit(1);
        }
        switch (option) {
            case "Table":
                try {
                    TcpMessage message = new TcpMessage(ip,port);
                    message.sendData(MessageHandler.GETTABLE);
                    String response = new String(message.receiveData());
                    String[] splitedMessage = response.split(MessageHandler.CRLF);
                    String header = splitedMessage[0];
                    String[] splitedHeader = header.split(" ");
                    if(splitedHeader[0].equals("ERROR"))
                    {
                        message.close();
                        throw new Exception();
                    }
                    System.out.println(splitedMessage[1]);
                    message.close();
                } catch (Exception e) {
                    //TODO: handle exception
                }    

                break;
        
            default:
                break;
        }
    }
}