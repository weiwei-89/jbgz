package cn.tj.food.monkey.model.statement;

import cn.tj.food.monkey.model.Expression;
import cn.tj.food.monkey.model.Statement;
import cn.tj.food.monkey.model.Token;
import cn.tj.food.monkey.model.expression.IdentifierExpression;

public class LetStatement implements Statement {
    private final Token token;

    public LetStatement(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    private IdentifierExpression name;
    private Expression value;

    public IdentifierExpression getName() {
        return name;
    }
    public void setName(IdentifierExpression name) {
        this.name = name;
    }
    public Expression getValue() {
        return value;
    }
    public void setValue(Expression value) {
        this.value = value;
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
        StringBuilder sb = new StringBuilder();
        sb.append("let");
        sb.append(" ");
        sb.append(this.name.string());
        sb.append(" ");
        sb.append("=");
        sb.append(" ");
        sb.append(this.value.string());
        return sb.toString();
    }
}