import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FTRapid {
    byte type;
    int numSeq;
    short file;
    long checkSumContent;
    byte[]header;
    byte[]content;

    static byte OK = 0;
    static byte LIST =1;
    static byte DATA =2;
    static byte ENDFILE=3;
    static byte END =4;
    static byte START=5;


    public FTRapid(byte type, short file, int numSeq, long checksum, byte[] header, byte[] data) {
        this.type= type;
        this.file=file;
        this.numSeq=numSeq;
        this.checkSumContent=checksum;
        this.header=header;
        this.content=data;
    }

    public static FTRapid buildPacket (byte type, short file, int numSeq,byte[]data){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(type);       //0
        long checksum = Util.getCRC32Checksum(data);
        try {
            outputStream.write(ByteBuffer.allocate(2).putShort(file).array());      //1-2
            outputStream.write(ByteBuffer.allocate(4).putInt(numSeq).array());      //3-6
            outputStream.write(ByteBuffer.allocate(8).putLong(checksum).array());   //7-14
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[]header = outputStream.toByteArray();
        return new FTRapid(type,file,numSeq,checksum,header,data);

    }

    public static FTRapid parse(byte[]data, int length) throws Exception {
        byte type =  data[0];
        byte[]header = Arrays.copyOfRange(data,0,15);
        short file=  ByteBuffer.wrap(Arrays.copyOfRange(header,1,3)).getShort();
        int numSeq = ByteBuffer.wrap(Arrays.copyOfRange(header,3,7)).getInt();
        long check = ByteBuffer.wrap(Arrays.copyOfRange(header,7,15)).getLong();
        byte[]content = Arrays.copyOfRange(data,15,length);
        long checkSum = Util.getCRC32Checksum(content);
        if(checkSum != check) {
            throw new Exception("CheckSum is not the same");
        }
        return new FTRapid(type,file,numSeq,checkSum,header,content);
    }

    public static byte[] buildList (List<String> names, List<Long> checksumList) throws IOException {
        byte[] nrFiles = ByteBuffer.allocate(2).putShort((short)names.size()).array();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(nrFiles);
        int checkIndex = 0;
        for (String file : names){
            byte[]name = file.getBytes();
            byte[]size = ByteBuffer.allocate(2).putShort((short)name.length).array();
            byte[]checksum = ByteBuffer.allocate(8).putLong(checksumList.get(checkIndex)).array();
            checkIndex++;
            outputStream.write(size);
            outputStream.write(name);
            outputStream.write(checksum);
        }
        return outputStream.toByteArray();
    }

    public static void parseList(byte[] data, Map<Short,String> files) {
        short len =  ByteBuffer.wrap(Arrays.copyOfRange(data,0,2)).getShort();
        short index = 2;
        List<Short> list = new ArrayList<>();
        for (short i =0;i < len;i++){
            short indexFile = ByteBuffer.wrap(Arrays.copyOfRange(data,index,index+=2)).getShort();
            list.add(indexFile);
        }
        int length = files.size();
        for (short i =0; i < length;i++){
            if(!list.contains(i)){
                files.remove(i);
                
            }
        }
    }
    public static byte[] buildNameAdress (String adressName){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            short len = (short) adressName.length();
            outputStream.write(ByteBuffer.allocate(2).putShort(len).array());
            outputStream.write(adressName.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    public static String parseNameAdress (byte[] data){
        short len = ByteBuffer.wrap(Arrays.copyOfRange(data,0,2)).getShort();
        return new String(Arrays.copyOfRange(data,2,2+len));
    }



}
