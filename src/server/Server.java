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

    public static Server singleton;

    private String server_number;
    private String protocol_version;

    private Udp MDB;
    private Udp MC;
    private Udp MDR;

    private long max_size=10000;//bytes
    private long current_size=0;

    // Fileid chunkNo rep degree
    private Info info_io;

    public HashMap<String, HashMap<String, ArrayList<String>>> info = new HashMap<String, HashMap<String, ArrayList<String>>>();
    public HashMap<String, Integer> files_info = new HashMap<String, Integer>();
    public HashMap<String, HashMap<String, ArrayList<String>>> confirmation = new HashMap<String, HashMap<String, ArrayList<String>>>();

    public static void main(String[] args) {
        if (args.length != 5) { //TODO inputs dos arguments vao ainda ser diferentes +info em section 3
            System.out.println(
                    "\nNo arguments provided. Use make server arguments=\"<protocol version> <server id> <access point> <IP address> <port number>\"\n");
            System.exit(1);
        }
        Server server = new Server(args[0],args[1],args[3], Integer.parseInt(args[4]));
        server.run(args[2]);
    }

    public Server(String version,String server_number, String address, int port) {
        Server.singleton=this;
        this.server_number=server_number;
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
    }

    /**
     * Função chamada no inicio do programa
     */
    public void run(String access_point) {
        // Create the server local storage directory
        String path = "./files/server/" + this.server_number + "/backup/";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
            
        String path2 = "./files/server/" + this.server_number + "/restored/";
        File directory2 = new File(path2);
        if (!directory2.exists())
            directory2.mkdirs();
        /*
        try {
            DBS obj = new DBS(this);
            ClientInterface stub = (ClientInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(access_point, stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }*/
        
        
        try {
            if (this.server_number.equals("1")) {
                //this.sendDeletemessage("1.1", "./files/client/t.txt");
                this.sendPutChunkMessage("1.1", "./files/client/t.txt", 1);
                //this.saveFile("./files/client/t.txt", this.sendGetChunkMessage("1.1", "./files/client/t.txt"));
            } else {
                // this.MDB.receive();
            }

        } catch (Exception e) {
            System.out.println("Message format wrong");
            //System.exit(3);
        }

    }

    /**
     * Guarda um ficheiro cuja localização esta em path
     *  
     * @param version Versão que o ficheiro tem e que quero com que seja guardada
     * @param path    Path onde se encontra guardado o ficheiro
     * @param rep_deg Degree de replicação que eu quero se seja atingido
     */
    public void sendPutChunkMessage(String version, String path, int rep_deg) {
        byte[] body_completo = this.readAnyFile(path);
        if(body_completo==null)return;
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
                    mensagem = new Message(new String[] { "PUTCHUNK", version, this.server_number,
                            path, chunk_no_j, Integer.toString(rep_deg) });
                    mensagem.hashFileId();
                } catch (Exception e) {
                    System.out.println("Message format wrong");
                    return;
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
                }
            }
            try {
                Thread.sleep(i * 1000);

            } catch (InterruptedException e) {
                System.out.println("Thread was interrupted.");
                Thread.currentThread().interrupt();
            }
            i*=2;
        }

        this.confirmation.remove(Message.getSHA(path));
        this.files_info.put(path, new Integer(divisoes));
        this.info_io.saveFileInfo();

    }

    /**
     * Manda uma packet para o MDB channel, Message & Body
     * 
     * @param message A mensagem que quero que seja inviada. @Message
     * @param body    Body do chunk que quero enviar
     * @return True se foi possivel mandar a mensagem ou false caso contrario
     */
    public Boolean MDBsendMessage(Message message, byte[] body) {
        try {
            System.out.println("Sending message to MDB.");
            this.MDB.sendMessageBody(message.getMessage(), body);
        } catch (Exception e) {
            System.out.println("Couldn't send a data message. Skipping...");
            return false;
        }
        return true;
    }

    /**
     * É chamada quando um packet é recebido no MDB channel. Informação sobre a
     * mensagem esta guardada em this.MDB.getMessage() e this.MDB.getBody().trim()
     */
    public void MDBmessageReceived(String[] mensagem,byte[] body) {
        System.out.println("Received a multicast data channel message");
        if(this.current_size+body.length>this.max_size)
        {
            System.out.println("Server doesn't have enough space for saving the file. Skipping...");
            return ;
        }
        String version = mensagem[1];
        //String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        String rep = mensagem[5];
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
        this.waitRandom();
        this.sendStoredMessage(version,this.server_number, file_id, chunk_no);

    }

    private HashMap<String,HashMap<String,byte[]>> chunk_body_string = new HashMap<String,HashMap<String,byte[]>>();

    public Boolean sendStoredMessage(String version, String sender_id, String file_id, String chunk_no) {
        try {
            Message mandar = new Message(
                    new String[]{ "STORED", version, this.server_number, file_id, chunk_no });
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the STORED message. Skipping...");
            return false;
        }
        return true;
    }


    public void saveFile(String path,byte[] body)
    {
        File file= new File(path);
        String name = file.getName();

        String path_save="./files/server/"+ this.server_number+"/restored/";
        File directory = new File(path_save);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(path_save+name);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                return;
            }
        }
        try {
            save_file.delete();
            save_file.createNewFile();
            FileOutputStream fos = new FileOutputStream(path_save+name);
            fos.write(body);
            fos.close();

        } catch (IOException e) {
            System.out.println("Couldn\'t write to file. Skipping...");
            save_file.delete();
            return;
        }

    }

    public byte[] sendGetChunkMessage(String version, String file_id) {
        int number_of_chunks;
        if (this.files_info.containsKey(file_id)) {
            number_of_chunks = this.files_info.get(file_id).intValue();
        } else {
            System.out.println("This peer doesn't own this file. (GETCHUNK)");
            return null;
        }
        byte[] devolver= new byte[this.files_info.get(file_id)*64000];
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
            while(waiting_number_chunks<number_of_chunks)
            {
                int espera = 1;
                for (int i = 0; i < number_of_chunks; i++) {
                    if(this.chunk_body_string.get(file_id).get(Integer.toString(i)) != null)
                    {
                        waiting_number_chunks++;
                        continue;
                    }
                    System.out.println("Getting chunk number "+i);
                    Message mandar = new Message(new String[]{ "GETCHUNK", version,this.server_number,
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

    public Boolean sendDeletemessage(String version,String file_id)
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
            Message mandar = new Message( new String[]{ "DELETE", version, this.server_number, file_id});
            mandar.hashFileId();
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the DELETE message. Skipping...");
            return false;
        }
        this.info_io.saveFileInfo();
        return true;

    }
    
    public Boolean sendRemovedMessage(String version,String file_id,String chunk_no)
    {
        try {
            Message mandar = new Message( new String[]{ "REMOVED", version,this.server_number, file_id ,chunk_no});
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the REMOVED message. Skipping...");
            return false;
        }
        this.info_io.saveFileInfo();
        return true;

    }

    /**
     * Mandar um packet para o canal MC
     * 
     * @param message A mensagem que eu quero mandar
     * @return True se foi possivel mandar a mensagem ou false caso contrario
     */
    public Boolean MCsendMessage(Message message) {
        try {
            System.out.println("Sending message to MC.");
            this.MC.sendMessage(message.getMessage());
        } catch (Exception e) {
            System.out.println("Couldn't send a control message. Skipping...");
            return false;
        }
        return true;
    }

    /**
     * Função chamada quando é recebida um packet no MC channel. Informação em
     * this.MC.getMessage()
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
            this.info_io.saveInfo();
        } else if (mensagem[0].equals("GETCHUNK")) {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(chunk_no)) {
                
                String file_path="./files/server/"+this.server_number+"/backup/"+file_id+"/"+chunk_no;
                byte[] body2= this.readAnyFile(file_path);

                try {
                    Message message = new Message(new String[]{"CHUNK",version,this.server_number,file_id,chunk_no});
                    this.waitRandom();
                    this.sendChunkMessage(message, body2);

                } catch (Exception e) {
                    System.out.println("Message with wrong format");
                    return;
                }
                
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
                        chunks.delete();
                        this.current_size-=chunks.length();
                    }
                    dir.delete();
                }
                this.info.remove(file_id);
                String path2="./files/server/"+this.server_number+"/backup/"+file_id;
                File dir2= new File(path2);
                dir2.delete();
            }
            this.info_io.saveInfo();
        }
        else if (mensagem[0].equals("REMOVED"))
        {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if (this.info.containsKey(file_id)  && this.info.get(file_id).containsKey(chunk_no))
            {
                this.info.get(file_id).get(chunk_no).remove(sender_id);
            }
            this.checkRepDegree();
            this.info_io.saveInfo();
        }
    }

    public Boolean sendChunkMessage(Message message,byte[] body)
    {
        try {
            System.out.println("Sending message to MDB.");
            this.MDR.sendMessageBody(message.getMessage(), body);
        } catch (Exception e) {
            System.out.println("Couldn't send a data message. Skipping...");
            return false;
        }
        return true;

    }

    public void MDRmessageReceived(String[] mensagem,byte[] body) {
        System.out.println("Received a multicast data recovery channel message");
        String version = mensagem[1];
        String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        if(this.chunk_body_string.containsKey(file_id) && this.chunk_body_string.get(file_id).containsKey(chunk_no))
        {
            this.chunk_body_string.get(file_id).put(chunk_no, body);
        }


    }

    public void setServerNumber(String number) {
        this.server_number = number;
    }

    public void setProtocolVersion(String ver) {
        this.protocol_version = ver;
    }

    public void setCurrentSize(long new_size)
    {
        this.current_size=new_size;
    }

    public long getCurrentSize()
    {
        return this.current_size;
    }

    public String getVersion() {
        return this.protocol_version;
    }

    public String getServerNumber() {
        return this.server_number;
    }

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
    public void waitAmount(int tempo)
    {
        try {
            Thread.sleep(tempo);
        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }
    }

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

    private void checkRepDegree()
    {
        //TODO: verificar se o count de replicação esta direito, valor dentro do ficheiro. Se não mandar putchunk do body

    }

}