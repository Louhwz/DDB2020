package transaction.model;

import transaction.exception.InvalidIndexException;

import java.io.Serializable;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 15:51
 */
public class Hotel implements ResourceItem, Serializable {
    // 每个地点只有一个hotel
    private String location;
    private int price;
    private int numRooms;
    private int numAvail;
    private boolean isDeleted;

    public Hotel(String location, int price, int numRooms, int numAvail) {
        this.location = location;
        this.price = price;
        this.numRooms = numRooms;
        this.numAvail = numAvail;
        isDeleted = false;
    }

    public String getLocation() {
        return location;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumRooms() {
        return numRooms;
    }

    public void addRooms(int addRooms) {
        this.numRooms += addRooms;
        this.numAvail += addRooms;
    }

    public boolean reduceRooms(int reduceRooms) {
        if (reduceRooms > this.numAvail) {
            return false;
        }

        this.numRooms -= reduceRooms;
        this.numAvail -= reduceRooms;
        return true;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void cancelResv(int num) {
        this.numAvail += num;
    }

    public boolean addResv(int num) {
        if (this.numAvail < num) {
            return false;
        }
        this.numAvail -= num;
        return true;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"location", "price", "numRooms", "numAvail", "isDeleted"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{location, String.valueOf(price), String.valueOf(numRooms), String.valueOf(numAvail), String.valueOf(isDeleted)};
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
        return new Hotel(this.location, this.price, this.numRooms, this.numAvail);
    }
}
