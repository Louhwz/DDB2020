package test;

import transaction.WorkflowController;

import static test.TestManager.Register;
import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class ACIDDieRMBeforeAbort {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "room1", 99, 399)) {
                System.err.println("Add room failed");
            }

            // phase 2
            wc.dieRMBeforeAbort("RMFlights");
            try {
                wc.abort(xid);
            } catch (Exception e) {
                // e.printStackTrace();
            }

            // phase 3
            Register("RMFlights");
            wc.reconnect();

            // phase 4
            xid = wc.start();
            int r1 = wc.queryFlight(xid, "flight1");
            Check(wc, -1, r1);
            int r3 = wc.queryRooms(xid, "room1");
            Check(wc, -1, r3);
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
