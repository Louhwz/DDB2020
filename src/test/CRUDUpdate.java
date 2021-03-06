package test;

import transaction.WorkflowController;

import static transaction.Utils.*;

/**
 * @Author myzhou
 * @Date 2020/8/1
 */
public class CRUDUpdate {

    public static void main(String[] a) {
        WorkflowController wc = bindWC("3345");

        try {
            // phase 1
            int xid = wc.start();
            if (!wc.addFlight(xid, "flight1", 100, 499)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "room1", 99, 299)) {
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
            Check(wc, 100 - 1, r1);
            int r2 = wc.queryFlightPrice(xid, "flight1");
            Check(wc, 499, r2);
            int r3 = wc.queryRooms(xid, "room1");
            Check(wc, 99-1, r3);
            int r4 = wc.queryRoomsPrice(xid, "room1");
            Check(wc, 299, r4);
            int r5 = wc.queryCars(xid, "car1");
            Check(wc, 89-1, r5);
            int r6 = wc.queryCarsPrice(xid, "car1");
            Check(wc, 299000, r6);
            int r7 = wc.queryCustomerBill(xid, "customer1");
            Check(wc, 499 + 299 + 299000, r7);
            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

            // phase 2
            if (!wc.addFlight(xid, "flight1", 100, 699)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "room1", 199, 599)) {
                System.err.println("Add room failed");
            }
            if (!wc.addCars(xid, "car2", 99, 499000)) {
                System.err.println("Add car failed");
            }
            if (!wc.reserveFlight(xid, "customer1", "flight1")) {
                System.err.println("Reserve flight failed");
            }
            if (!wc.reserveFlight(xid, "customer1", "room1")) {
                System.err.println("Reserve room failed");
            }
            if (!wc.reserveFlight(xid, "customer1", "car2")) {
                System.err.println("Reserve car failed");
            }
            xid = wc.start();
            r1 = wc.queryFlight(xid, "flight1");
            Check(wc, 200 - 2, r1);
            r2 = wc.queryFlightPrice(xid, "flight1");
            Check(wc, 699, r2);
            r3 = wc.queryRooms(xid, "room1");
            Check(wc, 99 + 199 - 2, r3);
            r4 = wc.queryRoomsPrice(xid, "room1");
            Check(wc, 599, r4);
            r5 = wc.queryCars(xid, "car1");
            Check(wc, 89 + 99 - 2, r5);
            r6 = wc.queryCarsPrice(xid, "car1");
            Check(wc, 499000, r6);
            r7 = wc.queryCustomerBill(xid, "customer1");
            Check(wc, 499 + 299 + 299000 + 699 + 599 + 499000, r7);
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
