package com.ugrong.framework.amqp.handler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.IQueueHandler;
import com.ugrong.framework.amqp.annotation.EventApi;
import com.ugrong.framework.amqp.utils.MessageQueueUtil;

public class MethodProxyQueueHandler<T extends IEvent> implements IQueueHandler<T> {

	private final Object target;

	private final Method method;

	private final Class<T> eventClass;

	@SuppressWarnings("unchecked")
	public MethodProxyQueueHandler(Object target, Method method) {
		this.target = target;
		this.method = method;

		Class<?>[] parameterTypes = method.getParameterTypes();
		this.eventClass = (Class<T>) Arrays.stream(parameterTypes)
				.filter(IEvent.class::isAssignableFrom)
				.filter(clazz -> Objects.nonNull(clazz.getAnnotation(EventApi.class)))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException(MessageQueueUtil
						.errorMsg("queue handler method parameter invalid", target, method)));
	}

	@Override
	public void handle(T event) throws Exception {
		method.invoke(target, event);
	}

	@Override
	public final Class<T> getEventClass() {
		return eventClass;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + target.getClass() + "#" + method.getName() + "]";
	}
}
