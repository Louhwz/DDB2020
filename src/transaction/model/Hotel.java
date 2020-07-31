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

    private Hotel(String location, int price, int numRooms, int numAvail) {
        this.location = location;
        this.price = price;
        this.numRooms = numRooms;
        this.numAvail = numAvail;
        isDeleted = false;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"location", "price", "numRooms", "numAvail"};
    }

    @Override
    public String[] getColumnValues() {
        return new String[]{location, String.valueOf(price), String.valueOf(numRooms), String.valueOf(numAvail)};
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
