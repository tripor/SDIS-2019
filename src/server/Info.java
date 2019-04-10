import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Info {


    public Info()
    {
        this.readInfo();
        this.readFileInfo();
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
        String path = "./files/server/" + Server.singleton.getServerNumber() + "/";
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
            bw.write(Server.singleton.info.size() + "\n");
            // File id
            for (String i : Server.singleton.info.keySet()) {
                bw.write(i + "\n");
                bw.write(Server.singleton.info.get(i).size() + "\n");
                // version
                for (String j : Server.singleton.info.get(i).keySet()) {
                    bw.write(j + "\n");
                    // rep
                    bw.write(Server.singleton.info.get(i).get(j).size() + "\n");
                    for (int l = 0; l < Server.singleton.info.get(i).get(j).size(); l++) {
                        bw.write(Server.singleton.info.get(i).get(j).get(l) + "\n");
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
        String path = "./files/server/" + Server.singleton.getServerNumber() + "/";
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
            bw.write(Server.singleton.files_info.size() + "\n");
            // File id
            for (String i : Server.singleton.files_info.keySet()) {
                bw.write(i + "\n");
                bw.write(Server.singleton.files_info.get(i) + "\n");
            }
            bw.close();

        } catch (IOException e) {
            System.out.println("Couldn\'t write to the info file. Skipping...");
            save_file.delete();
        }

        this.usingFileInfo = false;
        notifyAll();
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile())
                length += file.length();
            else
                length += folderSize(file);
        }
        return length;
    }

    /**
     * Lê a informação guardada sobre os ficheiro que já guardou, as versoes dos
     * ficheiros e que peers tem os chunk guardados(incluindo ele proprio)
     */
    public void readInfo() {
        String path = "./files/server/" + Server.singleton.getServerNumber() + "/";
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
                Server.singleton.info.clear();
                for (int i = 0; i < info_size; i++) {
                    String file_id = bufferedReader.readLine();
                    int chunk_no_size = Integer.parseInt(bufferedReader.readLine());
                    HashMap<String, ArrayList<String>> chunk_no_hash = new HashMap<String, ArrayList<String>>();
                    for (int j = 0; j < chunk_no_size; j++) {
                        String chunk_no_st = bufferedReader.readLine();
                        int rep = Integer.parseInt(bufferedReader.readLine());
                        ArrayList<String> senders = new ArrayList<String>();
                        for (int t = 0; t < rep; t++) {
                            String senderID = bufferedReader.readLine();
                            if(senderID.equals(Server.singleton.getServerNumber()))
                            {
                                //TODO Server.singleton. number_of_chunks++;
                            }
                            senders.add(senderID);
                        }
                        chunk_no_hash.put(chunk_no_st, senders);
                    }
                    Server.singleton.info.put(file_id, chunk_no_hash);
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
        String path = "./files/server/" + Server.singleton.getServerNumber() + "/";
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
                Server.singleton.files_info.clear();
                for (int i = 0; i < info_size; i++) {
                    String file_id = bufferedReader.readLine();
                    int chunk_no_size = Integer.parseInt(bufferedReader.readLine());
                    Server.singleton.files_info.put(file_id, chunk_no_size);
                }
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Couldn\'t write to the info file. Skipping...");
                save_file.delete();
            }
        }
    }

}