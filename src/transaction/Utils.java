package transaction;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 16:25
 */
public class Utils {
    public static String genrConSyntax(String rmiPort) {
        if(rmiPort == null)
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

    private static void ExitWC(WorkflowController wc, int status) {
        try {
            wc.dieNow("ALL");
        } catch (Exception e) {
        }
        System.exit(status);
    }
}

