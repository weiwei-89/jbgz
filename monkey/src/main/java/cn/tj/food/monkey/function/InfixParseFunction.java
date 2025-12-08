package cn.tj.food.monkey.function;

import cn.tj.food.monkey.model.Expression;

@FunctionalInterface
public interface InfixParseFunction {
    Expression apply(Expression expression) throws Exception;
}