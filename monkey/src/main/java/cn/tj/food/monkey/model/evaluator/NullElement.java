package cn.tj.food.monkey.model.evaluator;

import cn.tj.food.monkey.model.Element;

public enum NullElement implements Element {
    INSTANCE;

    @Override
    public Type type() {
        return Type.NULL;
    }

    @Override
    public String inspect() {
        return "null";
    }
}