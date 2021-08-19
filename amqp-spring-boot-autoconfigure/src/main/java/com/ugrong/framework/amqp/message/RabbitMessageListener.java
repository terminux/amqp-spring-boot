package com.ugrong.framework.amqp.message;

import java.util.List;

import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.amqp.support.converter.MessageConverter;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.IQueueHandler;
import com.ugrong.framework.amqp.IQueueHandlerContainer;
import com.ugrong.framework.amqp.exception.AmqpException;

/**
 * 消息消费监听器
 */
@AllArgsConstructor
@Slf4j
public class RabbitMessageListener implements ChannelAwareMessageListener {

	private final MessageConverter messageConverter;

	private final IQueueHandlerContainer container;

	@Override
	public void onMessage(Message message, Channel channel) throws Exception {
		log.debug("Received MQ message.{}", message);
		if (message == null || message.getMessageProperties() == null || message.getMessageProperties()
				.getType() == null) {
			return;
		}
		MessageProperties properties = message.getMessageProperties();
		String eventClassName = properties.getType();
		String consumerQueue = properties.getConsumerQueue();

		IEvent event = (IEvent) messageConverter.fromMessage(message);
		try {
			List<IQueueHandler<IEvent>> handlers = container.get(consumerQueue, eventClassName);
			if (CollectionUtils.isNotEmpty(handlers)) {
				for (IQueueHandler<IEvent> handler : handlers) {
					try {
						handler.handle(event);
					}
					catch (Exception e) {
						log.error("Failed to handle event.{}", event);
						throw e;
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Failed to consume the message.", e);
			throw new AmqpException(e.getMessage(), event);
		}
	}
}
