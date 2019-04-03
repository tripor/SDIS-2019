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

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(
                    "\nNo arguments provided. Use make server arguments=\"<IP address> <port number> <server number>\"\n");
            System.exit(1);
        }
        Server server = new Server(args[0], Integer.parseInt(args[1]));
        server.setServerNumber(Integer.parseInt(args[2]));
        server.readInfo();
        server.run();
    }

    public Server(String address, int port) {
        this.MDB = new Udp(address, port, this, "MDB");
        this.MC = new Udp(address, port + 1, this, "MC");
        this.MDR = new Udp(address, port + 2, this, "MDR");
    }

    public void run() {
        // Create the server local storage directory
        String path = "./files/server/" + this.server_number + "/save/";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();

        try {
            if (this.server_number == 1) {
                this.sendPutChunkMessage("1.1", "./files/client/t.txt", 3);
            } else {
                // this.MDB.receive();
            }

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
        }

    }

    private Boolean usingInfo = false;

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

    private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> confirmation = new HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>>();

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
        if (body_completo.length() / 64000 != 0) {

        } else {
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
                HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                chunk_no_hash.put("1", nada);
                HashMap<String, HashMap<String, ArrayList<String>>> version_hash = new HashMap<String, HashMap<String, ArrayList<String>>>();
                version_hash.put(version, chunk_no_hash);
                this.confirmation.put(mensagem.getFileId(), version_hash);
            }
            int i = 0;
            while (this.confirmation.get(mensagem.getFileId()).get(version).get("1").size() < rep_deg) {
                if (i != 0 && i!=6)
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

    }

    public void MDBsendMessage(Message message, String body) {
        System.out.println("Sending message to MDB.");
        this.MDB.sendMessageBody(message.getMessage(), body);
    }

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
        inserir.add(sender_id);
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
            this.MCsendMessage(
                    new String[] { "STORED", version, Integer.toString(this.server_number), file_id, chunk_no });

        } catch (InterruptedException e) {
            System.out.println("Thread was interrupted.");
            Thread.currentThread().interrupt();
        }

    }

    public Boolean MCsendMessage(String[] message) {
        try {
            Message nova_mensagem = new Message(message);
            this.MC.sendMessage(nova_mensagem.getMessage());
        } catch (Exception e) {
            System.out.println("Couldn't send a control message. Skipping...");
            return false;
        }
        return true;
    }

    public void MCmessageReceived() {
        System.out.println("Received a multicast control channel message");
        String[] mensagem = this.MDB.getMessage();

        if (mensagem[0].equals("STORED")) {
            String version = mensagem[1];
            String sender_id = mensagem[2];
            String file_id = mensagem[3];
            int chunk_no = Integer.parseInt(mensagem[4]);
            if (this.confirmation.containsKey(file_id)) {
                this.confirmation.get(file_id).get(version).get(chunk_no).add(sender_id);
            } else if (this.info.containsKey(file_id) && this.info.get(file_id).containsKey(version)
                    && this.info.get(file_id).get(version).containsKey(chunk_no)) {
                if (!this.info.get(file_id).get(version).get(chunk_no).contains(sender_id))
                    this.info.get(file_id).get(version).get(chunk_no).add(sender_id);
            }
        }
    }

    public void MDRmessageReceived() {
        System.out.println("Received a multicast data recovery channel message");
    }

    public void setServerNumber(int number) {
        this.server_number = number;
    }

    public int getServerNumber() {
        return this.server_number;
    }
}