package cn.tj.food.monkey.model.expression;

import cn.tj.food.monkey.model.Expression;
import cn.tj.food.monkey.model.Token;

public class IdentifierExpression implements Expression {
    private final Token token;

    public IdentifierExpression(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    private String value;

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
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
        return this.value;
    }
}