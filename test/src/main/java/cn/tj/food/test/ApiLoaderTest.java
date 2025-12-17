package cn.tj.food.test;

import cn.tj.food.common.router.ApiLoader;

public class ApiLoaderTest {
    public static void main(String[] args) throws Exception {
        ApiLoader apiLoader = new ApiLoader("org.edward.pandora.test.controller");
        apiLoader.init();
        apiLoader.execute("/user/shoot", "9876543210", "{'user_name':'张三'}");
    }
}