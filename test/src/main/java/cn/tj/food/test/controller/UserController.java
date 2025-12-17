package cn.tj.food.test.controller;

import cn.tj.food.common.router.JsonParam;
import cn.tj.food.common.router.Path;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(value="user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Path("hello")
    public String hello() {
        logger.info("hello!");
        return "hello!";
    }

    @Path("shoot")
    public void shoot(String token, @JsonParam String json) {
        logger.info("token: {}", token);
        logger.info("json: {}", json);
    }

    @Path("add")
    public void add(String token, @JsonParam UserInfo param) {
        logger.info("param: {}", JSON.toJSONString(param));
    }
}