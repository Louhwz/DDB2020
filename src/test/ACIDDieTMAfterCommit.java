package test;

import transaction.WorkflowController;

import java.rmi.RemoteException;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class ACIDDieTMAfterCommit {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }

            // phase 2: set die situation before commit
            wc.dieTMAfterCommit();
            try {
                if (!wc.commit(xid)) {
                    System.err.println("Commit failed");
                }
            } catch (RemoteException e) {
            }

            // phase 3
            Register("runtm");
            wc =  bindWC("3345");
            xid = wc.start();
            int r1 = wc.queryFlight(xid, "flight1");
            Check(wc,100, r1);

            System.out.println("Test pass.");
            ExitWC(wc, 0);
        } catch (Exception e) {
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 0);
        }
    }
}
