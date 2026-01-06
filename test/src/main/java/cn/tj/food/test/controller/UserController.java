package cn.tj.food.test.controller;

import cn.tj.food.common.router.ApiParam;
import cn.tj.food.common.router.FormParam;
import cn.tj.food.common.router.JsonParam;
import cn.tj.food.common.router.Path;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(value="/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Path("/hello")
    public String hello() {
        logger.info("hello!");
        return "hello!";
    }

    @Path("/hello3")
    public void hello3(String token, @FormParam ApiParam param) throws Exception {
        logger.info("token: {}", token);
        logger.info("param[name]: {}", param.getString("name", ""));
        logger.info("param[age]: {}", param.getInteger("age", 0));
        logger.info("param[hobby]: {}", param.getString("hobby", ""));
    }

    @Path("/shoot")
    public void shoot(String token, @JsonParam String json) {
        logger.info("token: {}", token);
        logger.info("json: {}", json);
    }

    @Path("/shoot2")
    public void shoot2(String token, @FormParam ApiParam param) throws Exception {
        logger.info("token: {}", token);
        logger.info("param[name]: {}", param.getString("name", ""));
        logger.info("param[age]: {}", param.getInteger("age", 0));
        logger.info("param[hobby]: {}", param.getString("hobby", ""));
    }

    @Path("/shoot3")
    public void shoot3(String token, @FormParam ApiParam param) throws Exception {
        logger.info("token: {}", token);
        logger.info("param[name]: {}", param.getString("name", ""));
        logger.info("param[age]: {}", param.getInteger("age", 0));
        logger.info("param[hobby]: {}", param.getString("hobby", ""));
//        logger.info("param[file1]: {}", param.getBytes("file1", new byte[0]));
    }

    @Path("/add")
    public void add(String token, @JsonParam UserInfo param) {
        logger.info("param: {}", JSON.toJSONString(param));
    }
}