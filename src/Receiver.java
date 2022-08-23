import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Receiver {
    FTConnection connection;
    String folder;
    List<String> toSend;



    public byte[] listAnswer (Set<Short> fileNames){
        short len = (short) fileNames.size();
        byte[] lenFiles = ByteBuffer.allocate(2).putShort(len).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(lenFiles);
            for (Short index : fileNames) {
                byte[] temp = ByteBuffer.allocate(2).putShort(index).array();
                outputStream.write(temp);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public void setToSend (List<String> list){
        this.toSend = new ArrayList<>(list);
    }

    public  Map<Short,String> getListNames(byte[]data,String folder) throws IOException {
        List<String> folderFiles = Util.listFiles(folder);
        Map<Short,String> result = new TreeMap<>();
        short nrFiles = Util.ByteToShort(Arrays.copyOfRange(data,0,2));
        int index = 2;
        for (int i = 0; i < nrFiles;i++){
            short size = Util.ByteToShort(Arrays.copyOfRange(data,index,index+=2));
            String name = new String(Arrays.copyOfRange(data ,index,index+=size));
            long check = ByteBuffer.wrap(Arrays.copyOfRange(data,index,index+=8)).getLong();
            String path = folder + "/" + name;
            if (Files.notExists(Paths.get(path))) {
                result.put((short)i,name);
            }
            else {
                byte[]file = Files.readAllBytes(Paths.get(path));
                long checkFile = Util.getCRC32Checksum(file);
                if (check != checkFile)
                    result.put((short)i,name);
                folderFiles.remove(name);
            }
        }
        setToSend(folderFiles);
        return result;
    }

    public Receiver(FTConnection connection, String folder) {
        this.connection = connection;
        this.folder = folder;;
    }

    public void receive(){
        List<String> fileNames = Collections.emptyList();
        Map<Short,String> listNames = new TreeMap<>();
        FTRapid ft;
        FFile file = new FFile();
        int fileIndex = 0;
        System.out.println("Start Receiving");
        while (true) {
            try {
                ft = connection.receive();
            }
            catch (Exception e){
                continue;
            }
            byte type = ft.type;
            if (type == FTRapid.LIST){
                try {
                    listNames = getListNames(ft.content,folder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileNames = new ArrayList<>(listNames.values());
                byte[]data = listAnswer(listNames.keySet());
                ft = FTRapid.buildPacket(FTRapid.LIST,ft.file,ft.numSeq,data);
                connection.send(ft);
                continue;
            }
            if (type == FTRapid.ENDFILE) {
                String fileName = fileNames.get(fileIndex);
                Status.addToStatus("Received File: " + fileName);
                file.setName(folder+"/"+ fileName);
                new Thread(file).start();
                file = new FFile();
                fileIndex++;
                ft=FTRapid.buildPacket(FTRapid.OK,ft.file, ft.numSeq,new byte[0]);
                connection.send(ft);
                continue;
            }
            if (type == FTRapid.START) {
                Status.addToStatus("Connected");
                ft=FTRapid.buildPacket(FTRapid.OK,ft.file, ft.numSeq,new byte[0]);
                connection.send(ft);
                continue;
            }

            if (type == FTRapid.END ) {
                System.out.println("Receive END");
                ft=FTRapid.buildPacket(FTRapid.OK,ft.file, ft.numSeq,new byte[0]);
                connection.send(ft);
                return;
            }
            file.addContent(ft.numSeq,ft.content);
            ft=FTRapid.buildPacket(FTRapid.OK,ft.file, ft.numSeq,new byte[0]);
            connection.send(ft);
        }
    }
}
