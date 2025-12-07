package cn.tj.food.common.task;

public interface Processor {
    void init() throws Exception;

    void process() throws Exception;
}