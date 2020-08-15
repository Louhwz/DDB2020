package transaction;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;

/**
 * @Author Louhwz and myzhou
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

    public static String getHostIP() {
        String hostname = "";
        try {
            hostname = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return hostname;
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

//    public static void Register(String component) {
//        try {
//            Runtime.getRuntime().exec(new String[]{
//                    "sh",
//                    "-c",
//                    "make " + component + " &"});
//        } catch (Exception e) {
//            System.err.println("Cannot launch " + component + ": " + e);
//            System.exit(1);
//        }
//        System.out.println(component + " launched");
//
//        try {
//            Thread.sleep(4000);
//        } catch (InterruptedException e) {
//            System.err.println("Sleep interrupted.");
//            System.exit(1);
//        }
//    }

    public static void CleanResults() {
        try {
            if (Runtime.getRuntime().exec("rm -rf data").waitFor() != 0) {
                System.err.println("Clean data not successful");
            }
        } catch (IOException e) {
            System.err.println("Cannot clean data: " + e);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("WaitFor interrupted.");
            System.exit(1);
        }
    }

    public static WorkflowController bindWC(String rmiPort) {
        WorkflowController wc = null;
        rmiPort = Utils.genrConSyntax(rmiPort);
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bound to WC");
        } catch (Exception e) {
            System.err.println("Cannot bind to WC:" + e);
            System.exit(1);
        }

        return wc;
    }

    public static void ExitWC(WorkflowController wc, int status) {
        try {
            wc.dieNow("ALL");
        } catch (Exception e) {
        }
        try{
            Thread.sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.exit(status);
    }

    public static void Check(WorkflowController wc, int expect, int real) {
        if (expect != real) {
            System.out.println(expect + " " + real);
            System.err.println("Test fail");
            ExitWC(wc, 1);
        }
    }

}


