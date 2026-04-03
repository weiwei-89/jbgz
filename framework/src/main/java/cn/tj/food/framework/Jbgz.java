package cn.tj.food.framework;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Jbgz {
    String value() default "";
    String configPrefix() default "";
}