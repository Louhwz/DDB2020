package test;

import transaction.WorkflowController;
import transaction.exception.InvalidTransactionException;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/13
 */
public class BasicXid {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            int xid = wc.start();
            xid = xid + 1;

            try {
                wc.commit(xid);
            }catch (InvalidTransactionException e){
                System.err.println("InvalidTransactionException failed");
            }

            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 1);
        }

    }
}
