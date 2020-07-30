package transaction;

import transaction.rm.ResourceManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.util.Enumeration;
import java.util.Properties;

/**
 * myzhou19@fudan.edu.cn
 * 2020-7-30
 */

public class Client {
    private static String RESULT_PATH = "results/";
    private static final String LOG_TYPE = ".log";

    public static void main(String args[]) {
        launch();
    }

    public static void launch() {
        String rmiPort2 = System.getProperty("rmiPort");
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/ddb.conf"));
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }
        Object[] prop_keys = prop.keySet().toArray();
        for (int i = 0; i < prop_keys.length; i++) {
            String port_key = prop_keys[i].toString();
            String rmiPort = prop.getProperty(port_key);
            if (rmiPort == null) {
                rmiPort = "";
            } else if (!rmiPort.equals("")) {
                rmiPort = "//:" + rmiPort + "/";
            }
//            try {
//                Runtime.getRuntime().exec(new String[]{
//                        "sh -c java -classpath .. -DrmiPort=" + rmiPort +
//                                " -DrmiName=" + rmi +
//                                " -Djava.security.policy=./security-policy transaction." + classNames[i] +
//                                " >>" + RESULT_PATH + rmi + LOG_TYPE + " 2>&1"});
//            } catch (IOException e) {
//                System.err.println("Cannot launch " + rmiNames[i] + ": " + e);
//                cleanUpExit(2);
//            }
        }


//        // 远程调用rmiNames中的几个方法
        String rmiPort = System.getProperty("rmiPort");
//        String[] rmiNames = new String[]{TransactionManager.RMIName,
//                ResourceManager.RMINameFlights,
//                ResourceManager.RMINameRooms,
//                ResourceManager.RMINameCars,
//                ResourceManager.RMINameCustomers,
//                WorkflowController.RMIName};
//        String[] classNames = new String[]{"TransactionManagerImpl",
//                "ResourceManagerImpl",
//                "ResourceManagerImpl",
//                "ResourceManagerImpl",
//                "ResourceManagerImpl",
//                "WorkflowControllerImpl"};
//
////         launch 这些 rmi 实例
//        for (String rmi : rmiNames) {
//            try {
//                Runtime.getRuntime().exec(new String[]{
//                        "sh -c java -classpath .. -DrmiPort=" + rmiPort +
//                                " -DrmiName=" + rmi +
//                                " -Djava.security.policy=./security-policy transaction." + classNames[i] +
//                                " >>" + RESULT_PATH + rmi + LOG_TYPE + " 2>&1"});
//            } catch (IOException e) {
//                System.err.println("Cannot launch " + rmiNames[i] + ": " + e);
//                cleanUpExit(2);
//            }
//            System.out.println(rmiNames[i] + " launched");
////
////            try {
////                Thread.sleep(LAUNCHSLEEP);
////            } catch (InterruptedException e) {
////                System.err.println("Sleep interrupted.");
////                System.exit(1);
////            }
//        }
    }

    public static void bind(String port_key) {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("conf/ddb.conf"));
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }
        String rmiPort = prop.getProperty(port_key);
        if (rmiPort == null) {
            rmiPort = "";
        } else if (!rmiPort.equals("")) {
            rmiPort = "//:" + rmiPort + "/";
        }

        WorkflowController wc = null;
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bound to WC");
        } catch (Exception e) {
            System.err.println("Cannot bind to WC:" + e);
            System.exit(1);
        }
    }
}
