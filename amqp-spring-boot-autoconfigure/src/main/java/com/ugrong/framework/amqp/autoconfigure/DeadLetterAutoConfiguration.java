package com.ugrong.framework.amqp.autoconfigure;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ugrong.framework.amqp.autoconfigure.properties.AmqpProperties;
import com.ugrong.framework.amqp.utils.MessageQueueUtil;


/**
 * 消息队列的全局配置,当前每个数据最多尝试消费3次，每间隔3秒消费一次 如果3次都没有办法消费掉
 * 那么数据进入死信队列中，防止无法消费的数据丢失
 */
@EnableConfigurationProperties(AmqpProperties.class)
@AllArgsConstructor
@Configuration
public class DeadLetterAutoConfiguration {

	private final AmqpProperties amqpProperties;

	/**
	 * 声明死信交换机
	 *
	 * @return the exchange
	 */
	@Bean("deadLetterExchange")
	public Exchange deadLetterExchange() {
		return MessageQueueUtil.newExchange(amqpProperties.getDeadLetterExchange());
	}

	/**
	 * 声明死信交队列
	 *
	 * @return the queue
	 */
	@Bean("deadLetterQueue")
	public Queue deadLetterQueue() {
		return MessageQueueUtil.newQueue(amqpProperties.getDeadLetterQueue());
	}

	/**
	 * 死信队列绑定到死信交换器上.
	 *
	 * @return the binding
	 */
	@Bean("deadLetterBinding")
	public Binding deadLetterBinding() {
		return MessageQueueUtil.binding(amqpProperties.getDeadLetterExchange(), amqpProperties.getDeadLetterQueue()
				, amqpProperties.getDeadLetterRoutingKey());
	}
}