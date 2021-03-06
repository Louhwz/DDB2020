package transaction.model;

import transaction.exception.InvalidIndexException;

import java.io.Serializable;

/**
 * @Author Louhwz
 * @Date 2020/07/31
 * @Time 15:26
 */
public class Flight implements ResourceItem, Serializable {
    private String flightNum;
    private int price;
    private int numSeats;
    private int numAvail;
    private boolean isDeleted;

    public Flight(String flightNum, int price, int numSeats, int numAvail) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numAvail;
        this.isDeleted = false;
    }

    public String getFlightNum() {
        return flightNum;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public void addSeats(int addSeats) {
        this.numSeats += addSeats;
        this.numAvail += addSeats;
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
        return new String[]{"flightNum", "price", "numSeats", "numAvail", "isDeleted"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{flightNum, String.valueOf(price), String.valueOf(numSeats), String.valueOf(numAvail), String.valueOf(isDeleted)};
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        return null;
    }

    @Override
    public Object getKey() {
        return flightNum;
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
        return new Flight(this.flightNum, this.price, this.numSeats, this.numAvail);
    }
}
