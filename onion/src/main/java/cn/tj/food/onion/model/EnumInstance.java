package cn.tj.food.onion.model;

import java.util.HashMap;

public class EnumInstance extends HashMap<String, Enum<?>> {
    public EnumInstance() {

    }

    public EnumInstance(int size) {
        super(size);
    }
}