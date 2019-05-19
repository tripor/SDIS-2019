import java.util.ArrayList;
import java.util.HashMap;

public class Delete implements Runnable {

    private Thread delete_thread;

    public Delete()
    {
        this.delete_thread= new Thread(this);
        this.delete_thread.start();
    }
    
    @Override
    public void run() {

        while(true)
        {
            try {
                Thread.sleep(60000);
            } catch (Exception e) {
                System.out.println("Thread delete interrupted. Stopping thread...");
                return ;
            }
            System.out.println("Checking if deleted chunks are deleted on everyone...");
            HashMap<String, HashMap<String, ArrayList<String>>> copy = new HashMap<String, HashMap<String, ArrayList<String>>>(Server.singleton.info);
            for(String i:copy.keySet())
            {
                for(String j:copy.get(i).keySet())
                {
                    if(!(copy.get(i).get(j).contains(Server.singleton.getServerNumber())))
                    {
                        System.out.println("Sending delete message.");
                        try {
                            Message mandar = new Message( new String[]{ "DELETE",Server.singleton.getVersion(), Server.singleton.getServerNumber(), i});
                            Server.singleton.MCsendMessage(mandar);
                        } catch (Exception e) {
                            System.out.println("Couldn't send the DELETE message. Skipping...");
                        }
                        break;
                    }
                }
            }
        }


    }

}