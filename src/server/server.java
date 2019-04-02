import java.io.*;

public class Server {

    private int server_number;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println(
                    "\nNo arguments provided. Use make server arguments=\"<IP address> <port number> <server number>\"\n");
            System.exit(1);
        }
        Server server = new Server(args[0], Integer.parseInt(args[1]));
        server.setServerNumber(Integer.parseInt(args[2]));
        server.run();
    }

    private Udp MDB;

    public Server(String address, int port) {
        this.MDB = new Udp(address, port, this, "MDB");
    }

    public void run() {
        // Create the server local storage directory
        String path = "./files/server/" + this.server_number + "/save/";
        File directory = new File(path);
        if (!directory.exists())
            directory.mkdirs();

        try {
            Message mensagem = new Message(
                    new String[] { "PUTCHUNK", "1.1", Integer.toString(this.server_number), "OLA", "1", "3" });
            mensagem.hashFileId();
            if (this.server_number == 1) {
                this.MDB.sendMessageBody(mensagem.getMessage(), "uuuuu");
            } else {
                // this.MDB.receive();
            }

        } catch (Exception e) {
            System.out.println("Message format wrong");
            System.exit(3);
        }

    }

    public void setServerNumber(int number) {
        this.server_number = number;
    }

    public int getServerNumber() {
        return this.server_number;
    }

    public void MDBmessageReceived() {
        String[] mensagem = this.MDB.getMessage();
        String body = this.MDB.getBody().trim();
        String version = mensagem[1];
        String file_id = mensagem[3];
        String chunk_no = mensagem[4];
        String file_path = "./files/server/" + this.server_number + "/save/" + file_id + "_" + chunk_no;
        File save_file = new File(file_path);
        if (save_file.exists()) {
            try {
                String line = null;
                FileReader fileReader = new FileReader(file_path);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                int i = 0;
                line = bufferedReader.readLine();
                if (line.compareTo(version) < 0) {
                    try {
                        save_file.delete();
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
                }
                bufferedReader.close();

            } catch (IOException e) {
                System.out.println("Couldn't read from file. Skipping...");
                return;
            }
        } else {
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
        }

    }

    public void MCmessageReceived() {
        System.out.println("here");
    }

    public void MDRmessageReceived() {
        System.out.println("here");
    }
}