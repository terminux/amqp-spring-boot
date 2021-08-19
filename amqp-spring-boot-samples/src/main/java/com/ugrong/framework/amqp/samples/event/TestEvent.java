package com.ugrong.framework.amqp.samples.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.ugrong.framework.amqp.IEvent;
import com.ugrong.framework.amqp.annotation.EventApi;
import com.ugrong.framework.amqp.samples.common.MQConstants;
import com.ugrong.framework.amqp.utils.JsonUtil;

@EventApi(exchange = MQConstants.TEST_EXCHANGE, routingKey = MQConstants.TEST_ROUTING_KEY)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestEvent implements IEvent {

	private static final long serialVersionUID = -2008758066962040680L;

	private int id;

	private String action;

	@Override
	public String toString() {
		return JsonUtil.toJsonStr(this);
	}
}
