package cn.tj.food.monkey.function;

import cn.tj.food.monkey.model.Expression;

@FunctionalInterface
public interface PrefixParseFunction {
    Expression apply() throws Exception;
}