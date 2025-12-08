package cn.tj.food.monkey.model.evaluator;

import cn.tj.food.monkey.model.Element;

public enum VoidElement implements Element {
    INSTANCE;

    @Override
    public Type type() {
        return Type.VOID;
    }

    @Override
    public String inspect() {
        return "void";
    }
}