import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import java.util.Random;

public class Server {

    private int server_number;

    private Udp MDB;
    private Udp MC;
    private Udp MDR;
    // Fileid Version chunkNo rep degree
    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> info = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>();
    private HashMap<String, HashMap<String, Integer>> files_info = new HashMap<String, HashMap<String, Integer>>();

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(
                    "\nNo arguments provided. Use make server arguments=\"<IP address> <port number> <server number>\"\n");
            System.exit(1);
        }
        Server server = new Server(args[0], Integer.parseInt(args[1]));
        server.setServerNumber(Integer.parseInt(args[2]));
        server.readInfo();
        server.readFileInfo();
        server.run();
    }

    public Server(String address, int port) {
        this.MDB = new Udp(address, port, this, "MDB");
        this.MC = new Udp(address, port + 1, this, "MC");
        this.MDR = new Udp(address, port + 2, this, "MDR");
    }

    /**
     * Função chamada no inicio do programa
     */
    public void run() {
        // Create the server local storage directory
        String path = "./files/server/" + this.server_number + "/save/";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();

        try {
            if (this.server_number == 1) {
                System.out.println(sendGetChunkMessage("1.1", "./files/client/t.txt"));
            } else {
                // this.MDB.receive();
            }

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
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
        String body_completo = "";
        try {
            String line;
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                body_completo += line;
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Unable to open file '" + path + "'");
        } catch (IOException ex) {
            System.out.println("Error while reading file '" + path + "'");
        }
        int divisoes = body_completo.length() / 64000;
        if (divisoes != 0) {
            if (body_completo.length() % 64000 != 0)
                divisoes++;
            int inicio, fim;
            inicio = 0;
            fim = 63999;
            for (int j = 1; j <= divisoes; j++) {
                System.out.println("Sending chunk number " + j);
                String chunk_no_j = Integer.toString(j);
                String mandar = body_completo.substring(inicio, fim);
                inicio += 64000;
                fim += 64000;
                if (fim > body_completo.length())
                    fim = body_completo.length();

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
                while (this.confirmation.get(mensagem.getFileId()).get(version).get(chunk_no_j).size() < rep_deg) {
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
                        "1", Integer.toString(rep_deg) });
                mensagem.hashFileId();
            } catch (Exception e) {
                System.out.println("Message format wrong");
                return;
            }
            ArrayList<String> nada = new ArrayList<String>();
            if (this.confirmation.containsKey(mensagem.getFileId())) {
                if (this.confirmation.get(mensagem.getFileId()).containsKey(version)) {
                    this.confirmation.get(mensagem.getFileId()).get(version).put("1", nada);
                } else {
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    chunk_no_hash.put("1", nada);
                    this.confirmation.get(mensagem.getFileId()).put(version, chunk_no_hash);
                }
            } else {
                System.out.println(mensagem.getFileId());
                HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                chunk_no_hash.put("1", nada);
                HashMap<String, HashMap<String, ArrayList<String>>> version_hash = new HashMap<String, HashMap<String, ArrayList<String>>>();
                version_hash.put(version, chunk_no_hash);
                this.confirmation.put(mensagem.getFileId(), version_hash);
            }
            int i = 0;
            while (this.confirmation.get(mensagem.getFileId()).get(version).get("1").size() < rep_deg) {
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
    public Boolean MDBsendMessage(Message message, String body) {
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
        String body = this.MDB.getBody().trim();
        String version = mensagem[1];
        String sender_id = mensagem[2];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        String path = "./files/server/" + this.server_number + "/save/" + file_id + "/" + version + "/";
        String file_path = path + "/" + chunk_no;
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();
        File save_file = new File(file_path);
        ArrayList<String> inserir = new ArrayList<String>();
        inserir.add(Integer.toString(this.server_number));
        if (!save_file.exists()) {
            try {
                save_file.createNewFile();

            } catch (IOException e) {
                System.out.println("Couldn't create file to write. Skipping...");
                return;
            }
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file_path));
                bw.write(mensagem[1] + "\n");
                bw.write(mensagem[2] + "\n");
                bw.write(mensagem[3] + "\n");
                bw.write(mensagem[4] + "\n");
                bw.write(mensagem[5] + "\n");
                bw.write(body);
                bw.close();

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
            this.info.get(file_id).get(version).put(chunk_no, inserir);
        }
        this.saveInfo();
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

    private HashMap<String,HashMap<String,HashMap<String,String>>> chunk_body_string = new HashMap<String,HashMap<String,HashMap<String,String>>>();

    public Boolean sendStoredMessage(String version, String sender_id, String file_id, String chunk_no) {
        try {
            Message mandar = new Message(
                    new String[] { "STORED", version, Integer.toString(this.server_number), file_id, chunk_no });
            this.MCsendMessage(mandar);
        } catch (Exception e) {
            System.out.println("Couldn't send the STORED message. Skipping...");
            return false;
        }
        return true;
    }

    public String sendGetChunkMessage(String version, String file_id) {
        int number_of_chunks;
        if (this.files_info.containsKey(file_id) && this.files_info.get(file_id).containsKey(version)) {
            number_of_chunks = this.files_info.get(file_id).get(version).intValue();
        } else {
            System.out.println("This peer doesn't own this file");
            return null;
        }
        file_id = Message.getSHA(file_id);
        String devolver="";
        try {
            if (this.chunk_body_string.containsKey(file_id) && this.chunk_body_string.get(file_id).containsKey(version)) {
                System.out.println("Someone is trying to get this file. Please try later.");
                return null;
            }

            for (int i = 1; i <= number_of_chunks; i++) {
                System.out.println("Getting chunk number "+i);
                if(this.chunk_body_string.containsKey(file_id))
                {
                    if(this.chunk_body_string.get(file_id).containsKey(version))
                    {
                        this.chunk_body_string.get(file_id).get(version).put(Integer.toString(i), "");
                    }
                    else
                    {
                        HashMap<String,String> version_hash = new HashMap<String,String>();
                        version_hash.put(Integer.toString(i), "");
                        this.chunk_body_string.get(file_id).put(version, version_hash);
                    }
                }
                else
                {
                    HashMap<String,HashMap<String,String>> file_hash = new HashMap<String,HashMap<String,String>>();
                    HashMap<String,String> version_hash = new HashMap<String,String>();
                    version_hash.put(Integer.toString(i), "");
                    file_hash.put(version, version_hash);
                    this.chunk_body_string.put(file_id, file_hash);
                }
                Message mandar = new Message(new String[] { "GETCHUNK", version, Integer.toString(this.server_number),
                        file_id, Integer.toString(i) });
                this.MCsendMessage(mandar);
                int espera = 1;
                while (true) {
                    Thread.sleep(espera * 1000);
                    if (this.chunk_body_string.get(file_id).get(version).get(Integer.toString(i)) == "") {
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
                devolver+=this.chunk_body_string.get(file_id).get(version).get(Integer.toString(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return devolver;

    }

    /**
     * Mandar um packet para o canal MC
     * 
     * @param message A mensagem que eu quero mandar
     * @return True se foi possivel mandar a mensagem ou false caso contrario
     */
    public Boolean MCsendMessage(Message message) {
        try {
            System.out.println("Sending message to MDB.");
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
            System.out.println(chunk_no);
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
            String body="";
            if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(version)
                    && this.info.get(file_id).get(version).containsKey(chunk_no)) {
                
                String file_path="./files/server/"+this.server_number+"/save/"+file_id+"/"+version+"/"+chunk_no;
                try {
                    String line;
                    FileReader fileReader = new FileReader(file_path);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    line = bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    line = bufferedReader.readLine();
                    while((line = bufferedReader.readLine())!=null)
                    {
                        body+=line;
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.println("Couldn\'t read from the chunk file. Skipping...");
                    return;
                }

                try {
                    Message message = new Message(new String[]{"CHUNK",version,Integer.toString(this.server_number),file_id,chunk_no});
                    this.sendChunkMessage(message, body);
                } catch (Exception e) {
                    System.out.println("Message with wrong format");
                    return;
                }
                
            }

        }
    }

    public Boolean sendChunkMessage(Message message,String body)
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
        String body = this.MDR.getBody().trim();
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

    public int getServerNumber() {
        return this.server_number;
    }
}