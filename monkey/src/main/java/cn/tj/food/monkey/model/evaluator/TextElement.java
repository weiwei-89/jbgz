package cn.tj.food.monkey.model.evaluator;

import cn.tj.food.monkey.model.Element;

public class TextElement implements Element {
    private final String value;

    public TextElement(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public Type type() {
        return Type.TEXT;
    }

    @Override
    public String inspect() {
        return this.value;
    }
}