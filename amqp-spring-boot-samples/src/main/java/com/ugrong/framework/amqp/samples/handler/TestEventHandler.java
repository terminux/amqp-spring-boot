package com.ugrong.framework.amqp.samples.handler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ugrong.framework.amqp.annotation.HandlerApi;
import com.ugrong.framework.amqp.samples.common.MQConstants;
import com.ugrong.framework.amqp.samples.event.TestEvent;

@Component
public class TestEventHandler {

	@HandlerApi(queueName = MQConstants.TEST_QUEUE, order = 1)
	public void handle1(TestEvent event) {

		System.out.println("====消费消息===TestEventHandler handle1(event) date:" + LocalDateTime.now());
		System.out.println(event);
		if (event.getId() == 1) {
			//模拟消费失败
			throw new RuntimeException("模拟消费失败");
		}
	}

	@HandlerApi(queueName = MQConstants.TEST_QUEUE, order = 2)
	public void handle2(TestEvent event) {

		System.out.println("====消费消息===TestEventHandler handle2(event) date:" + LocalDateTime.now());
		System.out.println(event);
	}
}
