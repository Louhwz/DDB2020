package test;

import transaction.WorkflowController;

import static transaction.Utils.ExitWC;
import static transaction.Utils.bindWC;

/**
 * @Author myzhou
 * @Date 2020/8/13
 */
public class CRUDCreateFailInvalidItem {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }

            if (!wc.newCustomer(xid, "customer1")){
                System.err.println("Add customer failed");
            }
            if (!wc.reserveFlight(xid, "customer1", "flight2")) {
                System.err.println("Reserve flight failed");
            }

            if (!wc.commit(xid)){
                System.err.println("Commit failed");
            }

            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 0);
        }
    }
}