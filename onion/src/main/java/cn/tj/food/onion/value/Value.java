package cn.tj.food.onion.value;

public interface Value {
    enum Type {
        NULL,
        EMPTY,
        STRING,
        NUMBER,
        BOOLEAN,
        CHARACTER,
        MAP,
        LIST
    }

    Type type();

    String inspect();
}