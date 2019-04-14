import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.Random;

public class Server {
    /**
     * One server, one singleton
     */
    public static Server singleton;
    /**
     * String with the identification of the server
     */
    private String server_number;
    /**
     * The current protocol version the server is running
     */
    private String protocol_version;
    /**
     * Multicast data channel
     */
    private Udp MDB;
    /**
     * Multicast control channel
     */
    private Udp MC;
    /**
     * Multicast data recovery channel
     */
    private Udp MDR;
    /**
     * Max space the server can save files in byte amount
     */
    private long max_size=1000000;
    /**
     * The current space the server is using in byte amount
     */
    private long current_size=0;

    private Delete delete_thread;

    /**
     * Class Info to save a retrieve information
     */
    private Info info_io;
    /**
     * Data structure for saving information about the chunks saved. File_id -> Chunk_no -> Peers that saved file (first value of the arraylist is "REP<desired replication degree>"")
     */
    public HashMap<String, HashMap<String, ArrayList<String>>> info = new HashMap<String, HashMap<String, ArrayList<String>>>();
    /**
     * Data structure for saving information about the file this peer has saved.
     */
    public HashMap<String, HashMap<String, ArrayList<String>>> files_info = new HashMap<String, HashMap<String, ArrayList<String>>>();
    /**
     * Data structure for confirming the putchunks. File_id -> Chunk_no -> Peers that saved file
     */
    public HashMap<String, HashMap<String, ArrayList<String>>> confirmation = new HashMap<String, HashMap<String, ArrayList<String>>>();
    /**
     * Main
     * @param args arguments
     */
    public static void main(String[] args) {
        if (args.length != 5) { //TODO inputs dos arguments vao ainda ser diferentes +info em section 3
            System.out.println(
                    "\nNo arguments provided. Use make server arguments=\"<protocol version> <server id> <access point> <IP address> <port number>\"\n");
            System.exit(1);
        }
        Server server = new Server(args[0],args[1],args[3], Integer.parseInt(args[4]));
        server.run(args[2]);
    }
    /**
     * Constructor for the class Server
     * @param version Version of the protocol currently in use. "1.0" for base protocol
     * @param server_number The identification of the Server
     * @param address The address to use for the multicast channels
     * @param port The port to use for the multicast channels. If 1, MDB will have 1 , MC 2 and MDR 3
     */
    public Server(String version,String server_number, String address, int port) {
        Server.singleton=this;
        this.server_number=server_number;
        // Create the server local storage directory
        String path = "./files/server/" + this.server_number + "/backup/";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
            
        String path2 = "./files/server/" + this.server_number + "/restored/";
        File directory2 = new File(path2);
        if (!directory2.exists())
            directory2.mkdirs();
        this.protocol_version=version;
        this.info_io= new Info();
        this.MDB = new Udp(address, port, "MDB");
        this.MC = new Udp(address, port + 1, "MC");
        this.MDR = new Udp(address, port + 2, "MDR");

        String backup_path = "./files/server/" + Server.singleton.getServerNumber() + "/backup";
        File backup_folder = new File(backup_path);
        long size=0;
        if(backup_folder.exists())
        {
            size=Info.folderSize(backup_folder);
        }
        Server.singleton.setCurrentSize(size);
        if(this.protocol_version.equals("1.1"))
            this.delete_thread= new Delete();
    }

    /**
     * Function called to finish the server setup
     * @param access_point 
     */
    public void run(String access_point) {
        
        try {
            DBS obj = new DBS();
            ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(access_point, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
        
        /*
        try {
            if (this.server_number.equals("1")) {
                //this.sendDeletemessage("./files/client/t.txt");
                //this.sendPutChunkMessage("./files/client/t.txt", 1);
                //this.saveFile("./files/client/t.txt", this.sendGetChunkMessage("./files/client/t.txt"));
            } else {
                //System.out.print(this.clearSpaceToSave(100000));
                // this.MDB.receive();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Message format wrong");
            //System.exit(3);
        } */

    }
    /**
     * Sends the putchunk message. Should be used when you want to save a file to peers
     * @param path The path where the file is stored in the client side
     * @param rep_deg The desired replication degree
     * @return True if it was possible to save the file in the peers
     */
    public Boolean sendPutChunkMessage(String path, int rep_deg) {
        byte[] body_completo = this.readAnyFile(path);
        if(body_completo==null)return false;
        int divisoes = body_completo.length / 64000;

        if(divisoes!=0)
        {
            if (body_completo.length % 64000 != 0)
                divisoes++;
        }
        else
        {
            divisoes++;
        }
        int inicio, fim;
        inicio = 0;
        fim = 63999;
        byte[][] guardar=new byte[divisoes][];
        Boolean[] mensagem_confirmadas= new Boolean[divisoes];

        for (int j = 0; j < divisoes; j++) {
            mensagem_confirmadas[j]=false;
            String chunk_no_j = Integer.toString(j);
            if (fim > body_completo.length-1)
                fim = body_completo.length-1;
            byte[] mandar= new byte[fim-inicio+1];
            System.arraycopy(body_completo, inicio, mandar, 0, fim-inicio+1);
            guardar[j]=mandar;
            inicio += 64000;
            fim += 64000;
            String encoded_file_id=Message.getSHA(path);
            ArrayList<String> nada = new ArrayList<String>();
            if (this.confirmation.containsKey(encoded_file_id)) {
                this.confirmation.get(encoded_file_id).put(chunk_no_j, nada);
            } else {
                HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                chunk_no_hash.put(chunk_no_j, nada);
                this.confirmation.put(encoded_file_id, chunk_no_hash);
            }
        }
        int numero_mensagens_confirmadas=0;

        int i = 1;
        while(numero_mensagens_confirmadas<divisoes)
        {
            for (int j = 0; j < divisoes; j++) 
            {
                if(mensagem_confirmadas[j])continue;
                String chunk_no_j=Integer.toString(j);
                Message mensagem = null;
                try {
                    mensagem = new Message(new String[] { "PUTCHUNK", this.protocol_version, this.server_number,
                            path, chunk_no_j, Integer.toString(rep_deg) });
                    mensagem.hashFileId();
                } catch (Exception e) {
                    System.out.println("Message format wrong");
                    return false;
                }

                if(this.confirmation.get(mensagem.getFileId()).get(chunk_no_j).size() < rep_deg)
                {
                    System.out.println("Sending chunk number " + j);
                    this.MDBsendMessage(mensagem, guardar[j]);
                }
                else
                {
                    mensagem_confirmadas[j]=true;
                    numero_mensagens_confirmadas++;

                    ArrayList<String> reps = new ArrayList<String>();
                    reps.add(Integer.toString(rep_deg)); //o rep desired
                    reps.add(Integer.toString(this.confirmation.get(mensagem.getFileId()).get(chunk_no_j).size())); //o rep verdadeiro
                    if(this.files_info.containsKey(path))
                    {
                        this.files_info.get(path).put(chunk_no_j, reps);
                    } else {
                        HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                        chunk_no_hash.put(chunk_no_j, reps);
                        this.files_info.put(path, chunk_no_hash);
                    }
                }
            }
            try {
                Thread.sleep(i * 1000);

            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted.");
                Thread.currentThread().interrupt();
            }
            i*=2;
            if (i == 32) {
                System.out.println("Couldn't backup the data with the replication degree desired. Skipping");
                return false;
            }
        }

        this.confirmation.remove(Message.getSHA(path));
        this.info_io.saveFileInfo();
        return true;
    }
    
    /**
     * Sends a putchunk message. Should be used when the replication degree falls below the desired degree
     * @param version The version of the protocol used when resived the removed message
     * @param path The path of file saved in the server
     * @param file_id The file id of the file. Should be already encripted
     * @param chunk_no The chunk number we want to resend to be saved again
     * @param rep_deg The replication degree desired
     */
    public void sendPutChunkChunkMessage(String version, String path, String file_id,String chunk_no, int rep_deg) {
        byte[] body_completo = this.readAnyFile(path);
        if(body_completo==null)return;


        ArrayList<String> nada = new ArrayList<String>();
        if (this.confirmation.containsKey(file_id)) {
            this.confirmation.get(file_id).put(chunk_no, nada);
        } else {
            HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
            chunk_no_hash.put(chunk_no, nada);
            this.confirmation.put(file_id, chunk_no_hash);
        }
        
        int numero_mensagens_confirmadas=0;

        int i = 1;
        while(numero_mensagens_confirmadas<1)
        {
            Message mensagem = null;
            try {
                mensagem = new Message(new String[]{ "PUTCHUNK", version, this.server_number,file_id, chunk_no, Integer.toString(rep_deg) });
            } catch (Exception e) {
                System.out.println("Message format wrong");
                return;
            }

            if(this.confirmation.get(file_id).get(chunk_no).size() < rep_deg)
            {
                System.out.println("Sending chunk number " + chunk_no);
                this.MDBsendMessage(mensagem, body_completo);
            }
            else
            {
                numero_mensagens_confirmadas++;

                ArrayList<String> reps = new ArrayList<String>();
                reps.add(Integer.toString(rep_deg)); //o rep desired
                reps.add(Integer.toString(this.confirmation.get(file_id).get(chunk_no).size())); //o rep verdadeiro
                if(this.files_info.containsKey(file_id))
                {
                    this.files_info.get(file_id).put(chunk_no, reps);
                } else {
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    chunk_no_hash.put(chunk_no, reps);
                    this.files_info.put(file_id, chunk_no_hash);
                }
            }
            
            try {
                Thread.sleep(i * 1000);

            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted.");
                Thread.currentThread().interrupt();
            }
            i*=2;
            if (i == 32) {
                System.out.println("Couldn't backup the data with the replication degree desired. Skipping");
                return;
            }
        }

        this.confirmation.remove(Message.getSHA(path));
        this.info_io.saveFileInfo();

    }

    /**
     * Sends a message to the MDB channel.
     * @param message The header of the message
     * @param body The body with the file information
     */
    public void MDBsendMessage(Message message, byte[] body) {
        try {
            System.out.println("Sending message to MDB.");
            this.MDB.sendMessageBody(message.getMessage(), body);
        } catch (Exception e) {
            System.out.println("Couldn't send a data message. Skipping...");
            return ;
        } 
    }


    /**
     * Clears unnecessary space with replication degree higher than the desired
     * @param amount The minimum amout to remove
     * @return True if it was possible to remove the desired amount, false otherwise
     */
    public Boolean clearSpaceToSave(long amount)
    {
        long removed=0;
        ArrayList<String> to_remove= new ArrayList<String>();
        for(String i:this.info.keySet())
        {
            if(removed>=amount) break;
            for(String j:this.info.get(i).keySet())
            {
                if(removed>=amount) break;
                String rep_string=this.info.get(i).get(j).get(0);
                String[] divided = rep_string.split("REP");
                int rep_deg=Integer.parseInt(divided[1]);
                if(this.info.get(i).get(j).size()-1 > rep_deg)
                {
                    this.waitRandom();
                    if(!(this.info.get(i).get(j).size()-1 > rep_deg))
                    {
                        continue;
                    }
                    String path="./files/server/"+this.server_number+"/backup/"+i+"/"+j;
                    File delete= new File(path);
                    if(delete.exists())
                    {
                        removed+=delete.length();
                        this.current_size-=delete.length();
                        delete.delete();
                        to_remove.add(i + " " +j);
                        //System.out.println("372");
                        this.sendRemovedMessage(i,j); 
                        //System.out.println("375");
                    }
                }

            }
        }
        for(int i=0;i<to_remove.size();i++)
        {
            String[] splited=to_remove.get(i).split(" ");
            this.info.get(splited[0]).remove(splited[1]);
        }
        this.info_io.saveInfo();
        if(removed>=amount) return true;
        else return false;
    }

    /**
     * This function is called when a MDB message is received.
     * @param mensagem The header of the message received
     * @param body The body of the message received
     */
    public void MDBmessageReceived(String[] mensagem,byte[] body) {
        System.out.println("Received a multicast data channel message");
        if(!mensagem[0].equals("PUTCHUNK"))
        {
            System.out.println("Wrong message. Skipping");
            return;
        }
        String version = mensagem[1];
        //String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        String rep = mensagem[5];
        if(this.rep_check.containsKey(file_id) && this.rep_check.get(file_id).contains(chunk_no))
            this.rep_check.get(file_id).remove(chunk_no);

        
        if(version.equals("1.1"))
        {
            this.waitRandom(1600);
            if(this.rep_confirmation.containsKey(file_id) && this.rep_confirmation.get(file_id).containsKey(chunk_no))
            {
                if(this.rep_confirmation.get(file_id).get(chunk_no).size() >= Integer.parseInt(rep))
                    return;
            }
        }
        if(this.current_size+body.length>this.max_size)
        {
            if(!this.clearSpaceToSave(this.current_size+body.length-this.max_size))
            {
                System.out.println("Server doesn't have enough space for saving the file. Skipping...");
                return ;
            }
        }
        String path = "./files/server/" + this.server_number + "/backup/" + file_id;
        String file_path = path + "/" + chunk_no;
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        ArrayList<String> inserir = new ArrayList<String>();
        inserir.add("REP"+rep);
        inserir.add(this.server_number);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(file_path);
                fos.write(body);
                fos.close();

                this.setCurrentSize(this.current_size + body.length);

            } catch (IOException e) {
                System.out.println("Couldn\'t write to file. Skipping...");
                save_file.delete();
                return;
            }
            if (this.info.containsKey(file_id)) {
                this.info.get(file_id).put(chunk_no, inserir);
            } else {
                HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                chunk_no_hash.put(chunk_no, inserir);
                this.info.put(file_id, chunk_no_hash);
            }
        } else {
            this.info.get(file_id).put(chunk_no, inserir);
        }
        this.info_io.saveInfo();
        if(version.equals("1.0"))
            this.waitRandom();
        this.sendStoredMessage(file_id, chunk_no);

    }
    /**
     * Used to save diferents part of a chunked message
     */
    private HashMap<String,HashMap<String,byte[]>> chunk_body_string = new HashMap<String,HashMap<String,byte[]>>();
    
    /**
     * Sends a stored message
     * @param file_id The file id of the stored chunk
     * @param chunk_no The chunk number saved
     */
    public void sendStoredMessage(String file_id, String chunk_no) {
        try {
            Message mandar = new Message(
                    new String[]{ "STORED", this.protocol_version, this.server_number, file_id, chunk_no });
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the STORED message. Skipping...");
            return ;
        }
    }

    /**
     * Saves a file to the restored directory
     * @param path The original path of the file, or the name of the file
     * @param body The content of the file
     * @return True if it was possible to save the file, false otherwise
     */
    public Boolean saveFile(String path,byte[] body)
    {
        File file= new File(path);
        String name = file.getName();

        String path_save="./files/server/"+ this.server_number+"/restored/";
        File directory = new File(path_save);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(path_save+name);
        try {
            save_file.delete();
            save_file.createNewFile();
            FileOutputStream fos = new FileOutputStream(path_save+name);
            fos.write(body);
            fos.close();

        } catch (IOException e) {
            System.out.println("Couldn\'t write to file. Skipping...");
            save_file.delete();
            return false;
        }
        return true;

    }
    /**
     * Sends a getchunk message
     * @param file_id The path or name of the original file
     * @return The content of the file
     */
    public byte[] sendGetChunkMessage(String file_id) {
        int number_of_chunks;
        if (this.files_info.containsKey(file_id)) {
            number_of_chunks = this.files_info.get(file_id).size();
        } else {
            System.out.println("This peer doesn't own this file. (GETCHUNK)");
            return null;
        }
        byte[] devolver= new byte[number_of_chunks*64000];
        file_id = Message.getSHA(file_id);
        int pos_atual=0;
        try {
            if (this.chunk_body_string.containsKey(file_id)) {
                System.out.println("Someone is trying to get this file. Please try later.");
                return null;
            }
            for (int i = 0; i < number_of_chunks; i++)
            {
                if(this.chunk_body_string.containsKey(file_id))
                {
                    this.chunk_body_string.get(file_id).put(Integer.toString(i), null);
                }
                else
                {
                    HashMap<String,byte[]> version_hash = new HashMap<String,byte[]>();
                    version_hash.put(Integer.toString(i), null);
                    this.chunk_body_string.put(file_id, version_hash);
                }
            }
            int waiting_number_chunks=0;
            int espera = 1;
            while(waiting_number_chunks<number_of_chunks)
            {
                for (int i = 0; i < number_of_chunks; i++) {
                    if(this.chunk_body_string.get(file_id).get(Integer.toString(i)) != null)
                    {
                        waiting_number_chunks++;
                        continue;
                    }
                    System.out.println("Getting chunk number "+i);
                    Message mandar = new Message(new String[]{ "GETCHUNK", this.protocol_version,this.server_number,
                            file_id, Integer.toString(i) });
                    this.MCsendMessage(mandar);
                }
                Thread.sleep(espera * 1000);
                espera*=2;
                if (espera == 32) {
                    System.out.println("Couldn't send the GETCHUNK message. Skipping");
                    return null;
                }
            }
            for (int i = 0; i < number_of_chunks; i++)
            {
                System.arraycopy(this.chunk_body_string.get(file_id).get(Integer.toString(i)), 0, devolver, pos_atual, this.chunk_body_string.get(file_id).get(Integer.toString(i)).length);
                pos_atual+=this.chunk_body_string.get(file_id).get(Integer.toString(i)).length;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        this.chunk_body_string.remove(file_id);
        return devolver;

    }
    ArrayList<String> confirm_delete= new ArrayList<String>();
    /**
     * Sends a delete message
     * @param file_id The file id we want to delete.
     */
    public Boolean sendDeletemessage(String file_id)
    {
        if(this.protocol_version.equals("1.1"))
        {
            if(!this.files_info.containsKey(file_id))
            {
                System.out.println("Couldn't send the DELETE message. File doesn't exist. Skipping...");
                return false;
            }
            this.confirm_delete.add(Message.getSHA(file_id));//Pode só um server ter a responsabilidade de garantir que todos os outros apaguem tudo. No entanto garantir o rep degree pode ser impossivel e a função nunca returna true
            for(int i=1;i<=5;i++)
            {
                try {
                    Message mandar = new Message( new String[]{ "DELETE", this.protocol_version, this.server_number, file_id});
                    mandar.hashFileId();
                    this.MCsendMessage(mandar);
                } catch (Exception e) {
                    System.out.println("Couldn't send the DELETE message. Skipping...");
                    return false;
                }
                this.waitAmount(i*1000);
                if(this.confirm_delete.contains(Message.getSHA(file_id)))
                {
                    if(i!=5)
                    {
                        System.out.println("No server has responded. Trying again...");
                    }
                    else
                    {
                        System.out.println("No server has responded.");
                    }
                }
                else
                {
                    System.out.println("Atleast one server has responded to the delete message");
                    this.files_info.remove(file_id);
                    break;
                }
            }
            this.confirm_delete.remove(Message.getSHA(file_id));
        }
        else
        {
            
            if(this.files_info.containsKey(file_id))
            {
                this.files_info.remove(file_id);
            }
            else
            {
                System.out.println("Couldn't send the DELETE message. File doesn't exist. Skipping...");
                return false;
            }
            try {
                Message mandar = new Message( new String[]{ "DELETE", this.protocol_version, this.server_number, file_id});
                mandar.hashFileId();
                this.MCsendMessage(mandar);
            } catch (Exception e) {
                System.out.println("Couldn't send the DELETE message. Skipping...");
                return false;
            }
        }
        this.info_io.saveFileInfo();
        return true;

    }
    /**
     * Sends a removed message
     * @param file_id The file id's chunk removed
     * @param chunk_no The chunk removed
     */
    public void sendRemovedMessage(String file_id,String chunk_no)
    {
        try {
            Message mandar = new Message( new String[]{ "REMOVED", this.protocol_version,this.server_number, file_id ,chunk_no});
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the REMOVED message. Skipping...");
            return;
        }
        this.info_io.saveFileInfo();

    }
    /**
     * Sends a message to the MC channel
     * @param message The header of the message
     */
    public void MCsendMessage(Message message) {
        try {
            System.out.println("Sending message to MC.");
            this.MC.sendMessage(message.getMessage());
        } catch (Exception e) {
            System.out.println("Couldn't send a control message. Skipping...");
            return ;
        }
    }

    /**
     * Data structure for helping in the first enhancement to keep rep degrees stable
     */
    HashMap<String, HashMap<String, ArrayList<String>>> rep_confirmation = new HashMap<String, HashMap<String, ArrayList<String>>>();
    /**
     * Data structure for helping in the flux control of the getchunk message
     */
    HashMap<String,HashMap<String,Boolean>> flux_control= new HashMap<String,HashMap<String,Boolean>>();
    /**
     * This function is called when a MC message is received
     * @param mensagem The header of the message
     * @param body The body if there is any
     */
    public void MCmessageReceived(String[] mensagem,byte[] body) {
        System.out.println("Received a multicast control channel message");

        if (mensagem[0].equals("STORED")) {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            
            if (this.confirmation.containsKey(file_id)) {
                if(this.confirmation.get(file_id).containsKey(chunk_no))
                    if (!this.confirmation.get(file_id).get(chunk_no).contains(sender_id))
                        this.confirmation.get(file_id).get(chunk_no).add(sender_id);
            } else if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(chunk_no)) {
                if (!this.info.get(file_id).get(chunk_no).contains(sender_id))
                    this.info.get(file_id).get(chunk_no).add(sender_id);
            }
            
            if(version.equals("1.1"))
            {
                ArrayList<String> inserir = new ArrayList<String>();
                inserir.add(sender_id);
                if (this.rep_confirmation.containsKey(file_id)) {
                    if(this.rep_confirmation.get(file_id).containsKey(chunk_no))
                    {
                        if(!this.rep_confirmation.get(file_id).get(chunk_no).contains(sender_id))
                            this.rep_confirmation.get(file_id).get(chunk_no).add(sender_id);
                    }
                    else
                        this.rep_confirmation.get(file_id).put(chunk_no, inserir);
                } else {
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    chunk_no_hash.put(chunk_no, inserir);
                    this.rep_confirmation.put(file_id, chunk_no_hash);
                }
            } 
            this.info_io.saveInfo();
        } else if (mensagem[0].equals("GETCHUNK")) {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(chunk_no)) {
                
                if(this.flux_control.containsKey(file_id))
                {
                    this.flux_control.get(file_id).put(chunk_no, false);
                }
                else
                {
                    HashMap<String,Boolean> novo = new HashMap<String,Boolean>();
                    novo.put(chunk_no, false);
                    this.flux_control.put(file_id, novo);
                }
                String file_path="./files/server/"+this.server_number+"/backup/"+file_id+"/"+chunk_no;
                byte[] body2= this.readAnyFile(file_path);

                try {
                    Message message = new Message(new String[]{"CHUNK",version,this.server_number,file_id,chunk_no});
                    this.waitRandom();
                    if(!this.flux_control.get(file_id).get(chunk_no))
                        this.sendChunkMessage(message, body2);

                } catch (Exception e) {
                    System.out.println("Message with wrong format");
                    return;
                }
                this.flux_control.get(file_id).remove(chunk_no);
                if(this.flux_control.get(file_id).size()==0)
                    this.flux_control.remove(file_id);
            }

        }
        else if (mensagem[0].equals("DELETE"))
        {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            if (this.info.containsKey(file_id))
            {
                String path="./files/server/"+this.server_number+"/backup/"+file_id;
                File dir= new File(path);
                if(dir.exists())
                {
                    for(File chunks:dir.listFiles())
                    {
                        this.current_size-=chunks.length();
                        chunks.delete();
                    }
                    dir.delete();
                }
                if(version.equals("1.0"))
                    this.info.remove(file_id);
                else if(version.equals("1.1"))
                {
                    ArrayList<String> to_remove= new ArrayList<String>();
                    for(String i:this.info.get(file_id).keySet())
                    {
                        this.info.get(file_id).get(i).remove(this.server_number);
                        this.info.get(file_id).get(i).remove(sender_id);
                        if(this.info.get(file_id).get(i).size()==1)
                            to_remove.add(i);
                    }
                    for(int i=0;i<to_remove.size();i++)
                    {
                        this.info.get(file_id).remove(to_remove.get(i));
                    }
                    if(this.info.get(file_id).size()==0)
                        this.info.remove(file_id);
                    this.sendRemovedMessage(file_id, "-1");
                }
                String path2="./files/server/"+this.server_number+"/backup/"+file_id;
                File dir2= new File(path2);
                dir2.delete();
            }
            else if(version.equals("1.1"))
            {
                this.sendRemovedMessage(file_id, "-1");
            }
            this.info_io.saveInfo();
        }
        else if (mensagem[0].equals("REMOVED"))
        {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if(version.equals("1.1"))
            {
                if(chunk_no.equals("-1"))
                {
                    this.confirm_delete.remove(file_id);
                    if (this.info.containsKey(file_id))
                    {
                        ArrayList<String> remover= new ArrayList<String>();
                        HashMap<String, HashMap<String, ArrayList<String>>> copy=new HashMap<String, HashMap<String, ArrayList<String>>>(this.info);
                        for(String i: copy.get(file_id).keySet())
                        {
                            this.info.get(file_id).get(i).remove(sender_id);
                            if(this.info.get(file_id).get(i).contains(this.server_number))
                                this.checkRepDegree(file_id,i,version);
                            if(this.info.get(file_id).get(i).size()==1)
                            {
                                remover.add(i);
                            }
                        }
                        for(int i=0;i<remover.size();i++)
                        {
                            this.info.get(file_id).remove(remover.get(i));
                        }
                        if(this.info.get(file_id).size()==0)
                        {
                            this.info.remove(file_id);
                        }
                    }
                }
                else
                {
                    if (this.info.containsKey(file_id)  && this.info.get(file_id).containsKey(chunk_no))
                    {
                        this.info.get(file_id).get(chunk_no).remove(sender_id);
                        if(this.info.get(file_id).get(chunk_no).contains(this.server_number))
                            this.checkRepDegree(file_id,chunk_no,version);
                        if(this.info.get(file_id).get(chunk_no).size()==1)
                        {
                            this.info.get(file_id).remove(chunk_no);
                        }
                        if(this.info.get(file_id).size()==0)
                        {
                            this.info.remove(file_id);
                        }
                    }
                }

                this.info_io.saveInfo();
            }
            else if (this.info.containsKey(file_id)  && this.info.get(file_id).containsKey(chunk_no))
            {
                this.info.get(file_id).get(chunk_no).remove(sender_id);
                this.checkRepDegree(file_id,chunk_no,version);
                this.info_io.saveInfo();
            }
        }
    }
    /**
     * Sends a chunk message 
     * @param message The header of the message
     * @param body The body of the chunk to send
     */
    public void sendChunkMessage(Message message,byte[] body)
    {
        try {
            System.out.println("Sending message to MDB.");
            this.MDR.sendMessageBody(message.getMessage(), body);
        } catch (Exception e) {
            System.out.println("Couldn't send a data message. Skipping...");
            return ;
        }

    }
    /**
     * This function is called when a MDR message is received
     * @param mensagem The header of the message received
     * @param body The body of the chunk received
     */
    public void MDRmessageReceived(String[] mensagem,byte[] body) {
        System.out.println("Received a multicast data recovery channel message");
        if(!mensagem[0].equals("CHUNK"))
        {
            System.out.println("Wrong message. Skipping...");
            return;
        }
        String version = mensagem[1];
        String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        if(this.flux_control.containsKey(file_id) && this.flux_control.get(file_id).containsKey(chunk_no))
        {
            this.flux_control.get(file_id).put(chunk_no, true);
        }
        if(this.chunk_body_string.containsKey(file_id) && this.chunk_body_string.get(file_id).containsKey(chunk_no))
        {
            this.chunk_body_string.get(file_id).put(chunk_no, body);
        }


    }

    /**
     * This function is called for the reclaim storage protocol
     * It must clean the chunks needed to get the current size to be < to the new maxSize
     */
    public void reclaim()
    {
        ArrayList<String> to_remove= new ArrayList<String>();
        
        for(String i:this.info.keySet())
        {
            for(String j:this.info.get(i).keySet())
            {
                //if the chunks deleted were enough to repair the current size so it is <= to max size
                if(this.max_size >= this.current_size)
                    return;

                String path="./files/server/"+this.server_number+"/backup/"+i+"/"+j;
                File delete= new File(path);

                if(delete.exists())
                {
                    this.current_size-=delete.length();
                    delete.delete();
                    to_remove.add(i + " " +j);

                    this.sendRemovedMessage(i,j);
                }
            }
        }

        for(int i=0;i<to_remove.size();i++)
        {
            String[] splited=to_remove.get(i).split(" ");
            this.info.get(splited[0]).remove(splited[1]);
        }
        this.info_io.saveInfo();
    }


    /**
     * Sets the server identifier
     * @param number The server identification
     */
    public void setServerNumber(String number) {
        this.server_number = number;
    }
    /**
     * Sets the protocol version of this server to run
     * @param ver The version I want to swap to
     */
    public void setProtocolVersion(String ver) {
        this.protocol_version = ver;
    }
    /**
     * Set the current size the server is taking
     * @param new_size The new size the server is taking
     */
    public void setCurrentSize(long new_size)
    {
        this.current_size=new_size;
    }

    /**
     * Set the max size the server is taking
     * @param new_size The new max size the server is taking
     */
    public void setMaxSize(long new_size)
    {
        this.max_size=new_size;
    }
    /**
     * Gets the current size the server is taking
     * @return The current size the server is taking
     */
    public long getCurrentSize()
    {
        return this.current_size;
    }
    /**
     * Get the current version the server is running
     * @return The version the server is running
     */
    public String getVersion() {
        return this.protocol_version;
    }
    /**
     * Gets the identification of the server
     * @return The identification of the server
     */
    public String getServerNumber() {
        return this.server_number;
    }
    /**
     * Waits a random amount of time between 0 and 400 ms
     */
    public void waitRandom()
    {
        try {
            Random rand = new Random();
            int n = rand.nextInt(401);
            Thread.sleep(n);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Waits a random amount of time between 0 and tempo ms
     * @param tempo The amount of time desired to sleep measured in ms
     */
    public void waitRandom(int tempo)
    {
        try {
            Random rand = new Random();
            int n = rand.nextInt(tempo+1);
            Thread.sleep(n);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }
    }
    /**
     * Sleeps the amount of time desired
     * @param tempo The amount of time desired to sleep measured in ms
     */
    public void waitAmount(int tempo)
    {
        try {
            Thread.sleep(tempo);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }
    }
    /**
     * Reads any type of file
     * @param path The path of the file we want to read
     * @return Array of bytes with the file information
     */
    public byte[] readAnyFile(String path)
    {
        File file = new File(path);
        byte fileContent[] = new byte[(int)file.length()];
        try {
            FileInputStream fin = new FileInputStream(file);
            fin.read(fileContent);
            return fileContent;
        }
        catch (Exception e) {
            System.out.println("Error reading file");
            return null;
        }
    }
    /**
     * Auxiliar to check if anyother peer have started the putchunk protocol on the same file
     */
    HashMap<String,ArrayList<String>> rep_check= new HashMap<String,ArrayList<String>>();
    /**
     * Check the replication degree of a selected chunk. Should be called after receiving a removed message
     * @param file_id The file id we want to check
     * @param chunk_no The chunk number we want to check
     * @param version The version of the protocol of the removed message
     */
    private void checkRepDegree(String file_id,String chunk_no,String  version)
    {
        if(this.info.containsKey(file_id) && this.info.get(file_id).containsKey(chunk_no))
        {
            String rep_string=this.info.get(file_id).get(chunk_no).get(0);
            String[] divided = rep_string.split("REP");
            int rep_deg=Integer.parseInt(divided[1]);
            if(this.info.get(file_id).get(chunk_no).size()-1 < rep_deg)
            {
                if(this.rep_check.containsKey(file_id))
                {
                    this.rep_check.get(file_id).add(chunk_no);
                }
                else
                {
                    ArrayList<String> novo= new ArrayList<String>();
                    novo.add(chunk_no);
                    this.rep_check.put(file_id,novo);
                }
                this.waitRandom();
                if(this.rep_check.get(file_id).contains(chunk_no))
                {
                    this.sendPutChunkChunkMessage(version, "./files/server/"+this.server_number+"/backup/"+file_id+"/"+chunk_no,file_id,chunk_no, rep_deg);
                }
            }
        }
    }
    /**
     * This function retrieves the local service state information
     * for each file whose backup this peer has initiated
     * @return A string with the info_file data for the local service state info
     */
    public String retrieve_info_file_data()
    {
        String data = "------------------------------------------- LOCAL SERVICE STATE INFO -------------------------------------------\n\n\n";
        
        for(String i : this.files_info.keySet())
        {
            data += "Backed up file information:\n\n";
            data += "\tFile Pathname: " + i + "\n";
            data += "\tBackup id: " + Message.getSHA(i) + "\n";
            Boolean init_info = true;
            for(String j : this.files_info.get(i).keySet())
            {
                if(init_info)
                {
                    data += "\tDesired Replication Degree: " + this.files_info.get(i).get(j).get(0) + "\n";
                    data += "\t\tChunk - Perceived Rep Degree:\n";
                    init_info = false;
                }
                data += "\t\t" + j + "\t" + this.files_info.get(i).get(j).get(1) + "\n";
            }
        }
        return data += "\n\n";
    }
    /**
     * This function retrieves the local service state information
     * for each chunk it stores
     * @return A string with the info data for the local service state info
     */
    public String retrieve_info_data()
    {
        String data = "";
        for(String i : this.info.keySet())
        {
            data += "Chunks Stored (" + i + "):\n\n";
            for(String j : this.info.get(i).keySet())
            {
                data += "Id: " + j + "\t";

                String path="./files/server/"+this.server_number+"/backup/"+i+"/"+j;
                File dir= new File(path);
                if(dir.exists())
                {
                    long SIZE = dir.length();
                    SIZE /= 1000;
                    data += "Size: " + Long.toString(SIZE) + "." + Long.toString(SIZE % 1000) + "KB\t";
                }
                data += "Perceived Rep Degree: " + Integer.toString(this.info.get(i).get(j).size()-1) + "\n";
            }
        }
        return data += "\n\n";
    }
    /**
     * This function retrieves the local service state information
     * for the peer's storage capacity and the amount of storage (both in KBytes) used to backup the chunks.
     * @return A string with the info data for the local service state info
     */
    public String retrieve_storage_data()
    {
        double perc = this.current_size*100/this.max_size;
        String data = "Peer's Stored Capacity: " + Long.toString(this.max_size / 1000) + "." + Long.toString(this.max_size % 1000) + "KB\t";
        data += "Used: " + Long.toString(this.current_size / 1000) + "." + Long.toString(this.current_size % 1000) + "KB (" + Double.toString(perc) + "%)\n\n";

        return data;
    }

}