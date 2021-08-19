package com.ugrong.framework.amqp.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
public @interface EventApi {

    /**
     * 交换机名称
     *
     * @return the exchange
     */
    String exchange();

    /**
     * 路由关键字，不填将使用配置文件中配置的默认路由key，适用于交换机与队列一一对应的场景
     *
     * @return the routingKey
     */
    String routingKey() default "";
}
