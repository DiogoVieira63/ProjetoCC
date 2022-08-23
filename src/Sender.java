import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class Sender {
    FTConnection connection;
    String folder;
    Map<Short,String> mapFiles;

    public Sender(FTConnection connection, String folder, Map<Short, String> mapFiles) {
        this.connection = connection;
        this.folder = folder;
        this.mapFiles = mapFiles;
    }

    private Map<Integer,byte[]> createMap (byte[] file, int parts, int last) {
        int len = Util.dataLength;
        Map<Integer,byte[]> fileMap = new TreeMap<>();
        for (int i = 0; i < parts; i++) {
            byte[] temp = Arrays.copyOfRange(file, i * len, (i + 1) * len);
            fileMap.put(i,temp);
        }
        byte[] temp = Arrays.copyOfRange(file, parts * len, parts * len + last);
        fileMap.put(parts,temp);
        return fileMap;
    }

    public void infoTransfer (double duration, int fileSize, String name){
        Status.addToStatus("Sent File: " + name);
        Status.addToStatus("Duration: " + duration + " ms");
        Double debito = fileSize/duration;
        DecimalFormat df = new DecimalFormat("#.####");
        Status.addToStatus("Debito Final: " + df.format(debito) + " bits/ms");
    }

    public void send(){
        FTRapid ft;
        System.out.println("Start Sending");
        for (Map.Entry<Short, String> file : mapFiles.entrySet()) {
            System.out.println("Sending file " + file.getKey());
            byte[] array = new byte[0];
            try {
                array = Files.readAllBytes(Paths.get(folder + "/" + file.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            int fileSize = array.length*8;
            int parts = array.length / Util.dataLength;
            int last = array.length % Util.dataLength;
            Map<Integer,byte[]> fileMap = createMap(array,parts,last);

            long startTime = System.nanoTime();
            for (int i = 0; i <= parts; i++) {
                byte[] temp = fileMap.get(i);
                ft =FTRapid.buildPacket(FTRapid.DATA,file.getKey(),i,temp);
                connection.sendAndReceive(ft);
            }
            long endTime = System.nanoTime();
            double duration =(double) (endTime - startTime) /1000000;  //divide by 1000000 to get milliseconds.
            infoTransfer(duration,fileSize,file.getValue());

            ft = FTRapid.buildPacket(FTRapid.ENDFILE,file.getKey(),0,new byte[0]);
            connection.sendAndReceive(ft);
        }
        System.out.println("Send END");
        ft = FTRapid.buildPacket(FTRapid.END,(short)0,0,new byte[0]);
        connection.sendAndReceive(ft);
    }
}
