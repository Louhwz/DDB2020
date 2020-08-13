package test;

import transaction.WorkflowController;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/1
 */
public class CRUDRead {

    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "room1", 99, 399)) {
                System.err.println("Add room failed");
            }
            if (!wc.addCars(xid, "car1", 89, 299000)) {
                System.err.println("Add car failed");
            }

            if (!wc.newCustomer(xid, "customer1")) {
                System.err.println("Add customer failed");
            }
            if (!wc.reserveFlight(xid, "customer1", "flight1")) {
                System.err.println("Reserve flight failed");
            }
            if (!wc.reserveRoom(xid, "customer1", "room1")) {
                System.err.println("Reserve room failed");
            }
            if (!wc.reserveCar(xid, "customer1", "car1")) {
                System.err.println("Reserve car failed");
            }

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            xid = wc.start();
            int r1 = wc.queryFlight(xid, "flight1");
            Check(wc, 99, r1);
            int r2 = wc.queryFlightPrice(xid, "flight1");
            Check(wc, 499, r2);
            int r3 = wc.queryRooms(xid, "room1");
            Check(wc, 98, r3);
            int r4 = wc.queryRoomsPrice(xid, "room1");
            Check(wc, 399, r4);
            int r5 = wc.queryCars(xid, "car1");
            Check(wc, 88, r5);
            int r6 = wc.queryCarsPrice(xid, "car1");
            Check(wc, 299000, r6);
            int r7 = wc.queryCustomerBill(xid, "customer1");
            Check(wc, 499 + 399 + 299000, r7);
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
