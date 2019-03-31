package src.server;

class Server
{
    public static void main(String[] args) {
        if(args[0].equals("error") || args.length==0)
        {
            System.out.println("No arguments provided. Use make Server arguments=\"<arguments>\"");
        }

    }
}