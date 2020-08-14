package test;

import transaction.WorkflowController;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class ACIDDieRM {
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

            // phase 2: set die situation after enlist
            xid = wc.start();
            try {
                if (!wc.addFlight(xid, "flight2", 200, 300)) {
                    System.err.println("Add flight failed");
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
            wc.dieNow("RMFlights");

            // phase 3
            Register("runrmflights");
            wc.reconnect();
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            // phase 4
            xid = wc.start();
            int r1 = wc.queryFlight(xid, "flight1");
            Check(wc, 100, r1);
            int r2 = wc.queryFlight(xid, "flight2");
            Check(wc, -1, r2);
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
