package test;

import transaction.WorkflowController;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class LockRR {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            // phase 2
            int xid1 = wc.start();
            int xid2 = wc.start();

            // phase 3
            int r1 = wc.queryFlight(xid1, "flight1");
            Check(wc,100, r1);
            int r2 = wc.queryFlightPrice(xid2, "flight1");
            Check(wc,499, r2);

            // phase 4
            wc.commit(xid2);
            wc.commit(xid1);

            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 0);
        }
    }
}
