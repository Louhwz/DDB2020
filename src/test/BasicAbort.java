package test;

import transaction.WorkflowController;

import static transaction.Utils.ExitWC;
import static transaction.Utils.bindWC;

public class BasicAbort {

    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            int xid = wc.start();
            wc.abort(xid);
            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 0);
        }
    }
}
