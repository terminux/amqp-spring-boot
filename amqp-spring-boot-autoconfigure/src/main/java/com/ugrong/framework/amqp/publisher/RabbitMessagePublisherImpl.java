package com.ugrong.framework.amqp.publisher;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.autoconfigure.properties.AmqpProperties;

public class RabbitMessagePublisherImpl extends AbstractMessagePublisher {

	private final RabbitTemplate rabbitTemplate;

	public RabbitMessagePublisherImpl(AmqpProperties amqpProperties, RabbitTemplate rabbitTemplate) {
		super(amqpProperties);
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	protected void sendMessage(String exchange, String routingKey, IEvent event) {
		this.rabbitTemplate.convertAndSend(exchange, routingKey, event);
	}

	@Override
	protected void sendDelayMessage(String exchange, String routingKey, IEvent event, long delay, TimeUnit delayUnit) {
		this.rabbitTemplate.convertAndSend(exchange, routingKey, event, message -> {
			MessageProperties properties = message.getMessageProperties();
			// 设置延迟时间 以毫秒为单位
			properties.setDelay((int) delayUnit.toMillis(delay));
			return message;
		});
	}
}
