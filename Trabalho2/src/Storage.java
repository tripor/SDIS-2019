package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Storage
 */
public class Storage {

    private ArrayList<Long> files;
    private String serverPath = "./files/server/";
    private long currentSize;

    public Storage(long serverId) {
        this.files = new ArrayList<Long>();
        this.currentSize = 0;
        Path path = Paths.get(serverPath);
        try {
            Files.createDirectory(path);
        } catch (FileAlreadyExistsException e) {

            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(serverPath))) {
                for (Path pf : directoryStream) {
                    this.currentSize += Files.size(pf);
                    File f = new File(pf.toString());
                    this.files.add(Long.parseLong(f.getName()));
                }
            } catch (Exception ee) {
                Colours.printRed("A error has ocurred while trying to read the files in files directory\n");
                System.exit(2);
            }
        } catch (IOException e) {
            Colours.printRed("A error has ocurred while trying to create the files directory\n");
            System.exit(2);
        }
        Colours.printYellow("Current size of storage: " + this.currentSize + " bytes\n");
    }
    public Storage(String clientPath) {
        this.serverPath = clientPath;
        
    }

    public void store(long id, byte[] info) throws IOException {
        Colours.printYellow("Storing file: " + id + "\n");
        String filePath = this.serverPath + id;
        File file = new File(filePath);
        file.createNewFile();
        RandomAccessFile aFile = new RandomAccessFile(filePath, "rw");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(info.length);
        buf.clear();
        buf.put(info);
        buf.flip();
        while (buf.hasRemaining()) {
            inChannel.write(buf);
        }
        this.currentSize += inChannel.size();
        inChannel.close();
        this.files.add(id);
        Colours.printYellow("Current size of storage: " + this.currentSize + " bytes\n");
    }
    public void store(String id, byte[] info) throws IOException {
        Colours.printYellow("Storing file: " + id + "\n");
        String filePath = this.serverPath + id;
        File file = new File(filePath);
        file.createNewFile();
        RandomAccessFile aFile = new RandomAccessFile(filePath, "rw");
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buf = ByteBuffer.allocate(info.length);
        buf.clear();
        buf.put(info);
        buf.flip();
        while (buf.hasRemaining()) {
            inChannel.write(buf);
        }
        inChannel.close();
    }
    

    public byte[] read(long id) throws IOException
    {
        Colours.printYellow("Reading file: " + id + "\n");
        String filePath = this.serverPath + id;
        RandomAccessFile aFile = new RandomAccessFile(filePath, "rw");
        FileChannel inChannel = aFile.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(2048);
        int bytesRead;
        int totalBytesRead=0;
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        ArrayList<Integer> listSize = new ArrayList<Integer>();
        while ((bytesRead = inChannel.read(buf)) != -1 ) {
            if(bytesRead==0)continue;
            totalBytesRead += bytesRead;
            buf.flip();
            byte[] add = new byte[bytesRead];
            System.arraycopy(buf.array(),0,add,0,bytesRead);
            list.add(add);
            buf.clear();
            listSize.add(bytesRead);
        }
        if(totalBytesRead < 0) totalBytesRead=0;
        byte[] devolver = new byte[totalBytesRead];
        int position=0;
        for(int i=0;i<list.size();i++)
        {
            System.arraycopy(list.get(i), 0, devolver, position, listSize.get(i));
            position+=listSize.get(i);
        }
        inChannel.close();
        return devolver;
    }
    public byte[] read(String id) throws IOException
    {
        Colours.printYellow("Reading file: " + id + "\n");
        String filePath = this.serverPath + id;
        RandomAccessFile aFile = new RandomAccessFile(filePath, "rw");
        FileChannel inChannel = aFile.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(2048);
        int bytesRead;
        int totalBytesRead=0;
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        ArrayList<Integer> listSize = new ArrayList<Integer>();
        while ((bytesRead = inChannel.read(buf)) != -1 ) {
            if(bytesRead==0)continue;
            totalBytesRead += bytesRead;
            buf.flip();
            byte[] add = new byte[bytesRead];
            System.arraycopy(buf.array(),0,add,0,bytesRead);
            list.add(add);
            buf.clear();
            listSize.add(bytesRead);
        }
        if(totalBytesRead < 0) totalBytesRead=0;
        byte[] devolver = new byte[totalBytesRead];
        int position=0;
        for(int i=0;i<list.size();i++)
        {
            System.arraycopy(list.get(i), 0, devolver, position, listSize.get(i));
            position+=listSize.get(i);
        }
        inChannel.close();
        return devolver;
    }

    public void delete(long id) throws IOException
    {
        Colours.printYellow("Deleting file: " + id + "\n");
        String filePath = this.serverPath + id;
        Path path = Paths.get(filePath);
        Files.delete(path);
        this.files.remove(id);
    }
    public Boolean contains(long id)
    {
        return this.files.contains(id);
    }
}