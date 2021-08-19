package com.ugrong.framework.amqp.publisher;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.IMessagePublisher;
import com.ugrong.framework.amqp.annotation.EventApi;
import com.ugrong.framework.amqp.autoconfigure.properties.AmqpProperties;
import com.ugrong.framework.amqp.utils.MessageQueueUtil;

@Slf4j
@AllArgsConstructor
public abstract class AbstractMessagePublisher implements IMessagePublisher {

	private final AmqpProperties amqpProperties;

	@Override
	public void publish(IEvent event) {
		EventApi eventApi = MessageQueueUtil.getEventApi(event.getClass());

		log.debug("Publish MQ event.{}", event);

		this.sendMessage(eventApi.exchange(), this.getRealRoutingKey(eventApi.routingKey()), event);
	}

	@Override
	public void publish(IEvent event, long delay, TimeUnit delayUnit) {
		Assert.isTrue(amqpProperties.isEnableDelayedMessage(), "Not support delayed messages.");
		EventApi eventApi = MessageQueueUtil.getEventApi(event.getClass());

		log.debug("Publish MQ delay event.event=[{}],delay=[{}],delayUnit=[{}]", event, delay, delayUnit);
		Assert.isTrue(delay > 0 && delayUnit != null, "缺少延时参数或延时参数有误");

		this.sendDelayMessage(eventApi.exchange(), this.getRealRoutingKey(eventApi.routingKey()),
				event, delay, delayUnit);
	}

	protected String getRealRoutingKey(String routingKey) {
		return StringUtils.isBlank(routingKey) ? amqpProperties.getDefaultRoutingKey() : routingKey;
	}

	@Override
	public void publish2DeadLetter(IEvent event) {
		log.debug("Publish MQ event to dead letter.{}", event);
		this.sendMessage(amqpProperties.getDeadLetterExchange(), amqpProperties.getDeadLetterRoutingKey(), event);
	}

	protected abstract void sendMessage(String exchange, String routingKey, IEvent event);

	protected abstract void sendDelayMessage(String exchange, String routingKey, IEvent event, long delay, TimeUnit delayUnit);
}
