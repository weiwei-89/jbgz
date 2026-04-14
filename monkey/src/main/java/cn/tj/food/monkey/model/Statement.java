package cn.tj.food.monkey.model;

public interface Statement extends Node {
    enum Type {
        COMMON,
        BLOCK,
        RETURN
    }

    Type type();

    void statementNode();
}