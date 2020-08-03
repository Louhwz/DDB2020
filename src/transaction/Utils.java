package transaction;

import java.io.*;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 16:25
 */
public class Utils {
    public static final String NO_DIE = "NoDie";

    public static final String TM_DIE_BEFORE_COMMIT = "DIE_BEFORE_COMMIT";
    public static final String TM_DIE_AFTER_COMMIT = "DIE_AFTER_COMMIT";


    public static String genrConSyntax(String rmiPort) {
        if (rmiPort == null)
            return "";
        else
            return "//:" + rmiPort + "/";
    }

    /**
     * override write
     *
     * @param filePath
     * @param obj
     * @return
     */
    public static boolean storeObject(String filePath, Object obj) {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new FileOutputStream(file));
            oout.writeObject(obj);
            oout.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (oout != null) {
                    oout.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object loadObject(String filePath) {
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new FileInputStream(new File(filePath)));
            return oin.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
