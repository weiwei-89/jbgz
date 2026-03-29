package cn.tj.food.framework;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TcpClient {
    String protocol() default "";
}