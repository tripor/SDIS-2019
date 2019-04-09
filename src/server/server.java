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

    private int server_number;
    private String protocol_version;

    private Udp MDB;
    private Udp MC;
    private Udp MDR;

    private int max_number_of_chunks=100;
    private int number_of_chunks=0;

    // Fileid Version chunkNo rep degree
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> info = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>();
    private HashMap<String, HashMap<String, Integer>> files_info = new HashMap<String, HashMap<String, Integer>>();

    public static void main(String[] args) {
        if (args.length != 5) { //TODO inputs dos arguments vao ainda ser diferentes +info em section 3
            System.out.println(
                    "\nNo arguments provided. Use make server arguments=\"<protocol version> <server id> <access point> <IP address> <port number>\"\n");
            System.exit(1);
        }
        Server server = new Server(args[3], Integer.parseInt(args[4]));
        server.setServerNumber(Integer.parseInt(args[1]));
        server.setProtocolVersion(args[0]);
        server.readInfo();
        server.readFileInfo();
        server.run(args[2]);
    }

    public Server(String address, int port) {
        this.MDB = new Udp(address, port, this, "MDB");
        this.MC = new Udp(address, port + 1, this, "MC");
        this.MDR = new Udp(address, port + 2, this, "MDR");
    }

    /**
     * Função chamada no inicio do programa
     */
    public void run(String access_point) {
        // Create the server local storage directory
        String path = "./files/server/" + this.server_number + "/save/";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
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
        }
        */
        
        try {
            if (this.server_number == 1) {
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

    private Boolean usingInfo = false;

    /**
     * Guarda informação sobre os ficheiro que já guardou, as versoes dos ficheiros
     * e que peers tem os chunk guardados(incluindo ele proprio)
     */
    public synchronized void saveInfo() {
        while (usingInfo) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
        this.usingInfo = true;
        String path = "./files/server/" + this.server_number + "/";
        String file_path = path + "info.txt";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                this.usingInfo = false;
                notify();
                return;
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file_path));
            bw.write("");
            bw.write(this.info.size() + "\n");
            // File id
            for (String i : this.info.keySet()) {
                bw.write(i + "\n");
                bw.write(this.info.get(i).size() + "\n");
                // version
                for (String j : this.info.get(i).keySet()) {
                    bw.write(j + "\n");
                    bw.write(this.info.get(i).get(j).size() + "\n");
                    // chunk no
                    for (String k : this.info.get(i).get(j).keySet()) {
                        bw.write(k + "\n");
                        // rep
                        bw.write(this.info.get(i).get(j).get(k).size() + "\n");
                        for (int l = 0; l < this.info.get(i).get(j).get(k).size(); l++) {
                            bw.write(this.info.get(i).get(j).get(k).get(l) + "\n");
                        }
                    }
                }
            }
            bw.close();

        } catch (IOException e) {
            System.out.println("Couldn\'t write to the info file. Skipping...");
            save_file.delete();
        }

        this.usingInfo = false;
        notify();
    }

    private Boolean usingFileInfo = false;

    /**
     * Guarda informação sobre os ficheiro que mandou guardar aos peers. Serve para
     * saber quanto os chunks feitos por cada ficheiro
     */
    public synchronized void saveFileInfo() {
        while (usingFileInfo) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted");
                Thread.currentThread().interrupt();
            }
        }
        this.usingFileInfo = true;
        String path = "./files/server/" + this.server_number + "/";
        String file_path = path + "file_info.txt";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                this.usingFileInfo = false;
                notifyAll();
                return;
            }
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file_path));
            bw.write("");
            bw.write(this.files_info.size() + "\n");
            // File id
            for (String i : this.files_info.keySet()) {
                bw.write(i + "\n");
                bw.write(this.files_info.get(i).size() + "\n");
                // version
                for (String j : this.files_info.get(i).keySet()) {
                    bw.write(j + "\n");
                    bw.write(this.files_info.get(i).get(j) + "\n");
                }
            }
            bw.close();

        } catch (IOException e) {
            System.out.println("Couldn\'t write to the info file. Skipping...");
            save_file.delete();
        }

        this.usingFileInfo = false;
        notifyAll();
    }

    /**
     * Lê a informação guardada sobre os ficheiro que já guardou, as versoes dos
     * ficheiros e que peers tem os chunk guardados(incluindo ele proprio)
     */
    public void readInfo() {
        String path = "./files/server/" + this.server_number + "/";
        String file_path = path + "info.txt";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(file_path));
                bw.write("0");
                bw.close();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                return;
            }
        } else {
            try {
                String line;
                FileReader fileReader = new FileReader(file_path);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                line = bufferedReader.readLine();
                int info_size = Integer.parseInt(line);
                this.info.clear();
                for (int i = 0; i < info_size; i++) {
                    String file_id = bufferedReader.readLine();
                    int version_size = Integer.parseInt(bufferedReader.readLine());
                    HashMap<String, HashMap<String, ArrayList<String>>> version_hash = new HashMap<String, HashMap<String, ArrayList<String>>>();
                    for (int j = 0; j < version_size; j++) {
                        String version_st = bufferedReader.readLine();
                        int chunk_no_size = Integer.parseInt(bufferedReader.readLine());
                        HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                        for (int k = 0; k < chunk_no_size; k++) {
                            String chunk_no_st = bufferedReader.readLine();
                            int rep = Integer.parseInt(bufferedReader.readLine());
                            ArrayList<String> senders = new ArrayList<String>();
                            for (int t = 0; t < rep; t++) {
                                String senderID = bufferedReader.readLine();
                                if(senderID.equals(Integer.toString(server_number)))this.number_of_chunks++;
                                senders.add(senderID);
                            }
                            chunk_no_hash.put(chunk_no_st, senders);
                        }
                        version_hash.put(version_st, chunk_no_hash);
                    }
                    this.info.put(file_id, version_hash);
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Couldn\'t write to the info file. Skipping...");
                save_file.delete();
            }
        }
    }

    /**
     * Lê a informação sobre os ficheiro que mandou guardar aos peers. Serve para
     * saber quanto os chunks feitos por cada ficheiro
     */
    public void readFileInfo() {
        String path = "./files/server/" + this.server_number + "/";
        String file_path = path + "file_info.txt";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(file_path));
                bw.write("0");
                bw.close();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                return;
            }
        } else {
            try {
                String line;
                FileReader fileReader = new FileReader(file_path);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                line = bufferedReader.readLine();
                int info_size = Integer.parseInt(line);
                this.files_info.clear();
                for (int i = 0; i < info_size; i++) {
                    String file_id = bufferedReader.readLine();
                    int version_size = Integer.parseInt(bufferedReader.readLine());
                    HashMap<String, Integer> version_hash = new HashMap<String, Integer>();
                    for (int j = 0; j < version_size; j++) {
                        String version_st = bufferedReader.readLine();
                        Integer chunk_no_size = Integer.parseInt(bufferedReader.readLine());
                        version_hash.put(version_st, chunk_no_size);
                    }
                    this.files_info.put(file_id, version_hash);
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Couldn\'t write to the info file. Skipping...");
                save_file.delete();
            }
        }
    }

    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> confirmation = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>();

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
        if (divisoes != 0) {
            if (body_completo.length % 64000 != 0)
                divisoes++;
            int inicio, fim;
            inicio = 0;
            fim = 63999;
            for (int j = 0; j < divisoes; j++) {
                System.out.println("Sending chunk number " + j);
                String chunk_no_j = Integer.toString(j);
                if (fim > body_completo.length-1)
                    fim = body_completo.length-1;
                byte[] mandar= new byte[fim-inicio+1];
                System.arraycopy(body_completo, inicio, mandar, 0, fim-inicio+1);
                inicio += 64000;
                fim += 64000;

                Message mensagem = null;
                try {
                    mensagem = new Message(new String[] { "PUTCHUNK", version, Integer.toString(this.server_number),
                            path, chunk_no_j, Integer.toString(rep_deg) });
                    mensagem.hashFileId();
                } catch (Exception e) {
                    System.out.println("Message format wrong");
                    return;
                }
                ArrayList<String> nada = new ArrayList<String>();
                if (this.confirmation.containsKey(mensagem.getFileId())) {
                    if (this.confirmation.get(mensagem.getFileId()).containsKey(version)) {
                        this.confirmation.get(mensagem.getFileId()).get(version).put(chunk_no_j, nada);
                    } else {
                        HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                        chunk_no_hash.put(chunk_no_j, nada);
                        this.confirmation.get(mensagem.getFileId()).put(version, chunk_no_hash);
                    }
                } else {
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    chunk_no_hash.put(chunk_no_j, nada);
                    HashMap<String, HashMap<String, ArrayList<String>>> version_hash = new HashMap<String, HashMap<String, ArrayList<String>>>();
                    version_hash.put(version, chunk_no_hash);
                    this.confirmation.put(mensagem.getFileId(), version_hash);
                }
                int i = 0;
                while (this.confirmation.get(mensagem.getFileId()).get(version).get(chunk_no_j).size() < rep_deg) { //todo -1? visto que o proprio server que tem o ficheiro tb conta para o rep degree
                    if (i != 0 && i != 6)
                        System.out.println("Peers didn't respond on time. Retrying...");
                    i++;
                    if (i == 6) {
                        System.out.println("Message couldn't be saved on servers");
                        return;
                    }
                    System.out.println("Trying to save file.");
                    this.MDBsendMessage(mensagem, mandar);
                    try {
                        Thread.sleep(i * 1000);

                    } catch (InterruptedException e) {
                        System.out.println("Thread was interrupted.");
                        Thread.currentThread().interrupt();
                    }
                }
            }

        } else {
            divisoes++;
            Message mensagem = null;
            try {
                mensagem = new Message(new String[] { "PUTCHUNK", version, Integer.toString(this.server_number), path,
                        "0", Integer.toString(rep_deg) });
                mensagem.hashFileId();
            } catch (Exception e) {
                System.out.println("Message format wrong");
                return;
            }
            ArrayList<String> nada = new ArrayList<String>();
            if (this.confirmation.containsKey(mensagem.getFileId())) {
                if (this.confirmation.get(mensagem.getFileId()).containsKey(version)) {
                    this.confirmation.get(mensagem.getFileId()).get(version).put("0", nada);
                } else {
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    chunk_no_hash.put("0", nada);
                    this.confirmation.get(mensagem.getFileId()).put(version, chunk_no_hash);
                }
            } else {
                HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                chunk_no_hash.put("0", nada);
                HashMap<String, HashMap<String, ArrayList<String>>> version_hash = new HashMap<String, HashMap<String, ArrayList<String>>>();
                version_hash.put(version, chunk_no_hash);
                this.confirmation.put(mensagem.getFileId(), version_hash);
            }
            int i = 0;
            while (this.confirmation.get(mensagem.getFileId()).get(version).get("0").size() < rep_deg) { //todo -1?
                if (i != 0 && i != 6)
                    System.out.println("Peers didn't respond on time. Retrying...");
                i++;
                if (i == 6) {
                    System.out.println("Message couldn't be saved on servers");
                    return;
                }
                System.out.println("Trying to save file.");
                this.MDBsendMessage(mensagem, body_completo);
                try {
                    Thread.sleep(i * 1000);

                } catch (InterruptedException e) {
                    System.out.println("Thread was interrupted.");
                    Thread.currentThread().interrupt();
                }
            }
        }
        this.confirmation.clear();

        if (this.files_info.containsKey(path)) {
            this.files_info.get(path).put(version, new Integer(divisoes));
        } else {
            HashMap<String, Integer> version_file = new HashMap<String, Integer>();
            version_file.put(version, new Integer(divisoes));
            this.files_info.put(path, version_file);
        }
        this.saveFileInfo();

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
    public void MDBmessageReceived() {
        System.out.println("Received a multicast data channel message");
        String[] mensagem = this.MDB.getMessage();
        byte[] body = this.MDB.getBody();
        String version = mensagem[1];
        //String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        String rep = mensagem[5];
        String path = "./files/server/" + this.server_number + "/save/" + file_id + "/" + version + "/";
        String file_path = path + "/" + chunk_no;
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        ArrayList<String> inserir = new ArrayList<String>();
        inserir.add("REP"+rep);
        inserir.add(Integer.toString(this.server_number));
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
                if (this.info.get(file_id).containsKey(version)) {
                    this.info.get(file_id).get(version).put(chunk_no, inserir);
                } else {
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    chunk_no_hash.put(chunk_no, inserir);
                    this.info.get(file_id).put(version, chunk_no_hash);
                }
            } else {
                HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                chunk_no_hash.put(chunk_no, inserir);
                HashMap<String, HashMap<String, ArrayList<String>>> version_hash = new HashMap<String, HashMap<String, ArrayList<String>>>();
                version_hash.put(version, chunk_no_hash);
                this.info.put(file_id, version_hash);
            }
        } else {
            this.info.get(file_id).get(version).put(chunk_no, inserir); //TODO: verificar isto aqui prq isto já implementa enhancement da section 3.2
            //alem disso vai ser possivel este chunk já ter informacao, se eu tentar fazer um putchunk do mesmo file 2 vezes
            //Com enhancement:Acho que a approuch indicada aqui seria fazer um delete deste file e de seguida fazer um novo putchunk dele
            //Sem enhancement: Criar um segundo file com o mesmo nome +(x) como se fosse uma outra copia daquele ficheiro
        }
        this.saveInfo(); // TODO: tal como o todo anterior aqui está a dar overwrite ao mesmo chunk que já lá tinha, o que kinda faz parte do enhancement
        this.tooMuchChunks();
        try {
            Random rand = new Random();
            int n = rand.nextInt(401);
            Thread.sleep(n);
            this.sendStoredMessage(version, Integer.toString(this.server_number), file_id, chunk_no);

        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }

    }

    private HashMap<String,HashMap<String,HashMap<String,byte[]>>> chunk_body_string = new HashMap<String,HashMap<String,HashMap<String,byte[]>>>();

    public Boolean sendStoredMessage(String version, String sender_id, String file_id, String chunk_no) {
        try {
            Message mandar = new Message(
                    new String[]{ "STORED", version, Integer.toString(this.server_number), file_id, chunk_no });
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

        String path_save="./files/client/save/";
        File directory = new File(path_save);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(path_save+name);
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                this.usingFileInfo = false;
                notifyAll();
                return;
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(path_save+name);
            fos.write(body);
            fos.close();

        } catch (IOException e) {
            System.out.println("Couldn\'t write to file. Skipping...");
            save_file.delete();
            return;
        }

    }

    //TODO que vai fazer com a informaçao do restore? Provavelmente criar um ficheiro com essa info 
    public byte[] sendGetChunkMessage(String version, String file_id) {
        int number_of_chunks;
        if (this.files_info.containsKey(file_id) && this.files_info.get(file_id).containsKey(version)) {
            number_of_chunks = this.files_info.get(file_id).get(version).intValue();
        } else {
            System.out.println("This peer doesn't own this file. (GETCHUNK)");
            return null;
        }
        byte[] devolver= new byte[this.files_info.get(file_id).get(version)*64000];
        file_id = Message.getSHA(file_id);
        int pos_atual=0;
        try {
            if (this.chunk_body_string.containsKey(file_id) && this.chunk_body_string.get(file_id).containsKey(version)) {
                System.out.println("Someone is trying to get this file. Please try later.");
                return null;
            }

            for (int i = 0; i < number_of_chunks; i++) {
                System.out.println("Getting chunk number "+i);
                if(this.chunk_body_string.containsKey(file_id))
                {
                    if(this.chunk_body_string.get(file_id).containsKey(version))
                    {
                        this.chunk_body_string.get(file_id).get(version).put(Integer.toString(i), null);
                    }
                    else
                    {
                        HashMap<String,byte[]> version_hash = new HashMap<String,byte[]>();
                        version_hash.put(Integer.toString(i), null);
                        this.chunk_body_string.get(file_id).put(version, version_hash);
                    }
                }
                else
                {
                    HashMap<String,HashMap<String,byte[]>> file_hash = new HashMap<String,HashMap<String,byte[]>>();
                    HashMap<String,byte[]> version_hash = new HashMap<String,byte[]>();
                    version_hash.put(Integer.toString(i), null);
                    file_hash.put(version, version_hash);
                    this.chunk_body_string.put(file_id, file_hash);
                }
                Message mandar = new Message(new String[] { "GETCHUNK", version, Integer.toString(this.server_number),
                        file_id, Integer.toString(i) });
                this.MCsendMessage(mandar);
                int espera = 1;
                while (true) {
                    Thread.sleep(espera * 1000);
                    if (this.chunk_body_string.get(file_id).get(version).get(Integer.toString(i)) == null) {
                        System.out.println("Resending the GETCHUNK message");
                        this.MCsendMessage(mandar);
                    }
                    else
                    {
                        break;
                    }
                    espera++;
                    if (espera == 6) {
                        System.out.println("Couldn't send the GETCHUNK message. Skipping");
                        return null;
                    }
                }
                System.arraycopy(this.chunk_body_string.get(file_id).get(version).get(Integer.toString(i)), 0, devolver, pos_atual, this.chunk_body_string.get(file_id).get(version).get(Integer.toString(i)).length);
                pos_atual+=this.chunk_body_string.get(file_id).get(version).get(Integer.toString(i)).length;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return devolver;

    }

    //TODO nao esta a apagar o directorio files do server o que seria suposto
    public Boolean sendDeletemessage(String version,String file_id)
    {
        //TODO falta apagar o file original não só os chunks dele guardados noutros servers/peers. E talvez o this.info tal como o file info tambem??
        if(this.files_info.containsKey(file_id) && this.files_info.get(file_id).containsKey(version))
        {
            this.files_info.get(file_id).remove(version);
            if(this.files_info.get(file_id).size()==0)
                this.files_info.remove(file_id);
        }
        else
        {
            System.out.println("Couldn't send the DELETE message. File doesn't exist. Skipping...");
            return false;
        }
        try {
            Message mandar = new Message( new String[]{ "DELETE", version, Integer.toString(this.server_number), file_id});
            mandar.hashFileId();
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the DELETE message. Skipping...");
            return false;
        }
        this.saveFileInfo();
        return true;

    }
    
    public Boolean sendRemovedMessage(String version,String file_id,String chunk_no)
    {
        try {
            Message mandar = new Message( new String[]{ "REMOVED", version, Integer.toString(this.server_number), file_id ,chunk_no});
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the DELETE message. Skipping...");
            return false;
        }
        this.saveFileInfo();
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
    public void MCmessageReceived() {
        System.out.println("Received a multicast control channel message");
        String[] mensagem = this.MC.getMessage();

        if (mensagem[0].equals("STORED")) {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if (this.confirmation.containsKey(file_id)) {
                if (!this.confirmation.get(file_id).get(version).get(chunk_no).contains(sender_id))
                    this.confirmation.get(file_id).get(version).get(chunk_no).add(sender_id);
            } else if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(version)
                    && this.info.get(file_id).get(version).containsKey(chunk_no)) {
                if (!this.info.get(file_id).get(version).get(chunk_no).contains(sender_id))
                    this.info.get(file_id).get(version).get(chunk_no).add(sender_id);
            }
            this.saveInfo();
        } else if (mensagem[0].equals("GETCHUNK")) {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(version)
                    && this.info.get(file_id).get(version).containsKey(chunk_no)) {
                
                String file_path="./files/server/"+this.server_number+"/save/"+file_id+"/"+version+"/"+chunk_no;
                byte[] body= this.readAnyFile(file_path);

                try {
                    Message message = new Message(new String[]{"CHUNK",version,Integer.toString(this.server_number),file_id,chunk_no});

                    Random rand = new Random();
                    int n = rand.nextInt(401);
                    Thread.sleep(n);
                    this.sendChunkMessage(message, body);

                } catch (InterruptedException e) {
                    System.out.println("Thread was interrupted.");
                    Thread.currentThread().interrupt();
                    return;
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
            if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(version))
            {
                String path="./files/server/"+this.server_number+"/save/"+file_id+"/"+version;
                File dir= new File(path);
                if(dir.exists())
                {
                    for(File chunks:dir.listFiles())
                    {
                        chunks.delete();
                    }
                    dir.delete();
                }
                for(String chunks:this.info.get(file_id).get(version).keySet())
                {
                    try {
                        Random rand = new Random();
                        int n = rand.nextInt(401);
                        Thread.sleep(n);
                        this.sendRemovedMessage(version, file_id, chunks);
            
                    } catch (InterruptedException e) {
                        System.out.println("Thread was interrupted.");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                this.info.get(file_id).remove(version);
                if(this.info.get(file_id).size()==0)
                {
                    this.info.remove(file_id);
                    String path2="./files/server/"+this.server_number+"/save/"+file_id;
                    File dir2= new File(path2);
                    dir2.delete();
                }
            }
            this.saveInfo();
        }
        else if (mensagem[0].equals("REMOVED"))
        {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            String chunk_no = mensagem[4];
            if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(version) && this.info.get(file_id).get(version).containsKey(chunk_no))
            {
                this.info.get(file_id).get(version).get(chunk_no).remove(sender_id);
            }
            //TODO verificar se o count de replicação esta direito, valor dentro do ficheiro. Se não mandar putchunk do body
            this.saveInfo();
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

    public void MDRmessageReceived() {
        System.out.println("Received a multicast data recovery channel message");
        String[] mensagem = this.MDR.getMessage();
        byte[] body = this.MDR.getBody();
        String version = mensagem[1];
        String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        if(this.chunk_body_string.containsKey(file_id) && this.chunk_body_string.get(file_id).containsKey(version) && this.chunk_body_string.get(file_id).get(version).containsKey(chunk_no))
        {
            this.chunk_body_string.get(file_id).get(version).put(chunk_no, body);
        }


    }

    public void setServerNumber(int number) {
        this.server_number = number;
    }

    public void setProtocolVersion(String ver) {
        this.protocol_version = ver;
    }

    public String getVersion() {
        return this.protocol_version;
    }

    public int getServerNumber() {
        return this.server_number;
    }


    public void tooMuchChunks()
    {
        //TODO 
        // Verificar se o limite total foi ultrapassado
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
}