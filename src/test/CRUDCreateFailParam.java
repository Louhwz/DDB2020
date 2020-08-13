package test;

import transaction.WorkflowController;

import static transaction.Utils.ExitWC;
import static transaction.Utils.bindWC;

/**
 * @Author myzhou
 * @Date 2020/8/13
 */
public class CRUDCreateFailParam {

    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // test for number
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", -1, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "room1", 99, -1)) {
                System.err.println("Add room failed");
            }
            if (!wc.addCars(xid, "car1", -1, -1)){
                System.err.println("Add car failed");
            }

            if (!wc.newCustomer(xid, null)){
                System.err.println("Add customer failed");
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
