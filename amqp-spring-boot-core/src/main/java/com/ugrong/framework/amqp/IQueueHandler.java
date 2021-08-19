package com.ugrong.framework.amqp;

public interface IQueueHandler<T extends IEvent> {

	void handle(T event) throws Exception;

	Class<T> getEventClass();

}
