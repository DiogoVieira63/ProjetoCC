import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class FFile implements Runnable{
    private String name;
    private Map<Integer,byte[]> content;


    public FFile(){
        content = new TreeMap<>();
    }

    public void setName(String name){
        this.name = name;
    }

    public void addContent (int num,byte[]array){
        this.content.putIfAbsent(num,array);
    }

    public void run(){
        try {
            FileOutputStream fos = new FileOutputStream(name);
            for (byte[] data : content.values())
                fos.write(data);
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
