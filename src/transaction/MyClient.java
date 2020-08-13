package transaction;

import transaction.exception.InvalidTransactionException;
import transaction.exception.TransactionAbortedException;

import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * @Author Louhwz
 * @Date 2020/08/01
 * @Time 21:51
 */
public class MyClient {
    public static void main(String[] args) {
        String rmiPort = System.getProperty("rmiPort");
        rmiPort = Utils.genrConSyntax(rmiPort);

        WorkflowController wc = null;
        try {
            wc = (WorkflowController) Naming.lookup(rmiPort + WorkflowController.RMIName);
            System.out.println("Bind to wc!");
        } catch (Exception e) {
            System.out.println("Cannot bind to wc" + e);
            System.exit(1);
        }

        try {
            int xid = wc.start();
            wc.addFlight(xid, "347", 100, 100);

            System.out.println("Flight 347 has " +
                    wc.queryFlight(xid, "347") +
                    " seats.");

            if (!wc.commit(xid)) {
                System.err.println("Commit failed");
            }
        } catch (RemoteException | InvalidTransactionException | TransactionAbortedException e) {
            e.printStackTrace();
        }
    }
}
