package cn.tj.food.monkey.model.statement;

import cn.tj.food.monkey.model.Expression;
import cn.tj.food.monkey.model.Statement;
import cn.tj.food.monkey.model.Token;

public class ExpressionStatement implements Statement {
    private final Token token;

    public ExpressionStatement(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    private Expression expression;

    public Expression getExpression() {
        return expression;
    }
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void statementNode() {

    }

    @Override
    public String tokenLiteral() {
        return this.token.getLiteral();
    }

    @Override
    public String string() {
        return this.expression.string();
    }
}