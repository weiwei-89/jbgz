package cn.tj.food.monkey.model;

public interface Element {
    enum Type {
        INTEGER,
        BOOLEAN,
        TEXT,
        NULL,
        VOID,
        FUNCTION
    }

    Type type();

    String inspect();
}