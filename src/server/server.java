class Server
{
    public static void main(String[] args) {
        if(args.length!=2)
        {
            System.out.println("\nNo arguments provided. Use make Server arguments=\"<IP address> <port number>\"\n");
            System.exit(1);
        }
        Server server= new Server(args[0],Integer.parseInt(args[1]));
    }

    private int port;
    private String address;

    public Server(String address,int port)
    {

    }
}