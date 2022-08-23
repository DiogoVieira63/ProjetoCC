import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("Erro: Deve conter 2 argumentos");
            return;
        }
        if (!Util.validateFolder(args[0])){
            System.out.println(args[0] + "não existe ou não é uma pasta");
            return;
        }
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(Util.port);
            FTRapid ft = FTRapid.buildPacket(FTRapid.OK,(short) 0,0,new byte[0]);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(ft.header);
                outputStream.write(ft.content);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[]data = outputStream.toByteArray();
            InetAddress address = InetAddress.getByName(args[1]);
            DatagramPacket packet = new DatagramPacket(data,data.length,address,Util.port);
            socket.send(packet);
            socket.setSoTimeout(500);
            socket.receive(packet);
            ft = FTRapid.parse(packet.getData(),packet.getLength());
            System.out.println("Type is ->" + ft.type);
            if(ft.type == FTRapid.END) return;
            socket.close();
            Cliente cliente = new Cliente();
            System.out.println("Client Created");
            cliente.start(args);
        }
        catch (SocketTimeoutException | UnknownHostException e){
            socket.close();
            Server server = new Server();
            System.out.println("Server Created");
            server.start(args);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
