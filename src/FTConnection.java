import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

public class FTConnection {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private int timeout;

    public FTConnection (){
        this.timeout = 10;
        this.port = Util.port;
        try {
            this.socket = new DatagramSocket(Util.port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public FTConnection(InetAddress address, int port) {
        try {
            this.socket = new DatagramSocket(Util.port);
        }
        catch (SocketException e){
            e.printStackTrace();
        }
        this.address = address;
        this.port = port;
    }

    public void changeTimeout (int timeout){
        this.timeout = timeout;
        try {
            socket.setSoTimeout(timeout);
        }
        catch (SocketException e){
            e.printStackTrace();
        }
    }

    public String waitConnection(String waiting) throws UnknownHostException {
        byte[]buffer = new byte[256];
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        while (true) {
            try {
                socket.receive(request);
                FTRapid ftRapid;
                ftRapid = FTRapid.parse(request.getData(), request.getLength());
                this.address = request.getAddress();
                if (ftRapid.type != FTRapid.START) {
                    InetAddress waitingAdress = InetAddress.getByName(waiting);
                    System.out.println(waitingAdress + " VS " + this.address);
                    if (waitingAdress.equals(this.address)) ftRapid = FTRapid.buildPacket(FTRapid.OK, (short) 0, 0, new byte[0]);
                    else ftRapid = FTRapid.buildPacket(FTRapid.END, (short) 0, 0, new byte[0]);
                    send(ftRapid);
                    continue;
                }
                String myAdress = FTRapid.parseNameAdress(ftRapid.content);
                byte[] data = FTRapid.buildNameAdress(waiting);
                ftRapid = FTRapid.buildPacket(FTRapid.START, (short) 0, 0, data);
                send(ftRapid);
                return myAdress;
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private DatagramPacket createPacket (FTRapid ft){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(ft.header);
            outputStream.write(ft.content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[]data = outputStream.toByteArray();
        return new DatagramPacket(data,data.length,this.address,this.port);
    }

    public FTRapid sendAndReceive(FTRapid ft){
        DatagramPacket packet = createPacket(ft);
        this.timeout = 50;
        int numSeq = ft.numSeq;
        byte type = ft.type;
        while (true) {
            try {
                socket.send(packet);
                socket.setSoTimeout(timeout);
                socket.receive(packet);
                ft = FTRapid.parse(packet.getData(),packet.getLength());
                if (ft.numSeq != numSeq) continue;
                if (type == FTRapid.LIST && type != ft.type) continue;
                break;
            } catch (SocketTimeoutException e) {
                System.out.println("TIMEOUT");
                timeout *= 2;
            }
            catch (Exception e) {
                //System.out.println("Checksum error");
            }
        }
        return ft;

    }
    public void send(FTRapid ft){
        DatagramPacket packet = createPacket(ft);
        try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public FTRapid receive() throws Exception {
        byte[]buffer = new byte[Util.packetLength];
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            socket.receive(request);
            socket.setSoTimeout(timeout=50);
        }
        catch (SocketTimeoutException e) {
            timeout *= 2;
            socket.setSoTimeout(timeout);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return FTRapid.parse(request.getData(),request.getLength());

    }


}
