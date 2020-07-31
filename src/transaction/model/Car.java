package transaction.model;

import transaction.exception.InvalidIndexException;

import java.io.Serializable;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 15:35
 */
public class Car implements ResourceItem, Serializable {
    private String location;
    private int price;
    private int numCars;
    private int numAvail;
    private boolean isDeleted;

    public Car(String location, int price, int numCars, int numAvail) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
        this.numAvail = numAvail;
        this.isDeleted = false;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"location", "price", "numCars", "numAvail", "isDeleted"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{location, String.valueOf(price), String.valueOf(numCars), String.valueOf(numAvail), String.valueOf(isDeleted)};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        return null;
    }

    @Override
    public Object getKey() {
        return this.location;
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
        return new Car(this.location, this.price, this.numCars, this.numAvail);
    }
}