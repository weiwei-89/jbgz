package cn.tj.food.garlic;

public interface IEventService {
    void register(String app) throws Exception;

    void subscribe(String app, String eventName) throws Exception;
}