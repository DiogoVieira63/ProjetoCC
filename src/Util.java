import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Util {
    public static int dataLength = 4096;
    public static int headerLength = 15;
    public static int packetLength= headerLength+dataLength;
    public static int port = 8888;


    public static short ByteToShort(byte[] array){
        ByteBuffer wrapped = ByteBuffer.wrap(array);
        return wrapped.getShort();
    }

    public static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static List<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static List<Long> checkSumList (String folder,List<String>namesList){
        List<Long> cheksumList = new ArrayList<>();
        for (String name : namesList){
            try {
                byte[]temp = Files.readAllBytes(Paths.get(folder + "/" + name));
                long check = Util.getCRC32Checksum(temp);
                cheksumList.add(check);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return cheksumList;
    }

    public static boolean validateFolder (String folder){
        Path path = Paths.get(folder);
        if (Files.exists(path))
            return Files.isDirectory(path);
        return false;

    }

}
