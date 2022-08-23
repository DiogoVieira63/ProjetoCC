import java.io.IOException;
import java.net.InetAddress;
import java.util.*;


public class Cliente {





    public void start(String[] args)  {
        String folder = args[0];
        //String hostname = "localhost";
        String adressName = args[1];

        byte[]adressNameData = FTRapid.buildNameAdress(adressName);
        try {
            InetAddress address = InetAddress.getByName(adressName);
            FTConnection connection = new FTConnection(address,Util.port);
            FTRapid ft = FTRapid.buildPacket(FTRapid.START,(short)0,0,adressNameData);
            ft = connection.sendAndReceive(ft);

            String myAdress = FTRapid.parseNameAdress(ft.content);
            System.out.println("Adress is " + myAdress);

            Status status = new Status(myAdress);
            Thread thread = new Thread(status);
            thread.start();

            //receiveFiles
            Receiver receiver = new Receiver(connection,folder);
            receiver.receive();
            //sendFiles
            List<String> listNames = receiver.toSend;
            Map<Short,String> files = new TreeMap<>();
            for (short i =0; i < listNames.size();i++)
                files.put(i,listNames.get(i));
            List<Long> cheksumList = Util.checkSumList(folder,listNames);
            byte[]dataList = FTRapid.buildList(listNames,cheksumList);
            Status.addToStatus("Sent list of files");

            ft = FTRapid.buildPacket(FTRapid.LIST,(short) 0,0,dataList);
            connection.changeTimeout(50);
            ft = connection.sendAndReceive(ft);
            connection.changeTimeout(10);
            FTRapid.parseList (ft.content,files);
            Status.addToStatus("Files to send :" + files.values());
            System.out.println("Files to send: " + files.values());

            Sender sender = new Sender(connection,folder,files);
            sender.send();
            Status.addToStatus("Finished");


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
    }

}
