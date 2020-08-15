
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * @author Administrator
 */
public class recover {
    public static void main(String[] args) {
        Integer c = Integer.valueOf(args[0]);
        String[] PATH = {"tm_xid_num.log","tm_xid_status.log","tm_xid_rms.log"};
        String path = PATH[c];
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new FileInputStream(new File(path)));
            Object k = oin.readObject();
            if(c==0){
                System.out.println((Integer) k);
            } else if(c==1){
                HashMap<Integer, String> xidStatus = (HashMap<Integer, String>) k;
                if (k == null) {
                    System.out.println("This is null");
                } else {
                    System.out.println(xidStatus);
                }
            } else{
//                HashMap<Integer, String> xidStatus = (HashMap<Integer, String>) k;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oin != null) {
                    oin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}