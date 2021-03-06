package test;

import transaction.WorkflowController;

import static transaction.Utils.ExitWC;
import static transaction.Utils.bindWC;

/**
 * @Author myzhou
 * @Date 2020/8/1
 */
public class BasicBind {

    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            int xid = wc.start();
            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 1);
        }
    }
}
