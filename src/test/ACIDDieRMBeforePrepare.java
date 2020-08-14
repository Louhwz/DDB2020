package test;

import transaction.WorkflowController;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class ACIDDieRMBeforePrepare {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }

            // phase 2
            wc.dieRMBeforePrepare("RMFlights");
            try {
                if (!wc.commit(xid)) {
                    System.err.println("Commit failed");
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }

            // phase 3
            Register("runrmflights");
            wc.reconnect();

            // phase 4
            xid = wc.start();
            int r1 = wc.queryFlight(xid, "flight1");
            Check(wc, -1, r1);
            if (!wc.commit(xid)) {
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
