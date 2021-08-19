package com.ugrong.framework.amqp.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Exchange;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.IQueueHandler;
import com.ugrong.framework.amqp.IQueueHandlerContainer;
import com.ugrong.framework.amqp.annotation.EventApi;
import com.ugrong.framework.amqp.autoconfigure.properties.AmqpProperties;
import com.ugrong.framework.amqp.utils.MessageQueueUtil;

import static java.util.Map.Entry.comparingByKey;
import static java.util.stream.Collectors.toList;

@Slf4j
public abstract class AbstractQueueHandlerContainer implements IQueueHandlerContainer {

	/**
	 * map<queueName,map<eventClassName,EventHandlerNode>>
	 *
	 */
	private final Map<String, Map<String, EventHandlerNode>> queueHandlerMappings = new ConcurrentHashMap<>();

	private final ConfigurableListableBeanFactory beanFactory;

	protected final AmqpProperties amqpProperties;

	public AbstractQueueHandlerContainer(AmqpProperties amqpProperties, ConfigurableListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		this.amqpProperties = amqpProperties;
	}

	private static class EventHandlerNode {

		private final Map<Integer, List<IQueueHandler<IEvent>>> orderHandlerMappings = new ConcurrentHashMap<>();

		private List<IQueueHandler<IEvent>> eventHandlers;

		public void put(Integer order, IQueueHandler<IEvent> handler) {
			List<IQueueHandler<IEvent>> handlers = orderHandlerMappings.get(order);
			if (handlers == null) {
				handlers = new ArrayList<>();
			}
			handlers.add(handler);
			orderHandlerMappings.put(order, handlers);

			eventHandlers = orderHandlerMappings.entrySet().stream()
					.sorted(comparingByKey())
					.map(Map.Entry::getValue)
					.flatMap(Collection::stream)
					.collect(toList());
		}

		public List<IQueueHandler<IEvent>> getEventHandlers() {
			return eventHandlers;
		}
	}

	@Override
	public void put(String queueName, int handlerOrder, IQueueHandler<IEvent> handler) {
		Map<String, EventHandlerNode> eventHandlerMappings = queueHandlerMappings.get(queueName);
		if (eventHandlerMappings == null) {
			eventHandlerMappings = new ConcurrentHashMap<>();
		}
		Class<IEvent> eventClass = handler.getEventClass();
		String eventClassName = eventClass.getName();
		this.initMQComponent(eventClass, queueName);

		EventHandlerNode node = eventHandlerMappings.get(eventClassName);
		if (node == null) {
			node = new EventHandlerNode();
		}
		node.put(handlerOrder, handler);

		eventHandlerMappings.put(eventClassName, node);
		queueHandlerMappings.put(queueName, eventHandlerMappings);
	}

	private void initMQComponent(Class<IEvent> eventClass, String queueName) {
		if (!beanFactory.containsBean(queueName)) {
			//queue
			beanFactory.registerSingleton(queueName, MessageQueueUtil.newQueue(queueName));
		}
		EventApi eventApi = MessageQueueUtil.getEventApi(eventClass);

		String exchangeName = eventApi.exchange();
		if (!beanFactory.containsBean(exchangeName)) {
			//exchange
			Exchange exchange;
			if (amqpProperties.isEnableDelayedMessage()) {
				//支持延时
				exchange = MessageQueueUtil.newExchangeWithDelay(exchangeName);
			}
			else {
				exchange = MessageQueueUtil.newExchange(exchangeName);
			}
			beanFactory.registerSingleton(exchangeName, exchange);
		}

		String routingKey = eventApi.routingKey();
		routingKey = StringUtils.isBlank(routingKey) ? amqpProperties.getDefaultRoutingKey() : routingKey;

		String bindingName = exchangeName.concat(".").concat(queueName).concat(".").concat(routingKey);
		if (!beanFactory.containsBean(bindingName)) {
			//binding
			beanFactory.registerSingleton(bindingName, MessageQueueUtil.binding(exchangeName, queueName, routingKey));
		}
	}

	@Override
	public List<IQueueHandler<IEvent>> get(String queueName, String eventClassName) {
		Assert.isTrue(StringUtils.isNotBlank(queueName),
				"This [queueName] is required; it must not be null.");
		Assert.isTrue(StringUtils.isNotBlank(eventClassName),
				"This [eventClassName] is required; it must not be null.");

		return Optional.ofNullable(queueHandlerMappings.get(queueName))
				.map(eventMappings -> eventMappings.get(eventClassName))
				.map(EventHandlerNode::getEventHandlers)
				.orElse(null);
	}

	@Override
	public Set<String> getQueueNames() {
		return queueHandlerMappings.keySet();
	}
}
