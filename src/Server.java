import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Server {

    public void start(String[] args) {
        String folder =args[0];
        String adress = args[1];
        try {

            FTConnection connection = new FTConnection();
            Status.addToStatus("Waiting connection from " + adress);
            String myAdress = connection.waitConnection(adress);


            Status status = new Status(myAdress);
            Thread statusThread = new Thread(status);
            statusThread.start();

            Status.addToStatus("Connected to " + adress);
            
            //sendFiles
            List<String> listFiles = Util.listFiles(folder);

            System.out.println("Files ->" + listFiles);

            Map<Short,String> files = new TreeMap<>();

            for (short i =0; i < listFiles.size();i++)
                files.put(i,listFiles.get(i));
            List<Long> cheksumList = new ArrayList<>();

            for (String name : listFiles){
                byte[]temp =Files.readAllBytes(Paths.get(folder + "/" + name));
                long check = Util.getCRC32Checksum(temp);
                cheksumList.add(check);
            }
            byte[]dataList = FTRapid.buildList(listFiles,cheksumList);
            Status.addToStatus("Sent list of files");

            FTRapid ft = FTRapid.buildPacket(FTRapid.LIST,(short) 0,0,dataList);
            connection.changeTimeout(500);
            ft = connection.sendAndReceive(ft);
            connection.changeTimeout(10);
            FTRapid.parseList (ft.content,files);
            Status.addToStatus("Files to send :" + files.values());

            Sender sender = new Sender(connection,folder,files);
            sender.send();

            //Receive Files
            Receiver receiver = new Receiver(connection,folder);
            receiver.receive();

            
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            status.stop();

        }
        catch (IOException e){
            e.printStackTrace();
        }
        Status.addToStatus("Finished");
    }


}
