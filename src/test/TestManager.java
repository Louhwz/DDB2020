package test;

import transaction.ResourceManager;
import transaction.TransactionManager;
import transaction.WorkflowController;

import java.io.*;

import static transaction.Utils.CleanResults;

/**
 * @Author myzhou
 * @Date 2020/8/1
 */
public class TestManager {

    public static void main(String[] args) throws IOException {
        String testClass = System.getProperty("testClass");
        CleanResults();
        // mkdir results folder
        File dataDir = new File("results/" + testClass);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // start the test with the corresponding parameter
        System.out.println("Launching test " + testClass);
        LaunchAll();
        try {
            Thread.sleep(3000);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{
                    "sh",
                    "-c",
                    "java -classpath .. -DrmiPort=" + System.getProperty("rmiPort") +
                            " -DtestName=" + testClass +
                            " -Djava.security.policy=./security-policy test." + testClass +
                            " >results/" + testClass + "/" + "out.log" +
                            " 2>results/" + testClass + "/" + "err.log"});
        } catch (IOException e) {
            System.err.println("Launch Test error: " + e);
            System.exit(1);
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            System.err.println("WaitFor interrupted.");
            System.exit(1);
        }

        // the sign of the process
        // 0: passed
        // other: failed
        int exitSign = process.exitValue();
        if (exitSign == 0) {
            System.out.println("Test " + testClass + " passed.");
        } else if (exitSign == 1) {
            System.out.println("Test " + testClass + " failed.");
        } else {
            SequenceInputStream sis = new SequenceInputStream(process.getInputStream(), process.getErrorStream());
            InputStreamReader inst = new InputStreamReader(sis);
            BufferedReader br = new BufferedReader(inst);

            String res = null;
            StringBuilder sb = new StringBuilder();
            while ((res = br.readLine()) != null) {
                System.out.println(res);
                sb.append(res).append("\n");
            }
            br.close();
            System.out.println(sb);
            System.err.println("Test " + testClass + " unknown error code (" + exitSign + ")");
            System.exit(1);
        }
        try {
            Thread.sleep(3000);
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void LaunchAll() {
        String[] rmiNames = new String[]{TransactionManager.RMIName,
                ResourceManager.RMINameFlights,
                ResourceManager.RMINameRooms,
                ResourceManager.RMINameCars,
                ResourceManager.RMINameCustomers,
                WorkflowController.RMIName};
        String[] classNames = new String[]{"TransactionManagerImpl",
                "ResourceManagerImpl",
                "ResourceManagerImpl",
                "ResourceManagerImpl",
                "ResourceManagerImpl",
                "WorkflowControllerImpl"};
        for (int i = 0; i < classNames.length; i++) {
            try {
                Runtime.getRuntime().exec(new String[]{
                        "sh",
                        "-c",
                        "/usr/bin/java -classpath .. -DrmiPort=" + System.getProperty("rmiPort") +
                                " -DrmiName=" + rmiNames[i] +
                                " -Djava.security.policy=./security-policy transaction." + classNames[i] +
                                " >>" + "results/" + System.getProperty("testClass") + "/" + rmiNames[i] + ".log" + " 2>&1"

            });

            } catch (IOException e) {
                System.err.println("Cannot launch " + rmiNames[i] + ": " + e);
                e.printStackTrace();
            }

        }
    }

    public static void Register(String who){
        String[] rmiNames = new String[]{TransactionManager.RMIName,
                ResourceManager.RMINameFlights,
                ResourceManager.RMINameRooms,
                ResourceManager.RMINameCars,
                ResourceManager.RMINameCustomers,
                WorkflowController.RMIName};
        String[] classNames = new String[]{"TransactionManagerImpl",
                "ResourceManagerImpl",
                "ResourceManagerImpl",
                "ResourceManagerImpl",
                "ResourceManagerImpl",
                "WorkflowControllerImpl"};

        for (int i = 0; i < rmiNames.length; i++) {
            if (who.equals(rmiNames[i]) || who.equals("ALL")) {
                try {
                    Runtime.getRuntime().exec(new String[]{
                            "sh",
                            "-c",
                            "java -classpath .. -DrmiPort=" + System.getProperty("rmiPort") +
                                    " -DrmiName=" + rmiNames[i] +
                                    " -Djava.security.policy=./security-policy transaction." + classNames[i] +
                                    " >>" + "results/" + System.getProperty("testName") + "/" + rmiNames[i] + ".log" + " 2>&1"});
                } catch (Exception e) {
                    System.err.println("Cannot launch " + rmiNames[i] + ": " + e);
                }

                System.out.println(rmiNames[i] + " launched");

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    System.err.println("Sleep interrupted.");
                    System.exit(1);
                }
            }
        }
    }
}
