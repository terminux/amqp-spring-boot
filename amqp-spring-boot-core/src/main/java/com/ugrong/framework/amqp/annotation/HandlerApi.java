package com.ugrong.framework.amqp.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义mq handler注解，可作用于方法上
 */
@Target({METHOD})
@Retention(RUNTIME)
@Inherited
public @interface HandlerApi {

	/**
	 * 队列名称
	 *
	 * @return the queueName
	 */
	String queueName();

	/**
	 * 当event有多个handler时的处理顺序，数值越小优先消费消息
	 *
	 * @return the order
	 */
	int order() default 0;

}
