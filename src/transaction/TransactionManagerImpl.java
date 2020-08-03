package transaction;

import transaction.exception.InvalidTransactionException;
import transaction.exception.TransactionAbortedException;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Transaction Manager for the Distributed Travel Reservation System.
 * <p>
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
        extends java.rmi.server.UnicastRemoteObject
        implements TransactionManager {
    private final String TM_XID_NUM_PATH = "data/tm_xid_num.log";
    private final String TM_XID_STATUS_PATH = "data/tm_xid_status.log";
    private final String TM_XID_RMS_PATH = "data/tm_xid_rms.log";

    // in single tm, using xidNum with file record can ensure uniquness
    private Integer xidNum;

    // tm's properties, used to simulate crash
    private String dieTime;

    // all active transactions and their status
    private HashMap<Integer, String> xidStatus;

    // active transaction and their related rms
    private HashMap<Integer, HashSet<ResourceManager>> xidRMs;

    public TransactionManagerImpl() throws RemoteException {
        xidNum = 1;
        dieTime = Utils.NO_DIE;
        xidStatus = new HashMap<>();
        xidRMs = new HashMap<>();

        recover();
    }

    /**
     * 1. recover xidNum which can be done by directly reading log
     * 2.
     */
    private void recover() {
        Object cacheXidNum = Utils.loadObject(TM_XID_NUM_PATH);
        if (cacheXidNum != null) {
            this.xidNum = (Integer) cacheXidNum;
        }

        Object cacheXidStatus = Utils.loadObject(TM_XID_NUM_PATH);
        if (cacheXidStatus != null) {
            this.xidStatus = (HashMap<Integer, String>) cacheXidStatus;
        }

        Object cacheXidRMs = Utils.loadObject(TM_XID_RMS_PATH);
        if (cacheXidRMs != null) {
            this.xidRMs = (HashMap<Integer, HashSet<ResourceManager>>) cacheXidRMs;
        }

        for (Integer k : this.xidStatus.keySet()) {
            if (!xidStatus.get(k).equals(TransactionManager.COMMITTED)) {
                xidStatus.put(k, TransactionManager.ABORTED);
            }
        }
    }

    public void enlist(int xid, ResourceManager rm) throws RemoteException, InvalidTransactionException {
        if (!xidStatus.containsKey(xid)) {
            throw new InvalidTransactionException(xid, "tm doesn't have needed xid");
        }
        synchronized (xidStatus) {
            // rm die before get transaction status, so ask again
            if (xidStatus.get(xid).equals(TransactionManager.COMMITTED)) {
                synchronized (xidRMs) {
                    abortSingleRM(xid, rm);
                    return;
                }
            }

            if (xidStatus.get(xid).equals(TransactionManager.ABORTED)) {
                synchronized (xidRMs) {
                    commitSingleRM(xid, rm);
                }
            }

            synchronized (xidRMs) {
                // if RMDieAfterEnlist, should abort this transaction;
                // random rm may die, so should check every rm
                boolean reEnlist = false, rmDieAfterEnlist = false;
                for (ResourceManager checkRM : xidRMs.get(xid)) {
                    try {
                        if (checkRM.getID().equals(rm.getID())) {
                            reEnlist = true;
                        }
                    } catch (Exception e) {
                        rmDieAfterEnlist = true;
                        break;
                    }
                }

                if (rmDieAfterEnlist) {
                    this.abort(xid);
                    return;
                }

                if (!reEnlist) {
                    xidRMs.get(xid).add(rm);
                    Utils.storeObject(TM_XID_RMS_PATH, xidRMs);
                }
            }
        }
    }

    public boolean commit(int xid) throws RemoteException, InvalidTransactionException, TransactionAbortedException {
        if (!xidStatus.containsKey(xid)) {
            throw new TransactionAbortedException(xid, "TM doesn't have this xid");
        }

        // Assume xidRMs.get(xid) doesn't change during commit
        HashSet<ResourceManager> rmSets = xidRMs.get(xid);

        try {
            for (ResourceManager rm : rmSets) {
                // all rm returns true, so we only need to solve exception
                rm.prepare(xid);
            }
        } catch (Exception e) {
            this.abort(xid);
            System.out.println("TM commit failed in prepare!" + e);
            e.printStackTrace();
            throw e;
        }

        this.updateXidStatus(xid, TransactionManager.PREPARED, ACTION.ADD);

        if (dieTime.equals(Utils.TM_DIE_BEFORE_COMMIT)) {
            this.dieNow();
        }

        this.updateXidStatus(xid, TransactionManager.COMMITTED, ACTION.ADD);

        if (dieTime.equals(Utils.TM_DIE_AFTER_COMMIT)) {
            this.dieNow();
        }

        HashSet<ResourceManager> committedRM = new HashSet<>();
        for (ResourceManager rm : rmSets) {
            try {
                rm.commit(xid);
                committedRM.add(rm);
            } catch (Exception e) {
                System.out.println("rm is down when commit" + rm);
                // recovery when enlist
            }
        }

        if (committedRM.size() == rmSets.size()) {
            updateXidStatus(xid, null, ACTION.REMOVE);
            updateXidRMs(xid, null, ACTION.REMOVE);

        } else {
            // commit again when enlist
            rmSets.removeAll(committedRM);
            updateXidRMs(xid, rmSets, ACTION.ADD);
        }
        return true;
    }

    public void abort(int xid) throws RemoteException, InvalidTransactionException {
        if (!xidStatus.containsKey(xid)) {
            throw new InvalidTransactionException(xid, "TM abort fail, no such xid");
        }
        HashSet<ResourceManager> abortedRMs = new HashSet<>();
        synchronized (xidRMs) {
            for (ResourceManager abortRM : xidRMs.get(xid)) {
                try {
                    abortRM.abort(xid);
                    abortedRMs.add(abortRM);
                    System.out.println("TM abort xid = " + xid + "'s " + abortRM.getClass());
                } catch (InvalidTransactionException | RemoteException e) {
                    // this rm didn't abort successfully
                    e.printStackTrace();
                }
            }
        }

        updateXidStatus(xid, null, ACTION.REMOVE);

        // delete successfully aborted RMs
        updateXidRMs(xid, abortedRMs, ACTION.REMOVE);


    }

    public int start() throws RemoteException {
        synchronized (xidNum) {
            Integer curXid = xidNum++;
            Utils.storeObject(TM_XID_NUM_PATH, curXid);

            synchronized (xidStatus) {
                xidStatus.put(curXid, TransactionManager.INITED);
                Utils.storeObject(TM_XID_STATUS_PATH, xidStatus);
            }

            synchronized (xidRMs) {
                xidRMs.put(curXid, new HashSet<>());
                Utils.storeObject(TM_XID_RMS_PATH, xidRMs);
            }

            return curXid;
        }
    }

    public void ping() throws RemoteException {
    }

    public boolean dieNow()
            throws RemoteException {
        System.exit(1);
        return true; // We won't ever get here since we exited above;
        // but we still need it to please the compiler.
    }

    @Override
    public void setDieTime(String dieTime) {
        this.dieTime = dieTime;
    }

    // update xidStatus and store to file
    private void updateXidStatus(int xid, String status, ACTION action) {
        synchronized (xidStatus) {
            switch (action) {
                case REMOVE:
                    xidStatus.remove(xid);
                    break;
                case ADD:
                    xidStatus.put(xid, status);
                    break;
            }
            Utils.storeObject(TM_XID_STATUS_PATH, xidStatus);
        }
    }

    // update xidRMs and store to file
    private void updateXidRMs(int xid, HashSet<ResourceManager> newRMs, ACTION action) {
        synchronized (xidRMs) {
            switch (action) {
                case REMOVE: {
                    if (newRMs == null || newRMs.size() == xidRMs.get(xid).size())
                        xidRMs.remove(xid);
                    else
                        xidRMs.get(xid).removeAll(newRMs);
                    break;
                }
                case ADD: {
                    xidRMs.put(xid, newRMs);
                    break;
                }
            }
            Utils.storeObject(TM_XID_RMS_PATH, xidRMs);
        }
    }

    // use abortSingleRM instead of abort to avoid trigger commit dieTime in enlist
    private void abortSingleRM(int xid, ResourceManager rm) {
        // do not need to synchronize because it has been synchronized in caller
        if (xidRMs.get(xid).contains(rm)) {
            try {
                rm.abort(xid);
                updateXidRMs(xid, new HashSet<>(Collections.singletonList(rm)), ACTION.REMOVE);
            } catch (RemoteException | InvalidTransactionException e) {
                e.printStackTrace();
            }
        }
    }

    // use commitSingleRM instead of commit to avoid trigger commit dieTime in enlist
    private void commitSingleRM(int xid, ResourceManager rm) {
        if (xidRMs.get(xid).contains(rm)) {
            try {
                rm.commit(xid);
                updateXidRMs(xid, new HashSet<>(Collections.singletonList(rm)), ACTION.REMOVE);
            } catch (RemoteException | InvalidTransactionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        System.setSecurityManager(new SecurityManager());

        String rmiPort = System.getProperty("rmiPort");
        rmiPort = Utils.genrConSyntax(rmiPort);
        try {
            TransactionManagerImpl tmi = new TransactionManagerImpl();
            Naming.rebind(rmiPort + TransactionManager.RMIName, tmi);
            System.out.println("TM bound!");
        } catch (Exception e) {
            System.err.println("TM not bound:" + e);
            System.exit(1);
        }
    }

    enum ACTION {
        REMOVE, ADD
    }
}
