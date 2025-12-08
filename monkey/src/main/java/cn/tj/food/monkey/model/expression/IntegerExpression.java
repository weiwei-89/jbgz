package cn.tj.food.monkey.model.expression;

import cn.tj.food.monkey.model.Expression;
import cn.tj.food.monkey.model.Token;

public class IntegerExpression implements Expression {
    private final Token token;

    public IntegerExpression(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    private int value;

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void expressionNode() {

    }

    @Override
    public String tokenLiteral() {
        return this.token.getLiteral();
    }

    @Override
    public String string() {
        return String.valueOf(this.value);
    }
}