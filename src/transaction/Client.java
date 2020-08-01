package transaction;

import java.net.Socket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static transaction.Utils.bindWC;

/**
 * mengying zhou
 * myzhou19@fudan.edu.cn
 * 2020-7-30
 */

public class Client {
    public static void main(String[] args) {
        WorkflowController wc = bindWC("3345");

        test_case1(wc);

    }

    public static void test_case1(WorkflowController wc) {
        try {
            int xid = wc.start();
            System.out.println("Flight 347 has " +
                    wc.queryFlight(6, "347") +
                    " seats.");

            if (!wc.addFlight(xid, "347", 230, 999)) {
                System.err.println("Add flight failed");
            }
            if (!wc.addRooms(xid, "SFO", 500, 150)) {
                System.err.println("Add room failed");
            }

            System.out.println("Flight 347 has " +
                    wc.queryFlight(xid, "347") +
                    " seats.");
            if (!wc.reserveFlight(xid, "John", "347")) {
                System.err.println("Reserve flight failed");
            }
            System.out.println("Flight 347 now has " +
                    wc.queryFlight(xid, "347") +
                    " seats.");

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }

        } catch (Exception e) {
            System.err.println("Received exception:" + e);
            System.exit(1);
        }
    }
}
