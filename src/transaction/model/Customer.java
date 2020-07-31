package transaction.model;

import transaction.exception.InvalidIndexException;

import java.io.Serializable;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 16:06
 */
public class Customer implements ResourceItem, Serializable {
    private String custName;
    private boolean isDeleted;

    public Customer(String custName) {
        this.custName = custName;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"custName"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{custName};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        return null;
    }

    @Override
    public Object getKey() {
        return custName;
    }

    @Override
    public boolean isDeleted() {
        return this.isDeleted;
    }

    @Override
    public void delete() {
        this.isDeleted = true;
    }

    @Override
    public Object clone() {
        return new Customer(custName);
    }
}
