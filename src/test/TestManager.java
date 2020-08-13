package test;

import java.io.*;

/**
 * @Author myzhou
 * @Date 2020/8/1
 */
public class TestManager {

    public static void main(String[] args) throws IOException {
        String testClass = System.getProperty("testClass");

        // mkdir results folder
        File dataDir = new File("results/" + testClass);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // start the test with the corresponding parameter
        System.out.println("Launching test " + testClass);
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

//        try {
//            proc.waitFor();
//        } catch (InterruptedException e) {
//            System.err.println("WaitFor interrupted.");
//            System.exit(1);
//        }

        // the sign of the process
        // 1: passed
        // 2: failed
        int exitSign = process.exitValue();
        if (exitSign == 0) {
            System.out.println("Test " + testClass + " passed.");
        } else if (exitSign == 2) {
            System.out.println("Test " + testClass + " failed.");
        } else {
//            SequenceInputStream sis = new SequenceInputStream(process.getInputStream(), process.getErrorStream());
//            InputStreamReader inst = new InputStreamReader(sis);
//            BufferedReader br = new BufferedReader(inst);
//
//            String res = null;
//            StringBuilder sb = new StringBuilder();
//            while ((res = br.readLine()) != null) {
//                System.out.println(res);
//                sb.append(res).append("\n");
//            }
//            br.close();
//            System.out.println(sb);
            System.err.println("Test " + testClass + " unknown error code (" + exitSign + ")");
            System.exit(1);
        }
    }
}
