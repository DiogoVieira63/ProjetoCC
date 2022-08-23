import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Status implements Runnable{
    
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";

    public static List<String> listStatus = new ArrayList<>();

    public static void addToStatus (String text){
        listStatus.add(text);
    }

    private ServerSocket serverSocket;

    public Status(String adress){
        try {
            
            this.serverSocket = new ServerSocket();
            InetAddress inetAddress=InetAddress.getByName(adress);
            int port=Util.port;
            ServerSocket serverSocket = new ServerSocket();
            SocketAddress endPoint= new InetSocketAddress(inetAddress, port);
            serverSocket.bind(endPoint);
            this.serverSocket= serverSocket;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop (){
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }

    @Override
    public void run() {
        try {
            Socket connectionSocket = serverSocket.accept();

            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new BufferedOutputStream(connectionSocket.getOutputStream()), "UTF-8")
            );
            StringBuilder stringBuilder = new StringBuilder();
            for (String string : listStatus){
                stringBuilder.append(string).append("<br>").append("\n");
            }
            String text = new String(stringBuilder);
            String output = "<html><head><title>Status</title></head><body><p>\n"+ text  + "</p></body></html>";

            out.write(OUTPUT_HEADERS + output.length() + OUTPUT_END_OF_HEADERS + output);
            out.flush();
            out.close();
            connectionSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.getMessage();
        }
    }
}
