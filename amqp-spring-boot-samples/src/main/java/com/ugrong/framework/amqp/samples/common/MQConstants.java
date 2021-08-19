package com.ugrong.framework.amqp.samples.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MQConstants {

	/**
	 * 测试交换机
	 */
	public static final String TEST_EXCHANGE = "spring-boot-exchange";

	/**
	 * 测试路由key
	 */
	public static final String TEST_ROUTING_KEY = "123456";

	/**
	 * 测试队列
	 */
	public static final String TEST_QUEUE = "spring-boot-queue";

	//－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－－
}
