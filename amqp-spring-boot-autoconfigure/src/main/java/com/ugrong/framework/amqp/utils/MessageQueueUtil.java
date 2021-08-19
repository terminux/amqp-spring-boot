package com.ugrong.framework.amqp.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.annotation.EventApi;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class MessageQueueUtil {

	public static EventApi getEventApi(Class<? extends IEvent> eventClass) {
		return Optional.ofNullable(eventClass).map(clazz -> clazz.getAnnotation(EventApi.class))
				.filter(api -> StringUtils.isNotBlank(api.exchange()))
				.orElseThrow(() -> new RuntimeException("event invalid"));
	}

	public static String errorMsg(String message, Object target, Method method) {
		return String.format("%s,for[%s#%s].", message, target.getClass(), method.getName());
	}

	public static Exchange newExchange(String exchangeName) {
		return buildExchange(exchangeName).build();
	}

	public static Exchange newExchangeWithDelay(String exchangeName) {
		Map<String, Object> args = new HashMap<>();
		args.put("x-delayed-type", "direct");
		return buildExchange(exchangeName).delayed().withArguments(args).build();
	}

	private static ExchangeBuilder buildExchange(String exchangeName) {
		return ExchangeBuilder.directExchange(exchangeName).durable(true);
	}

	public static Queue newQueue(String queueName) {
		return QueueBuilder.durable(queueName).build();
	}

	public static Binding binding(String exchangeName, String queueName, String routingKey) {
		return new Binding(queueName, Binding.DestinationType.QUEUE,
				exchangeName, routingKey, null);
	}

}
