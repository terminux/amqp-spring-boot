package com.ugrong.framework.amqp;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface IQueueHandlerContainer extends Serializable {

    void init();

    void put(String queueName, int handlerOrder, IQueueHandler<IEvent> handler);

    List<IQueueHandler<IEvent>> get(String queueName, String eventClassName);

    Set<String> getQueueNames();
}
