package test;

import transaction.WorkflowController;

import static test.TestManager.Register;
import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class ACIDDieWC {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.newCustomer(xid, "customer1")){
                System.err.println("Add customer failed");
            }
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            // phase 2: set die situation after enlist
            xid = wc.start();
            if (!wc.reserveFlight(xid, "customer1", "flight1")) {
                System.err.println("Reserve flight failed");
            }
            try {
                wc.dieNow("WC");
            } catch (Exception e) {
                // e.printStackTrace();
            }

            // phase 3
            Register("WC");
            wc =  bindWC("3345");
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            // phase 4
            xid = wc.start();
            int r1 = wc.queryFlight(xid, "flight1");
            Check(wc, 99, r1);
            int r7 = wc.queryCustomerBill(xid, "customer1");
            Check(wc, 499, r7);
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 1);
        }
    }
}
