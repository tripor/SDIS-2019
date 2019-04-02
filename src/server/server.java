class Server
{
    public static void main(String[] args) {
        if(args.length!=3)
        {
            System.out.println("\nNo arguments provided. Use make Server arguments=\"<IP address> <port number> <server number>\"\n");
            System.exit(1);
        }
        Server server= new Server(args[0],Integer.parseInt(args[1]));
    }
    private Udp main_channel;

    public Server(String address,int port)
    {
        main_channel = new Udp(address,port);
    }

    
}