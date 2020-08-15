package test;

import transaction.WorkflowController;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/14
 */
public class LockWW {
    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid1 = wc.start();
            int xid2 = wc.start();

            // phase 2
            if (!wc.addFlight(xid1, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            try{
                if (!wc.addFlight(xid2, "flight1", 99, 399)) {
                    System.err.println("Add flight failed");
                }
            }catch (Exception e){
                System.out.println("Test pass.");
                ExitWC(wc, 0);
            }
            wc.commit(xid1);

            // phase 3
            wc.commit(xid2);

            // phase 4
            int xid3 = wc.start();
            int r1 = wc.queryFlightPrice(xid3, "flight1");
            Check(wc,399, r1);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test fail:" + e.getMessage());
            ExitWC(wc, 1);
        }
    }
}
