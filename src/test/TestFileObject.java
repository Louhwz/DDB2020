package test;

import transaction.Utils;
import transaction.model.ResourceItem;

import java.util.HashSet;

/**
 * @Author Louhwz
 * @Date 2020/08/01
 * @Time 11:07
 */
public class TestFileObject {
    public static void main(String[] args) {
        String path = "src/test/data/tm_xids.log";
        HashSet<ResourceItem> set = new HashSet<>();

//        Car car = new Car("SH", 100000, 20, 20);
//        set.add(car);
//        set.add(new Flight("123", 123, 123, 123));
//        Utils.storeObject(path, set);
        HashSet<ResourceItem> s1 = (HashSet) Utils.loadObject(path);
        for (ResourceItem k : s1) {
            System.out.println(k.getKey());
        }
    }
}
